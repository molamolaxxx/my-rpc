package com.mola.rpc.core.proxy;

import com.alibaba.fastjson.JSONObject;
import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.common.entity.AddressInfo;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.core.remoting.Async;
import com.mola.rpc.core.remoting.AsyncResponseFuture;
import com.mola.rpc.core.remoting.netty.NettyConnectPool;
import com.mola.rpc.core.remoting.netty.NettyRemoteClient;
import com.mola.rpc.core.remoting.protocol.RemotingCommand;
import com.mola.rpc.core.remoting.protocol.RemotingCommandCode;
import com.mola.rpc.core.strategy.balance.LoadBalance;
import com.mola.rpc.core.util.BytesUtil;
import com.mola.rpc.core.util.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-07-30 21:44
 **/
public class RpcProxyInvokeHandler implements InvocationHandler {

    private static final Logger log = LoggerFactory.getLogger(RpcProxyInvokeHandler.class);

    private RpcContext rpcContext;

    private LoadBalance loadBalance;

    private NettyConnectPool nettyConnectPool;

    private NettyRemoteClient nettyRemoteClient;

    /**
     * 调用服务的beanName
     */
    private String beanName;

    @Override
    public Object invoke(Object obj, Method method, Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        }
        // 获取服务对应的元数据唯一key
        RpcMetaData consumerMeta = rpcContext.getConsumerMeta(method.getDeclaringClass().getName(), beanName);
        if (null == consumerMeta) {
            throw new RuntimeException("consumer invoke failed, consumerMeta is null, clazz = " + method.getDeclaringClass().getName());
        }
        List<AddressInfo> addressInfoList = consumerMeta.getAddressList();
        if (CollectionUtils.isEmpty(addressInfoList)) {
            throw new RuntimeException("consumer invoke failed, addressList is empty, meta = " + JSONObject.toJSONString(consumerMeta));
        }
        // 过滤器责任链过滤地址
        List<String> addressList = addressInfoList.stream()
                .map(AddressInfo::getAddress)
                .collect(Collectors.toList());

        // 负载均衡策略
        String targetProviderAddress = loadBalance.getTargetProviderAddress(consumerMeta, args);
        if (null == targetProviderAddress) {
            if (addressList.size() == 0) {
                throw new RuntimeException("no provider available");
            }
            targetProviderAddress = addressList.get(0);
        }
        // 构建request
        InvokeMethod invokeMethod = assemblyInvokeMethod(method, args);
        RemotingCommand request = buildRemotingCommand(method, invokeMethod, consumerMeta.getClientTimeout(), targetProviderAddress, consumerMeta);
        // 执行远程调用
        if (isAsyncExecute(consumerMeta, method)) {
            AsyncResponseFuture asyncResponseFuture = nettyRemoteClient.asyncInvoke(targetProviderAddress, request, invokeMethod, method, consumerMeta.getClientTimeout());
            Async.addFuture(asyncResponseFuture);
            return null;
        }
        RemotingCommand response = nettyRemoteClient.syncInvoke(targetProviderAddress, request, invokeMethod, consumerMeta.getClientTimeout());
        // 服务端执行异常
        if (response.getCode() == RemotingCommandCode.SYSTEM_ERROR) {
            throw new RuntimeException(response.getRemark());
        }
        // 读取服务端返回结果
        if (null == response) {
            return null;
        }
        // response转换成对象
        Object invokeResult = BytesUtil.bytesToObject(response.getBody());
        return invokeResult;
    }

    /**
     * 构建协议包
     * @param invokeMethod 执行的方法
     * @return
     */
    private RemotingCommand buildRemotingCommand(Method method, InvokeMethod invokeMethod,
                                                 long timeout, String address, RpcMetaData consumerMeta) {
        RemotingCommand request = new RemotingCommand();
        // 1、构建body
        byte[] requestBody = null;
        try {
            requestBody = BytesUtil.objectToBytes(invokeMethod.toString());
        } catch (Throwable e) {
            log.error("[RpcProxyInvokeHandler]: objectToBytes error"
                    + ", server:" + address
                    + ", timeout:" + timeout
                    + ", methodName:" + method.getName(), e);
            return null;
        }
        if(null == requestBody) {
            log.error("[RpcProxyInvokeHandler]: requestBody is null"
                    + ", server:" + address
                    + ", timeout:" + timeout
                    + ", methodName:" + method.getName());
            return null;
        }

        request.setCode(RemotingCommandCode.NORMAL);
        request.setBody(requestBody);
        request.setVersion(consumerMeta.getVersion());
        request.setGroup(consumerMeta.getGroup());

        return request;
    }

    /**
     * 是否异步执行
     * @param consumerMeta
     * @return
     */
    private Boolean isAsyncExecute(RpcMetaData consumerMeta, Method method) {
        Set<String> asyncExecuteMethods = consumerMeta.getAsyncExecuteMethods();
        if (CollectionUtils.isEmpty(asyncExecuteMethods)) {
            return Boolean.FALSE;
        }
        if (asyncExecuteMethods.contains("*") || asyncExecuteMethods.contains(method.getName())) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 组装InvokeMethodreturn beanName;
     * @return
     */
    private InvokeMethod assemblyInvokeMethod(Method method, Object[] args) {
        // 1、变量class类型
        Class<?>[] parameterTypesClass = method.getParameterTypes();
        // 2、变量class类型的string表示
        String[] parameterTypesString = new String[parameterTypesClass.length];
        // 3、真实的变量类型string
        String[] actualParameterTypesString = new String[parameterTypesClass.length];
        // 构建参数
        RemotingHelper.buildParameter(args,
                parameterTypesClass,
                parameterTypesString,
                actualParameterTypesString);
        // 构建invokeMethod
        InvokeMethod invokeMethod = new InvokeMethod(method.getName(),
                parameterTypesString,
                args,
                method.getReturnType().getName(),
                method.getDeclaringClass().getName());
        return invokeMethod;
    }


    public void setRpcContext(RpcContext rpcContext) {
        this.rpcContext = rpcContext;
    }

    public void setLoadBalance(LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }

    public void setNettyConnectPool(NettyConnectPool nettyConnectPool) {
        this.nettyConnectPool = nettyConnectPool;
    }

    public void setNettyRemoteClient(NettyRemoteClient nettyRemoteClient) {
        this.nettyRemoteClient = nettyRemoteClient;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanName() {
        return beanName;
    }
}
