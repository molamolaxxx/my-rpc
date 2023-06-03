package com.mola.rpc.core.loadbalance;

import com.google.common.collect.Maps;
import com.mola.rpc.common.entity.RpcMetaData;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Map;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-08-06 10:48
 **/
public class LoadBalance implements LoadBalanceStrategy {

    private Map<String, LoadBalanceStrategy> loadBalanceStrategyMap = Maps.newConcurrentMap();

    @Override
    public String getTargetProviderAddress(RpcMetaData consumerMeta, Object[] args) {
        LoadBalanceStrategy loadBalanceStrategy = loadBalanceStrategyMap.get(consumerMeta.getLoadBalanceStrategy());
        Assert.notNull(loadBalanceStrategy, "loadBalanceStrategy is null");
        return loadBalanceStrategy.getTargetProviderAddress(consumerMeta, args);
    }

    public void setStrategy(String name, LoadBalanceStrategy strategy) {
        this.loadBalanceStrategyMap.put(name, strategy);
    }

    public Collection<LoadBalanceStrategy> getStrategyCollection() {
        return this.loadBalanceStrategyMap.values();
    }
}
