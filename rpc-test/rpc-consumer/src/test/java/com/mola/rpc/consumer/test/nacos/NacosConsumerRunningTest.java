package com.mola.rpc.consumer.test.nacos;

import com.mola.rpc.consumer.test.ConsumerRunningTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 单测：
 * 1、spring环境下消费者正常运行，多个bean注册正常，netty正常启动
 * 请先启动如下，并保证测试配置的正确：
 * 1、一个或多个rpc-provider服务，保证单元测试正常运行
 * 2、zookeeper
 * @date : 2023-01-22 16:43
 **/
@ActiveProfiles("nacos")
public class NacosConsumerRunningTest extends ConsumerRunningTest {
}
