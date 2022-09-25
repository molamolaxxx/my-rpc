package com.mola.rpc.client;

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
}
