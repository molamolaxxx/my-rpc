package com.mola.rpc.core.proto;

import com.mola.rpc.common.properties.RpcProperties;
import com.mola.rpc.core.proxy.RpcProxyInvokeHandler;

import java.lang.reflect.Proxy;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: rpc调用门面（去spring）
 * @date : 2022-10-16 23:45
 **/
public class RpcInvoker {

    /**
     * 配置rpc
     * @param rpcProperties
     */
    public static void config(RpcProperties rpcProperties) {

    }

    /**
     * 创建rpc接口代理
     * @param consumerInterface
     * @param <T>
     * @return
     */
    public static <T> T create(Class<T> consumerInterface) {
        RpcProxyInvokeHandler rpcProxyInvokeHandler = new RpcProxyInvokeHandler();
        rpcProxyInvokeHandler.setRpcContext(null);
        rpcProxyInvokeHandler.setLoadBalance(null);
        rpcProxyInvokeHandler.setNettyConnectPool(null);
        rpcProxyInvokeHandler.setNettyRemoteClient(null);
        rpcProxyInvokeHandler.setBeanName(consumerInterface.getSimpleName());
        return (T) Proxy.newProxyInstance(
                null,
                new Class[]{consumerInterface},
                rpcProxyInvokeHandler);
    }
}
