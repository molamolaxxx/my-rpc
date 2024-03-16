package com.mola.rpc.core.loadbalance;

import com.google.common.collect.Maps;
import com.mola.rpc.common.annotation.ConsistencyHashKey;
import com.mola.rpc.common.constants.LoadBalanceConstants;
import com.mola.rpc.common.context.InvokeContext;
import com.mola.rpc.common.entity.AddressInfo;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.common.lifecycle.ConsumerLifeCycleHandler;
import com.mola.rpc.common.utils.JSONUtil;
import com.mola.rpc.core.util.HashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 哈希一致性负载均衡
 * @date : 2022-10-04 12:29
 **/
public class ConsistencyHashingBalance implements LoadBalanceStrategy, ConsumerLifeCycleHandler {

    private static final Logger log = LoggerFactory.getLogger(ConsistencyHashingBalance.class);

    private static final Map<Class<?>, Method> consistencyHashMethodMapping = Maps.newConcurrentMap();

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
        int hash = HashUtil.getHash(fetchKeyToHashFromArgs(args));
        SortedMap<Integer, String> tailMap = virtualAddressNodeMap.tailMap(hash);
        if (tailMap.size() == 0) {
            return getAddressFromVirtualNode(virtualAddressNodeMap.get(virtualAddressNodeMap.firstKey()));
        }
        return getAddressFromVirtualNode(tailMap.get(tailMap.firstKey()));
    }

    /**
     * 从参数中获取待hash的key
     * @param args
     * @return
     */
    private String fetchKeyToHashFromArgs(Object[] args) {
        // 从方法中获取
        for (Object arg : args) {
            String rawKey = fetchKeyToHashFromMethods(arg);
            if (rawKey == null || rawKey.length() == 0) {
                continue;
            }
            return rawKey;
        }
        return JSONUtil.toJSONString(args);
    }

    private String fetchKeyToHashFromMethods(Object arg) {
        Method fetchMethod = consistencyHashMethodMapping.get(arg.getClass());
        if (fetchMethod == null) {
            for (Method method : arg.getClass().getMethods()) {
                if (method.isAnnotationPresent(ConsistencyHashKey.class)) {
                    consistencyHashMethodMapping.putIfAbsent(arg.getClass(), method);
                    fetchMethod = method;
                    break;
                }
            }
        }
        if (fetchMethod == null) {
            return null;
        }
        try {
            Object res = fetchMethod.invoke(arg);
            if (!(res instanceof String)) {
                log.warn("consistency hash from method failed, return not str!" +
                                " clazz = {}, method = {}",
                        arg.getClass(), fetchMethod);
                return null;
            }
            return (String) res;
        } catch (Exception e) {
            log.warn("consistency hash from method failed, clazz = {}, method = {}",
                    arg.getClass(), fetchMethod);
        }
        return null;
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
        if (split != null && split.length == 2) {
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
