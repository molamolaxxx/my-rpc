package com.mola.rpc.webmanager.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-03-27 22:48
 **/
@Configuration
@EnableConfigurationProperties(RpcWebConfigurationProperties.class)
public class RpcWebConfiguration {
}
