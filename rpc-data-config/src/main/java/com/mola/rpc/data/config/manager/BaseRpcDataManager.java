package com.mola.rpc.data.config.manager;

import com.google.common.collect.Lists;
import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.common.entity.AddressInfo;
import com.mola.rpc.common.entity.RpcMetaData;

import java.util.List;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-10-16 20:01
 **/
public class BaseRpcDataManager implements RpcDataManager<RpcMetaData> {

    @Override
    public void init(RpcContext rpcContext) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public List<AddressInfo> getRemoteProviderAddress(String interfaceClazz, String group, String version, String environment) {
        return Lists.newCopyOnWriteArrayList();
    }

    @Override
    public void registerProviderDataListener(String interfaceClazz, String group, String version, String environment, RpcMetaData consumerMetaData) {

    }

    @Override
    public boolean isProviderAvailable(String interfaceClazz, String group, String version, String environment) {
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
}
