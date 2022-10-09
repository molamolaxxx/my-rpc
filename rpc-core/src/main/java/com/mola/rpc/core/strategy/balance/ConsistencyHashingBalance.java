package com.mola.rpc.core.strategy.balance;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 哈希一致性负载均衡
 * @date : 2022-10-04 12:29
 **/
public class ConsistencyHashingBalance implements LoadBalanceStrategy {

    /**
     * 哈希环，hash => ip#idx
     */
    private SortedMap<Integer, String> virtualAddressNodeMap = new TreeMap<>();

    /**
     * 总虚拟节点个数
     */
    private static final int TOTAL_VIRTUAL_NODE_NUM = 1500;

    private Set<String> addressSet = Sets.newHashSet();

    /**
     * 重建hash索引的锁
     */
    private ReentrantLock rebuildHashLock = new ReentrantLock();

    @Override
    public String getTargetProviderAddress(List<String> addressList, String strategyName, Object[] args) {
        if (needRebuildHash(addressList)) {
            rebuildHash(addressList);
        }
        if (virtualAddressNodeMap.size() == 0) {
            throw new RuntimeException("virtualAddressNodeMap is empty");
        }
        int hash = getHash(JSONObject.toJSONString(args));
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
     * @param addressList
     */
    private void rebuildHash(List<String> addressList) {
        rebuildHashLock.lock();
        try {
            if (!needRebuildHash(addressList)) {
                return;
            }
            addressSet = new HashSet<>(addressList.size());
            virtualAddressNodeMap = new TreeMap<>();
            int totalNodeNum = TOTAL_VIRTUAL_NODE_NUM / addressList.size();
            for (String address : addressList) {
                if (addressSet.contains(address)) {
                    continue;
                }
                for (int i = 0; i < totalNodeNum; i++) {
                    String virtualAddress = String.format("%s#%s", address, UUID.randomUUID());
                    virtualAddressNodeMap.put(getHash(virtualAddress), virtualAddress);
                }
                addressSet.add(address);
            }
        } finally {
            rebuildHashLock.unlock();
        }
    }

    /**
     * 是否需要重建hash索引
     * @param addressList
     * @return
     */
    private Boolean needRebuildHash(List<String> addressList) {
        if (addressList.size() != addressSet.size()) {
            return true;
        }
        for (String address : addressList) {
            if (!addressSet.contains(address)) {
                return true;
            }
        }
        return false;
    }

    /**
     * FNV1_32_HASH算法
     * @param str
     * @return
     */
    private int getHash(String str) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < str.length(); i++) {
            hash = (hash ^ str.charAt(i)) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        //如果算出来的值为负数则取其绝对值
        if (hash < 0) {
            hash = Math.abs(hash);
        }
        return hash;
    }
}
