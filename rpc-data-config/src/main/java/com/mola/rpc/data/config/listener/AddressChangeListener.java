package com.mola.rpc.data.config.listener;

import com.mola.rpc.common.entity.RpcMetaData;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 地址变动
 * @date : 2022-10-10 01:19
 **/
public interface AddressChangeListener {

    /**
     * 地址变化后回调
     * @param consumerMetaData
     */
    void afterAddressChange(RpcMetaData consumerMetaData);
}
