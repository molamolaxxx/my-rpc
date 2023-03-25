package com.mola.rpc.spring;

import com.mola.rpc.common.constants.CommonConstants;
import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.common.entity.RpcMetaData;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-08-07 11:06
 **/
public class RpcBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    private RpcContext rpcContext;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        try {
            for (String beanDefinitionName : beanFactory.getBeanDefinitionNames()) {
                BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);
                if (beanDefinition.getAttribute(CommonConstants.BEAN_DEF_CONSUMER_META) != null) {
                    RpcMetaData rpcMetaData = (RpcMetaData) beanDefinition.getAttribute(CommonConstants.BEAN_DEF_CONSUMER_META);
                    // 初始化客户端meta
                    rpcContext.addConsumerMeta(rpcMetaData.getInterfaceClazz().getName(), beanDefinitionName, rpcMetaData);
                } else if (beanDefinition.getAttribute(CommonConstants.BEAN_DEF_PROVIDER_META) != null) {
                    // 初始化服务端meta
                    RpcMetaData rpcMetaData = (RpcMetaData) beanDefinition.getAttribute(CommonConstants.BEAN_DEF_PROVIDER_META);
                    rpcMetaData.setProviderBeanClazz(beanDefinition.getBeanClassName());
                    rpcMetaData.setProviderBeanName(beanDefinitionName);
                    rpcContext.addProviderMeta(rpcMetaData.getInterfaceClazz().getName(), rpcMetaData);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setRpcContext(RpcContext rpcContext) {
        this.rpcContext = rpcContext;
    }
}
