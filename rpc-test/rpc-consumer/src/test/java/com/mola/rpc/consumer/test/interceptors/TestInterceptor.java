package com.mola.rpc.consumer.test.interceptors;

import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.common.interceptor.RpcInterceptor;
import org.springframework.stereotype.Component;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-06-03 14:17
 **/
@Component
public class TestInterceptor implements RpcInterceptor {
    @Override
    public boolean intercept(RpcMetaData rpcMetaData) {
        return false;
    }

    @Override
    public int priority() {
        return 0;
    }
}
