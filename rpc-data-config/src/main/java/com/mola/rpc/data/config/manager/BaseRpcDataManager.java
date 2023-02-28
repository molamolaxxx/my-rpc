package com.mola.rpc.data.config.manager;

import com.google.common.collect.Lists;
import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.common.entity.AddressInfo;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.data.config.listener.AddressChangeListener;

import java.util.List;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-10-16 20:01
 **/
public class BaseRpcDataManager implements RpcDataManager<RpcMetaData> {


    /**
     * 地址变更监听器
     */
    protected List<AddressChangeListener> addressChangeListeners;

    @Override
    public void init(RpcContext rpcContext) {
    }

    @Override
    public List<AddressInfo> getRemoteProviderAddress(String interfaceClazz, String group, String version, String environment) {
        return Lists.newArrayList();
    }

    @Override
    public void registerProviderDataListener(String interfaceClazz, String group, String version, String environment, RpcMetaData consumerMetaData) {

    }

    @Override
    public boolean isProviderExist(String interfaceClazz, String group, String version, String environment) {
        return false;
    }

    @Override
    public void uploadRemoteProviderData(RpcMetaData providerMetaData, String environment, String appName, String address) {

    }

    @Override
    public void deleteRemoteProviderData(RpcMetaData providerMetaData, String environment, String appName, String address) {

    }

    @Override
    public void uploadConsumerData(RpcMetaData consumerMetaData) {

    }

    @Override
    public void setAddressChangeListener(List<AddressChangeListener> addressChangeListeners) {
        this.addressChangeListeners = addressChangeListeners;
    }
}
