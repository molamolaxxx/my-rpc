package com.mola.rpc.consumer;

import com.mola.rpc.common.utils.JSONUtil;
import com.google.common.collect.Lists;
import com.mola.rpc.client.OperateUser;
import com.mola.rpc.client.Order;
import com.mola.rpc.client.OrderService;
import com.mola.rpc.client.ServerResponse;
import com.mola.rpc.core.remoting.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-07-30 18:35
 **/
@Controller
@ResponseBody
public class OrderController {

    @Resource
    private OrderService orderService;

    @Resource
    private OrderService orderServiceGray;

//    @RpcConsumer(loadBalanceStrategy = LoadBalanceConstants.CONSISTENCY_HASHING_STRATEGY)
//    private OrderService orderService;
//
//    @RpcConsumer(loadBalanceStrategy = LoadBalanceConstants.CONSISTENCY_HASHING_STRATEGY, group = "gray",
//            asyncMethods = {"queryOrderList","searchOrderListWithUser"})
//    private OrderService orderServiceGray;

    @GetMapping("/queryOrderList")
    public List<Order> queryOrderList(@RequestParam Integer time) {
        List<String> idList = new ArrayList<>();
        for (int i = 0; i < time; i++) {
            idList.add(UUID.randomUUID().toString());
        }
        List<Order> test = orderService.queryOrderList("test", idList);
        Order order = test.get(0);
        return test;
    }

    @GetMapping("/queryOrder")
    public List<Order> queryOrder(@RequestParam String orderId) {
        if (orderId.contains("gray")) {
            Async.from(orderServiceGray.queryOrderList("test", Lists.newArrayList(orderId)))
                    .consume(list -> {
                        System.out.println(JSONUtil.toJSONString(list) + ":" + Thread.currentThread().getName());
                    });
        }
        return orderServiceGray.queryOrderList("test", Lists.newArrayList(orderId));
    }

    @GetMapping("/queryOrderAsync")
    public List<Order> queryOrderAsync(@RequestParam String orderId) throws InterruptedException {
//        Async.from(orderServiceGray.queryOrderList("test", Lists.newArrayList(orderId)))
//                .consume(list -> {
//                    System.out.println(Thread.currentThread());
//                    System.out.println(JSONUtil.toJSONString(list));
//                });
        long start = System.currentTimeMillis();
        Async<List<Order>> async = Async.from(orderServiceGray.queryOrderList("test", Lists.newArrayList(orderId)));
        System.out.println(System.currentTimeMillis() - start);
        List<Order> orders = async.get();
        System.out.println(System.currentTimeMillis() - start);
        return orders;
    }

    @GetMapping("/saveOrder")
    public Boolean saveOrder() throws InterruptedException {
        List<Order> orders = Lists.newArrayList();
        Order order = new Order();
        order.setDesc("aaaa");
        order.setId("1111");
        order.setCode("kkkk");
        orders.add(order);
        return orderService.saveOrder(orders);
    }

    @GetMapping("/searchOrderListWithUser")
    public ServerResponse<List<Order>> searchOrderListWithUser(@RequestParam String desc, @RequestParam String code, @RequestParam String userName, @RequestParam Boolean async) {
        Order order = new Order();
        order.setDesc(desc);
        order.setId(UUID.randomUUID().toString());
        order.setCode(code);
        ServerResponse<List<Order>> listServerResponse = null;
        if (async) {
            Async<ServerResponse<List<Order>>> from = Async.from(
                    orderServiceGray
                            .searchOrderListWithUser(order, new OperateUser(UUID.randomUUID().toString(), userName)));
            from.consume(response -> {
                List<Order> data = response.getData();
                for (Order datum : data) {
                    System.out.println(JSONUtil.toJSONString(datum));
                }
            });
            return ServerResponse.createBySuccess();
        } else {
            listServerResponse = orderService.searchOrderListWithUser(order, new OperateUser(UUID.randomUUID().toString(), userName));
        }
//        List<Order> data = listServerResponse.getData();
//        for (Order datum : data) {
//            System.out.println(JSONUtil.toJSONString(datum));
//        }
        return listServerResponse;
    }
}
