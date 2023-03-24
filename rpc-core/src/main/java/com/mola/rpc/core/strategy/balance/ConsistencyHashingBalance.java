package com.mola.rpc.core.strategy.balance;

import com.alibaba.fastjson.JSONObject;
import com.mola.rpc.common.constants.LoadBalanceConstants;
import com.mola.rpc.common.entity.AddressInfo;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.core.util.HashUtil;
import com.mola.rpc.data.config.listener.AddressChangeListener;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 哈希一致性负载均衡
 * @date : 2022-10-04 12:29
 **/
public class ConsistencyHashingBalance implements LoadBalanceStrategy, AddressChangeListener {

    /**
     * 总虚拟节点个数
     */
    private static final int TOTAL_VIRTUAL_NODE_NUM = 1000;

    @Override
    public String getTargetProviderAddress(RpcMetaData consumerMeta, Object[] args) {
        Assert.isTrue(CollectionUtils.isEmpty(consumerMeta.getAppointedAddress()),
                "consistency hashing balance not support appointed address!");
        SortedMap<Integer, String> virtualAddressNodeMap = consumerMeta.getVirtualAddressNodeMap();
        if (virtualAddressNodeMap == null || virtualAddressNodeMap.size() == 0) {
            throw new RuntimeException("virtualAddressNodeMap is empty");
        }
        int hash = HashUtil.getHash(JSONObject.toJSONString(args));
        SortedMap<Integer, String> tailMap = virtualAddressNodeMap.tailMap(hash);
        if (tailMap.size() == 0) {
            return getAddressFromVirtualNode(virtualAddressNodeMap.get(virtualAddressNodeMap.firstKey()));
        }
        return getAddressFromVirtualNode(tailMap.get(tailMap.firstKey()));
    }

    /**
     * 截取虚拟节点，获取真实地址
     * @param virtualAddressNode
     * @return
     */
    private String getAddressFromVirtualNode(String virtualAddressNode) {
        String[] split = StringUtils.split(virtualAddressNode, "#");
        if (split.length == 2) {
            return split[0];
        }
        throw new RuntimeException("getAddressFromVirtualNode failed");
    }

    /**
     * 重建hash索引
     * @param consumerMeta
     */
    public void rebuildHash(RpcMetaData consumerMeta) {
        synchronized (consumerMeta) {
            List<String> addressList = consumerMeta.getAddressList().stream().map(AddressInfo::getAddress).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(addressList)) {
                consumerMeta.setVirtualAddressNodeMap(null);
                return;
            }
            SortedMap<Integer, String> virtualAddressNodeMap = new TreeMap<>();
            int totalNodeNum = TOTAL_VIRTUAL_NODE_NUM / addressList.size();
            for (String address : addressList) {
                for (int i = 0; i < totalNodeNum; i++) {
                    String virtualAddress = String.format("%s#%s", address, UUID.randomUUID());
                    virtualAddressNodeMap.put(HashUtil.getHash(virtualAddress), virtualAddress);
                }
            }
            consumerMeta.setVirtualAddressNodeMap(virtualAddressNodeMap);
        }
    }

    @Override
    public void afterAddressChange(RpcMetaData consumerMetaData) {
        if (LoadBalanceConstants.CONSISTENCY_HASHING_STRATEGY.equals(consumerMetaData.getLoadBalanceStrategy())) {
            this.rebuildHash(consumerMetaData);
        }
    }
}
