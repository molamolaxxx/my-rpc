package com.mola.rpc.common.context;

import com.mola.rpc.common.annotation.ConsumerSide;
import com.mola.rpc.common.annotation.ProviderSide;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.common.utils.TestUtil;
import com.mola.rpc.common.utils.AssertUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-07-30 21:51
 **/
public class RpcContext {

    private RpcContext() {}
    static class Singleton {
        private static RpcContext rpcContext = new RpcContext();
    }

    /**
     * 是否是spring环境
     */
    private boolean inSpringEnvironment = false;

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
        if (consumerMetaMap == null || !consumerMetaMap.containsKey(key)) {
            return null;
        }
        return consumerMetaMap.get(key);
    }

    public RpcMetaData getProviderMeta(String consumerClazzName, String group, String version) {
        String key = consumerClazzName + ":" + group + ":" + version;
        if (providerMetaMap == null || !providerMetaMap.containsKey(key)) {
            return null;
        }
        return providerMetaMap.get(key);
    }

    public void addConsumerMeta(String consumerClazzName, String beanName, RpcMetaData rpcMetaData) {
        this.consumerMetaMap.put(consumerClazzName + ":" + beanName, rpcMetaData);
    }

    public void addProviderMeta(String providerClazzName, RpcMetaData rpcMetaData) {
        String key = providerClazzName + ":" + rpcMetaData.getGroup() + ":" + rpcMetaData.getVersion();
        AssertUtil.isTrue(TestUtil.isJUnitTest() || !this.providerMetaMap.containsKey(key), "provider already exist, key = " + key);
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

    public void setInSpringEnvironment(boolean inSpringEnvironment) {
        this.inSpringEnvironment = inSpringEnvironment;
    }

    public boolean isInSpringEnvironment() {
        return inSpringEnvironment;
    }

    public static RpcContext fetch() {
        return Singleton.rpcContext;
    }
}
