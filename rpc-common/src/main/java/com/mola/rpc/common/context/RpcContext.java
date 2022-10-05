package com.mola.rpc.common.context;

import com.mola.rpc.common.annotation.ConsumerSide;
import com.mola.rpc.common.annotation.ProviderSide;
import com.mola.rpc.common.entity.RpcMetaData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-07-30 21:51
 **/
public class RpcContext {

    /**
     * 消费者元数据
     * @key consumer beanName
     */
    @ConsumerSide
    private Map<String, RpcMetaData> consumerMetaMap = new ConcurrentHashMap<>(256);

    /**
     * 服务提供者元数据
     * @key interfaceClazzName
     */
    @ProviderSide
    private Map<String, RpcMetaData> providerMetaMap = new ConcurrentHashMap<>(256);


    @ProviderSide
    private String providerAddress;

    public RpcMetaData getConsumerMeta(String consumerClazzName, String beanName) {
        String key = consumerClazzName + ":" + beanName;
        if (null == consumerMetaMap || !consumerMetaMap.containsKey(key)) {
            return null;
        }
        return consumerMetaMap.get(key);
    }

    public RpcMetaData getProviderMeta(String consumerClazzName, String group, String version) {
        String key = consumerClazzName + ":" + group + ":" + version;
        if (null == providerMetaMap || !providerMetaMap.containsKey(key)) {
            return null;
        }
        return providerMetaMap.get(key);
    }

    public void addConsumerMeta(String consumerClazzName, String beanName, RpcMetaData rpcMetaData) {
        this.consumerMetaMap.put(consumerClazzName + ":" + beanName, rpcMetaData);
    }

    public void addProviderMeta(String providerClazzName, RpcMetaData rpcMetaData) {
        String key = providerClazzName + ":" + rpcMetaData.getGroup() + ":" + rpcMetaData.getVersion();
        this.providerMetaMap.put(key, rpcMetaData);
    }

    public Map<String, RpcMetaData> getConsumerMetaMap() {
        return consumerMetaMap;
    }

    public Map<String, RpcMetaData> getProviderMetaMap() {
        return providerMetaMap;
    }

    public String getProviderAddress() {
        return providerAddress;
    }

    public void setProviderAddress(String providerAddress) {
        this.providerAddress = providerAddress;
    }

}
