package com.mola.rpc.common.entity;

import com.alibaba.fastjson.JSONObject;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 存在zk上的配置数据
 * @date : 2022-09-25 20:57
 **/
public class ProviderConfigData {

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

    public static ProviderConfigData create(String appName, String host) {
        ProviderConfigData providerConfigData = new ProviderConfigData();
        providerConfigData.appName = appName;
        providerConfigData.providerLastHeartBeatTime = System.currentTimeMillis();
        providerConfigData.host = host;
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

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
