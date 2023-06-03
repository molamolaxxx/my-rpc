package com.mola.rpc.data.config.spring;

import com.alibaba.fastjson.JSONObject;
import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.common.entity.AddressInfo;
import com.mola.rpc.common.entity.BaseRpcProperties;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.common.lifecycle.ConsumerLifeCycle;
import com.mola.rpc.data.config.manager.RpcDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 客户端数据拉取
 * @date : 2022-08-27 15:40
 **/
public class RpcProviderDataInitBean {

    private static final Logger log = LoggerFactory.getLogger(RpcProviderDataInitBean.class);

    /**
     * rpc上下文
     */
    private RpcContext rpcContext;

    /**
     * 运行环境
     */
    private String environment;

    /**
     * 应用名
     */
    private String appName;

    /**
     * rpc服务
     */
    private RpcDataManager<RpcMetaData> rpcDataManager;


    /**
     * 定时上报服务端配置
     */
    private ScheduledExecutorService providerInfoUploadMonitorService;

    /**
     * 定时下载客户端配置
     */
    private ScheduledExecutorService consumerInfoDownloadMonitorService;

    private BaseRpcProperties rpcProperties;

    public void init(BaseRpcProperties rpcProperties) {
        Assert.notNull(rpcContext, "拉取数据失败，上下文为空");
        this.rpcProperties = rpcProperties;
        // 上报provider信息
        Collection<RpcMetaData> providerMetaDataCollection = rpcContext.getProviderMetaMap().values();
        for (RpcMetaData providerMetaData : providerMetaDataCollection) {
            uploadRemoteProviderData(providerMetaData);
        }
        // 拉取订阅的provider数据，填充到上下文
        pullProviderDataList();
        // 注册监听器
        registerProviderDataListeners();
        // 定时上报服务
        startProviderInfoUploadMonitor();
        // 定时刷新consumer
        startConsumerInfoDownloadMonitor();
    }

    public void uploadRemoteProviderData(RpcMetaData providerMetaData) {
        // 反向代理模式不用进行服务发现
        if (Boolean.TRUE.equals(providerMetaData.getReverseMode())) {
            return;
        }
        rpcDataManager.uploadRemoteProviderData(providerMetaData, environment, appName, rpcContext.getProviderAddress());
    }

    private void registerProviderDataListeners() {
        Collection<RpcMetaData> consumerMetaDataCollection = rpcContext.getConsumerMetaMap().values();
        for (RpcMetaData consumerMetaData : consumerMetaDataCollection) {
            registerProviderDataListener(consumerMetaData);
        }
    }

    /**
     * 注册provider监听器，如果provider有变化会回调到本地监听器
     * @param consumerMetaData
     */
    public void registerProviderDataListener(RpcMetaData consumerMetaData) {
        // 服务名
        String serviceName = consumerMetaData.getInterfaceClazz().getName();
        // 泛化调用
        if (Boolean.TRUE.equals(consumerMetaData.getGenericInvoke())) {
            Assert.hasText(consumerMetaData.getGenericInterfaceName(), "GenericInterfaceName can not be empty! because of this service is Generic Service");
            serviceName = consumerMetaData.getGenericInterfaceName();
        }
        // group
        String group = consumerMetaData.getGroup();
        // version
        String version = consumerMetaData.getVersion();
        // 从配置中心上拉取配置，注册监听器
        rpcDataManager.registerProviderDataListener(serviceName, group, version, environment, consumerMetaData);
    }

    public void pullProviderDataList() {
        Collection<RpcMetaData> consumerMetaDataCollection = rpcContext.getConsumerMetaMap().values();
        for (RpcMetaData consumerMetaData : consumerMetaDataCollection) {
            // 反向代理模式不用进行服务发现
            if (Boolean.TRUE.equals(consumerMetaData.getReverseMode())) {
                continue;
            }
            pullProviderData(consumerMetaData);
        }
    }

