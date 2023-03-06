package com.mola.rpc.consumer.test.nacos;

import com.google.common.collect.Sets;
import com.mola.rpc.client.UnitTestService;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.consumer.test.ConsumerTestContext;
import com.mola.rpc.consumer.test.proto.ProtoConsumerInvokeTest;
import com.mola.rpc.core.proto.ProtoRpcConfigFactory;
import com.mola.rpc.core.proto.RpcInvoker;
import org.junit.Before;
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
@SpringBootTest(classes = ConsumerTestContext.class)
@RunWith(SpringRunner.class)
@ActiveProfiles("nacos")
public class NacosProtoConsumerInvokeTest extends ProtoConsumerInvokeTest {

    @Before
    public void before() {
        RpcMetaData asyncMetaData = new RpcMetaData();
        asyncMetaData.setAsyncExecuteMethods(Sets.newHashSet("*"));
        this.unitTestServiceAsync = RpcInvoker.consumer(UnitTestService.class, asyncMetaData, "unitTestServiceAsync");
        this.unitTestService = RpcInvoker.consumer(UnitTestService.class);
        this.rpcProviderDataInitBean = ProtoRpcConfigFactory.get().getRpcProviderDataInitBean();
    }
}
