package com.mola.rpc.consumer.test;

import com.mola.rpc.client.UserService;
import com.mola.rpc.common.annotation.RpcProvider;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-01-23 15:42
 **/
@RpcProvider(interfaceClazz = UserService.class)
public class UserServiceTestImpl implements UserService {

    @Override
    public String queryUserName(String id) {
        return "test:" + id;
    }
}
