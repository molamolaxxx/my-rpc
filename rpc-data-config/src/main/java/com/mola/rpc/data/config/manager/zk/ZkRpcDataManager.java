package com.mola.rpc.data.config.manager.zk;

import com.alibaba.fastjson.JSONObject;
import com.mola.rpc.common.constants.CommonConstants;
import com.mola.rpc.common.entity.AddressInfo;
import com.mola.rpc.common.entity.ProviderConfigData;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.data.config.listener.ZkProviderConfigChangeListenerImpl;
import com.mola.rpc.data.config.manager.RpcDataManager;
import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * provider: myRpc/provider/{providerPath}/ip
 * @date : 2022-08-27 20:42
 **/
public class ZkRpcDataManager implements RpcDataManager<RpcMetaData> {

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

    public ZkRpcDataManager(String configServerIpPort, Integer connectTimeout) {
        this.configServerIpPort = configServerIpPort;
        this.connectTimeout = connectTimeout;
    }

    @Override
    public void init() {
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
                .map(path -> new AddressInfo(path)).collect(Collectors.toList());

    }

    @Override
    public void registerProviderDataListener(String interfaceClazz, String group, String version, String environment, RpcMetaData consumerMetaData) {
        String remoteProviderPath = getRemoteProviderPath(interfaceClazz, group, version, environment);
        zkClient.subscribeChildChanges(remoteProviderPath, new ZkProviderConfigChangeListenerImpl(
                consumerMetaData, zkClient
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
        ProviderConfigData providerConfigData = ProviderConfigData.create(appName, providerMetaData.getHost());
        String childPath = remoteProviderParentPath + "/" + address;
        if (!zkClient.exists(childPath)) {
            zkClient.create(childPath, providerConfigData.toString(), CreateMode.EPHEMERAL);
        }
        zkClient.updateDataSerialized(remoteProviderParentPath, data -> {
            ProviderConfigData providerConfig = JSONObject.parseObject((String) data, ProviderConfigData.class);
            providerConfig.setProviderLastHeartBeatTime(System.currentTimeMillis());
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
    public void updateProviderHeartBeat(RpcMetaData providerMetaData, String environment, String address) {
        String remoteProviderParentPath = getRemoteProviderPath(providerMetaData.getInterfaceClazz().getName(), providerMetaData.getGroup(),
                providerMetaData.getVersion(), environment);
        if (!zkClient.exists(remoteProviderParentPath)) {
            log.warn("remoteProviderParentPath not exist! path = " + remoteProviderParentPath);
            return;
        }
        String childPath = remoteProviderParentPath + "/" + address;
        if (!zkClient.exists(childPath)) {
            log.warn("remoteProviderParentPath not exist! path = " + childPath);
            return;
        }
        zkClient.updateDataSerialized(childPath, data -> {
            ProviderConfigData providerConfig = JSONObject.parseObject((String) data, ProviderConfigData.class);
            providerConfig.setProviderLastHeartBeatTime(System.currentTimeMillis());
            return providerConfig.toString();
        });
    }

    @Override
    public void uploadConsumerData(RpcMetaData consumerMetaData) {
    }
}
