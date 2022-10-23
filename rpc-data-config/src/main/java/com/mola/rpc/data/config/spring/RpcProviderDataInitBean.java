package com.mola.rpc.data.config.spring;

import com.google.common.collect.Lists;
import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.common.entity.AddressInfo;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.data.config.listener.AddressChangeListener;
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
     * 地址变更监听器
     */
    private List<AddressChangeListener> addressChangeListeners = Lists.newArrayList();


    /**
     * 定时上报服务端配置
     */
    private ScheduledExecutorService providerInfoUploadMonitorService;

    /**
     * 定时下载客户端配置
     */
    private ScheduledExecutorService consumerInfoDownloadMonitorService;

    public void init() {
        Assert.notNull(rpcContext, "拉取数据失败，上下文为空");
        rpcDataManager.setAddressChangeListener(addressChangeListeners);
        // 上报提供的provider数据
        Collection<RpcMetaData> providerMetaDataCollection = rpcContext.getProviderMetaMap().values();
        for (RpcMetaData providerMetaData : providerMetaDataCollection) {
            uploadRemoteProviderData(providerMetaData);
        }
        // 拉取订阅的provider数据，填充到上下文
        pullProviderDataList();
        // 注册监听器
        registerProviderDataListeners();
        // 上报服务线程
        startProviderInfoUploadMonitor();
        // 刷新consumer线程
        startConsumerInfoDownloadMonitor();
    }

    public void uploadRemoteProviderData(RpcMetaData providerMetaData) {
        rpcDataManager.uploadRemoteProviderData(providerMetaData, environment, appName, rpcContext.getProviderAddress());
    }

    private void registerProviderDataListeners() {
        Collection<RpcMetaData> consumerMetaDataCollection = rpcContext.getConsumerMetaMap().values();
        for (RpcMetaData consumerMetaData : consumerMetaDataCollection) {
            registerProviderDataListener(consumerMetaData);
        }
    }

    public void registerProviderDataListener(RpcMetaData consumerMetaData) {
        // 服务名
        String serviceName = consumerMetaData.getInterfaceClazz().getName();
        // group
        String group = consumerMetaData.getGroup();
        // version
        String version = consumerMetaData.getVersion();
        // 从配置中心上拉取配置，注册监听器
        rpcDataManager.registerProviderDataListener(serviceName, group, version, environment, consumerMetaData);
    }

    private void pullProviderDataList() {
        Collection<RpcMetaData> consumerMetaDataCollection = rpcContext.getConsumerMetaMap().values();
        for (RpcMetaData consumerMetaData : consumerMetaDataCollection) {
            pullProviderData(consumerMetaData);
        }
    }

    public void pullProviderData(RpcMetaData consumerMetaData) {
        // 服务名
        String serviceName = consumerMetaData.getInterfaceClazz().getName();
        // group
        String group = consumerMetaData.getGroup();
        // version
        String version = consumerMetaData.getVersion();
        // 判断服务是否存在
        if (!rpcDataManager.isProviderExist(serviceName, group, version, environment)) {
            throw new RuntimeException("provider not exist! env = " + environment + ", meta = " + consumerMetaData.toString());
        }
        List<AddressInfo> addressInfoList = rpcDataManager.getRemoteProviderAddress(serviceName, group, version, environment);
        if (null == addressInfoList) {
            log.warn("addressInfoList is null , meta : " + consumerMetaData.toString());
            return;
        }
        consumerMetaData.setAddressList(addressInfoList);
        addressChangeListeners.forEach(addressChangeListener -> addressChangeListener.afterAddressChange(consumerMetaData));
    }

    private void startProviderInfoUploadMonitor() {
        this.providerInfoUploadMonitorService = Executors.newScheduledThreadPool(1,
                new ThreadFactory() {
                    AtomicInteger threadIndex = new AtomicInteger(0);
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "provider-info-upload-monitor-thread-" + this.threadIndex.incrementAndGet());
                    }
                });
        this.providerInfoUploadMonitorService.scheduleAtFixedRate(() -> {
            // 获取所有提供服务信息
            for (RpcMetaData rpcMetaData : rpcContext.getProviderMetaMap().values()) {
                // 获取主机运行状态
                // 上报信息
                rpcDataManager.uploadRemoteProviderData(rpcMetaData, environment, appName, rpcContext.getProviderAddress());
            }
        },90, 60, TimeUnit.SECONDS);
    }

    private void startConsumerInfoDownloadMonitor() {
        this.consumerInfoDownloadMonitorService = Executors.newScheduledThreadPool(1,
                new ThreadFactory() {
                    AtomicInteger threadIndex = new AtomicInteger(0);
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "consumer-info-download-monitor-thread-" + this.threadIndex.incrementAndGet());
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

    public void addAddressChangeListener(AddressChangeListener addressChangeListener) {
        this.addressChangeListeners.add(addressChangeListener);
    }
}
