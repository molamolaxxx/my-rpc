package com.mola.rpc.consumer.test;

import com.mola.rpc.client.UnitTestService;
import com.mola.rpc.common.annotation.RpcConsumer;
import org.springframework.context.annotation.Configuration;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-01-22 21:34
 **/
@Configuration
public class ConsumerTestConf {

    @RpcConsumer
    private UnitTestService unitTestService;

    @RpcConsumer
    private UnitTestService unitTestService2;

    @RpcConsumer
    private UnitTestService unitTestService3;
}
