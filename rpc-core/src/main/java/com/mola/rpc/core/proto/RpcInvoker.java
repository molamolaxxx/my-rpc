package com.mola.rpc.core.proto;

import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.core.properties.RpcProperties;
import com.mola.rpc.core.proxy.RpcProxyInvokeHandler;
import com.mola.rpc.core.remoting.netty.NettyRemoteClient;
import com.mola.rpc.core.remoting.netty.NettyRemoteServer;
import com.mola.rpc.data.config.spring.RpcProviderDataInitBean;

import java.lang.reflect.Proxy;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: rpc调用门面（去spring）
 * @date : 2022-10-16 23:45
 **/
public class RpcInvoker {

    private static final String PROTO_MODE_CONSUMER = "PROTO_MODE_CONSUMER";

    /**
     * 创建消费者接口代理
     * @param consumerInterface
     * @param <T>
     * @return
     */
    public static <T> T consume(Class<T> consumerInterface, RpcMetaData rpcMetaData) {
        if (!ProtoRpcConfigFactory.INIT_FLAG.get()) {
            throw new RuntimeException("please init rpc config in proto mode!");
        }
        ProtoRpcConfigFactory protoRpcConfigFactory = ProtoRpcConfigFactory.get();
        RpcProperties rpcProperties = protoRpcConfigFactory.getRpcProperties();
        // 订阅服务
        RpcContext rpcContext = protoRpcConfigFactory.getRpcContext();
        rpcMetaData.setInterfaceClazz(consumerInterface);
        rpcContext.addConsumerMeta(consumerInterface.getName(), PROTO_MODE_CONSUMER, rpcMetaData);
        // 启动client
        NettyRemoteClient nettyRemoteClient = protoRpcConfigFactory.getNettyRemoteClient();
        if (!nettyRemoteClient.isStart()) {
            nettyRemoteClient.start();
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
        rpcProxyInvokeHandler.setBeanName(PROTO_MODE_CONSUMER);
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
    public static <T> void provide(Class<T> consumerInterface,T providerObject, RpcMetaData rpcMetaData) {
        if (!ProtoRpcConfigFactory.INIT_FLAG.get()) {
            throw new RuntimeException("please init rpc config in proto mode!");
        }
        ProtoRpcConfigFactory protoRpcConfigFactory = ProtoRpcConfigFactory.get();
        // 提供服务
        RpcContext rpcContext = protoRpcConfigFactory.getRpcContext();
        RpcProperties rpcProperties = protoRpcConfigFactory.getRpcProperties();
        rpcMetaData.setProviderObject(providerObject);
        rpcMetaData.setInterfaceClazz(consumerInterface);
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
    }
}
