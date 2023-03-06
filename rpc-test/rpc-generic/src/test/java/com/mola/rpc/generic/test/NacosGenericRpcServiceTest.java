package com.mola.rpc.generic.test;

import com.mola.rpc.common.constants.CommonConstants;
import com.mola.rpc.core.properties.RpcProperties;
import com.mola.rpc.core.proto.ProtoRpcConfigFactory;
import com.mola.rpc.data.config.manager.nacos.NacosRpcDataManager;
import org.junit.Before;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-02-28 19:43
 **/
public class NacosGenericRpcServiceTest extends ZookeeperGenericRpcServiceTest{

    @Before
    public void before() {
        // 配置
        RpcProperties rpcProperties = new RpcProperties();
        // 环境标
        rpcProperties.setEnvironment("public");
        rpcProperties.setRpcDataManager(new NacosRpcDataManager(rpcProperties));
        rpcProperties.setConfigServerType(CommonConstants.NACOS);
        rpcProperties.setConfigServerAddress("127.0.0.1:8848");
        ProtoRpcConfigFactory protoRpcConfigFactory = ProtoRpcConfigFactory.get();
        protoRpcConfigFactory.init(rpcProperties);
    }
}