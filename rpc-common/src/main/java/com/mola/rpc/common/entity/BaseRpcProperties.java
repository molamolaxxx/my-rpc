package com.mola.rpc.common.entity;

import com.mola.rpc.common.constants.CommonConstants;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-02-27 23:21
 **/
public class BaseRpcProperties {

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
     * 发布到nacos为命名空间的id
     */
    private String environment = CommonConstants.DEFAULT_ENVIRONMENT;

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
     * 注册中心类型
     * 1、zookeeper
     * 2、nacos
     */
    private String configServerType = CommonConstants.ZOOKEEPER;

    /**
     * 是否在启动时检查依赖的服务提供者，默认否
     */
    private Boolean checkDependencyProviderBeforeStart = Boolean.FALSE;

    /**
     * 客户端最大超时时间，默认三分钟
     */
    private Long maxClientTimeout = 180 * 1000L;

    /**
     * 是否启动注册中心，默认启动
     * 如果在无注册中心的环境下，如点对点调用、反向代理，配置为false
     */
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

    public Boolean getStartConfigServer() {
        return startConfigServer;
    }

    public void setStartConfigServer(Boolean startConfigServer) {
        this.startConfigServer = startConfigServer;
    }

    public String getConfigServerType() {
        return configServerType;
    }

    public void setConfigServerType(String configServerType) {
        this.configServerType = configServerType;
    }

    public Boolean getCheckDependencyProviderBeforeStart() {
        return checkDependencyProviderBeforeStart;
    }

    public void setCheckDependencyProviderBeforeStart(Boolean checkDependencyProviderBeforeStart) {
        this.checkDependencyProviderBeforeStart = checkDependencyProviderBeforeStart;
    }

    public Long getMaxClientTimeout() {
        return maxClientTimeout;
    }

    public void setMaxClientTimeout(Long maxClientTimeout) {
        this.maxClientTimeout = maxClientTimeout;
    }
}
