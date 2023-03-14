package com.mola.rpc.data.config.listener;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mola.rpc.common.entity.AddressInfo;
import com.mola.rpc.common.entity.ProviderConfigData;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.common.entity.SystemInfo;
import com.mola.rpc.common.utils.ObjectUtils;
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
public class NacosProviderConfigChangeListenerImpl implements EventListener {

    private static final Logger log = LoggerFactory.getLogger(NacosProviderConfigChangeListenerImpl.class);

    private RpcMetaData consumerMetaData;

    private Lock addressListChangeLock;

    /**
     * 地址变更监听器
     */
    private List<AddressChangeListener> addressChangeListeners;

    public NacosProviderConfigChangeListenerImpl(RpcMetaData consumerMetaData, List<AddressChangeListener> addressChangeListeners) {
        this.consumerMetaData = consumerMetaData;
        this.addressChangeListeners = addressChangeListeners;
        this.addressListChangeLock = new ReentrantLock();
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof NamingEvent) {
            NamingEvent namingEvent = (NamingEvent) event;
            log.info("remote address refresh from nacos, serviceName = {}, event = {}", namingEvent.getServiceName(), JSONObject.toJSONString(event));
            List<Instance> instances = namingEvent.getInstances();
            this.addressListChangeLock.lock();
            try {
                List<AddressInfo> addressList = consumerMetaData.getAddressList();
                // 老的地址放在这个map里，减缓访问configserver的频次
                Map<String, AddressInfo> addressInfoMap = Maps.newHashMap();
                if (!CollectionUtils.isEmpty(addressList)) {
                    addressList.forEach(addressInfo -> addressInfoMap.put(addressInfo.getAddress(), addressInfo));
                }
                List<AddressInfo> newAddressInfo = Lists.newArrayList();
                instances.forEach(
                        instance -> {
                            String addressStr = instance.getIp() + ":" + instance.getPort();
                            ProviderConfigData providerConfigData = ObjectUtils.parseMap(instance.getMetadata(), ProviderConfigData.class);
                            SystemInfo systemInfo = JSONObject.parseObject(instance.getMetadata().get("systemInfoKey"), SystemInfo.class);
                            providerConfigData.setSystemInfo(systemInfo);
                            if (!addressInfoMap.containsKey(addressStr)) {
                                newAddressInfo.add(new AddressInfo(addressStr, providerConfigData));
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
}
