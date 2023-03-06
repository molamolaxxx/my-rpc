package com.mola.rpc.core.strategy.balance;

import com.mola.rpc.common.entity.AddressInfo;
import com.mola.rpc.common.entity.RpcMetaData;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * 取地址，优先使用指定地址，没有则使用cs地址
     * @param consumerMeta
     * @return
     */
    default List<String> getAddressList(RpcMetaData consumerMeta) {
        List<String> addressList = consumerMeta.getAppointedAddress();
        if (CollectionUtils.isEmpty(addressList)) {
            addressList = consumerMeta.getAddressList().stream()
                    .filter(e -> e.getAddress() != null).map(AddressInfo::getAddress)
                    .collect(Collectors.toList());
        }
        Assert.notNull(addressList, "addressList is null");
        return addressList;
    }
}
