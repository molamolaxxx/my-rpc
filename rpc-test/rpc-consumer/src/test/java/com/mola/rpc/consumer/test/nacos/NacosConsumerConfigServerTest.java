package com.mola.rpc.consumer.test.nacos;

import com.mola.rpc.consumer.test.ConsumerConfigServerTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 单测：
 * 1、服务发现地址和configserver地址相同
 * @date : 2023-01-22 16:43
 **/
@ActiveProfiles("nacos")
public class NacosConsumerConfigServerTest extends ConsumerConfigServerTest {
}
