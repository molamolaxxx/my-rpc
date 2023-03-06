package com.mola.rpc.consumer.test.nacos;

import com.mola.rpc.consumer.test.ConsumerInvokeTest;
import com.mola.rpc.consumer.test.ConsumerTestContext;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 单测：
 * spring
 * 1、同步调用成功，检测参数类型
 * 2、同步调用失败，检测异常
 * 3、异步调用成功，检测参数类型
 * 4、异步调用失败，检测异常
 * 5、异步转同步，调用成功、失败，检测
 * proto同上
 * @date : 2023-01-22 16:43
 **/
@ActiveProfiles("nacos")
@SpringBootTest(classes = ConsumerTestContext.class)
@RunWith(SpringRunner.class)
public class NacosConsumerInvokeTest extends ConsumerInvokeTest {
}
