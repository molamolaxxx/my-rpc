package com.mola.rpc.core.proxy;

import com.google.common.collect.Maps;
import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.common.lifecycle.ConsumerLifeCycle;
import com.mola.rpc.core.properties.RpcProperties;
import com.mola.rpc.core.proto.GenericRpcService;
import com.mola.rpc.core.proto.ProtoRpcConfigFactory;
import com.mola.rpc.core.proto.RpcInvoker;
import com.mola.rpc.core.remoting.netty.NettyRemoteClient;
import com.mola.rpc.data.config.spring.RpcProviderDataInitBean;
import com.mola.rpc.common.utils.AssertUtil;

import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 泛化调用代理生产工厂
 * @date : 2023-01-10 19:09
 **/
public class GenericRpcServiceProxyFactory {

    private static final String GENERIC_MODE_CONSUMER = "GENERIC_MODE_CONSUMER";

    /**
     * 所有代理由工厂维护一份缓存
     */
    private static final Map<String, GenericRpcService> genericRpcServiceMap = Maps.newConcurrentMap();

    /**
     * 获取代理
     * @param interfaceClazzName
     * @return
     */
    public static GenericRpcService getProxy(String interfaceClazzName, RpcMetaData rpcMetaData) {
        // 直接从缓存中取，取不到创建
        if (genericRpcServiceMap.containsKey(interfaceClazzName)) {
            return genericRpcServiceMap.get(interfaceClazzName);
        }
        ProtoRpcConfigFactory protoRpcConfigFactory = ProtoRpcConfigFactory.fetch();
        AssertUtil.isTrue(protoRpcConfigFactory.initialized(),
                "protoRpcConfigFactory has not been start! please call ProtoRpcConfigFactory::init first");
        RpcProperties rpcProperties = protoRpcConfigFactory.getRpcProperties();
        // 订阅服务
        RpcContext rpcContext = protoRpcConfigFactory.getRpcContext();
        rpcMetaData.setInterfaceClazz(GenericRpcService.class);
        rpcMetaData.setGenericInvoke(Boolean.TRUE);
        rpcMetaData.setGenericInterfaceName(interfaceClazzName);
        rpcContext.addConsumerMeta(interfaceClazzName, GENERIC_MODE_CONSUMER, rpcMetaData);
        // 启动client
        NettyRemoteClient nettyRemoteClient = protoRpcConfigFactory.getNettyRemoteClient();
        AssertUtil.isTrue(nettyRemoteClient.isStart(), "nettyRemoteClient has not been start! please call ProtoRpcConfigFactory::init first");
        // config server 动态注册
        if (rpcProperties.getStartConfigServer()) {
            RpcProviderDataInitBean rpcProviderDataInitBean = protoRpcConfigFactory.getRpcProviderDataInitBean();
            rpcProviderDataInitBean.pullProviderData(rpcMetaData);
            rpcProviderDataInitBean.registerProviderDataListener(rpcMetaData);
        }
        // 设置代理
        GenericRpcProxyInvokeHandler rpcProxyInvokeHandler = new GenericRpcProxyInvokeHandler();
        rpcProxyInvokeHandler.setRpcContext(protoRpcConfigFactory.getRpcContext());
        rpcProxyInvokeHandler.setLoadBalance(protoRpcConfigFactory.getLoadBalance());
        rpcProxyInvokeHandler.setNettyConnectPool(protoRpcConfigFactory.getNettyConnectPool());
        rpcProxyInvokeHandler.setNettyRemoteClient(protoRpcConfigFactory.getNettyRemoteClient());
        rpcProxyInvokeHandler.setBeanName(GENERIC_MODE_CONSUMER);
        rpcProxyInvokeHandler.setActualInterfaceClazzName(interfaceClazzName);
        GenericRpcService genericRpcService = (GenericRpcService) Proxy.newProxyInstance(
                RpcInvoker.class.getClassLoader(),
                new Class[]{GenericRpcService.class},
                rpcProxyInvokeHandler);
        genericRpcServiceMap.put(interfaceClazzName, genericRpcService);
        // 生命周期回调
        ConsumerLifeCycle.fetch().afterInitialize(rpcMetaData);
        return genericRpcService;
    }
}
