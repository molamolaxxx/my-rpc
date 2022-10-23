package com.mola.rpc.core.properties;

import com.mola.rpc.common.constants.CommonConstants;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.data.config.manager.RpcDataManager;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-07-30 18:42
 **/
public class RpcProperties {

    /**
     * configServer地址
     */
    private String configServerAddress = "127.0.0.1:2181";

    /**
     * 负载均衡策略
     */
    private String loadBalanceStrategy = "RANDOM";

    /**
     * 服务端开启端口
     */
    private Integer serverPort = 9000;

    /**
     * 应用服务名
     */
    private String appName = CommonConstants.UNKNOWN_APP;

    /**
     * 环境名
     */
    private String environment = CommonConstants.UNKNOWN_ENVIRONMENT;

    /**
     * 默认核心线程数量
     */
    private Integer coreBizThreadNum = 10;

    /**
     * 默认最大线程数量
     */
    private Integer maxBizThreadNum = 80;

    /**
     * 最大任务数量
     */
    private Integer maxBlockingQueueSize = 1024;

    /**
     * 自定义注册中心bean
     */
    private String configServerBeanName;

    /**
     * 自定义configure server实现
     */
    private RpcDataManager<RpcMetaData> rpcDataManager;

    private Boolean startConfigServer = Boolean.TRUE;


    public String getConfigServerAddress() {
        return configServerAddress;
    }

    public void setConfigServerAddress(String configServerAddress) {
        this.configServerAddress = configServerAddress;
    }

    public String getLoadBalanceStrategy() {
        return loadBalanceStrategy;
    }

    public void setLoadBalanceStrategy(String loadBalanceStrategy) {
        this.loadBalanceStrategy = loadBalanceStrategy;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public Integer getCoreBizThreadNum() {
        return coreBizThreadNum;
    }

    public void setCoreBizThreadNum(Integer coreBizThreadNum) {
        this.coreBizThreadNum = coreBizThreadNum;
    }

    public Integer getMaxBizThreadNum() {
        return maxBizThreadNum;
    }

    public void setMaxBizThreadNum(Integer maxBizThreadNum) {
        this.maxBizThreadNum = maxBizThreadNum;
    }

    public Integer getMaxBlockingQueueSize() {
        return maxBlockingQueueSize;
    }

    public void setMaxBlockingQueueSize(Integer maxBlockingQueueSize) {
        this.maxBlockingQueueSize = maxBlockingQueueSize;
    }

    public void setConfigServerBeanName(String configServerBeanName) {
        this.configServerBeanName = configServerBeanName;
    }

    public String getConfigServerBeanName() {
        return configServerBeanName;
    }

    public RpcDataManager<RpcMetaData> getRpcDataManager() {
        return rpcDataManager;
    }

    public void setRpcDataManager(RpcDataManager<RpcMetaData> rpcDataManager) {
        this.rpcDataManager = rpcDataManager;
    }

    public Boolean getStartConfigServer() {
        return startConfigServer;
    }

    public void setStartConfigServer(Boolean startConfigServer) {
        this.startConfigServer = startConfigServer;
    }
}
