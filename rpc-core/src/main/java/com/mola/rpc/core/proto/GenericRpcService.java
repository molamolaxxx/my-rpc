package com.mola.rpc.core.proto;

import com.mola.rpc.common.entity.GenericParam;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 泛化调用服务接口
 * @date : 2023-01-10 14:33
 **/
public interface GenericRpcService {

    /**
     * 泛化调用方法
     * @param method 方法名称
     * @param genericParams 泛化调用参数
     * @return
     */
    Object invoke(String method, GenericParam... genericParams);
}