    /**
     * 拉取consumer对应provider在configserver上的信息，存储到应用本地
     * @param consumerMetaData
     */
    public void pullProviderData(RpcMetaData consumerMetaData) {
        // 服务名
        String serviceName = consumerMetaData.getInterfaceClazz().getName();
        // 泛化调用
        if (consumerMetaData.getGenericInvoke()) {
            Assert.hasText(consumerMetaData.getGenericInterfaceName(), "GenericInterfaceName can not be empty! because of this service is Generic Service");
            serviceName = consumerMetaData.getGenericInterfaceName();
        }
        // group
        String group = consumerMetaData.getGroup();
        // version
        String version = consumerMetaData.getVersion();
        // 判断服务是否存在
        if (!rpcDataManager.isProviderAvailable(serviceName, group, version, environment)) {
            if (this.rpcProperties.getCheckDependencyProviderBeforeStart()) {
                throw new RuntimeException("provider not exist! env = " + environment + ", meta = " + consumerMetaData.toString());
            }
        }
        List<AddressInfo> addressInfoList = rpcDataManager.getRemoteProviderAddress(serviceName, group, version, environment);
        if (addressInfoList == null) {
            log.warn("addressInfoList is null , meta : " + consumerMetaData.toString());
            return;
        }
        consumerMetaData.setAddressList(addressInfoList);
        // 主动拉取地址回调
        ConsumerLifeCycle.fetch().afterAddressChange(consumerMetaData);
    }

    private void startProviderInfoUploadMonitor() {
        if (!rpcDataManager.requireSendProviderHeartBeat()) {
            return;
        }
        this.providerInfoUploadMonitorService = Executors.newScheduledThreadPool(1,
                new ThreadFactory() {
                    AtomicInteger threadIndex = new AtomicInteger(0);
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r, "provider-info-upload-monitor-thread-" + this.threadIndex.incrementAndGet());
                        thread.setDaemon(true);
                        return thread;
                    }
                });
        this.providerInfoUploadMonitorService.scheduleAtFixedRate(() -> {
            // 获取所有提供服务信息
            for (RpcMetaData rpcMetaData : rpcContext.getProviderMetaMap().values()) {
                // 判断服务是否存在
                if (!rpcDataManager.isInstanceAvailable(rpcMetaData.getInterfaceClazz().getName(),
                        rpcMetaData.getGroup(), rpcMetaData.getVersion(), environment, rpcContext.getProviderAddress())) {
                    log.warn("service is not exist! will never upload, meta = " + JSONObject.toJSONString(rpcMetaData));
                    continue;
                }
                // 获取主机运行状态
                // 上报信息
                rpcDataManager.uploadRemoteProviderData(rpcMetaData, environment, appName, rpcContext.getProviderAddress());
            }
        },30, 60, TimeUnit.SECONDS);
    }

    private void startConsumerInfoDownloadMonitor() {
        this.consumerInfoDownloadMonitorService = Executors.newScheduledThreadPool(1,
                new ThreadFactory() {
                    AtomicInteger threadIndex = new AtomicInteger(0);
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r, "consumer-info-download-monitor-thread-" + this.threadIndex.incrementAndGet());
                        thread.setDaemon(true);
                        return thread;
                    }
                });
        this.consumerInfoDownloadMonitorService.scheduleAtFixedRate(() -> {
            // 获取所有客户端信息
            pullProviderDataList();
        },30, 60, TimeUnit.SECONDS);
    }

    public RpcContext getRpcContext() {
        return rpcContext;
    }

    public void setRpcContext(RpcContext rpcContext) {
        this.rpcContext = rpcContext;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public RpcDataManager<RpcMetaData> getRpcDataManager() {
        return rpcDataManager;
    }

    public void setRpcDataManager(RpcDataManager<RpcMetaData> rpcDataManager) {
        this.rpcDataManager = rpcDataManager;
    }

    public void refresh(RpcDataManager rpcDataManager) {
        if (this.rpcDataManager != null) {
            this.rpcDataManager.destroy();
        }
        this.rpcDataManager = rpcDataManager;
        if (this.providerInfoUploadMonitorService != null) {
            this.providerInfoUploadMonitorService.shutdown();
        }
        if (this.consumerInfoDownloadMonitorService != null) {
            this.consumerInfoDownloadMonitorService.shutdown();
        }
        init(rpcProperties);
    }

    public void shutdownMonitor() {
        if (this.providerInfoUploadMonitorService != null) {
            this.providerInfoUploadMonitorService.shutdown();
        }
        if (this.consumerInfoDownloadMonitorService != null) {
            this.consumerInfoDownloadMonitorService.shutdown();
        }
    }
}
