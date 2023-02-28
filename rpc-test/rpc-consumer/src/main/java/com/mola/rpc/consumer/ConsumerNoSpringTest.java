package com.mola.rpc.consumer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mola.rpc.client.OperateUser;
import com.mola.rpc.client.Order;
import com.mola.rpc.client.OrderService;
import com.mola.rpc.client.ServerResponse;
import com.mola.rpc.common.entity.GenericParam;
import com.mola.rpc.core.proto.GenericRpcService;
import com.mola.rpc.core.proto.RpcInvoker;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-01-10 14:17
 **/
@Controller
@ResponseBody
public class ConsumerNoSpringTest {

    @GetMapping("/noSpring/queryOrderList")
    public List<Order> queryOrderList(@RequestParam Integer time) {
        OrderService orderService = RpcInvoker.consumer(OrderService.class);
        ServerResponse<List<Order>> res = orderService.searchOrderListWithUser(new Order(), new OperateUser("1", "mola"));
        return res.getData();
    }

    @GetMapping("/noSpring/searchOrderGeneric")
    public List<Order> searchOrderGeneric(@RequestParam Integer time) {
        OrderService orderService = RpcInvoker.consumer(OrderService.class);
        long orderCode = orderService.getOrderCode();
        Order order = new Order();
        order.setCode("" + orderCode);
        ServerResponse<List<Order>> res = orderService.searchOrderGeneric(order, new OperateUser("1", "mola"), time);
        return res.getData();
    }

    @GetMapping("/noSpring/appointed/queryOrderList")
    public List<Order> queryOrderListInAppointedMode() {
        OrderService orderService = RpcInvoker.consumer(OrderService.class, Lists.newArrayList("127.0.0.1:9004","127.0.0.1:9003"));
        ServerResponse<List<Order>> res = orderService.searchOrderListWithUser(new Order(), new OperateUser("1", "mola"));
        return res.getData();
    }

    @GetMapping("/noSpring/generic/queryOrderList")
    public ServerResponse queryOrderListInGenericMode() {
        GenericRpcService genericService = RpcInvoker.genericConsumer("com.mola.rpc.client.OrderService");
        Map<String, String> orderMap = Maps.newHashMap();
        orderMap.put("code", "generic code" + System.currentTimeMillis());
        Map<String, String> operateUserMap = Maps.newHashMap();
        operateUserMap.put("id","generic id" + System.currentTimeMillis());
        operateUserMap.put("userName","generic userName" + System.currentTimeMillis());
        ServerResponse invokeRes = (ServerResponse) genericService.invoke(
                "searchOrderGeneric",
                    GenericParam.ofMap(orderMap, "com.mola.rpc.client.Order"),
                    GenericParam.ofMap(operateUserMap, "com.mola.rpc.client.OperateUser"),
                    GenericParam.ofInt(10)
                );
//        Map invokeRes = genericService.invoke(
//                "searchOrderGeneric"
//        );
        return invokeRes;
    }

}
