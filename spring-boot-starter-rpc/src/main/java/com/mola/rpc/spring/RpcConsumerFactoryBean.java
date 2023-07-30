package com.mola.rpc.spring;

import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.common.lifecycle.ConsumerLifeCycle;
import com.mola.rpc.core.proxy.RpcProxyInvokeHandler;
import com.mola.rpc.core.remoting.netty.NettyRemoteClient;
import com.mola.rpc.core.remoting.netty.pool.NettyConnectPool;
import com.mola.rpc.core.loadbalance.LoadBalance;
import org.springframework.beans.factory.FactoryBean;

import javax.annotation.Resource;
import java.lang.reflect.Proxy;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-07-30 19:04
 **/
public class RpcConsumerFactoryBean implements FactoryBean {

    @Resource
    private RpcContext rpcContext;

    @Resource
    private LoadBalance loadBalance;

    @Resource
    private NettyConnectPool nettyConnectPool;

    @Resource
    private NettyRemoteClient nettyRemoteClient;

    private final Class<?> consumerInterface;

    private final String beanName;

    public RpcConsumerFactoryBean(Class<?> consumerInterface, String beanName) {
        this.consumerInterface = consumerInterface;
        this.beanName = beanName;
    }

    @Override
    public Object getObject() throws Exception {
        RpcProxyInvokeHandler rpcProxyInvokeHandler = new RpcProxyInvokeHandler();
        rpcProxyInvokeHandler.setRpcContext(rpcContext);
        rpcProxyInvokeHandler.setLoadBalance(loadBalance);
        rpcProxyInvokeHandler.setNettyConnectPool(nettyConnectPool);
        rpcProxyInvokeHandler.setNettyRemoteClient(nettyRemoteClient);
        rpcProxyInvokeHandler.setBeanName(beanName);
        Object proxyInstance = Proxy.newProxyInstance(
                RpcConsumerFactoryBean.class.getClassLoader(),
                new Class[]{consumerInterface},
                rpcProxyInvokeHandler);
        // 生命周期回调
        RpcMetaData consumerMeta = rpcContext.getConsumerMeta(consumerInterface.getName(), beanName);
        ConsumerLifeCycle.fetch().afterInitialize(consumerMeta);
        return proxyInstance;
    }

    @Override
    public Class<?> getObjectType() {
        return consumerInterface;
    }
}
