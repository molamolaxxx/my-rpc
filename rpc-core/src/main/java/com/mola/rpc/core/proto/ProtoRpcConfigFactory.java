package com.mola.rpc.core.proto;

import com.mola.rpc.common.constants.LoadBalanceConstants;
import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.core.properties.RpcProperties;
import com.mola.rpc.core.remoting.netty.NettyConnectPool;
import com.mola.rpc.core.remoting.netty.NettyRemoteClient;
import com.mola.rpc.core.remoting.netty.NettyRemoteServer;
import com.mola.rpc.core.strategy.balance.*;
import com.mola.rpc.core.util.NetUtils;
import com.mola.rpc.data.config.listener.AddressChangeListener;
import com.mola.rpc.data.config.manager.RpcDataManager;
import com.mola.rpc.data.config.manager.zk.ZkRpcDataManager;
import com.mola.rpc.data.config.spring.RpcProviderDataInitBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 直接使用的配置工厂
 * @date : 2022-10-23 11:38
 **/
public class ProtoRpcConfigFactory {

    private static final Logger log = LoggerFactory.getLogger(ProtoRpcConfigFactory.class);

    public static AtomicBoolean INIT_FLAG = new AtomicBoolean(false);

    /**
     * 全局rpc上下文
     */
    private RpcContext rpcContext;

    /**
     * 负载均衡器
     */
    private LoadBalance loadBalance;

    /**
     * 网络连接池
     */
    private NettyConnectPool nettyConnectPool;

    /**
     * 网络客户端
     */
    private NettyRemoteClient nettyRemoteClient;

    /**
     * 网络服务端
     */
    private NettyRemoteServer nettyRemoteServer;

    /**
     * config server 配置
     */
    private RpcProviderDataInitBean rpcProviderDataInitBean;

    /**
     * 配置
     */
    private RpcProperties rpcProperties;

    /**
     * 自定义configure server实现
     */
    private RpcDataManager<RpcMetaData> rpcDataManager;

    private ProtoRpcConfigFactory(){}
    static class Singleton{
        private static ProtoRpcConfigFactory protoRpcConfigFactory = new ProtoRpcConfigFactory();
    }

    public static ProtoRpcConfigFactory get(){
        return Singleton.protoRpcConfigFactory;
    }

    /**
     * 配置
     * @param rpcProperties
     */
    public static final void configure(RpcProperties rpcProperties) {
        try {
            ProtoRpcConfigFactory protoRpcConfigFactory = get();
            if (INIT_FLAG.get()) {
                log.warn("ProtoRpcConfigFactory has been init!");
                return;
            }
            protoRpcConfigFactory.rpcProperties = rpcProperties;
            INIT_FLAG.compareAndSet(false, true);
            // 上下文初始化
            protoRpcConfigFactory.initContext();
            // 负载均衡初始化
            protoRpcConfigFactory.initLoadBalance();
            // 网络初始化
            protoRpcConfigFactory.initNettyConfiguration();
            // config server配置
            if (rpcProperties.getStartConfigServer()) {
                protoRpcConfigFactory.initConfigServer();
            }
        } catch (Exception e) {
            INIT_FLAG.compareAndSet(true, false);
            throw e;
        }
    }

    private void initContext() {
        this.rpcContext = new RpcContext();
    }

    private void initLoadBalance() {
        LoadBalance loadBalance = new LoadBalance();
        loadBalance.setStrategy(LoadBalanceConstants.LOAD_BALANCE_RANDOM_STRATEGY, new RandomLoadBalance());
        loadBalance.setStrategy(LoadBalanceConstants.LOAD_BALANCE_ROUND_ROBIN_STRATEGY, new RoundRobinBalance());
        loadBalance.setStrategy(LoadBalanceConstants.CONSISTENCY_HASHING_STRATEGY, new ConsistencyHashingBalance());
        loadBalance.setStrategy(LoadBalanceConstants.LOAD_BALANCE_APPOINTED_RANDOM_STRATEGY, new AppointedRandomLoadBalance());
        this.loadBalance = loadBalance;
    }

    private void initNettyConfiguration() {
        this.nettyConnectPool = new NettyConnectPool();
        NettyRemoteClient nettyRemoteClient = new NettyRemoteClient("proto");
        nettyRemoteClient.setNettyConnectPool(nettyConnectPool);
        nettyRemoteClient.setRpcContext(rpcContext);
        nettyRemoteClient.start(true);
        this.nettyRemoteClient = nettyRemoteClient;
        NettyRemoteServer nettyRemoteServer = new NettyRemoteServer("proto");
        nettyRemoteServer.setRpcProperties(rpcProperties);
        nettyRemoteServer.setRpcContext(rpcContext);
        nettyRemoteServer.setProviderFetcher(providerName -> {
            return null;
        });
        nettyRemoteServer.start();
        this.nettyRemoteServer = nettyRemoteServer;
    }

    private void initConfigServer() {
        RpcProviderDataInitBean rpcProviderDataInitBean = new RpcProviderDataInitBean();
        rpcContext.setProviderAddress(NetUtils.getLocalAddress().getHostAddress() + ":" + rpcProperties.getServerPort());
        rpcProviderDataInitBean.setRpcContext(rpcContext);
        rpcProviderDataInitBean.setAppName(rpcProperties.getAppName());
        rpcProviderDataInitBean.setEnvironment(rpcProperties.getEnvironment());
        RpcDataManager<RpcMetaData> rpcDataManager = null;
        if (null == rpcProperties.getRpcDataManager()) {
            rpcDataManager = new ZkRpcDataManager(rpcProperties.getConfigServerAddress(), 10000);
        } else {
            rpcDataManager = rpcProperties.getRpcDataManager();
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
        this.rpcDataManager = rpcDataManager;
        this.rpcProviderDataInitBean = rpcProviderDataInitBean;
    }

    public RpcContext getRpcContext() {
        return rpcContext;
    }

    public LoadBalance getLoadBalance() {
        return loadBalance;
    }

    public NettyConnectPool getNettyConnectPool() {
        return nettyConnectPool;
    }

    public NettyRemoteClient getNettyRemoteClient() {
        return nettyRemoteClient;
    }

    public NettyRemoteServer getNettyRemoteServer() {
        return nettyRemoteServer;
    }

    public RpcProviderDataInitBean getRpcProviderDataInitBean() {
        return rpcProviderDataInitBean;
    }

    public RpcProperties getRpcProperties() {
        return rpcProperties;
    }

    public RpcDataManager<RpcMetaData> getRpcDataManager() {
        return rpcDataManager;
    }
}
