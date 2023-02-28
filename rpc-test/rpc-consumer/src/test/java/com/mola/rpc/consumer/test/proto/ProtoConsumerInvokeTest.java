package com.mola.rpc.consumer.test.proto;

import com.google.common.collect.Sets;
import com.mola.rpc.client.UnitTestService;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.consumer.test.ConsumerInvokeTest;
import com.mola.rpc.core.properties.RpcProperties;
import com.mola.rpc.core.proto.ProtoRpcConfigFactory;
import com.mola.rpc.core.proto.RpcInvoker;
import org.junit.Before;

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
public class ProtoConsumerInvokeTest extends ConsumerInvokeTest {

    @Before
    public void before() {
        // 配置
        RpcProperties rpcProperties = new RpcProperties();
        // 环境标
        rpcProperties.setEnvironment("pre");
        if (ProtoRpcConfigFactory.INIT_FLAG.get()) {
            ProtoRpcConfigFactory.configure(rpcProperties);
        } else {
            ProtoRpcConfigFactory.init(rpcProperties);
        }
        RpcMetaData asyncMetaData = new RpcMetaData();
        asyncMetaData.setAsyncExecuteMethods(Sets.newHashSet("*"));
        this.unitTestServiceAsync = RpcInvoker.consumer(UnitTestService.class, asyncMetaData, "unitTestServiceAsync");
        this.unitTestService = RpcInvoker.consumer(UnitTestService.class);
        this.rpcProviderDataInitBean = ProtoRpcConfigFactory.get().getRpcProviderDataInitBean();
    }
}
