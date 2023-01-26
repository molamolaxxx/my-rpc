package com.mola.rpc.data.config.manager.zk;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.mola.rpc.common.constants.CommonConstants;
import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.common.entity.AddressInfo;
import com.mola.rpc.common.entity.ProviderConfigData;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.common.entity.SystemInfo;
import com.mola.rpc.data.config.listener.AddressChangeListener;
import com.mola.rpc.data.config.listener.ZkProviderConfigChangeListenerImpl;
import com.mola.rpc.data.config.manager.BaseRpcDataManager;
import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * provider: myRpc/provider/{providerPath}/ip
 * @date : 2022-08-27 20:42
 **/
public class ZkRpcDataManager extends BaseRpcDataManager {

    private static final Logger log = LoggerFactory.getLogger(ZkRpcDataManager.class);

    /**
     * zk实例
     */
    private ZkClient zkClient;

    /**
     * 注册中心地址
     */
    private String configServerIpPort;

    /**
     * 超时时间
     */
    private Integer connectTimeout;

    /**
     * 地址变更监听器
     */
    private List<AddressChangeListener> addressChangeListeners;

    public ZkRpcDataManager(String configServerIpPort, Integer connectTimeout) {
        this.configServerIpPort = configServerIpPort;
        this.connectTimeout = connectTimeout;
    }

    @Override
    public void init(RpcContext rpcContext) {
        super.init(rpcContext);
        ZkClient zkClient = null;
        try {
            zkClient = new ZkClient(this.configServerIpPort, this.connectTimeout);
            this.zkClient = zkClient;
            createIfNotExist(CommonConstants.PATH_MY_RPC);
            createIfNotExist(CommonConstants.PATH_MY_RPC_PROVIDER);
            createIfNotExist(CommonConstants.PATH_MY_RPC_CONSUMER);
        } catch (Exception e) {
            throw new RuntimeException("zooKeeper init failed!" + e.getMessage());
        }
    }

    private void createIfNotExist(String path) {
        if (!zkClient.exists(path)) {
            zkClient.create(path, System.currentTimeMillis(), CreateMode.PERSISTENT);
        }
    }

    @Override
    public List<AddressInfo> getRemoteProviderAddress(String interfaceClazz, String group, String version, String environment) {
        String remoteProviderPath = getRemoteProviderPath(interfaceClazz, group, version, environment);
        return zkClient.getChildren(remoteProviderPath).stream()
                .map(path -> new AddressInfo(path,
                        JSONObject.parseObject(zkClient.readData(remoteProviderPath + "/" + path), ProviderConfigData.class))).collect(Collectors.toList());

    }

    @Override
    public void registerProviderDataListener(String interfaceClazz, String group, String version, String environment, RpcMetaData consumerMetaData) {
        String remoteProviderPath = getRemoteProviderPath(interfaceClazz, group, version, environment);
        zkClient.subscribeChildChanges(remoteProviderPath, new ZkProviderConfigChangeListenerImpl(
                consumerMetaData, zkClient, addressChangeListeners
        ));
    }

    @Override
    public Boolean isProviderExist(String interfaceClazz, String group, String version, String environment) {
        String remoteProviderPath = getRemoteProviderPath(interfaceClazz, group, version, environment);
        return zkClient.exists(remoteProviderPath);
    }

    @Override
    public void uploadRemoteProviderData(RpcMetaData providerMetaData, String environment, String appName, String address) {
        String remoteProviderParentPath = getRemoteProviderPath(providerMetaData.getInterfaceClazz().getName(), providerMetaData.getGroup(),
                providerMetaData.getVersion(), environment);
        if (!zkClient.exists(remoteProviderParentPath)) {
            zkClient.create(remoteProviderParentPath, providerMetaData.toString(), CreateMode.PERSISTENT);
        }
        zkClient.updateDataSerialized(remoteProviderParentPath, data -> providerMetaData.toString());
        if (StringUtils.isEmpty(address)) {
            return;
        }
        ProviderConfigData providerConfigData = ProviderConfigData.create(remoteProviderParentPath,
                appName, providerMetaData.getHost(), address, providerMetaData.getProto());
        String childPath = remoteProviderParentPath + "/" + address;
        if (!zkClient.exists(childPath)) {
            zkClient.create(childPath, providerConfigData.toString(), CreateMode.EPHEMERAL);
        }
        zkClient.updateDataSerialized(remoteProviderParentPath + "/" + address, data -> {
            ProviderConfigData providerConfig = JSONObject.parseObject((String) data, ProviderConfigData.class);
            providerConfig.setProviderLastHeartBeatTime(System.currentTimeMillis());
            providerConfig.setSystemInfo(SystemInfo.get());
            return providerConfig.toString();
        });
    }

    @Override
    public void deleteRemoteProviderData(RpcMetaData providerMetaData, String environment, String appName, String address) {
        String remoteProviderParentPath = getRemoteProviderPath(providerMetaData.getInterfaceClazz().getName(), providerMetaData.getGroup(),
                providerMetaData.getVersion(), environment);
        if (!zkClient.exists(remoteProviderParentPath)) {
            return;
        }
        String childPath = remoteProviderParentPath + "/" + address;
        if (!zkClient.exists(childPath)) {
            return;
        }
        zkClient.delete(childPath);
    }

    @Override
    public void uploadConsumerData(RpcMetaData consumerMetaData) {
    }

    @Override
    public Map<String, RpcMetaData> getAllProviderMetaData() {
        List<String> providerKeys = zkClient.getChildren(CommonConstants.PATH_MY_RPC_PROVIDER);
        Map<String, RpcMetaData> rpcMetaDataMap = new HashMap<>(providerKeys.size());
        for (String providerKey : providerKeys) {
            Object providerMetaData = zkClient.readData(CommonConstants.PATH_MY_RPC_PROVIDER + "/" + providerKey);
            if (providerMetaData instanceof String && !StringUtils.isEmpty(providerMetaData)) {
                RpcMetaData rpcMetaData = JSONObject.parseObject((String) providerMetaData, RpcMetaData.class);
                rpcMetaDataMap.put(providerKey, rpcMetaData);
            }
        }
        return rpcMetaDataMap;
    }

    @Override
    public List<ProviderConfigData> getAllProviderConfigData(String interfaceClazz, String group, String version, String environment) {
        String remoteProviderPath = getRemoteProviderPath(interfaceClazz, group, version, environment);
        List<String> childrenPaths = zkClient.getChildren(remoteProviderPath);
        if (CollectionUtils.isEmpty(childrenPaths)) {
            return Lists.newArrayList();
        }
        List<ProviderConfigData> providerConfigDataList = Lists.newArrayList();
        for (String path : childrenPaths) {
            Object providerConfigDataJson = zkClient.readData(remoteProviderPath + "/" + path);
            if (providerConfigDataJson instanceof String && !StringUtils.isEmpty(providerConfigDataJson)) {
                ProviderConfigData providerConfigData = JSONObject.parseObject((String) providerConfigDataJson, ProviderConfigData.class);
                providerConfigDataList.add(providerConfigData);
            }
        }
        return providerConfigDataList;
    }

    @Override
    public void setAddressChangeListener(List<AddressChangeListener> addressChangeListeners) {
        this.addressChangeListeners = addressChangeListeners;
    }
}
