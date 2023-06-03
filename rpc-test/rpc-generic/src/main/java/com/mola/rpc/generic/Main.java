package com.mola.rpc.generic;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mola.rpc.common.entity.GenericParam;
import com.mola.rpc.core.properties.RpcProperties;
import com.mola.rpc.core.proto.GenericRpcService;
import com.mola.rpc.core.proto.ProtoRpcConfigFactory;
import com.mola.rpc.core.proto.RpcInvoker;

import java.util.Map;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-01-21 20:21
 **/
public class Main {

    public static void main(String[] args) {
        // 配置
        RpcProperties rpcProperties = new RpcProperties();
        // 环境标
        rpcProperties.setEnvironment("pre");
        // 如果只有只有单点调用，则可以开启，不启动configserver
//        rpcProperties.setStartConfigServer(false);
        ProtoRpcConfigFactory.fetch().init(rpcProperties);
        GenericRpcService genericService = RpcInvoker.genericConsumer("com.mola.rpc.client.OrderService", Lists.newArrayList("127.0.0.1:9004","127.0.0.1:9003"));
        Map<String, String> orderMap = Maps.newHashMap();
        orderMap.put("code", "generic code" + System.currentTimeMillis());
        Map<String, String> operateUserMap = Maps.newHashMap();
        operateUserMap.put("id","generic id" + System.currentTimeMillis());
        operateUserMap.put("userName","generic userName" + System.currentTimeMillis());
        Map<String, Object> res = (Map<String, Object>) genericService.invoke(
                "searchOrderGeneric",
                GenericParam.ofMap(orderMap, "com.mola.rpc.client.Order"),
                GenericParam.ofMap(operateUserMap, "com.mola.rpc.client.OperateUser"),
                GenericParam.ofInt(10)
        );
        long code = (long) genericService.invoke("getOrderCode");
        for (int i = 0; i < 100; i++) {
            System.out.println(genericService.invoke("getServerAddress"));
        }
        System.out.println("debug");
    }
}
