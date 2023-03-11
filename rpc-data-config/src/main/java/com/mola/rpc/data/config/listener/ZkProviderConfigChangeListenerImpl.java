package com.mola.rpc.data.config.listener;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mola.rpc.common.entity.AddressInfo;
import com.mola.rpc.common.entity.ProviderConfigData;
import com.mola.rpc.common.entity.RpcMetaData;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: zk监听器实现
 * @date : 2022-09-25 21:23
 **/
public class ZkProviderConfigChangeListenerImpl implements IZkChildListener {

    private static final Logger log = LoggerFactory.getLogger(ZkProviderConfigChangeListenerImpl.class);

    private RpcMetaData consumerMetaData;

    private ZkClient zkClient;

    private Lock addressListChangeLock;

    /**
     * 地址变更监听器
     */
    private List<AddressChangeListener> addressChangeListeners;

    public ZkProviderConfigChangeListenerImpl(RpcMetaData consumerMetaData, ZkClient zkClient, List<AddressChangeListener> addressChangeListeners) {
        this.consumerMetaData = consumerMetaData;
        this.zkClient = zkClient;
        this.addressChangeListeners = addressChangeListeners;
        this.addressListChangeLock  = new ReentrantLock();
    }

    @Override
    public void handleChildChange(String parentPath, List<String> childList) throws Exception {
        log.warn("remote address available , service is " + this.consumerMetaData.getInterfaceClazz().getName() + ":" + JSONObject.toJSONString(childList));
        this.addressListChangeLock.lock();
        try {
            List<AddressInfo> addressList = consumerMetaData.getAddressList();
            Map<String, AddressInfo> addressInfoMap = Maps.newHashMap();
            if (!CollectionUtils.isEmpty(addressList)) {
                addressList.forEach(addressInfo -> addressInfoMap.put(addressInfo.getAddress(), addressInfo));
            }
            addressList.forEach(addressInfo -> addressInfoMap.put(addressInfo.getAddress(), addressInfo));
            List<AddressInfo> newAddressInfo = Lists.newArrayList();
            childList.forEach(
                    addressStr -> {
                        if (!addressInfoMap.containsKey(addressStr)) {
                            newAddressInfo.add(new AddressInfo(addressStr,
                                    JSONObject.parseObject(zkClient.readData(parentPath + "/" + addressStr), ProviderConfigData.class)));
                        } else {
                            newAddressInfo.add(addressInfoMap.get(addressStr));
                        }
                    }
            );
            consumerMetaData.setAddressList(newAddressInfo);
        } finally {
            this.addressListChangeLock.unlock();
        }
        // 监听器回调
        if (!CollectionUtils.isEmpty(addressChangeListeners)) {
            addressChangeListeners.forEach(listener -> listener.afterAddressChange(consumerMetaData));
        }
    }
}
