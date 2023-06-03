package com.mola.rpc.spring.autoconfig;

import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.common.ext.ExtensionRegistryManager;
import com.mola.rpc.common.interceptor.RpcInterceptor;
import com.mola.rpc.core.proto.ProtoRpcConfigFactory;
import com.mola.rpc.core.remoting.netty.NettyRemoteClient;
import com.mola.rpc.core.remoting.netty.NettyRemoteServer;
import com.mola.rpc.core.remoting.netty.pool.NettyConnectPool;
import com.mola.rpc.core.loadbalance.LoadBalance;
import com.mola.rpc.data.config.spring.RpcProviderDataInitBean;
import com.mola.rpc.spring.listeners.ApplicationDestroyListener;
import com.mola.rpc.spring.listeners.ApplicationStartupListener;
import com.mola.rpc.spring.postprocessor.RpcBeanFactoryPostProcessor;
import com.mola.rpc.spring.postprocessor.RpcConsumerInjectBeanProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Map;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-07-30 18:21
 **/
@Configuration
@EnableConfigurationProperties(RpcSpringConfigurationProperties.class)
@Import({RpcConsumerImportBeanDefinitionRegistrar.class})
public class RpcBeanPropertiesConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RpcBeanPropertiesConfiguration.class);

    @Bean
    public RpcContext rpcContext() {
        RpcContext rpcContext = RpcContext.fetch();
        rpcContext.setInSpringEnvironment(true);
        return rpcContext;
    }

    @Bean
    public ProtoRpcConfigFactory protoRpcConfigFactory(RpcSpringConfigurationProperties rpcProperties, ApplicationContext applicationContext, RpcContext rpcContext) {
        ProtoRpcConfigFactory protoRpcConfigFactory = ProtoRpcConfigFactory.fetch();
        protoRpcConfigFactory.init(rpcProperties);
        protoRpcConfigFactory.changeProviderObjectFetcher(providerMeta -> {
            if (providerMeta.getProviderObject() != null) {
                return providerMeta.getProviderObject();
            }
            return applicationContext.getBean(providerMeta.getProviderBeanName());
        });
        return protoRpcConfigFactory;
    }

    @Bean
    public LoadBalance loadBalance(ProtoRpcConfigFactory protoRpcConfigFactory) {
        return protoRpcConfigFactory.getLoadBalance();
    }

    @Bean
    public NettyConnectPool nettyConnectPool(ProtoRpcConfigFactory protoRpcConfigFactory) {
        return protoRpcConfigFactory.getNettyConnectPool();
    }

    @Bean
    public NettyRemoteClient nettyRemoteClient(ProtoRpcConfigFactory protoRpcConfigFactory) {
        return protoRpcConfigFactory.getNettyRemoteClient();
    }

    @Bean
    public NettyRemoteServer nettyRemoteServer(ProtoRpcConfigFactory protoRpcConfigFactory){
        return protoRpcConfigFactory.getNettyRemoteServer();
    }

    @Bean
    public RpcProviderDataInitBean rpcProviderDataInitBean(ProtoRpcConfigFactory protoRpcConfigFactory) {
        return protoRpcConfigFactory.getRpcProviderDataInitBean();
    }

    @Bean
    public ExtensionRegistryManager extensionRegistryManager(ProtoRpcConfigFactory protoRpcConfigFactory, ApplicationContext applicationContext) {
        ExtensionRegistryManager extensionRegistryManager = protoRpcConfigFactory.getExtensionRegistryManager();
        // 拦截器注册
        Map<String, RpcInterceptor> interceptorBeans = applicationContext.getBeansOfType(RpcInterceptor.class);
        if (interceptorBeans != null && interceptorBeans.size() > 0) {
            interceptorBeans.values().forEach(extensionRegistryManager::addInterceptor);
        }
        return extensionRegistryManager;
    }

    @Bean
    public BeanFactoryPostProcessor rpcConsumerBeanFactoryPostProcessor(){
        RpcBeanFactoryPostProcessor rpcConsumerBeanFactoryPostProcessor = new RpcBeanFactoryPostProcessor();
        rpcConsumerBeanFactoryPostProcessor.setRpcContext(RpcContext.fetch());
        return rpcConsumerBeanFactoryPostProcessor;
    }

    @Bean
    public RpcConsumerInjectBeanProcessor rpcConsumerInjectBeanProcessor(){
        RpcConsumerInjectBeanProcessor rpcConsumerInjectBeanProcessor = new RpcConsumerInjectBeanProcessor();
        return rpcConsumerInjectBeanProcessor;
    }

    @Bean
    public ApplicationStartupListener applicationStartupListener() {
        return new ApplicationStartupListener();
    }

    @Bean
    public ApplicationDestroyListener applicationDestroyListener(ProtoRpcConfigFactory protoRpcConfigFactory) {
        return new ApplicationDestroyListener(protoRpcConfigFactory);
    }
}
