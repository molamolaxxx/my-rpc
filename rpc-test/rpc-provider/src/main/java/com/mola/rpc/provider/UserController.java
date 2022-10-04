package com.mola.rpc.provider;

import com.mola.rpc.client.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-07-30 18:35
 **/
@Controller
@ResponseBody
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RpcConsumerConfig rpcConsumerConfig;

    @GetMapping("/queryUserName/{id}")
    public String queryUserName(@PathVariable String id) {
        return userService.queryUserName(id);
    }
}
