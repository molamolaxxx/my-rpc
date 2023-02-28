package com.mola.rpc.core.properties;

import com.mola.rpc.common.entity.BaseRpcProperties;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.data.config.manager.RpcDataManager;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-07-30 18:42
 **/
public class RpcProperties extends BaseRpcProperties {

    /**
     * 自定义configure server实现
     */
    private RpcDataManager<RpcMetaData> rpcDataManager;


    public RpcDataManager<RpcMetaData> getRpcDataManager() {
        return rpcDataManager;
    }

    public void setRpcDataManager(RpcDataManager<RpcMetaData> rpcDataManager) {
        this.rpcDataManager = rpcDataManager;
    }
}
