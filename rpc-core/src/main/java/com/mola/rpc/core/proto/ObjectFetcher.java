package com.mola.rpc.core.proto;

import com.mola.rpc.common.entity.RpcMetaData;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 获取bean的接口
 * @date : 2022-09-13 00:25
 **/
public interface ObjectFetcher {

    /**
     * 获取对象
     * @param providerMeta
     * @return
     */
    Object getObject(RpcMetaData providerMeta);
}
