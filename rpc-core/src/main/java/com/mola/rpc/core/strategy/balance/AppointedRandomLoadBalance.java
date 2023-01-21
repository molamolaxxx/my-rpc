package com.mola.rpc.core.strategy.balance;

import com.alibaba.fastjson.JSONObject;
import com.mola.rpc.common.entity.RpcMetaData;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Random;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 在指定的地址中，执行随机选择策略
 * @date : 2022-08-06 10:51
 **/
public class AppointedRandomLoadBalance implements LoadBalanceStrategy {

    @Override
    public String getTargetProviderAddress(RpcMetaData consumerMeta, Object[] args) {
        List<String> appointedAddress = consumerMeta.getAppointedAddress();
        Assert.notEmpty(appointedAddress, "appointedAddress is empty! consumerMeta = " + JSONObject.toJSONString(consumerMeta));
        Random random = new Random();
        int pos = random.nextInt(appointedAddress.size());
        while (pos >= appointedAddress.size()) {
            if (appointedAddress.size() == 0) {
                return null;
            }
            pos = random.nextInt(appointedAddress.size());
        }
        return pos < appointedAddress.size() ? appointedAddress.get(pos) : null;
    }
}
