package com.mola.rpc.consumer.test;

import com.mola.rpc.client.UnitTestService;
import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.common.entity.AddressInfo;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.core.remoting.netty.NettyRemoteClient;
import com.mola.rpc.data.config.manager.RpcDataManager;
import com.mola.rpc.data.config.spring.RpcProviderDataInitBean;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import com.mola.rpc.common.utils.AssertUtil;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

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
@SpringBootTest(classes = ConsumerTestContext.class)
@RunWith(SpringRunner.class)
public class ConsumerRunningTest {

    @Resource
    private RpcContext rpcContext;

    @Resource
    private UnitTestService unitTestService;

    @Resource
    private UnitTestService unitTestService2;

    @Resource
    private UnitTestService unitTestService3;

    @Resource
    private NettyRemoteClient nettyRemoteClient;

    @Resource
    private RpcProviderDataInitBean rpcProviderDataInitBean;

    @Before
    public void before() {
        RpcDataManager<RpcMetaData> rpcDataManager = rpcProviderDataInitBean.getRpcDataManager();
        AssertUtil.isTrue(rpcDataManager.isProviderAvailable("com.mola.rpc.client.UnitTestService", "default", "1.0.0", "public"), "UnitTestService的provider不存在，请检查是否启动");
        List<AddressInfo> remoteProviderAddress = rpcDataManager.getRemoteProviderAddress("com.mola.rpc.client.UnitTestService", "default", "1.0.0", "public");
        AssertUtil.notEmpty(remoteProviderAddress, "UnitTestService的provider不存在可用地址，请检查是否启动");
    }

    /**
     * spring环境下运行正常
     */
    @Test
    public void springEnvironmentRunningTest() {
        AssertUtil.notNull(rpcContext, "rpcContext is null!");
        AssertUtil.notNull(unitTestService, "unitTestService is null!");
        AssertUtil.notNull(nettyRemoteClient, "nettyRemoteClient is null!");
        Map<String, RpcMetaData> consumerMetaMap = rpcContext.getConsumerMetaMap();
        AssertUtil.notNull(consumerMetaMap, "consumerMetaMap为空，初始化可能有问题");
        AssertUtil.isTrue(consumerMetaMap.size() > 0, "consumerMetaMap大小不唯一，有可能没注册上");
        AssertUtil.isTrue(consumerMetaMap.containsKey("com.mola.rpc.client.UnitTestService:unitTestService"),
                "不包含测试服务unitTestService，consumerMetaMap可能存在key格式的修改，请注意");
        AssertUtil.isTrue(nettyRemoteClient.isStart(), "netty客户端未正常启动");
    }

    /**
     * 多客户端可注册
     */
    @Test
    public void multiConsumerCanRegister() {
        Map<String, RpcMetaData> consumerMetaMap = rpcContext.getConsumerMetaMap();
        AssertUtil.isTrue(consumerMetaMap.containsKey("com.mola.rpc.client.UnitTestService:unitTestService"),
                "不包含测试服务unitTestService，consumerMetaMap可能存在key格式的修改，请注意");
        AssertUtil.isTrue(consumerMetaMap.containsKey("com.mola.rpc.client.UnitTestService:unitTestService2"),
                "不包含测试服务unitTestService2，consumerMetaMap可能存在key格式的修改，请注意");
        AssertUtil.isTrue(consumerMetaMap.containsKey("com.mola.rpc.client.UnitTestService:unitTestService3"),
                "不包含测试服务unitTestService3，consumerMetaMap可能存在key格式的修改，请注意");
    }
}
