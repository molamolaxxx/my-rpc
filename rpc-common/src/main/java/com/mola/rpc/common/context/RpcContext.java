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
     */
    @ConsumerSide
    private Map<String, RpcMetaData> consumerMetaMap = new ConcurrentHashMap<>(256);

    /**
     * 服务提供者元数据
     */
    @ProviderSide
    private Map<String, RpcMetaData> providerMetaMap = new ConcurrentHashMap<>(256);


    @ProviderSide
    private String providerAddress;

    public RpcMetaData getConsumerMeta(String consumerClazzName) {
        if (null == consumerMetaMap || !consumerMetaMap.containsKey(consumerClazzName)) {
            return null;
        }
        return consumerMetaMap.get(consumerClazzName);
    }

    public RpcMetaData getProviderMeta(String consumerClazzName) {
        if (null == providerMetaMap || !providerMetaMap.containsKey(consumerClazzName)) {
            return null;
        }
        return providerMetaMap.get(consumerClazzName);
    }

    public void addConsumerMeta(String consumerClazzName, RpcMetaData rpcMetaData) {
        this.consumerMetaMap.put(consumerClazzName, rpcMetaData);
    }

    public void addProviderMeta(String providerClazzName, RpcMetaData rpcMetaData) {
        this.providerMetaMap.put(providerClazzName, rpcMetaData);
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
