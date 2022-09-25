package com.mola.rpc.consumer;

import com.mola.rpc.client.OrderService;
import com.mola.rpc.common.annotation.RpcConsumer;
import org.springframework.context.annotation.Configuration;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-07-25 23:37
 **/
@Configuration
public class RpcConsumerConfig {

    @RpcConsumer
    private OrderService orderService;
}
