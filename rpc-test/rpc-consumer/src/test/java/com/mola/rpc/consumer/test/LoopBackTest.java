package com.mola.rpc.consumer.test;

import com.mola.rpc.client.Order;
import com.mola.rpc.client.ServerResponse;
import com.mola.rpc.client.UnitTestService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import javax.annotation.Resource;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 环路测试
 * Test-Env -> Provider1-APP -> Test-Env
 * @date : 2023-01-23 15:21
 **/
@SpringBootTest(classes = ConsumerTestContext.class)
@RunWith(SpringRunner.class)
public class LoopBackTest {

    @Resource
    private UnitTestService unitTestService;

    @Test
    public void loopBackTest001() {
        long start = System.currentTimeMillis();
        Order order = new Order();
        order.setId("123");
        ServerResponse<Order> response = unitTestService.loopBackTest(order);
        Assert.isTrue(response != null && response.isSuccess(), "loopBackTest001测试失败,调用失败");
        Assert.isTrue(response.getData().getOperator().equals("test:123"), "loopBackTest001测试失败");
        System.out.println(System.currentTimeMillis() - start);
    }
}
