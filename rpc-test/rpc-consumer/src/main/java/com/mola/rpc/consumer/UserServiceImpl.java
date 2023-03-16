package com.mola.rpc.consumer;

import com.mola.rpc.client.UserService;
import com.mola.rpc.common.annotation.RpcProvider;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-07-25 23:42
 **/
@RpcProvider(interfaceClazz = UserService.class)
public class UserServiceImpl implements UserService {

    @Override
    public String queryUserName(String id) {
        return "mola:" + id;
    }

    @Override
    public String queryUserNameAsync(String id) {
        return null;
    }
}
