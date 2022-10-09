package com.mola.rpc.core.strategy.balance;

import com.mola.rpc.common.entity.RpcMetaData;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 轮询
 * @date : 2022-08-06 10:51
 **/
public class RoundRobinBalance implements LoadBalanceStrategy {

    /**
     * 下标计数器
     */
    private AtomicInteger idxCounter = new AtomicInteger();

    @Override
    public String getTargetProviderAddress(RpcMetaData consumerMeta, Object[] args) {
        Integer idx = idxCounter.getAndIncrement() % consumerMeta.getAddressList().size();
        return consumerMeta.getAddressList().get(idx).getAddress();
    }
}
