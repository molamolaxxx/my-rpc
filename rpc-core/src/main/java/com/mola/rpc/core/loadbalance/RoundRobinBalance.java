package com.mola.rpc.core.loadbalance;

import com.mola.rpc.common.entity.RpcMetaData;

import java.util.List;
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
        List<String> addressList = consumerMeta.fetchProviderAddressList();
        Integer idx = idxCounter.getAndIncrement() % addressList.size();
        return addressList.get(idx);
    }
}
