package com.mola.rpc.data.config.manager.zk;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.mola.rpc.common.constants.CommonConstants;
import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.common.entity.*;
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
 * provider: myRpc/provider/{environment}/{providerPath}/ip
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
    private String configServerAddress;

    /**
     * 超时时间
     */
    private Integer connectTimeout;

    private BaseRpcProperties rpcProperties;

    private String providerRootPath;

    public ZkRpcDataManager(BaseRpcProperties rpcProperties) {
        this.configServerAddress = rpcProperties.getConfigServerAddress();
        this.connectTimeout = 10000;
        this.rpcProperties = rpcProperties;
    }

    @Override
    public void init(RpcContext rpcContext) {
        super.init(rpcContext);
        ZkClient zkClient = null;
        try {
            zkClient = new ZkClient(this.configServerAddress, this.connectTimeout);
            this.zkClient = zkClient;
            createIfNotExist(CommonConstants.PATH_MY_RPC);
            createIfNotExist(CommonConstants.PATH_MY_RPC_PROVIDER);
            createIfNotExist(CommonConstants.PATH_MY_RPC_CONSUMER);
            // 环境
            this.providerRootPath = CommonConstants.PATH_MY_RPC_PROVIDER + "/" + rpcProperties.getEnvironment();
            createIfNotExist(providerRootPath);
//            createIfNotExist(CommonConstants.PATH_MY_RPC_CONSUMER + "/" + rpcProperties.getEnvironment());
        } catch (Exception e) {
            throw new RuntimeException("zooKeeper init failed!" + e.getMessage());
        }
    }

    @Override
    public void destroy() {
        if (null  != this.zkClient)  {
            this.zkClient.close();
        }
        this.zkClient = null;
        this.rpcProperties = null;
    }

    private void createIfNotExist(String path) {
        if (!zkClient.exists(path)) {
            zkClient.create(path, System.currentTimeMillis(), CreateMode.PERSISTENT);
        }
    }

    @Override
    public List<AddressInfo> getRemoteProviderAddress(String interfaceClazz, String group, String version, String environment) {
        String remoteProviderPath = getRemoteProviderPath(interfaceClazz, group, version, environment);
        try {
            return Lists.newCopyOnWriteArrayList(
                    zkClient.getChildren(remoteProviderPath).stream().map(path -> new AddressInfo(path,
                            JSONObject.parseObject(zkClient.readData(remoteProviderPath + "/" + path), ProviderConfigData.class)))
                            .collect(Collectors.toList())
            );
        } catch (Exception e) {
            log.error("getRemoteProviderAddress getChildren error, remoteProviderPath = " + remoteProviderPath, e);
        }
        return Lists.newCopyOnWriteArrayList();
    }

    @Override
    public void registerProviderDataListener(String interfaceClazz, String group, String version, String environment, RpcMetaData consumerMetaData) {
        String remoteProviderPath = getRemoteProviderPath(interfaceClazz, group, version, environment);
        zkClient.subscribeChildChanges(remoteProviderPath, new ZkProviderConfigChangeListenerImpl(
                consumerMetaData, zkClient
        ));
    }

    @Override
    public boolean isProviderAvailable(String interfaceClazz, String group, String version, String environment) {
        String remoteProviderPath = getRemoteProviderPath(interfaceClazz, group, version, environment);
        return zkClient.exists(remoteProviderPath);
    }

    @Override
    public boolean isInstanceAvailable(String interfaceClazz, String group, String version, String environment, String address) {
        String remoteProviderPath = getRemoteProviderPath(interfaceClazz, group, version, environment);
        return zkClient.exists(remoteProviderPath +  "/" + address);
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
        List<String> providerKeys = zkClient.getChildren(this.providerRootPath);
        Map<String, RpcMetaData> rpcMetaDataMap = new HashMap<>(providerKeys.size());
        for (String providerKey : providerKeys) {
            Object providerMetaData = zkClient.readData(this.providerRootPath + "/" + providerKey);
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
}
