package com.mola.rpc.consumer;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.mola.rpc.client.Order;
import com.mola.rpc.client.OrderService;
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

    @GetMapping("/queryOrderList")
    public List<Order> queryOrderList(@RequestParam Integer time) {
        List<String> idList = new ArrayList<>();
        for (int i = 0; i < time; i++) {
            idList.add(UUID.randomUUID().toString());
        }
        return orderService.queryOrderList("test", idList);
    }

    @GetMapping("/queryOrder")
    public List<Order> queryOrder(@RequestParam String orderId) {
        if (orderId.contains("gray")) {
            Async.from(orderServiceGray.queryOrderList("test", Lists.newArrayList(orderId)))
                    .register(list -> {
                        System.out.println(JSONObject.toJSONString(list) + ":" + Thread.currentThread().getName());
                    });
        }
        return orderServiceGray.queryOrderList("test", Lists.newArrayList(orderId));
    }

    @GetMapping("/queryOrderAsync")
    public List<Order> queryOrderAsync(@RequestParam String orderId) {
        Async.from(orderServiceGray.queryOrderList("test", Lists.newArrayList(orderId)))
                .register(list -> {
                    System.out.println(Thread.currentThread());
                    System.out.println(JSONObject.toJSONString(list));
                });
        return Lists.newArrayList();
    }
}
