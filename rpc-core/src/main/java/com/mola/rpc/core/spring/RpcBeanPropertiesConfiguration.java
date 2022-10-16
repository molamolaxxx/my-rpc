package com.mola.rpc.core.spring;

import com.mola.rpc.common.constants.LoadBalanceConstants;
import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.core.remoting.netty.NettyConnectPool;
import com.mola.rpc.core.remoting.netty.NettyRemoteClient;
import com.mola.rpc.core.remoting.netty.NettyRemoteServer;
import com.mola.rpc.core.strategy.balance.*;
import com.mola.rpc.core.util.NetUtils;
import com.mola.rpc.data.config.listener.AddressChangeListener;
import com.mola.rpc.data.config.manager.RpcDataManager;
import com.mola.rpc.data.config.manager.zk.ZkRpcDataManager;
import com.mola.rpc.data.config.spring.RpcProviderDataInitBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-07-30 18:21
 **/
@Configuration
@EnableConfigurationProperties(RpcProperties.class)
@Import({RpcConsumerImportBeanDefinitionRegistrar.class})
public class RpcBeanPropertiesConfiguration {

    @Bean
    public RpcContext rpcContext() {
        RpcContext rpcContext = new RpcContext();
        return rpcContext;
    }

    @Bean
    public LoadBalance loadBalance() {
        LoadBalance loadBalance = new LoadBalance();
        loadBalance.setStrategy(LoadBalanceConstants.LOAD_BALANCE_RANDOM_STRATEGY, new RandomLoadBalance());
        loadBalance.setStrategy(LoadBalanceConstants.LOAD_BALANCE_ROUND_ROBIN_STRATEGY, new RoundRobinBalance());
        loadBalance.setStrategy(LoadBalanceConstants.CONSISTENCY_HASHING_STRATEGY, new ConsistencyHashingBalance());
        return loadBalance;
    }

    @Bean
    public NettyConnectPool nettyConnectPool() {
        NettyConnectPool nettyConnectPool = new NettyConnectPool();
        return nettyConnectPool;
    }

    @Bean
    public NettyRemoteClient nettyRemoteClient(NettyConnectPool nettyConnectPool, RpcContext rpcContext) {
        NettyRemoteClient nettyRemoteClient = new NettyRemoteClient();
        nettyRemoteClient.setNettyConnectPool(nettyConnectPool);
        nettyRemoteClient.setRpcContext(rpcContext);
        nettyRemoteClient.start();
        return nettyRemoteClient;
    }

    @Bean
    public NettyRemoteServer nettyRemoteServer(RpcProperties rpcProperties, RpcContext rpcContext, ApplicationContext applicationContext) {
        NettyRemoteServer nettyRemoteServer = new NettyRemoteServer();
        nettyRemoteServer.setRpcProperties(rpcProperties);
        nettyRemoteServer.setRpcContext(rpcContext);
        nettyRemoteServer.setApplicationContext(applicationContext);
        nettyRemoteServer.start();
        return nettyRemoteServer;
    }

    @Bean
    public BeanFactoryPostProcessor rpcConsumerBeanFactoryPostProcessor(RpcContext rpcContext){
        RpcBeanFactoryPostProcessor rpcConsumerBeanFactoryPostProcessor = new RpcBeanFactoryPostProcessor();
        rpcConsumerBeanFactoryPostProcessor.setRpcContext(rpcContext);
        return rpcConsumerBeanFactoryPostProcessor;
    }

    @Bean
    public RpcProviderDataInitBean rpcProviderDataPuller(RpcContext rpcContext, RpcProperties rpcProperties, ApplicationContext applicationContext, LoadBalance loadBalance) {
        RpcProviderDataInitBean rpcProviderDataInitBean = new RpcProviderDataInitBean();
        rpcContext.setProviderAddress(NetUtils.getLocalAddress().getHostAddress() + ":" + rpcProperties.getServerPort());
        rpcProviderDataInitBean.setRpcContext(rpcContext);
        rpcProviderDataInitBean.setAppName(rpcProperties.getAppName());
        rpcProviderDataInitBean.setEnvironment(rpcProperties.getEnvironment());
        RpcDataManager<RpcMetaData> rpcDataManager = null;
        if (StringUtils.isEmpty(rpcProperties.getConfigServerBeanName())) {
            rpcDataManager = new ZkRpcDataManager(rpcProperties.getConfigServerAddress(), 10000);
        } else {
            rpcDataManager = applicationContext.getBean(rpcProperties.getConfigServerBeanName(), RpcDataManager.class);
        }
        Assert.notNull(rpcDataManager, "rpcDataManager is null");
        rpcDataManager.init(rpcContext);
        rpcProviderDataInitBean.setRpcDataManager(rpcDataManager);
        // 负载均衡监听变化
        for (LoadBalanceStrategy loadBalanceStrategy : loadBalance.getStrategyCollection()) {
            if (loadBalanceStrategy instanceof AddressChangeListener) {
                rpcProviderDataInitBean.addAddressChangeListener((AddressChangeListener) loadBalanceStrategy);
            }
        }
        rpcProviderDataInitBean.init();
        return rpcProviderDataInitBean;
    }
}
