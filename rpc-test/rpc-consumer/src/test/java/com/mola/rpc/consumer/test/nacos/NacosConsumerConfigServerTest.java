package com.mola.rpc.consumer.test.nacos;

import com.mola.rpc.common.entity.AddressInfo;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.consumer.test.ConsumerConfigServerTest;
import com.mola.rpc.consumer.test.ConsumerTestContext;
import com.mola.rpc.data.config.manager.RpcDataManager;
import com.mola.rpc.data.config.manager.nacos.NacosRpcDataManager;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 单测：
 * 1、服务发现地址和configserver地址相同
 * @date : 2023-01-22 16:43
 **/
@ActiveProfiles("nacos")
@SpringBootTest(classes = ConsumerTestContext.class)
@RunWith(SpringRunner.class)
public class NacosConsumerConfigServerTest extends ConsumerConfigServerTest {

    @Before
    public void before() {
        RpcDataManager<RpcMetaData> rpcDataManager = rpcProviderDataInitBean.getRpcDataManager();
        Assert.isTrue(rpcDataManager instanceof NacosRpcDataManager, "rpcDataManager 类型错误");
        Assert.isTrue(rpcDataManager.isProviderExist("com.mola.rpc.client.UnitTestService", "default", "1.0.0", "pre"), "UnitTestService的provider不存在，请检查是否启动");
        List<AddressInfo> remoteProviderAddress = rpcDataManager.getRemoteProviderAddress("com.mola.rpc.client.UnitTestService", "default", "1.0.0", "pre");
        Assert.notEmpty(remoteProviderAddress, "UnitTestService的provider不存在可用地址，请检查是否启动");
    }
}
