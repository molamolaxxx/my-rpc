package com.mola.rpc.consumer.test;

import com.mola.rpc.client.UserService;
import com.mola.rpc.common.annotation.RpcProvider;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-01-23 15:42
 **/
@RpcProvider(interfaceClazz = UserService.class, reverseMode = true,
        group = "reverse_spring", reverseModeConsumerAddress = {"127.0.0.1:9003", "127.0.0.1:9013"})
public class UserServiceReverseTestImpl implements UserService {

    @Override
    public String queryUserName(String id) {
        return "reverse-spring-mode-" + id;
    }

    @Override
    public String queryUserNameAsync(String id) {
        return "reverse-spring-mode-async-" + id;
    }
}
