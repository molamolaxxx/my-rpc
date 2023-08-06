package com.mola.rpc.core.proto;

import com.mola.rpc.common.constants.CommonConstants;
import com.mola.rpc.common.constants.LoadBalanceConstants;
import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.common.ext.ExtensionRegistryManager;
import com.mola.rpc.common.lifecycle.ConsumerLifeCycle;
import com.mola.rpc.common.lifecycle.ConsumerLifeCycleHandler;
import com.mola.rpc.common.utils.TestUtil;
import com.mola.rpc.core.excutors.factory.RpcExecutorFactory;
import com.mola.rpc.core.excutors.factory.RpcTaskFactory;
import com.mola.rpc.core.loadbalance.*;
import com.mola.rpc.core.properties.RpcProperties;
import com.mola.rpc.core.remoting.handler.NettyRpcRequestHandler;
import com.mola.rpc.core.remoting.handler.NettyRpcResponseHandler;
import com.mola.rpc.core.remoting.netty.NettyRemoteClient;
import com.mola.rpc.core.remoting.netty.NettyRemoteServer;
import com.mola.rpc.core.remoting.netty.pool.NettyConnectPool;
import com.mola.rpc.core.system.ReverseInvokeHelper;
import com.mola.rpc.core.util.NetUtils;
import com.mola.rpc.data.config.manager.RpcDataManager;
import com.mola.rpc.data.config.manager.nacos.NacosRpcDataManager;
import com.mola.rpc.data.config.manager.zk.ZkRpcDataManager;
import com.mola.rpc.data.config.spring.RpcProviderDataInitBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mola.rpc.common.utils.AssertUtil;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 直接使用的配置工厂
 * @date : 2022-10-23 11:38
 **/
public class ProtoRpcConfigFactory {

    private static final Logger log = LoggerFactory.getLogger(ProtoRpcConfigFactory.class);

    private static final AtomicBoolean INIT_FLAG = new AtomicBoolean(false);

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
     * 执行器工厂
     */
    private RpcExecutorFactory rpcExecutorFactory;

    /**
     * 执行器任务工厂
     */
    private RpcTaskFactory rpcTaskFactory;

    private ConsumerLifeCycle consumerLifeCycle;

    private NettyRpcRequestHandler requestHandler;
    private NettyRpcResponseHandler responseHandler;

    private ObjectFetcher providerObjectFetcher = providerMeta -> providerMeta.getProviderObject();

    private ExtensionRegistryManager extensionRegistryManager;
    protected ProtoRpcConfigFactory(){}
    static class Singleton{
        private static ProtoRpcConfigFactory protoRpcConfigFactory = new ProtoRpcConfigFactory();
    }

    public static ProtoRpcConfigFactory fetch(){
        return Singleton.protoRpcConfigFactory;
    }
    /**
     * 配置
     * @param rpcProperties
     */
    public void init(RpcProperties rpcProperties) {
        try {
            if (INIT_FLAG.get() && !TestUtil.isJUnitTest()) {
                log.warn("ProtoRpcConfigFactory has been init!");
                return;
            }
            this.consumerLifeCycle = ConsumerLifeCycle.fetch();
            this.rpcProperties = rpcProperties;
            // 上下文初始化
            initContext();
            // 上下文检查
            checkContext();
            // 负载均衡初始化
            initLoadBalance();
            // 执行器初始化
            initExecutors();
            // 网络初始化
            initNettyConfiguration();
            // 创建cs客户端
            createConfigServerClient();
            // 扩展点管理初始化
            initExtensionManager();
            /*
            如果是spring环境，服务上报需要在spring启动完成后进行
             */
            if (!rpcContext.isInSpringEnvironment()) {
                // config server配置，上报服务
                rpcProviderDataInitBean.init(rpcProperties);
                // 反向provider注册，上报反向代理服务
                registerReverseProvider();
            }

            INIT_FLAG.compareAndSet(false, true);
        } catch (Exception e) {
            INIT_FLAG.compareAndSet(true, false);
            throw e;
        }
    }

    public void shutdown() {
        // 关闭netty服务器
        if (nettyRemoteServer != null && nettyRemoteServer.isStart()) {
            nettyRemoteServer.shutdown();
        }
        if (nettyRemoteClient != null && nettyRemoteClient.isStart()) {
            nettyRemoteClient.shutdown();
        }
        // 关闭监听线程
        ReverseInvokeHelper.instance().shutdownMonitor();
        rpcProviderDataInitBean.shutdownMonitor();
        // 删除配置中心数据
        AssertUtil.notNull(this.rpcProperties, "rpcProperties is null");
        if (!rpcProperties.getStartConfigServer()) {
            return;
        }
        RpcContext rpcContext = rpcProviderDataInitBean.getRpcContext();
        RpcDataManager rpcDataManager = rpcProviderDataInitBean.getRpcDataManager();
        Collection<RpcMetaData> providerMetaDataCollection = rpcContext.getProviderMetaMap().values();
        for (RpcMetaData providerMetaData : providerMetaDataCollection) {
            rpcDataManager.deleteRemoteProviderData(providerMetaData,
                    rpcProviderDataInitBean.getEnvironment(), rpcProviderDataInitBean.getAppName(), rpcContext.getProviderAddress());
        }
        log.info("delete config server data success!");
    }

    protected void initContext() {
        this.rpcContext = RpcContext.fetch();
    }

    protected void checkContext() {
        // 检查客户端超时时间
        for (RpcMetaData clientMeta : rpcContext.getConsumerMetaMap().values()) {
            // 客户端超时时间检查
            if (clientMeta.getClientTimeout() > rpcProperties.getMaxClientTimeout()) {
                throw new RuntimeException(String.format("%s's timeout can not longer than max client timeout, current is %s, max is %s",
                        clientMeta.getInterfaceClazz().getName(), clientMeta.getClientTimeout(), rpcProperties.getMaxClientTimeout()));
            }
        }
    }

