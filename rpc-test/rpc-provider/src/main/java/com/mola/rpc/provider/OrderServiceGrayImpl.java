package com.mola.rpc.provider;

import com.alibaba.fastjson.JSONObject;
import com.mola.rpc.client.OperateUser;
import com.mola.rpc.client.Order;
import com.mola.rpc.client.OrderService;
import com.mola.rpc.client.ServerResponse;
import com.mola.rpc.common.annotation.RpcProvider;
import com.mola.rpc.core.properties.RpcProperties;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
            Thread.sleep(3000);
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

    @Override
    public Boolean saveOrder(List<Order> orderList) {
        for (Order order : orderList) {
            System.out.println(JSONObject.toJSONString(order));
        }
        return Boolean.TRUE;
    }

    @Override
    public ServerResponse<List<Order>> searchOrderListWithUser(Order order, OperateUser operateUser) {
        try {
            List<Order> orders = new ArrayList<>();
            orders.add(new Order(UUID.randomUUID().toString() + "-async", new Date(), order.getDesc(), order.getCode()));
            orders.add(new Order(UUID.randomUUID().toString() + "-async", new Date(), order.getDesc(), order.getCode()));
            orders.add(new Order(UUID.randomUUID().toString() + "-async", new Date(), order.getDesc(), order.getCode()));
            orders.add(new Order(UUID.randomUUID().toString() + "-async", new Date(), order.getDesc(), order.getCode()));
            orders.add(new Order(UUID.randomUUID().toString() + "-async", new Date(), order.getDesc(), order.getCode()));
            for (Order o : orders) {
                o.setOperator(operateUser.getUserName());
            }
            return ServerResponse.createBySuccess(orders);
        } catch (Exception e) {
            return ServerResponse.createByErrorCodeMessage(500, e.getMessage());
        }
    }
}
