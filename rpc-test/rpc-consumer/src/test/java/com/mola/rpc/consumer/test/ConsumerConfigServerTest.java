package com.mola.rpc.consumer.test;

import com.mola.rpc.data.config.spring.RpcProviderDataInitBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 单测：
 * 1、服务发现地址和configserver地址相同
 * @date : 2023-01-22 16:43
 **/
@SpringBootTest(classes = ConsumerTestContext.class)
@RunWith(SpringRunner.class)
public class ConsumerConfigServerTest {

    @Resource
    private RpcProviderDataInitBean rpcProviderDataInitBean;

    /**
     * 服务发现地址和configserver地址相同
     */
    @Test
    public void configServerAddressEqualsConsumerFind() {

    }
}
