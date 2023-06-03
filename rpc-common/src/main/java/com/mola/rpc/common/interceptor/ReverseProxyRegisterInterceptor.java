package com.mola.rpc.common.interceptor;

import com.mola.rpc.common.entity.RpcMetaData;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 反向代理调用
 * 反向代理服务注册拦截器
 * @date : 2023-03-05 18:55
 **/
public class ReverseProxyRegisterInterceptor implements RpcInterceptor{

    /**
     * 拦截
     * @param proxyProviderMetaData
     * @return true 拦截 false 不拦截
     */
    @Override
    public boolean intercept(RpcMetaData proxyProviderMetaData) {
        return false;
    }

    @Override
    public int priority() {
        return 0;
    }
}
