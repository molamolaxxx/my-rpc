package com.mola.rpc.core.strategy.balance;

import java.util.List;
import java.util.Random;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-08-06 10:51
 **/
public class RandomLoadBalance implements LoadBalanceStrategy {

    @Override
    public String getTargetProviderAddress(List<String> addressList, String strategyName, Object[] args) {
        Random random = new Random();
        int pos = random.nextInt(addressList.size());
        while (pos >= addressList.size()) {
            if (addressList.size() == 0) {
                return null;
            }
            pos = random.nextInt(addressList.size());
        }
        return pos < addressList.size() ? addressList.get(pos) : null;
    }
}
