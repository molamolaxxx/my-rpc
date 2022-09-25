package com.mola.rpc.core.spring;

import com.mola.rpc.core.proxy.RpcProxyInvokeHandler;
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