    protected void initLoadBalance() {
        LoadBalance loadBalance = new LoadBalance();
        loadBalance.setStrategy(LoadBalanceConstants.RANDOM_STRATEGY, new RandomLoadBalance());
        loadBalance.setStrategy(LoadBalanceConstants.ROUND_ROBIN_STRATEGY, new RoundRobinBalance());
        loadBalance.setStrategy(LoadBalanceConstants.CONSISTENCY_HASHING_STRATEGY, new ConsistencyHashingBalance());
        this.loadBalance = loadBalance;
    }

    protected void initExecutors() {
        this.rpcTaskFactory = new RpcTaskFactory();
        this.rpcExecutorFactory = new RpcExecutorFactory(rpcProperties, rpcContext);
    }

    protected void initExtensionManager() {
        this.extensionRegistryManager = new ExtensionRegistryManager();
    }

    protected void initNettyConfiguration() {
        // 响应处理器
        this.responseHandler = new NettyRpcResponseHandler();
        // rpc连接池
        this.nettyConnectPool = new NettyConnectPool();
        // 请求处理器
        this.requestHandler = new NettyRpcRequestHandler(rpcContext, providerObjectFetcher);
        NettyRemoteClient nettyRemoteClient = new NettyRemoteClient(requestHandler, responseHandler);
        nettyRemoteClient.setNettyConnectPool(nettyConnectPool);
        nettyRemoteClient.setRpcContext(rpcContext);
        nettyRemoteClient.start(true);
        this.nettyRemoteClient = nettyRemoteClient;
        NettyRemoteServer nettyRemoteServer = new NettyRemoteServer(requestHandler, responseHandler);
        nettyRemoteServer.setRpcProperties(rpcProperties);
        nettyRemoteServer.setRpcContext(rpcContext);
        nettyRemoteServer.start();
        this.nettyRemoteServer = nettyRemoteServer;
    }

    public void createConfigServerClient() {
        RpcProviderDataInitBean rpcProviderDataInitBean = new RpcProviderDataInitBean();
        if (!rpcProperties.getStartConfigServer()) {
            log.error("will not start config server! startConfigServer = false");
            this.rpcProviderDataInitBean = rpcProviderDataInitBean;
            return;
        }
        rpcContext.setProviderAddress(NetUtils.getLocalAddress().getHostAddress() + ":" + rpcProperties.getServerPort());
        rpcProviderDataInitBean.setRpcContext(rpcContext);
        rpcProviderDataInitBean.setAppName(rpcProperties.getAppName());
        rpcProviderDataInitBean.setEnvironment(rpcProperties.getEnvironment());
        RpcDataManager<RpcMetaData> rpcDataManager = null;
        if (CommonConstants.ZOOKEEPER.equals(rpcProperties.getConfigServerType())) {
            rpcDataManager = new ZkRpcDataManager(rpcProperties);
        } else if (CommonConstants.NACOS.equals(rpcProperties.getConfigServerType())){
            rpcDataManager = new NacosRpcDataManager(rpcProperties);
        } else {
            rpcDataManager = rpcProperties.getRpcDataManager();
        }
        AssertUtil.notNull(rpcDataManager, "rpcDataManager is null");
        rpcDataManager.init(rpcContext);
        rpcProviderDataInitBean.setRpcDataManager(rpcDataManager);
        // 负载均衡监听变化
        for (LoadBalanceStrategy loadBalanceStrategy : loadBalance.getStrategyCollection()) {
            if (loadBalanceStrategy instanceof ConsumerLifeCycleHandler) {
                consumerLifeCycle.addListener((ConsumerLifeCycleHandler) loadBalanceStrategy);
            }
        }
        this.rpcProviderDataInitBean = rpcProviderDataInitBean;
    }

    public void registerReverseProvider() {
        Collection<RpcMetaData> rpcMetaDataCollection = rpcContext.getProviderMetaMap().values();
        boolean requireStartMonitor = false;
        for (RpcMetaData providerMeta : rpcMetaDataCollection) {
            if (Boolean.TRUE.equals(providerMeta.getReverseMode())) {
                ReverseInvokeHelper.instance().registerProviderToServer(providerMeta);
                requireStartMonitor = true;
            }
        }
        if (requireStartMonitor) {
            ReverseInvokeHelper.instance().startMonitor();
        }
    }

    public void changeProviderObjectFetcher(ObjectFetcher providerObjectFetcher) {
        this.providerObjectFetcher = providerObjectFetcher;
        // 覆盖老的provider获取器
        this.requestHandler.setProviderFetcher(providerObjectFetcher);
    }

    public boolean initialized() {
        return INIT_FLAG.get();
    }

    /*
    getset
     */
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

    public RpcExecutorFactory getRpcExecutorFactory() {
        return rpcExecutorFactory;
    }

    public void setRpcExecutorFactory(RpcExecutorFactory rpcExecutorFactory) {
        this.rpcExecutorFactory = rpcExecutorFactory;
    }

    public RpcTaskFactory getRpcTaskFactory() {
        return rpcTaskFactory;
    }

    public void setRpcTaskFactory(RpcTaskFactory rpcTaskFactory) {
        this.rpcTaskFactory = rpcTaskFactory;
    }

    public ConsumerLifeCycle getConsumerLifeCycle() {
        return consumerLifeCycle;
    }

    public ExtensionRegistryManager getExtensionRegistryManager() {
        return extensionRegistryManager;
    }
}
