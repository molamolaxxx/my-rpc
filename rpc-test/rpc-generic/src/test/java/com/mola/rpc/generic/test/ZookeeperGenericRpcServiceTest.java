package com.mola.rpc.generic.test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mola.rpc.common.entity.GenericParam;
import com.mola.rpc.core.properties.RpcProperties;
import com.mola.rpc.core.proto.GenericRpcService;
import com.mola.rpc.core.proto.ProtoRpcConfigFactory;
import com.mola.rpc.core.proto.RpcInvoker;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.nio.file.AccessMode;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-02-28 19:43
 **/
public class ZookeeperGenericRpcServiceTest {

    @Before
    public void before() {
        // 配置
        RpcProperties rpcProperties = new RpcProperties();
        // 环境标
        rpcProperties.setEnvironment("public");
        ProtoRpcConfigFactory protoRpcConfigFactory = ProtoRpcConfigFactory.fetch();
        protoRpcConfigFactory.init(rpcProperties);
    }

    @Test
    public void AppointedGenericInvoke() {
        GenericRpcService genericService = null;
        if (this instanceof NacosGenericRpcServiceTest) {
            genericService = RpcInvoker.genericConsumer(
                    "com.mola.rpc.client.OrderService",
                    Lists.newArrayList("127.0.0.1:9013"));
        } else {
            genericService = RpcInvoker.genericConsumer(
                    "com.mola.rpc.client.OrderService",
                    Lists.newArrayList("127.0.0.1:9003"));
        }

        // #1
        Map<String, String> orderMap = Maps.newHashMap();
        orderMap.put("code", "generic code" + System.currentTimeMillis());
        // #2
        Map<String, String> operateUserMap = Maps.newHashMap();
        operateUserMap.put("id","generic id" + System.currentTimeMillis());
        operateUserMap.put("userName","generic userName" + System.currentTimeMillis());
        // #invoke
        Map<String, Object> res = (Map<String, Object>) genericService.invoke(
                "searchOrderGeneric",
                GenericParam.ofMap(orderMap, "com.mola.rpc.client.Order"),
                GenericParam.ofMap(operateUserMap, "com.mola.rpc.client.OperateUser"),
                GenericParam.ofInt(10)
        );
        // #result
        long code = (long) genericService.invoke("getOrderCode");
        long cost = System.currentTimeMillis() - code;
        Assert.isTrue( cost < 100, "genericInvokeWithZk case 0 failed!");
        for (int i = 0; i < 100; i++) {
            Object getServerAddress = genericService.invoke("getServerAddress");
            Assert.isTrue(getServerAddress instanceof String, "genericInvokeWithZk case 0 failed!");
            Assert.isTrue(((String) getServerAddress).contains(":"), "genericInvokeWithZk case 0 failed!");
            Assert.isTrue(((String) getServerAddress).contains(":9013")
                    || ((String) getServerAddress).contains(":9003"),"genericInvokeWithZk case 0 failed!" );
        }
    }

    @Test
    public void genericInvokeOfObject() {
        GenericRpcService genericService = RpcInvoker.genericConsumer(
                "com.mola.rpc.client.UnitTestService");
        //1、数组对象（暂不支持泛化调用）
//        // #1
//        Map<String, String>[] params1 = new Map[]{
//                new HashMap(){{put("id","genericInvokeOfObject#arr1");}},
//                new HashMap(){{put("id","genericInvokeOfObject#arr2");}}
//        };
//        // #2
//        Map<String, String>[] params2 = new Map[]{
//                new HashMap(){{put("id","genericInvokeOfObject#arr3");}},
//                new HashMap(){{put("id","genericInvokeOfObject#arr4");}}
//        };
//        // #invoke
//        Map<String, Object> res = (Map<String, Object>) genericService.invoke(
//                "test002",
//                GenericParam.ofObj(params1, "[Lcom.mola.rpc.client.Order;"),
//                GenericParam.ofObj(params2,"[Lcom.mola.rpc.client.Order;")
//        );
        //2、复杂对象
        // #1
        Map<String, Object> param = new HashMap<>();
        param.put("bigDecimal", new BigDecimal("3.14159"));
        Date cur = new Date();
        param.put("date", cur);
        param.put("accessMode", AccessMode.EXECUTE);
        param.put("operateUser", new HashMap(){{put("id","test-generic-id");
        put("userName","test-generic-name");}});
        Map<String, Object> res = (Map<String, Object>) genericService.invoke(
        "specialObjectTransform",
        GenericParam.ofMap(param, "com.mola.rpc.client.SpecialObject"));
        Assert.isTrue(new BigDecimal("3.14159").equals(res.get("bigDecimal")), "genericInvokeWithZk case 1 failed!");
        Assert.isTrue(cur.equals(res.get("date")), "genericInvokeWithZk case 1 failed!");
        Assert.isTrue(AccessMode.EXECUTE.equals(res.get("accessMode")), "genericInvokeWithZk case 1 failed!");
        Map operateUser = (Map)res.get("operateUser");
        Assert.isTrue("test-generic-id".equals(operateUser.get("id")), "genericInvokeWithZk case 1 failed!");
        Assert.isTrue("test-generic-name".equals(operateUser.get("userName")), "genericInvokeWithZk case 1 failed!");
    }
}