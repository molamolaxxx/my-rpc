package com.mola.rpc.webmanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-03-27 22:48
 **/
@ConfigurationProperties(prefix = "rpc.web")
public class RpcWebConfigurationProperties {

    /**
     * 是否刷新configServer的数据到DB
     */
    private Boolean refreshDatabase = Boolean.FALSE;

    /**
     * 是否从DB刷新网关数据到缓存
     */
    private Boolean refreshGatewayMapping = Boolean.FALSE;


    public void setRefreshDatabase(Boolean refreshDatabase) {
        this.refreshDatabase = refreshDatabase;
    }

    public Boolean getRefreshDatabase() {
        return refreshDatabase;
    }

    public void setRefreshGatewayMapping(Boolean refreshGatewayMapping) {
        this.refreshGatewayMapping = refreshGatewayMapping;
    }

    public Boolean getRefreshGatewayMapping() {
        return refreshGatewayMapping;
    }
}
