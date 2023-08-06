package com.mola.rpc.common.entity;

import com.mola.rpc.common.utils.JSONUtil;
import com.mola.rpc.common.annotation.ConsumerSide;
import com.mola.rpc.common.annotation.ProviderSide;
import com.mola.rpc.common.constants.LoadBalanceConstants;
import com.mola.rpc.common.context.InvokeContext;
import com.mola.rpc.common.utils.AssertUtil;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-07-30 21:52
 **/
public class RpcMetaData {

    /**
     * 服务提供组
     */
    @ProviderSide
    @ConsumerSide
    private String group = "default";

    /**
     * 服务提供版本
     */
    @ProviderSide
    @ConsumerSide
    private String version = "1.0.0";

    /**
     * 接口名
     */
    @ProviderSide
    @ConsumerSide
    private Class<?> interfaceClazz;

    /**
     * 是否使用纤程
     */
    @ProviderSide
    private Boolean inFiber = false;

    /**
     * 服务提供者ip:port
     */
    @ConsumerSide
    private List<AddressInfo> addressList;

    /**
     * 客户端超时时间
     */
    @ConsumerSide
    private long clientTimeout = 10000;

    /**
     * 负载均衡策略
     */
    @ConsumerSide
    private String loadBalanceStrategy = LoadBalanceConstants.RANDOM_STRATEGY;

    /**
     * 服务提供者bean名称
     */
    @ProviderSide
    private String providerBeanName;

    /**
     * 服务提供者对象
     */
    @ProviderSide
    private Object providerObject;

    /**
     * 服务提供者类名
     */
    @ProviderSide
    private String providerBeanClazz;

    /**
     * 异步调用方法
     */
    @ConsumerSide
    private Set<String> asyncExecuteMethods;

    /**
     * 单向调用方法
     */
    @ConsumerSide
    private Set<String> onewayExecuteMethods;

    @ProviderSide
    private String host;

    /**
     * 是否泛化调用
     */
    @ConsumerSide
    private Boolean genericInvoke = Boolean.FALSE;

    /**
     * 泛化调用服务名
     */
    @ConsumerSide
    private String genericInterfaceName;

    /**
     * 哈希环，hash => ip#idx
     * 用于负载均衡
     */
    @ConsumerSide
    private transient SortedMap<Integer, String> virtualAddressNodeMap = new TreeMap<>();

    /**
     * provider反向代理到consumer的服务器地址
     */
    @ProviderSide
    private List<String> appointedAddress;

    /**
     * 正向模式下，由consumer通过注册中心主动发现provider，但provider如果在当前网络环境中无法被发现
     * 则需要provider发现consumer，与consumer建立连接
     * 1、consumer用于判断是否是反向模式调用，反向模式不走configServer
     * 2、provider用于判断是否向consumer注册自己，目前支持固定节点注册
     * reverseModeConsumerAddress
     */
    @ConsumerSide
    @ProviderSide
    private Boolean reverseMode = Boolean.FALSE;

    @ProviderSide
    private List<String> reverseModeConsumerAddress;

    /**
     * 是否是以原型方式提供的服务
     */
    @ProviderSide
    private Boolean proto = Boolean.FALSE;


