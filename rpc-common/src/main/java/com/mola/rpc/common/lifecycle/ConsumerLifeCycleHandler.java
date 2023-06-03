package com.mola.rpc.common.lifecycle;

import com.mola.rpc.common.entity.RpcMetaData;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: consumer生命周期监听器
 * @date : 2023-05-27 11:15
 **/
public interface ConsumerLifeCycleHandler {

    /**
     * rpc配置工厂初始化后回调
     * @param consumerMetaData
     */
    default void afterInitialize(RpcMetaData consumerMetaData) {}

    /**
     * 地址变化后回调
     * @param consumerMetaData
     */
    default void afterAddressChange(RpcMetaData consumerMetaData) {}

    /**
     * 监听器名称
     * @return
     */
    default String getName() {
        return this.getClass().getName();
    }
}
