package com.mola.rpc.consumer.test;

import com.mola.rpc.client.UnitTestService;
import com.mola.rpc.common.annotation.RpcConsumer;
import com.mola.rpc.common.constants.LoadBalanceConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-01-22 21:34
 **/
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class ConsumerTestConf {

    @RpcConsumer
    private UnitTestService unitTestService;

    @RpcConsumer
    private UnitTestService unitTestService2;

    @RpcConsumer
    private UnitTestService unitTestService3;

    @RpcConsumer(asyncMethods = "*")
    private UnitTestService unitTestServiceAsync;

    @RpcConsumer(appointedAddress = "127.0.0.1:9003")
    private UnitTestService unitTestServiceAppointZk;

    @RpcConsumer(appointedAddress = "127.0.0.1:9003")
    private UnitTestService unitTestServiceAppointAsync;

    @RpcConsumer(appointedAddress = "127.0.0.1:9013")
    private UnitTestService unitTestServiceAppointNacos;

    @RpcConsumer(loadBalanceStrategy = LoadBalanceConstants.RANDOM_STRATEGY)
    protected UnitTestService unitTestServiceUseRandom;

    @RpcConsumer(loadBalanceStrategy = LoadBalanceConstants.ROUND_ROBIN_STRATEGY)
    protected UnitTestService unitTestServiceUseRoundRobin;

    @RpcConsumer(loadBalanceStrategy = LoadBalanceConstants.CONSISTENCY_HASHING_STRATEGY)
    protected UnitTestService unitTestServiceUseHash;

    @RpcConsumer(
            loadBalanceStrategy = LoadBalanceConstants.CONSISTENCY_HASHING_STRATEGY,
            appointedAddress = {
                    "127.0.0.1:9238", "127.0.0.1:9239", "127.0.0.1:9211",
                    "127.0.0.1:9248", "127.0.0.1:9339", "127.0.0.1:9212",
                    "127.0.0.1:9258", "127.0.0.1:9439", "127.0.0.1:9213",
                    "127.0.0.1:9268", "127.0.0.1:9539", "127.0.0.1:9214",
                    "127.0.0.1:9278", "127.0.0.1:9639", "127.0.0.1:9215"
            }
    )
    protected UnitTestService unitTestServiceUseHashAppoint;


    @RpcConsumer(
            loadBalanceStrategy = LoadBalanceConstants.ROUND_ROBIN_STRATEGY
    )
    protected UnitTestService unitTestServiceRoundRobinAppoint;
}
