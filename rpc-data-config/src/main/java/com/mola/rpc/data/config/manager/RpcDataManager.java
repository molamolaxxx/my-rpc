package com.mola.rpc.data.config.manager;

import com.mola.rpc.common.annotation.ConsumerSide;
import com.mola.rpc.common.annotation.ProviderSide;
import com.mola.rpc.common.annotation.WebSide;
import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.common.entity.AddressInfo;
import com.mola.rpc.common.entity.ProviderConfigData;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.data.config.listener.AddressChangeListener;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;
import java.util.Map;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-08-27 20:23
 **/
public interface RpcDataManager<T extends RpcMetaData> {

    /**
     * 初始化/销毁
     */
    void init(RpcContext rpcContext);
    void destroy();

    /**
     * 获取服务提供者数据
     * @param interfaceClazz
     * @param group
     * @param version
     * @param environment
     * @return
     */
    @ConsumerSide
    default List<AddressInfo> getRemoteProviderAddress(String interfaceClazz, String group, String version, String environment){
        throw new NotImplementedException();
    }

    /**
     * 获取服务提供者数据
     * @param interfaceClazz
     * @param group
     * @param version
     * @param environment
     * @return
     */
    @ConsumerSide
    default void registerProviderDataListener(String interfaceClazz, String group, String version, String environment, T consumerMetaData){
        throw new NotImplementedException();
    }

    /**
     * 获取服务提供者数据
     * @param interfaceClazz
     * @param group
     * @param version
     * @param environment
     * @return
     */
    @ConsumerSide
    default boolean isProviderAvailable(String interfaceClazz, String group, String version, String environment){
        throw new NotImplementedException();
    }

    @ProviderSide
    default boolean isInstanceAvailable(String interfaceClazz, String group, String version, String environment, String address){
        throw new NotImplementedException();
    }

    /**
     * 上报服务元数据
     * @param providerMetaData
     */
    @ProviderSide
    default void uploadRemoteProviderData(T providerMetaData, String environment, String appName, String address){
        throw new NotImplementedException();
    }

    /**
     * 是否需要定时上报心跳
     */
    @ProviderSide
    default boolean requireSendProviderHeartBeat(){
        return true;
    }

    /**
     * 删除服务元数据
     * @param providerMetaData
     */
    @ProviderSide
    default void deleteRemoteProviderData(T providerMetaData, String environment, String appName, String address){
        throw new NotImplementedException();
    }

    /**
     * 上报服务订阅元数据
     * @param consumerMetaData
     */
    @ConsumerSide
    default void uploadConsumerData(T consumerMetaData){
        throw new NotImplementedException();
    }

    /**
     * 获得所有provider的信息
     * @return
     */
    @WebSide
    default Map<String, RpcMetaData> getAllProviderMetaData() {
        throw new NotImplementedException();
    }

    /**
     * 获得provider下所有地址信息
     * @return
     */
    @WebSide
    default List<ProviderConfigData> getAllProviderConfigData(String interfaceClazz, String group, String version, String environment) {
        throw new NotImplementedException();
    }

    /**
     * 获取服务配置路径
     * @param interfaceClazz
     * @param group
     * @param version
     * @param environment
     * @return
     */
    default String getRemoteProviderPath(String interfaceClazz, String group, String version, String environment) {
        return String.format("/myRpc/provider/%s/%s:%s:%s", environment, interfaceClazz, group, version);
    }

    void setAddressChangeListener(List<AddressChangeListener> addressChangeListeners);
}
