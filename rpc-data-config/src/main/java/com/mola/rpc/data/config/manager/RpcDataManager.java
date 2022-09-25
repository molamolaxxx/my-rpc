package com.mola.rpc.data.config.manager;

import com.mola.rpc.common.annotation.ConsumerSide;
import com.mola.rpc.common.annotation.ProviderSide;
import com.mola.rpc.common.entity.AddressInfo;
import com.mola.rpc.common.entity.RpcMetaData;

import java.util.List;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-08-27 20:23
 **/
public interface RpcDataManager<T extends RpcMetaData> {

    /**
     * 初始化
     */
    void init();

    /**
     * 获取服务提供者数据
     * @param interfaceClazz
     * @param group
     * @param version
     * @param environment
     * @return
     */
    @ConsumerSide
    List<AddressInfo> getRemoteProviderAddress(String interfaceClazz, String group, String version, String environment);

    /**
     * 获取服务提供者数据
     * @param interfaceClazz
     * @param group
     * @param version
     * @param environment
     * @return
     */
    @ConsumerSide
    void registerProviderDataListener(String interfaceClazz, String group, String version, String environment, T consumerMetaData);

    /**
     * 获取服务提供者数据
     * @param interfaceClazz
     * @param group
     * @param version
     * @param environment
     * @return
     */
    @ConsumerSide
    Boolean isProviderExist(String interfaceClazz, String group, String version, String environment);

    /**
     * 上报服务元数据
     * @param providerMetaData
     */
    @ProviderSide
    void uploadRemoteProviderData(T providerMetaData, String environment, String appName, String address);

    /**
     * 删除服务元数据
     * @param providerMetaData
     */
    @ProviderSide
    void deleteRemoteProviderData(T providerMetaData, String environment, String appName, String address);

    /**
     * 上报心跳
     * @param providerMetaData
     * @param environment
     * @param address
     */
    @ProviderSide
    void updateProviderHeartBeat(T providerMetaData, String environment, String address);

    /**
     * 上报服务订阅元数据
     * @param consumerMetaData
     */
    @ConsumerSide
    void uploadConsumerData(T consumerMetaData);

    /**
     * 获取服务配置路径
     * @param interfaceClazz
     * @param group
     * @param version
     * @param environment
     * @return
     */
    default String getRemoteProviderPath(String interfaceClazz, String group, String version, String environment) {
        return String.format("/myRpc/provider/%s-%s-%s-%s", interfaceClazz, group, version, environment);
    }
}
