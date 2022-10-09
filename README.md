# my-rpc
my-rpc is a rpc framework based on netty


# My-Rpc原理解析

## 一、简单架构设计

https://github.com/molamolaxxx/my-rpc

#### 1、rpc结构图

![image-20221005114124660](https://raw.githubusercontent.com/molamolaxxx/my-img/master/image-20221005114124660.png)

#### 2、代码结构图

![image-20221005115155479](https://raw.githubusercontent.com/molamolaxxx/my-img/master/image-20221005115155479.png)

## 二、功能拆解和实现

#### 1、接口动态代理

src/main/java/com/mola/rpc/core/spring

使用起来很简单：

```java
@Configuration
public class RpcConsumerConfig {
    
    @RpcConsumer(loadBalanceStrategy = LoadBalanceConstants.CONSISTENCY_HASHING_STRATEGY)
    private OrderService orderService;
}
```

但是在consumer中，并没有OrderService的实现，注入的对象是jdk代理对象（Proxy）

因为需要根据注入bean的接口类型，动态创建代理类，所以使用FactoryBean实现

```java
public class RpcConsumerFactoryBean implements FactoryBean {

    @Resource
    private RpcProxyInvokeHandler nettyProxyInvokeHandler;

    private Class<?> consumerInterface;

    public RpcConsumerFactoryBean(Class<?> consumerInterface) {
        this.consumerInterface = consumerInterface;
    }

    @Override
    public Object getObject() throws Exception {
        Object proxyInstance = Proxy.newProxyInstance(
                RpcConsumerFactoryBean.class.getClassLoader(),
                new Class[]{consumerInterface},
                nettyProxyInvokeHandler);
        return proxyInstance;
    }

    @Override
    public Class<?> getObjectType() {
        return consumerInterface;
    }
}
```

需要手动创建bean defination，通过ImportBeanDefinitionRegistrar导入到spring中

```java
public class RpcConsumerImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        try {
            for (String beanDefinitionName : beanDefinitionRegistry.getBeanDefinitionNames()) {
                ....
                // 获取configure中的fieldName，作为beanName
                Field[] fields = clazz.getDeclaredFields();
                Set<String> alreadyAddedClazzNamesSet = Sets.newHashSet();
                for (Field field : fields) {
                    RpcConsumer annotation = field.getAnnotation(RpcConsumer.class);
                    if (null == annotation) {
                        continue;
                    }
                    Class<?> type = field.getType();
                    if (alreadyAddedClazzNamesSet.contains(type.getName())) {
                        continue;
                    }
                    BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();
                    AbstractBeanDefinition consumerBeanDefinition = builder.getBeanDefinition();
                    // 设置bean的class类型为FactoryBean
                    consumerBeanDefinition.setBeanClass(RpcConsumerFactoryBean.class);
                    // 构造参数传入接口类型，用于代理
                    consumerBeanDefinition.getConstructorArgumentValues().addGenericArgumentValue(type);
                    RpcMetaData clientMeta = RpcMetaData.of(annotation.group(), annotation.version(), type);
                    clientMeta.setClientTimeout(annotation.timeout());
                    clientMeta.setLoadBalanceStrategy(annotation.loadBalanceStrategy());
                    BeanMetadataAttribute attribute = new BeanMetadataAttribute(CommonConstants.BEAN_DEF_CONSUMER_META,
                            clientMeta);
                    consumerBeanDefinition.addMetadataAttribute(attribute);
                    // 添加beanDefinition
                    beanDefinitionRegistry.registerBeanDefinition(field.getName(), consumerBeanDefinition);
                    alreadyAddedClazzNamesSet.add(type.getName());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
```

在代理中，可以获取到调用的接口，调用的参数，来做后续的处理

```java
@Override
public Object invoke(Object obj, Method method, Object[] args) throws Throwable {
    if (Object.class.equals(method.getDeclaringClass())) {
        return method.invoke(this, args);
    }
    // 获取服务对应的元数据唯一key
    RpcMetaData consumerMeta = rpcContext.getConsumerMeta(method.getDeclaringClass().getName());
    if (null == consumerMeta) {
        throw new RuntimeException("consumer invoke failed, consumerMeta is null, clazz = " + method.getDeclaringClass().getName());
    }
    List<AddressInfo> addressInfoList = consumerMeta.getAddressList();
    if (CollectionUtils.isEmpty(addressInfoList)) {
        throw new RuntimeException("consumer invoke failed, addressList is empty, meta = " + JSONObject.toJSONString(consumerMeta));
    }
    // 地址过滤器
    List<String> addressList = addressInfoList.stream()
        .map(AddressInfo::getAddress)
        .collect(Collectors.toList());

    // 负载均衡策略
    String targetProviderAddress = loadBalance.getTargetProviderAddress(addressList, consumerMeta.getLoadBalanceStrategy(), args);
    if (null == targetProviderAddress) {
        if (addressList.size() == 0) {
            throw new RuntimeException("no provider available");
        }
        targetProviderAddress = addressList.get(0);
    }
    // 构建request
    InvokeMethod invokeMethod = assemblyInvokeMethod(method, args);
    RemotingCommand request = buildRemotingCommand(method, invokeMethod, consumerMeta.getClientTimeout(), targetProviderAddress);
    // 执行远程调用
    RemotingCommand response = nettyRemoteClient.syncInvoke(targetProviderAddress,
                                                            request, invokeMethod, consumerMeta.getClientTimeout());
    // 服务端执行异常
    if (response.getCode() == RemotingCommandCode.SYSTEM_ERROR) {
        throw new RuntimeException(response.getRemark());
    }
    // 读取服务端返回结果
    if (null == response) {
        return null;
    }
    // response转换成对象
    String body = (String) BytesUtil.bytesToObject(response.getBody());
    Object res = RemotingSerializableUtil.fromJson(body, method.getReturnType());
    return res;
}
```

#### 2、地址过滤器和负载均衡

（1）动态地址路由（feature）

服务维度配置路由规则，使用groovy脚本实现，脚本存储在configserver

入参：args

出参：ip、environment、group

（2）负载均衡：src/main/java/com/mola/rpc/core/strategy/balance

- 随机负载均衡
- round robin
- 一致性hash算法

![image-20221005120116566](https://raw.githubusercontent.com/molamolaxxx/my-img/master/image-20221005120116566.png)

#后代表虚拟节点，默认为每个地址分配10个虚拟节点

对request进行json化，进行hash运算，取第一个hash值大于其的虚拟节点

具体实现使用treemap：

```java
/**
 * 哈希环，hash => ip#idx
 */
private SortedMap<Integer, String> virtualAddressNodeMap = new TreeMap<>();

@Override
public String getTargetProviderAddress(List<String> addressList, String strategyName, Object[] args) {
    if (needRebuildHash(addressList)) {
        rebuildHash(addressList);
    }
    if (virtualAddressNodeMap.size() == 0) {
        throw new RuntimeException("virtualAddressNodeMap is empty");
    }
    int hash = getHash(JSONObject.toJSONString(args));
    // sort map截取
    SortedMap<Integer, String> tailMap = virtualAddressNodeMap.tailMap(hash);
    if (tailMap.size() == 0) {
        return getAddressFromVirtualNode(virtualAddressNodeMap.get(virtualAddressNodeMap.firstKey()));
    }
    return getAddressFromVirtualNode(tailMap.get(tailMap.firstKey()));
}

/**
 * 重建hash索引
 * @param addressList
*/
private void rebuildHash(List<String> addressList) {
    rebuildHashLock.lock();
    try {
        if (!needRebuildHash(addressList)) {
            return;
        }
        addressSet = new HashSet<>(addressList.size());
        virtualAddressNodeMap = new TreeMap<>();
        int totalNodeNum = TOTAL_VIRTUAL_NODE_NUM / addressList.size();
        for (String address : addressList) {
            if (addressSet.contains(address)) {
                continue;
            }
            for (int i = 0; i < totalNodeNum; i++) {
                String virtualAddress = String.format("%s#%s", address, UUID.randomUUID());
                virtualAddressNodeMap.put(getHash(virtualAddress), virtualAddress);
            }
            addressSet.add(address);
        }
    } finally {
        rebuildHashLock.unlock();
    }
}
```

#### 3、网络模型

![image-20221005145943988](https://raw.githubusercontent.com/molamolaxxx/my-img/master/image-20221005145943988.png)

采用基于reactor模式的netty框架实现，客户端使用worker-pipeline线程模型，服务端使用boss-worker-pipeline-biz线程模型

#### 4、协议与序列化

![image-20221005153253534](https://raw.githubusercontent.com/molamolaxxx/my-img/master/image-20221005153253534.png)

tcp连接存在分包粘包的问题，所以需要通过报文头（header）和报文体（body）划分数据报

（1）netty frame length

四字节，32位整型，标记数据帧的长度，让netty基于LengthFieldBasedFrameDecoder进行数据帧的划分

（2）message header length

四字节，32位整型，标记header body长度，用于读取buffer中的header body

（3）header body（frame length - 8 - header length）

json序列化的header体，包括请求唯一序号，请求状态码，附加信息等

（4）header body

hessian序列化的InvokeMethod对象，InvokeMethod是jvm维度执行方法的抽象，具有可序列化传输，可单独在jvm中执行的功能

```java
public class InvokeMethod {

	/** 调用方法 */
	private String methodName;

	/** 参数类型 */
	private String[] parameterTypes;

	/** 参数 */
	private String[] arguments;

	/** 返回类型 */
	private String returnType;

	/**
	 * 调用接口名称
	 */
	private String interfaceClazz;

	public Object invoke(Object providerBean) {
		try {
			Assert.notNull(providerBean, "providerBean is null, name = " + providerBean);
			// 1、反序列化类型
			Class<?>[] paramTypes = new Class<?>[this.parameterTypes.length];
			for (int i = 0; i < this.parameterTypes.length; i++) {
				paramTypes[i] = Class.forName(this.parameterTypes[i]);
			}
			// 2、反序列化参数tongshi
			Object[] args = new Object[this.arguments.length];
			for (int i = 0; i < this.arguments.length; i++) {
				args[i] = RemotingSerializableUtil.fromJson(this.arguments[i], paramTypes[i]);
			}
			Method method = providerBean.getClass().getMethod(this.methodName, paramTypes);
			// 3、反射调用provider
			return method.invoke(providerBean, args);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
```

#### 5、同步与异步调用

（1）同步调用

consumer中看似同步的调用其实是异步的网络请求，但是需要给用户看上去是同步的调用（非用户使用异步的场景下），发送请求的同时，需要阻塞线程等待返回

![image-20221005155430801](https://raw.githubusercontent.com/molamolaxxx/my-img/master/image-20221005155430801.png)

在客户端发送请求的同时，生成一个ResponseFuture，并以opaqueId为key存储在map中

```java
/**
* 缓存对外所有请求
* opaque -> ResponseFuture
*/
protected final Map<Integer, ResponseFuture> responseMap = new ConcurrentHashMap<>(256);
```

ResponseFuture内部实现为发令枪，当发送请求后，调用waitResponse方法，传入超时时间，阻塞当前线程

caiyong当服务端返回时，取到opaqueId，取出客户端缓存的ResponseFuture，调用putResponse，让线程继续执行

```java
private final CountDownLatch countDownLatch = new CountDownLatch(1);

public RemotingCommand waitResponse(final long timeoutMillis) throws InterruptedException {
	this.countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
	return this.responseCommand;
}


public void putResponse(final RemotingCommand responseCommand) {
	this.responseCommand = responseCommand;
	this.countDownLatch.countDown();
}
```

（2）异步调用

实现方式，在@RpcConsumer上配置asyncMethods

```java
@RpcConsumer(loadBalanceStrategy = LoadBalanceConstants.LOAD_BALANCE_ROUND_ROBIN_STRATEGY, group = "gray", asyncMethods = {"queryOrderList"})
private OrderService orderServiceGray;
```

```java
// 异步回调
Async.from(orderServiceGray.queryOrderList(orderId))
                .consume(list -> {
                    System.out.println(Thread.currentThread());
                    System.out.println(JSONObject.toJSONString(list));
                });
// 异步转同步
Async<List<Order>> async = Async.from(orderServiceGray.queryOrderList(orderId));
Thread.sleep(3000);
//do othder something
List<Order> orders = async.get();
```

异步调用做法是将ResponseFuture放入threadlocal上下文中，交给异步组件Async处理，交给async的好处是相比于hsf和dubbo可以实现较美观的范式

dubbo：

```java
Result<ProductVO> result = productService.findProduct(dto);//dubbo异步调用，此时输出result是null
Future<Object> future = RpcContext.getContext().getFuture();//获取异步执行结果Future
//do othder something
try {
    result = (Result<ProductVO>) future.get();//获取具体的异步执行结果
} catch (InterruptedException | ExecutionException e) {
    e.printStackTrace();
}
return result;
```

my-rpc:

```java
// 1、异步回调
Async.from(orderServiceGray.queryOrderList(orderId))
                .consume(list -> {
                    System.out.println(Thread.currentThread());
                    System.out.println(JSONObject.toJSONString(list));
                });
// 2、异步转同步
Async<List<Order>> async = Async.from(orderServiceGray.queryOrderList(orderId));
Thread.sleep(3000);
//do othder something
List<Order> orders = async.get();
```

```java
public class Async<T> {

    private static final ThreadLocal<AsyncResponseFuture> asyncFutureThreadLocal = new ThreadLocal<>();

    /**
     * 生成异步器
     * @param result
     * @param <T>
     * @return
     */
    public static <T> Async<T> from(T result) {
        if (null != result) {
            throw new RuntimeException("please check if this method is an async method!");
        }
        return new Async();
    }

    public static void addFuture(AsyncResponseFuture responseFuture) {
        asyncFutureThreadLocal.set(responseFuture);
    }

    /**
     * 注册监听器
     * @param consumer
     */
    public void register(Consumer<T> consumer) {
        try {
            AsyncResponseFuture<T> responseFuture = asyncFutureThreadLocal.get();
            Assert.notNull(responseFuture, "responseFuture is null");
            responseFuture.setConsumer(consumer);
        } finally {
            asyncFutureThreadLocal.remove();
        }
    }

    public T sync(long timeout) {
        try {
            AsyncResponseFuture<T> responseFuture = asyncFutureThreadLocal.get();
            Assert.notNull(responseFuture, "responseFuture is null");
            return responseFuture.get(timeout);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            asyncFutureThreadLocal.remove();
        }
    }
}
```

#### 6、provider线程模型

provider采用boss-worker-pipeline-biz线程模型，最终业务逻辑的执行放在biz线程池，针对单个服务流量特别大的情况，可以自己定制线程池，我们常说的线程池满也就是业务线程池溢出

## 三、新特性

#### 1、支持协程

协程不同于线程需要靠jvm进行系统调用进行切换，可以在用户态实现异步调用，避免线程切换带来的开销，并且能够调度更多的任务，JDK19已经在虚拟机层面支持协程。

```java
@RpcProvider(interfaceClazz = OrderService.class, inFiber = true)
public class OrderServiceImpl implements OrderService {

    @Resource
    private RpcProperties rpcProperties;

    @Override
    public List<Order> queryOrderList(String code, List<String> idList) {
        List<Order> orders = new ArrayList<>();
        for (String s : idList) {
            Order order = new Order();
            order.setCode("UM1111111");
            order.setId(s);
            order.setDesc(NetUtils.getLocalAddress().getHostAddress() + ":" + rpcProperties.getServerPort());
            orders.add(order);
        }
        return orders;
    }
}
```

provider开启协程调用后，可以获得更高的吞吐量，缺点是线程相关的内容将无法支持

jdk8协程框架采用quasar支持，jdk19采用虚拟线程支持

## 四、最佳实践

