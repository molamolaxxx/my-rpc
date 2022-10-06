package com.mola.rpc.consumer;

import com.mola.rpc.client.OrderService;
import com.mola.rpc.common.annotation.RpcConsumer;
import com.mola.rpc.common.constants.LoadBalanceConstants;
import org.springframework.context.annotation.Configuration;

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

    @RpcConsumer(loadBalanceStrategy = LoadBalanceConstants.LOAD_BALANCE_ROUND_ROBIN_STRATEGY, group = "gray", asyncMethods = {"queryOrderList"})
    private OrderService orderServiceGray;
}
