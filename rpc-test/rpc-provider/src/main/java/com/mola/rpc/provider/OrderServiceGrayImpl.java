package com.mola.rpc.provider;

import com.mola.rpc.client.Order;
import com.mola.rpc.client.OrderService;
import com.mola.rpc.common.annotation.RpcProvider;
import com.mola.rpc.core.spring.RpcProperties;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-07-25 23:42
 **/
@RpcProvider(interfaceClazz = OrderService.class, version = "1.0.0", group = "gray")
public class OrderServiceGrayImpl implements OrderService {

    @Resource
    private RpcProperties rpcProperties;

    @Override
    public List<Order> queryOrderList(String code, List<String> idList) {
        List<Order> orders = new ArrayList<>();
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (String s : idList) {
            Order order = new Order();
            order.setCode("UM1111111");
            order.setId(s);
            order.setDesc("I am gray service!");
            orders.add(order);
        }
        return orders;
    }
}
