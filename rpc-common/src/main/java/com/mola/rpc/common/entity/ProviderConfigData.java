package com.mola.rpc.common.entity;

import com.mola.rpc.common.utils.JSONUtil;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 存在zk上的配置数据
 * @date : 2022-09-25 20:57
 **/
public class ProviderConfigData {

    /**
     * 服务全限定路径
     */
    private String servicePath;

    /**
     * 地址
     */
    private String address;

    /**
     * 提供服务的应用名
     */
    private String appName;

    /**
     * 服务提供者最后一次心跳时间
     */
    private Long providerLastHeartBeatTime;

    /**
     * 服务ip所在域名
     */
    private String host;

    /**
     * 机器负载
     */
    private SystemInfo systemInfo;

    /**
     * 是否是以原子能力的方式提供服务
     */
    private Boolean proto;

    private String description;

    public static ProviderConfigData create(String parentPath, String appName, String description,
                                            String host, String address, Boolean proto) {
        ProviderConfigData providerConfigData = new ProviderConfigData();
        providerConfigData.servicePath = parentPath;
        providerConfigData.appName = appName;
        providerConfigData.providerLastHeartBeatTime = System.currentTimeMillis();
        providerConfigData.host = host;
        providerConfigData.address = address;
        providerConfigData.proto = proto;
        providerConfigData.description = description;
        return providerConfigData;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Long getProviderLastHeartBeatTime() {
        return providerLastHeartBeatTime;
    }

    public void setProviderLastHeartBeatTime(Long providerLastHeartBeatTime) {
        this.providerLastHeartBeatTime = providerLastHeartBeatTime;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getServicePath() {
        return servicePath;
    }

    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public SystemInfo getSystemInfo() {
        return systemInfo;
    }

    public void setSystemInfo(SystemInfo systemInfo) {
        this.systemInfo = systemInfo;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return JSONUtil.toJSONString(this);
    }
}
