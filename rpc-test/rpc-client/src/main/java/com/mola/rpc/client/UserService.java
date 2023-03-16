package com.mola.rpc.client;

/**
 * 订单服务
 */
public interface UserService {

    /**
     * 查询用户姓名
     * @param id
     * @return
     */
    String queryUserName(String id);

    /**
     * 查询用户姓名
     * @param id
     * @return
     */
    String queryUserNameAsync(String id);
}
