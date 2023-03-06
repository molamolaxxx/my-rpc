package com.mola.rpc.provider;

import com.mola.rpc.client.UserService;
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
    private UserService userService;

    @RpcConsumer(reverseMode = true, group = "reverse_proto")
    private UserService userServiceReverse;

    @RpcConsumer(reverseMode = true, group = "reverse_anno")
    private UserService userServiceReverseInAnno;
}
