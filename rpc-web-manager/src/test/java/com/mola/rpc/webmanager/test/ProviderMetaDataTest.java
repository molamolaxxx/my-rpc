package com.mola.rpc.webmanager.test;

import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.data.config.manager.RpcDataManager;
import com.mola.rpc.data.config.spring.RpcProviderDataInitBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-01-24 22:06
 **/
@SpringBootTest(classes = {CsTestContext.class})
@RunWith(SpringRunner.class)
public class ProviderMetaDataTest {

    @Resource
    private RpcProviderDataInitBean rpcProviderDataInitBean;

    @Test
    public void getAll() {
        RpcDataManager<RpcMetaData> rpcDataManager = rpcProviderDataInitBean.getRpcDataManager();
        Map<String, RpcMetaData> allProviderMetaData = rpcDataManager.getAllProviderMetaData();
        Assert.notEmpty(allProviderMetaData, "getAll-case1测试失败");
    }
}
