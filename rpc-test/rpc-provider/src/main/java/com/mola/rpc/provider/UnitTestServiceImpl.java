package com.mola.rpc.provider;

import com.google.common.collect.Lists;
import com.mola.rpc.client.Order;
import com.mola.rpc.client.ServerResponse;
import com.mola.rpc.client.UnitTestService;
import com.mola.rpc.client.UserService;
import com.mola.rpc.common.annotation.RpcProvider;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 单元测试provider
 * @date : 2023-01-22 21:41
 **/
@RpcProvider(interfaceClazz = UnitTestService.class)
public class UnitTestServiceImpl implements UnitTestService {

    @Resource
    private UserService userService;

    @Override
    public ServerResponse<String> test001(String input) {
        return ServerResponse.createBySuccess(input);
    }

    @Override
    public ServerResponse<String> test001(String input1, String input2) {
        return ServerResponse.createBySuccess(input1 + input2);
    }

    @Override
    public ServerResponse<String> test001(String input1, String input2, String[] input3) {
        return ServerResponse.createBySuccess(input1 + input2 + String.join("", input3));
    }

    @Override
    public ServerResponse<Order> test002(Order order) {
        return ServerResponse.createBySuccess(order);
    }

    @Override
    public ServerResponse<Order> test002(Order order, String code) {
        order.setCode(code);
        return ServerResponse.createBySuccess(order);
    }

    @Override
    public ServerResponse<List<Order>> test002(Order order1, Order order2, String code1, String code2) {
        order1.setCode(code1);
        order2.setCode(code2);
        List<Order> orderList = Lists.newArrayList(order1, order2);
        return ServerResponse.createBySuccess(orderList);
    }

    @Override
    public ServerResponse<List<Order>> test002(Order[] order1, Order[] order2) {
        List<Order> order1List = Lists.newArrayList(order1);
        order1List.addAll(Lists.newArrayList(order2));
        return ServerResponse.createBySuccess(order1List);
    }

    @Override
    public int test003(int param) {
        return param + 1;
    }

    @Override
    public char[] test003(int param1, char param2, long param3, short param4, boolean param5) {
        String res = String.join(";", param1 + "", param2 + "", param3 + "", param4 + "", param5 + "");
        return res.toCharArray();
    }

    @Override
    public int test003(int[] paramArr) {
        int res = 0;
        for (int i : paramArr) {
            res += i;
        }
        return res;
    }

    @Override
    public int[][] test003(int[][] paramArr, int x, int y, int data) {
        paramArr[x][y] = data;
        return paramArr;
    }

    @Override
    public ServerResponse throwException() {
        throw new RuntimeException("throwException:服务端抛出运行时异常");
    }

    @Override
    public ServerResponse<Order> loopBackTest(Order order) {
        String username = userService.queryUserName(order.getId());
        order.setOperator(username);
        return ServerResponse.createBySuccess(order);
    }
}
