package com.mola.rpc.data.config.manager.nacos;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Cluster;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.Service;
import com.alibaba.nacos.api.utils.StringUtils;
import com.google.common.collect.Lists;
import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.common.entity.*;
import com.mola.rpc.common.utils.ObjectUtils;
import com.mola.rpc.data.config.listener.NacosProviderConfigChangeListenerImpl;
import com.mola.rpc.data.config.manager.BaseRpcDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: data-config-manager对于nacos的适配
 * @date : 2023-02-27 18:07
 **/
public class NacosRpcDataManager extends BaseRpcDataManager {

    private static final Logger log = LoggerFactory.getLogger(NacosRpcDataManager.class);

    private NamingService namingService;

    /**
     * nacos注册中心地址
     */
    private String configServerAddress;

    private BaseRpcProperties rpcProperties;

    public NacosRpcDataManager(BaseRpcProperties rpcProperties) {
        this.configServerAddress = rpcProperties.getConfigServerAddress();
        this.rpcProperties = rpcProperties;
    }

    @Override
    public void init(RpcContext rpcContext) {
        try {
            Properties properties = new Properties();
            properties.setProperty(PropertyKeyConst.SERVER_ADDR, configServerAddress);
            properties.setProperty(PropertyKeyConst.NAMESPACE, rpcProperties.getEnvironment());
            if(StringUtils.isBlank(System.getProperty("project.name"))){
                System.setProperty("project.name", rpcProperties.getAppName());
            }
            this.namingService = NamingFactory.createNamingService(properties);
        } catch (Exception e) {
            log.error("NacosRpcDataManager init failed", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy() {
        if (null != this.namingService) {
            try {
                this.namingService.shutDown();
            } catch (NacosException e) {
                // ignore
            }
        }
        this.namingService = null;
        this.rpcProperties = null;
    }

    @Override
    public List<AddressInfo> getRemoteProviderAddress(String interfaceClazz, String group, String version, String environment) {
        String serviceName = String.format("%s:%s:%s", interfaceClazz, group, version);
        try {
            List<Instance> allInstances = this.namingService.selectInstances(serviceName, true);
            if (CollectionUtils.isEmpty(allInstances)) {
                return Lists.newArrayList();
            }
            // by环境过滤+聚合实例到AddressInfo
            return allInstances.stream().map(instance -> {
                ProviderConfigData providerConfigData = ObjectUtils.parseMap(instance.getMetadata(), ProviderConfigData.class);
                SystemInfo systemInfo = JSONObject.parseObject(instance.getMetadata().get("systemInfoKey"), SystemInfo.class);
                providerConfigData.setSystemInfo(systemInfo);
                AddressInfo info = new AddressInfo(instance.getIp()+":" +instance.getPort(), providerConfigData);
                return info;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("getRemoteProviderAddress getAllInstances error, serviceName = " + serviceName, e);
        }
        return Lists.newArrayList();
    }

    @Override
    public void registerProviderDataListener(String interfaceClazz, String group, String version, String environment, RpcMetaData consumerMetaData) {
        String serviceName = String.format("%s:%s:%s", interfaceClazz, group, version);
        try {
            this.namingService.subscribe(serviceName, new NacosProviderConfigChangeListenerImpl(
                    consumerMetaData, addressChangeListeners
            ));
        } catch (Exception e) {
            log.error("registerProviderDataListener subscribe error, serviceName = " + serviceName, e);
        }
    }

    @Override
    public boolean isProviderAvailable(String interfaceClazz, String group, String version, String environment) {
        String serviceName = String.format("%s:%s:%s", interfaceClazz, group, version);
        try {
            List<Instance> allInstances = this.namingService.selectInstances(serviceName, true);
            if (!CollectionUtils.isEmpty(allInstances)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("getRemoteProviderAddress getAllInstances error, serviceName = " + serviceName, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void uploadRemoteProviderData(RpcMetaData providerMetaData, String environment, String appName, String address) {
        // 构建实例对象
        Instance instance = buildInstance(providerMetaData, environment, appName, address);
        // 构建服务对象
        Service service = buildService(providerMetaData, environment, appName, address);
        // 集群对象（应用名）
        Cluster cluster = buildCluster(providerMetaData, environment, appName, address);
        instance.setServiceName(service.getName());
        instance.setClusterName(cluster.getName());
        try {
            this.namingService.registerInstance(service.getName(), instance);
        } catch (Exception e) {
            log.error("uploadRemoteProviderData registerInstance error,serviceName = " + service.getName(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteRemoteProviderData(RpcMetaData providerMetaData, String environment, String appName, String address) {
        // 对应nacos删除实例
        String serviceName = String.format("%s:%s:%s",
                providerMetaData.getInterfaceClazz().getName(),
                providerMetaData.getGroup(), providerMetaData.getVersion());
        try {
            String[] split = address.split(":");
            Assert.isTrue(split.length == 2, "address format error!");
            this.namingService.deregisterInstance(
                    serviceName,
                    split[0], Integer.parseInt(split[1]), appName);
        } catch (Exception e) {
            log.error("getRemoteProviderAddress deregisterInstance error, serviceName = " + serviceName, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean requireSendProviderHeartBeat() {
        // nacos自带心跳检测，无需使用
        return false;
    }

    private Instance buildInstance(RpcMetaData providerMetaData, String environment, String appName, String address) {
        Instance instance = new Instance();
        String[] split = address.split(":");
        Assert.isTrue(split.length == 2, "address format error!");
        instance.setIp(split[0]);
        instance.setPort(Integer.parseInt(split[1]));
        String remoteProviderParentPath = getRemoteProviderPath(providerMetaData.getInterfaceClazz().getName(), providerMetaData.getGroup(),
                providerMetaData.getVersion(), environment);
        ProviderConfigData providerConfigData = ProviderConfigData.create(remoteProviderParentPath,
                appName, providerMetaData.getHost(), address, providerMetaData.getProto());
        providerConfigData.setSystemInfo(null);
        instance.setMetadata(ObjectUtils.parseObject(providerConfigData));
        instance.getMetadata().put("systemInfoKey", JSONObject.toJSONString(SystemInfo.get()));
        return instance;
    }

    private Service buildService(RpcMetaData providerMetaData, String environment, String appName, String address) {
        Service service = new Service(
                String.format("%s:%s:%s",
                        providerMetaData.getInterfaceClazz().getName(),
                        providerMetaData.getGroup(), providerMetaData.getVersion()));
        service.setAppName(appName);
        service.setGroupName(providerMetaData.getGroup());
        service.setProtectThreshold(0.8F);
        service.setMetadata(ObjectUtils.parseObject(providerMetaData));
        return service;
    }

    private Cluster buildCluster(RpcMetaData providerMetaData, String environment, String appName, String address) {
        Cluster cluster = new Cluster();
        // 不同环境是不同的集群，应用+环境=集群
        cluster.setName(appName);
        Map<String, String> clusterMeta = new HashMap<>();
        cluster.setMetadata(clusterMeta);
        return cluster;
    }
}
