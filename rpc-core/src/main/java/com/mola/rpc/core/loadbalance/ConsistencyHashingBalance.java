package com.mola.rpc.core.loadbalance;

import com.alibaba.fastjson.JSONObject;
import com.mola.rpc.common.constants.LoadBalanceConstants;
import com.mola.rpc.common.context.InvokeContext;
import com.mola.rpc.common.entity.AddressInfo;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.common.lifecycle.ConsumerLifeCycleHandler;
import com.mola.rpc.core.util.HashUtil;
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
public class ConsistencyHashingBalance implements LoadBalanceStrategy, ConsumerLifeCycleHandler {

    /**
     * 总节点个数 = 实际节点个数 * (VIRTUAL_NODE_RATE + 1)
     */
    private static final int VIRTUAL_NODE_RATE = 10;

    @Override
    public String getTargetProviderAddress(RpcMetaData consumerMeta, Object[] args) {
        SortedMap<Integer, String> virtualAddressNodeMap = fetchVirtualAddressNodeMap(consumerMeta);
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
     * 获取hash环对应的sortMap
     * @param consumerMeta
     * @return
     */
    private SortedMap<Integer, String> fetchVirtualAddressNodeMap(RpcMetaData consumerMeta) {
        InvokeContext invokeContext = InvokeContext.fetch();
        if (invokeContext != null && !CollectionUtils.isEmpty(invokeContext.getAddressList())) {
            // 如果动态指定，实时构建hash环
            return rebuildHashInner(invokeContext.getAddressList(),
                    invokeContext.getAddressList().size() * (VIRTUAL_NODE_RATE + 1));
        }
        // 1、静态指定，在启动环节构建完成hash环
        // 2、非静态指定，在cs回调的时候构建完成hash环
        return consumerMeta.getVirtualAddressNodeMap();
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
            List<String> addressList = consumerMeta.getAddressList()
                    .stream().map(AddressInfo::getAddress)
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(addressList)) {
                consumerMeta.setVirtualAddressNodeMap(null);
                return;
            }
            consumerMeta.setVirtualAddressNodeMap(rebuildHashInner(addressList,
                    addressList.size() * (VIRTUAL_NODE_RATE + 1)));
        }
    }

    private SortedMap<Integer, String>  rebuildHashInner(List<String> addressList, int nodeCnt) {
        SortedMap<Integer, String> virtualAddressNodeMap = new TreeMap<>();
        for (String address : addressList) {
            for (int i = 0; i < nodeCnt; i++) {
                String virtualAddress = String.format("%s#%s", address, UUID.randomUUID());
                virtualAddressNodeMap.put(HashUtil.getHash(virtualAddress), virtualAddress);
            }
        }
        return virtualAddressNodeMap;
    }

    @Override
    public void afterAddressChange(RpcMetaData consumerMetaData) {
        if (!LoadBalanceConstants.CONSISTENCY_HASHING_STRATEGY.equals(consumerMetaData.getLoadBalanceStrategy())) {
            return;
        }
        // 如果没有指定地址，则使用cs传递的地址构建索引
        if (CollectionUtils.isEmpty(consumerMetaData.getAppointedAddress())) {
            this.rebuildHash(consumerMetaData);
        }
    }

    @Override
    public void afterInitialize(RpcMetaData consumerMetaData) {
        if (!LoadBalanceConstants.CONSISTENCY_HASHING_STRATEGY.equals(consumerMetaData.getLoadBalanceStrategy())) {
            return;
        }
        List<String> appointedAddress = consumerMetaData.getAppointedAddress();
        if (!CollectionUtils.isEmpty(appointedAddress)) {
            consumerMetaData.setVirtualAddressNodeMap(rebuildHashInner(appointedAddress,
                    appointedAddress.size() * (VIRTUAL_NODE_RATE + 1)));
        }
    }
}
