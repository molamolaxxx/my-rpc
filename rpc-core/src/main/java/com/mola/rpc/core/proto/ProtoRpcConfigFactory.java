package com.mola.rpc.core.proto;

import com.mola.rpc.common.constants.CommonConstants;
import com.mola.rpc.common.constants.LoadBalanceConstants;
import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.common.utils.TestUtil;
import com.mola.rpc.core.properties.RpcProperties;
import com.mola.rpc.core.remoting.handler.NettyRpcRequestHandler;
import com.mola.rpc.core.remoting.handler.NettyRpcResponseHandler;
import com.mola.rpc.core.remoting.netty.pool.NettyConnectPool;
import com.mola.rpc.core.remoting.netty.NettyRemoteClient;
import com.mola.rpc.core.remoting.netty.NettyRemoteServer;
import com.mola.rpc.core.strategy.balance.*;
import com.mola.rpc.core.system.ReverseInvokeHelper;
import com.mola.rpc.core.util.NetUtils;
import com.mola.rpc.data.config.listener.AddressChangeListener;
import com.mola.rpc.data.config.manager.RpcDataManager;
import com.mola.rpc.data.config.manager.nacos.NacosRpcDataManager;
import com.mola.rpc.data.config.manager.zk.ZkRpcDataManager;
import com.mola.rpc.data.config.spring.RpcProviderDataInitBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

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

    private static AtomicBoolean INIT_FLAG = new AtomicBoolean(false);

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

    private NettyRpcRequestHandler requestHandler;
    private NettyRpcResponseHandler responseHandler;

    private ObjectFetcher providerObjectFetcher = providerMeta -> providerMeta.getProviderObject();
    protected ProtoRpcConfigFactory(){}
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
    public void init(RpcProperties rpcProperties) {
        try {
            if (INIT_FLAG.get() && !TestUtil.isJUnitTest()) {
                log.warn("ProtoRpcConfigFactory has been init!");
                return;
            }
            this.rpcProperties = rpcProperties;
            // 上下文初始化
            this.initContext();
            // 负载均衡初始化
            this.initLoadBalance();
            // 网络初始化
            this.initNettyConfiguration();
            // config server配置
            this.initConfigServer();
            // 反向provider注册
            this.registerReverseProvider();
            INIT_FLAG.compareAndSet(false, true);
        } catch (Exception e) {
            INIT_FLAG.compareAndSet(true, false);
            throw e;
        }
    }

    public void shutdown() {
        // 关闭netty服务器
        if (null != nettyRemoteServer && nettyRemoteServer.isStart()) {
            nettyRemoteServer.shutdown();
        }
        if (null != nettyRemoteClient && nettyRemoteClient.isStart()) {
            nettyRemoteClient.shutdown();
        }
        // 关闭监听线程
        ReverseInvokeHelper.instance().shutdownMonitor();
        rpcProviderDataInitBean.shutdownMonitor();
        // 删除配置中心数据
        Assert.notNull(this.rpcProperties, "rpcProperties is null");
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

    protected void initLoadBalance() {
        LoadBalance loadBalance = new LoadBalance();
        loadBalance.setStrategy(LoadBalanceConstants.LOAD_BALANCE_RANDOM_STRATEGY, new RandomLoadBalance());
        loadBalance.setStrategy(LoadBalanceConstants.LOAD_BALANCE_ROUND_ROBIN_STRATEGY, new RoundRobinBalance());
        loadBalance.setStrategy(LoadBalanceConstants.CONSISTENCY_HASHING_STRATEGY, new ConsistencyHashingBalance());
        this.loadBalance = loadBalance;
    }

    protected void initNettyConfiguration() {
        // 响应处理器
        this.responseHandler = new NettyRpcResponseHandler();
        // rpc连接池
        this.nettyConnectPool = new NettyConnectPool();
        // 请求处理器
        this.requestHandler = new NettyRpcRequestHandler(rpcContext, providerObjectFetcher, rpcProperties, nettyConnectPool);
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
        ReverseInvokeHelper.instance().startMonitor();
    }

    /**
     * 切换configServer
     * @param rpcProperties
     */
    public void changeConfigServer(RpcProperties rpcProperties) {
        RpcProperties currentProperties = this.rpcProperties;
        currentProperties.setConfigServerType(rpcProperties.getConfigServerType());
        currentProperties.setConfigServerAddress(rpcProperties.getConfigServerAddress());
        currentProperties.setEnvironment(rpcProperties.getEnvironment());
        currentProperties.setStartConfigServer(rpcProperties.getStartConfigServer());
        RpcDataManager<RpcMetaData> rpcDataManager = null;
        if (CommonConstants.ZOOKEEPER.equals(rpcProperties.getConfigServerType())) {
            rpcDataManager = new ZkRpcDataManager(rpcProperties);
        } else if (CommonConstants.NACOS.equals(rpcProperties.getConfigServerType())){
            rpcDataManager = new NacosRpcDataManager(rpcProperties);
        } else {
            rpcDataManager = rpcProperties.getRpcDataManager();
        }
        Assert.notNull(rpcDataManager, "rpcDataManager is null");
        rpcDataManager.init(rpcContext);
        this.rpcProviderDataInitBean.refresh(rpcDataManager);
    }

    protected void initConfigServer() {
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
        Assert.notNull(rpcDataManager, "rpcDataManager is null");
        rpcDataManager.init(rpcContext);
        rpcProviderDataInitBean.setRpcDataManager(rpcDataManager);
        // 负载均衡监听变化
        for (LoadBalanceStrategy loadBalanceStrategy : loadBalance.getStrategyCollection()) {
            if (loadBalanceStrategy instanceof AddressChangeListener) {
                rpcProviderDataInitBean.addAddressChangeListener((AddressChangeListener) loadBalanceStrategy);
            }
        }
        rpcProviderDataInitBean.init(rpcProperties);
        this.rpcProviderDataInitBean = rpcProviderDataInitBean;
    }

    private void registerReverseProvider() {
        Collection<RpcMetaData> rpcMetaDataCollection = this.rpcContext.getProviderMetaMap().values();
        for (RpcMetaData providerMeta : rpcMetaDataCollection) {
            if (Boolean.TRUE.equals(providerMeta.getReverseMode())) {
                ReverseInvokeHelper.instance().registerProviderToServer(providerMeta);
            }
        }
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

    public void setProviderObjectFetcher(ObjectFetcher providerObjectFetcher) {
        this.providerObjectFetcher = providerObjectFetcher;
        this.requestHandler.setProviderFetcher(providerObjectFetcher);
    }
}
