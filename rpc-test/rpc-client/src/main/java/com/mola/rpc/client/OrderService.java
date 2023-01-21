package com.mola.rpc.client;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;

/**
 * 订单服务
 */
public interface OrderService {

    /**
     * 查询订单列表
     * @param code
     * @return
     */
    List<Order> queryOrderList(String code, List<String> idList);

    /**
     * 保存订单
     * @param orderList
     * @return
     */
    Boolean saveOrder(List<Order> orderList);

    /**
     * 根据条件查询订单
     * @param order
     * @param operateUser
     * @return
     */
    default ServerResponse<List<Order>> searchOrderListWithUser(Order order, OperateUser operateUser) {
        throw new NotImplementedException();
    }

    default ServerResponse<List<Order>> searchOrderGeneric(Order order, OperateUser operateUser, int time) {
        throw new NotImplementedException();
    }

    default long getOrderCode() {
        throw new NotImplementedException();
    }

    default String getServerAddress() {
        throw new NotImplementedException();
    }
}
