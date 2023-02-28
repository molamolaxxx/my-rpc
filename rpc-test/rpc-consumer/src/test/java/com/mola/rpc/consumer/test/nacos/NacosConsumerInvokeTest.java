package com.mola.rpc.consumer.test.nacos;

import com.mola.rpc.consumer.test.ConsumerInvokeTest;
import org.springframework.test.context.ActiveProfiles;

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
public class NacosConsumerInvokeTest extends ConsumerInvokeTest {
}
