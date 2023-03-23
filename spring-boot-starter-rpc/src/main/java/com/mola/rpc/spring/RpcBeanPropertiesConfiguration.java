package com.mola.rpc.spring;

import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.core.proto.ProtoRpcConfigFactory;
import com.mola.rpc.core.remoting.netty.pool.NettyConnectPool;
import com.mola.rpc.core.remoting.netty.NettyRemoteClient;
import com.mola.rpc.core.remoting.netty.NettyRemoteServer;
import com.mola.rpc.core.strategy.balance.LoadBalance;
import com.mola.rpc.data.config.spring.RpcProviderDataInitBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

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
    public RpcContext rpcContext(ProtoRpcConfigFactory protoRpcConfigFactory) {
        return protoRpcConfigFactory.getRpcContext();
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
    public ProtoRpcConfigFactory protoRpcConfigFactory(RpcSpringConfigurationProperties rpcProperties, ApplicationContext applicationContext) {
        ProtoRpcConfigFactory protoRpcConfigFactory = ProtoRpcConfigFactory.get();
        protoRpcConfigFactory.init(rpcProperties);
        protoRpcConfigFactory.setProviderObjectFetcher(providerMeta -> {
            if (null != providerMeta.getProviderObject()) {
                return providerMeta.getProviderObject();
            }
            return applicationContext.getBean(providerMeta.getProviderBeanName());
        });
        return protoRpcConfigFactory;
    }

    @Bean
    public NettyRemoteServer nettyRemoteServer(ProtoRpcConfigFactory protoRpcConfigFactory){
        return protoRpcConfigFactory.getNettyRemoteServer();
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
    public RpcProviderDataInitBean rpcProviderDataPuller(ProtoRpcConfigFactory protoRpcConfigFactory) {
        return protoRpcConfigFactory.getRpcProviderDataInitBean();
    }
}
