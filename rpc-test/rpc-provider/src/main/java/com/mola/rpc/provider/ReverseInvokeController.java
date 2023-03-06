package com.mola.rpc.provider;

import com.mola.rpc.client.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
public class ReverseInvokeController {

    @Resource
    private UserService userServiceReverse;

    @GetMapping("/reverse")
    public String queryOrderList(@RequestParam Integer time) {
        String test = userServiceReverse.queryUserName("test");
        return test;
    }
}
