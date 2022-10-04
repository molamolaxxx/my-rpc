package com.mola.rpc.core.strategy.balance;

import com.google.common.collect.Maps;
import org.springframework.util.Assert;

import java.util.List;
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
    public String getTargetProviderAddress(List<String> addressList, String strategyName, Object[] args) {
        LoadBalanceStrategy loadBalanceStrategy = loadBalanceStrategyMap.get(strategyName);
        Assert.notNull(loadBalanceStrategy, "loadBalanceStrategy is null");
        Assert.notNull(addressList, "addressList is null");
        if (addressList.size() == 0) {
            return null;
        }
        return loadBalanceStrategy.getTargetProviderAddress(addressList, strategyName, args);
    }

    public void setStrategy(String name, LoadBalanceStrategy strategy) {
        this.loadBalanceStrategyMap.put(name, strategy);
    }
}
