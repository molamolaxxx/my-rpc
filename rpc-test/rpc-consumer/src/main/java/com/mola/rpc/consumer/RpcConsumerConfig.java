package com.mola.rpc.consumer;

import com.mola.rpc.client.OrderService;
import com.mola.rpc.common.annotation.RpcConsumer;
import com.mola.rpc.common.constants.LoadBalanceConstants;
import com.mola.rpc.core.properties.RpcProperties;
import com.mola.rpc.core.proto.ProtoRpcConfigFactory;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-07-25 23:37
 **/
@Configuration
public class RpcConsumerConfig {

    @RpcConsumer(loadBalanceStrategy = LoadBalanceConstants.CONSISTENCY_HASHING_STRATEGY)
    private OrderService orderService;

    @RpcConsumer(loadBalanceStrategy = LoadBalanceConstants.CONSISTENCY_HASHING_STRATEGY, group = "gray",
            asyncMethods = {"queryOrderList","searchOrderListWithUser"})
    private OrderService orderServiceGray;

    /**
     * 原始调用、泛化调用配置
     */
    @PostConstruct
    public void init() {
        RpcProperties rpcProperties = new RpcProperties();
        // 环境标
        rpcProperties.setEnvironment("pre");
        // 如果只有只有单点调用，则可以开启，不启动configserver
//        rpcProperties.setStartConfigServer(false);
        ProtoRpcConfigFactory.configure(rpcProperties);
    }

}
