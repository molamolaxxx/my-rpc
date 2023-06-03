package com.mola.rpc.common.interceptor;

import com.mola.rpc.common.entity.RpcMetaData;

public interface RpcInterceptor {

    /**
     * 拦截
     * @param rpcMetaData
     * @return true 拦截 false 不拦截
     */
    boolean intercept(RpcMetaData rpcMetaData);

    /**
     * 优先级
     * @return
     */
    int priority();
}
