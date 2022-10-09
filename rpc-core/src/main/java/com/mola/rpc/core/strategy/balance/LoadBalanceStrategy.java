package com.mola.rpc.core.strategy.balance;

import com.mola.rpc.common.entity.RpcMetaData;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 负载均衡策略
 * @date : 2022-08-06 10:46
 **/
public interface LoadBalanceStrategy {

    /**
     * 获取provider的地址
     * @param consumerMeta
     * @return
     */
    String getTargetProviderAddress(RpcMetaData consumerMeta, Object[] args);
}
