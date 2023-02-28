package com.mola.rpc.spring;

import com.mola.rpc.core.properties.RpcProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-10-23 11:10
 **/
@ConfigurationProperties(prefix = "rpc")
public class RpcSpringConfigurationProperties extends RpcProperties {
}
