package com.mola.rpc.core.strategy.balance;

import java.util.List;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-08-06 10:51
 **/
public class RoundRobinBalance implements LoadBalanceStrategy{

    @Override
    public String getTargetProviderAddress(List<String> addressList, String strategyName) {
        return null;
    }
}
