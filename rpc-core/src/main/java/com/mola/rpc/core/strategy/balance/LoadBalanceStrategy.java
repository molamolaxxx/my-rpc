package com.mola.rpc.core.strategy.balance;

import java.util.List;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 负载均衡策略
 * @date : 2022-08-06 10:46
 **/
public interface LoadBalanceStrategy {

    /**
     * 获取provider的地址
     * @param addressList
     * @return
     */
    String getTargetProviderAddress(List<String> addressList, String strategyName);
}
