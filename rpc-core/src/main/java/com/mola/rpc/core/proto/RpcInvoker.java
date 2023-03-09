package com.mola.rpc.core.proto;

import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.core.properties.RpcProperties;
import com.mola.rpc.core.proxy.GenericRpcServiceProxyFactory;
import com.mola.rpc.core.proxy.RpcProxyInvokeHandler;
import com.mola.rpc.core.remoting.netty.NettyRemoteClient;
import com.mola.rpc.core.remoting.netty.NettyRemoteServer;
import com.mola.rpc.core.system.ReverseInvokeHelper;
import com.mola.rpc.data.config.spring.RpcProviderDataInitBean;
import org.springframework.util.Assert;

import java.lang.reflect.Proxy;
import java.util.List;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: rpc调用门面（去spring）
 * @date : 2022-10-16 23:45
 **/
public class RpcInvoker {

    private static final String PROTO_MODE_CONSUMER = "PROTO_MODE_CONSUMER";

    /**
     * 创建消费者接口代理（建议在应用初始化的时候完成）
     * @param consumerInterface
     * @param <T>
     * @return
     */
    public static <T> T consumer(Class<T> consumerInterface) {
        return consumer(consumerInterface, new RpcMetaData(), PROTO_MODE_CONSUMER);
    }

    /**
     * 指定服务提供地址的消费者（建议在应用初始化的时候完成）
     * @param consumerInterface
     * @param <T>
     */
    public static <T> T consumer(Class<T> consumerInterface, List<String> addressList) {
        ProtoRpcConfigFactory protoRpcConfigFactory = ProtoRpcConfigFactory.get();
        RpcProperties rpcProperties = protoRpcConfigFactory.getRpcProperties();
        RpcMetaData rpcMetaData = new RpcMetaData();
        // 指定provider的服务提供者
        rpcMetaData.setAppointedAddress(addressList);
        return consumer(consumerInterface, rpcMetaData, PROTO_MODE_CONSUMER);
    }

    /**
     * 泛化调用消费者，接口维度全局单例（建议在应用初始化的时候完成）
     * @param interfaceClazzName
     * @return
     */
    public static GenericRpcService genericConsumer(String interfaceClazzName) {
        return GenericRpcServiceProxyFactory.getProxy(interfaceClazzName, new RpcMetaData());
    }

    /**
     * 泛化调用消费者，接口维度全局单例（建议在应用初始化的时候完成）
     * 指定服务提供者
     * @param interfaceClazzName
     * @param addressList
     * @return
     */
    public static GenericRpcService genericConsumer(String interfaceClazzName, List<String> addressList) {
        RpcMetaData rpcMetaData = new RpcMetaData();
        // 指定provider的服务提供者
        rpcMetaData.setAppointedAddress(addressList);
        return GenericRpcServiceProxyFactory.getProxy(interfaceClazzName, rpcMetaData);
    }

    public static <T> T consumer(Class<T> consumerInterface, RpcMetaData rpcMetaData, String consumerName) {
        ProtoRpcConfigFactory protoRpcConfigFactory = ProtoRpcConfigFactory.get();
        RpcProperties rpcProperties = protoRpcConfigFactory.getRpcProperties();
        // 订阅服务
        RpcContext rpcContext = protoRpcConfigFactory.getRpcContext();
        rpcMetaData.setInterfaceClazz(consumerInterface);
        rpcContext.addConsumerMeta(consumerInterface.getName(), consumerName, rpcMetaData);
        // 启动client
        NettyRemoteClient nettyRemoteClient = protoRpcConfigFactory.getNettyRemoteClient();
        Assert.isTrue(nettyRemoteClient.isStart(), "nettyRemoteClient has not been start! please call ProtoRpcConfigFactory.configure first");
        // 反向调用，启动server
        if (rpcMetaData.getReverseMode()) {
            NettyRemoteServer nettyRemoteServer = protoRpcConfigFactory.getNettyRemoteServer();
            if (!nettyRemoteServer.isStart()) {
                nettyRemoteServer.start();
            }
        }
        // config server 动态注册
        if (rpcProperties.getStartConfigServer()) {
            RpcProviderDataInitBean rpcProviderDataInitBean = protoRpcConfigFactory.getRpcProviderDataInitBean();
            rpcProviderDataInitBean.pullProviderData(rpcMetaData);
            rpcProviderDataInitBean.registerProviderDataListener(rpcMetaData);
        }
        // 设置代理
        RpcProxyInvokeHandler rpcProxyInvokeHandler = new RpcProxyInvokeHandler();
        rpcProxyInvokeHandler.setRpcContext(protoRpcConfigFactory.getRpcContext());
        rpcProxyInvokeHandler.setLoadBalance(protoRpcConfigFactory.getLoadBalance());
        rpcProxyInvokeHandler.setNettyConnectPool(protoRpcConfigFactory.getNettyConnectPool());
        rpcProxyInvokeHandler.setNettyRemoteClient(protoRpcConfigFactory.getNettyRemoteClient());
        rpcProxyInvokeHandler.setBeanName(consumerName);
        return (T) Proxy.newProxyInstance(
                RpcInvoker.class.getClassLoader(),
                new Class[]{consumerInterface},
                rpcProxyInvokeHandler);
    }

    /**
     * 创建服务提供者接口代理
     * @param consumerInterface
     * @param <T>
     * @return
     */
    public static <T> void provider(Class<T> consumerInterface, T providerObject) {
        provider(consumerInterface, providerObject, new RpcMetaData());
    }

    public static <T> void provider(Class<T> consumerInterface, T providerObject, RpcMetaData rpcMetaData) {
        ProtoRpcConfigFactory protoRpcConfigFactory = ProtoRpcConfigFactory.get();
        // 提供服务
        RpcContext rpcContext = protoRpcConfigFactory.getRpcContext();
        RpcProperties rpcProperties = protoRpcConfigFactory.getRpcProperties();
        rpcMetaData.setProviderObject(providerObject);
        rpcMetaData.setInterfaceClazz(consumerInterface);
        rpcMetaData.setProto(Boolean.TRUE);
        rpcContext.addProviderMeta(consumerInterface.getName(), rpcMetaData);
        // 启动server
        NettyRemoteServer nettyRemoteServer = protoRpcConfigFactory.getNettyRemoteServer();
        if (!nettyRemoteServer.isStart()) {
            nettyRemoteServer.start();
        }
        // config server 动态注册
        if (rpcProperties.getStartConfigServer()) {
            RpcProviderDataInitBean rpcProviderDataInitBean = protoRpcConfigFactory.getRpcProviderDataInitBean();
            rpcProviderDataInitBean.uploadRemoteProviderData(rpcMetaData);
        }
        ReverseInvokeHelper.instance().registerProviderProxyToServer(rpcMetaData);
    }
}
