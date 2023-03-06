package com.mola.rpc.consumer.test.nacos;

import com.google.common.collect.Lists;
import com.mola.rpc.client.UnitTestService;
import com.mola.rpc.client.UserService;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.consumer.test.ConsumerTestContext;
import com.mola.rpc.consumer.test.proto.ReverseInvokeTest;
import com.mola.rpc.core.proto.RpcInvoker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import javax.annotation.Resource;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 反向调用测试
 * 客户端注册服务端
 * 服务端consumer调用客户端provider
 * @date : 2023-03-05 23:12
 **/
@SpringBootTest(classes = ConsumerTestContext.class)
@RunWith(SpringRunner.class)
@ActiveProfiles("nacos")
public class NacosReverseInvokeTest extends ReverseInvokeTest {

    @Resource
    private UnitTestService unitTestServiceAppointNacos;

    @Test
    public void test() throws InterruptedException {
        RpcMetaData rpcMetaData = new RpcMetaData();
        rpcMetaData.setReverseMode(Boolean.TRUE);
        rpcMetaData.setReverseModeConsumerAddress(Lists.newArrayList("127.0.0.1:9013"));
        rpcMetaData.setGroup("reverse_proto");
        RpcInvoker.provider(
                UserService.class,
                id -> "reverse-proto-mode-nacos-" + id,
                rpcMetaData
        );
        String id = System.currentTimeMillis() + "";
        String res = unitTestServiceAppointNacos.testReverseLoopBack(id);
        Assert.isTrue(("reverse-proto-mode-nacos-"+id).equals(res), "ReverseInvokeTest case 1 failed");
    }
}
