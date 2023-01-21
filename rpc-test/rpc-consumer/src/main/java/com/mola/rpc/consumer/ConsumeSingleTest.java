package com.mola.rpc.consumer;

import com.google.common.collect.Lists;
import com.mola.rpc.client.OperateUser;
import com.mola.rpc.client.Order;
import com.mola.rpc.client.OrderService;
import com.mola.rpc.client.ServerResponse;
import com.mola.rpc.core.properties.RpcProperties;
import com.mola.rpc.core.proto.ProtoRpcConfigFactory;
import com.mola.rpc.core.proto.RpcInvoker;

import java.util.List;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 单点调用测试
 * @date : 2022-10-23 19:41
 **/
public class ConsumeSingleTest {
    public static void main(String[] args) {
        RpcProperties rpcProperties = new RpcProperties();
        rpcProperties.setEnvironment("pre");
        rpcProperties.setStartConfigServer(false);
        ProtoRpcConfigFactory.configure(rpcProperties);
        ProtoRpcConfigFactory protoRpcConfigFactory = ProtoRpcConfigFactory.get();

        OrderService orderService = RpcInvoker.appointedAddressConsumer(OrderService.class, Lists.newArrayList("127.0.0.1:9003"));

        ServerResponse<List<Order>> res = orderService.searchOrderListWithUser(new Order(), new OperateUser("1", "mola"));
        System.out.println("debug");

    }
}