    public static RpcMetaData of(String group, String version, Class<?> clazzType) {
        RpcMetaData rpcMetaData = new RpcMetaData();
        rpcMetaData.setGroup(group);
        rpcMetaData.setVersion(version);
        rpcMetaData.setInterfaceClazz(clazzType);
        return rpcMetaData;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<AddressInfo> getAddressList() {
        return addressList;
    }

    public void setAddressList(List<AddressInfo> addressList) {
        this.addressList = addressList;
    }

    public String getLoadBalanceStrategy() {
        return loadBalanceStrategy;
    }

    public void setLoadBalanceStrategy(String loadBalanceStrategy) {
        this.loadBalanceStrategy = loadBalanceStrategy;
    }

    public long getClientTimeout() {
        return clientTimeout;
    }

    public void setClientTimeout(long clientTimeout) {
        this.clientTimeout = clientTimeout;
    }

    public Class<?> getInterfaceClazz() {
        return interfaceClazz;
    }

    public void setInterfaceClazz(Class<?> interfaceClazz) {
        this.interfaceClazz = interfaceClazz;
    }

    public String getProviderBeanName() {
        return providerBeanName;
    }

    public void setProviderBeanName(String providerBeanName) {
        this.providerBeanName = providerBeanName;
    }

    public String getProviderBeanClazz() {
        return providerBeanClazz;
    }

    public void setProviderBeanClazz(String providerBeanClazz) {
        this.providerBeanClazz = providerBeanClazz;
    }

    public Boolean getInFiber() {
        return inFiber;
    }

    public void setInFiber(Boolean inFiber) {
        this.inFiber = inFiber;
    }

    public Set<String> getAsyncExecuteMethods() {
        return asyncExecuteMethods;
    }

    public void setAsyncExecuteMethods(Set<String> asyncExecuteMethods) {
        this.asyncExecuteMethods = asyncExecuteMethods;
    }

    public void setOnewayExecuteMethods(Set<String> onewayExecuteMethods) {
        this.onewayExecuteMethods = onewayExecuteMethods;
    }

    public Set<String> getOnewayExecuteMethods() {
        return onewayExecuteMethods;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    @Override
    public String toString() {
        return JSONUtil.toJSONString(this, true);
    }

    public SortedMap<Integer, String> getVirtualAddressNodeMap() {
        return virtualAddressNodeMap;
    }

    public void setVirtualAddressNodeMap(SortedMap<Integer, String> virtualAddressNodeMap) {
        this.virtualAddressNodeMap = virtualAddressNodeMap;
    }

    public Object getProviderObject() {
        return providerObject;
    }

    public void setProviderObject(Object providerObject) {
        this.providerObject = providerObject;
    }

    public void setGenericInvoke(Boolean genericInvoke) {
        this.genericInvoke = genericInvoke;
    }

    public Boolean getGenericInvoke() {
        return genericInvoke;
    }

    public String getGenericInterfaceName() {
        return genericInterfaceName;
    }

    public void setGenericInterfaceName(String genericInterfaceName) {
        this.genericInterfaceName = genericInterfaceName;
    }

    public List<String> getAppointedAddress() {
        return appointedAddress;
    }

    public void setAppointedAddress(List<String> appointedAddress) {
        this.appointedAddress = appointedAddress;
    }

    public Boolean getProto() {
        return proto;
    }

    public void setProto(Boolean proto) {
        this.proto = proto;
    }

    public void setReverseMode(Boolean reverseMode) {
        this.reverseMode = reverseMode;
    }

    public Boolean getReverseMode() {
        return reverseMode;
    }

    public void setReverseModeConsumerAddress(List<String> reverseModeConsumerAddress) {
        this.reverseModeConsumerAddress = reverseModeConsumerAddress;
    }

    public List<String> getReverseModeConsumerAddress() {
        return reverseModeConsumerAddress;
    }

    /**
     * 客户端取服务端地址，优先级如下
     * 1、执行上下文
     * 2、consumer配置
     * 3、配置中心
     * @return
     */
    public List<String> fetchProviderAddressList() {
        RpcMetaData consumerMeta = this;
        InvokeContext context = InvokeContext.fetch();
        if (context != null && !CollectionUtils.isEmpty(context.getAddressList())) {
            return context.getAddressList();
        }
        List<String> addressList = consumerMeta.getAppointedAddress();
        if (CollectionUtils.isEmpty(addressList)) {
            addressList = consumerMeta.getAddressList().stream()
                    .filter(e -> e.getAddress() != null).map(AddressInfo::getAddress)
                    .collect(Collectors.toList());
        }
        AssertUtil.notNull(addressList, "addressList is null");
        return addressList;
    }
}
