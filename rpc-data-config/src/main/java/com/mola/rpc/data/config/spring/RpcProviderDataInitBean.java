package com.mola.rpc.data.config.spring;

import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.common.entity.AddressInfo;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.data.config.manager.RpcDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.List;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 客户端数据拉取
 * @date : 2022-08-27 15:40
 **/
public class RpcProviderDataInitBean {

    private static final Logger log = LoggerFactory.getLogger(RpcProviderDataInitBean.class);

    /**
     * rpc上下文
     */
    private RpcContext rpcContext;

    /**
     * 运行环境
     */
    private String environment;

    /**
     * 应用名
     */
    private String appName;

    /**
     * rpc服务
     */
    private RpcDataManager<RpcMetaData> rpcDataManager;


    public void init() {
        Assert.notNull(rpcContext, "拉取数据失败，上下文为空");
        // 上报提供的provider数据
        Collection<RpcMetaData> providerMetaDataCollection = rpcContext.getProviderMetaMap().values();
        for (RpcMetaData providerMetaData : providerMetaDataCollection) {
            rpcDataManager.uploadRemoteProviderData(providerMetaData, environment, appName, rpcContext.getProviderAddress());
        }
        // 拉取订阅的provider数据，填充到上下文
        Collection<RpcMetaData> consumerMetaDataCollection = rpcContext.getConsumerMetaMap().values();
        for (RpcMetaData consumerMetaData : consumerMetaDataCollection) {
            // 服务名
            String serviceName = consumerMetaData.getInterfaceClazz().getName();
            // group
            String group = consumerMetaData.getGroup();
            // version
            String version = consumerMetaData.getVersion();
            // 判断服务是否存在
            if (!rpcDataManager.isProviderExist(serviceName, group, version, environment)) {
                throw new RuntimeException("provider not exist! meta = " + consumerMetaData.toString());
            }
            // 从配置中心上拉取配置，注册监听器
            rpcDataManager.registerProviderDataListener(serviceName, group, version, environment, consumerMetaData);
            List<AddressInfo> addressInfoList = rpcDataManager.getRemoteProviderAddress(serviceName, group, version, environment);
            if (null == addressInfoList) {
                log.warn("addressInfoList is null , meta : " + consumerMetaData.toString());
                continue;
            }
            consumerMetaData.setAddressList(addressInfoList);
        }
    }

    public RpcContext getRpcContext() {
        return rpcContext;
    }

    public void setRpcContext(RpcContext rpcContext) {
        this.rpcContext = rpcContext;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public RpcDataManager<RpcMetaData> getRpcDataManager() {
        return rpcDataManager;
    }

    public void setRpcDataManager(RpcDataManager<RpcMetaData> rpcDataManager) {
        this.rpcDataManager = rpcDataManager;
    }
}
