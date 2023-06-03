package com.mola.rpc.core.loadbalance;

import com.mola.rpc.common.entity.RpcMetaData;

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
    public String getTargetProviderAddress(RpcMetaData consumerMeta, Object[] args) {
        List<String> addressList = consumerMeta.fetchProviderAddressList();
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
