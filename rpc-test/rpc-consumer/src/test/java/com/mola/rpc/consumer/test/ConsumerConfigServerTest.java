package com.mola.rpc.consumer.test;

import com.mola.rpc.common.utils.JSONUtil;
import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.common.entity.AddressInfo;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.data.config.manager.RpcDataManager;
import com.mola.rpc.data.config.manager.zk.ZkRpcDataManager;
import com.mola.rpc.data.config.spring.RpcProviderDataInitBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import com.mola.rpc.common.utils.AssertUtil;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    protected RpcProviderDataInitBean rpcProviderDataInitBean;

    @Resource
    private RpcContext rpcContext;

    /**
     * 服务发现地址和configserver地址相同
     */
    @Test
    public void configServerAddressEqualsConsumerFind() {
        validateRpcDataManager();
        RpcDataManager<RpcMetaData> rpcDataManager = rpcProviderDataInitBean.getRpcDataManager();
        List<AddressInfo> remoteProviderAddress = rpcDataManager.getRemoteProviderAddress("com.mola.rpc.client.UnitTestService", "default", "1.0.0", "public");
        AssertUtil.notEmpty(remoteProviderAddress, "remoteProviderAddress为空，请检查cs启动情况和provider注册情况");
        // 判断和rpcContext的地址是否相同
        Map<String, RpcMetaData> consumerMetaMap = rpcContext.getConsumerMetaMap();
        consumerMetaMap.forEach((interfaceWithBeanName, rpcMetaData) -> {
            if (interfaceWithBeanName.contains("com.mola.rpc.client.UnitTestService")) {
                List<AddressInfo> addressList = rpcMetaData.getAddressList();
                AssertUtil.isTrue(addressList.size() == remoteProviderAddress.size(), "cs地址个数与本地不相同，请检查cs同步地址功能是否被修改");
                Set<String> addressSet = addressList.stream().map(e -> e.getAddress()).collect(Collectors.toSet());
                for (AddressInfo addressInfo : remoteProviderAddress) {
                    System.out.println(JSONUtil.toJSONString(addressSet) + ":" + addressInfo.getAddress());
                    AssertUtil.isTrue(addressSet.contains(addressInfo.getAddress()), "cs本地地址个数与远程不相同，请检查cs同步地址功能是否被修改");
                }
            }
        });
    }

    protected void validateRpcDataManager() {
        RpcDataManager<RpcMetaData> rpcDataManager = rpcProviderDataInitBean.getRpcDataManager();
        AssertUtil.isTrue(rpcDataManager instanceof ZkRpcDataManager, "rpcDataManager 类型错误");
        AssertUtil.isTrue(rpcDataManager.isProviderAvailable("com.mola.rpc.client.UnitTestService", "default", "1.0.0", "public"), "UnitTestService的provider不存在，请检查是否启动");
        List<AddressInfo> remoteProviderAddress = rpcDataManager.getRemoteProviderAddress("com.mola.rpc.client.UnitTestService", "default", "1.0.0", "public");
        AssertUtil.notEmpty(remoteProviderAddress, "UnitTestService的provider不存在可用地址，请检查是否启动");
        rpcProviderDataInitBean.pullProviderDataList();
    }
}
