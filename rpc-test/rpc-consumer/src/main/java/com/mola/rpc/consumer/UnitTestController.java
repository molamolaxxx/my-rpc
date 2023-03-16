package com.mola.rpc.consumer;

import com.mola.rpc.client.UnitTestService;
import com.mola.rpc.common.annotation.RpcConsumer;
import com.mola.rpc.core.remoting.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-07-30 18:35
 **/
@Controller
@ResponseBody
public class UnitTestController {

    @RpcConsumer(asyncMethods = "processDelayLongTime")
    private UnitTestService unitTestService;

    @RequestMapping("/processDelayLongTime/{seconds}")
    public String processDelayLongTime(@PathVariable Integer seconds) {
        long start =  System.currentTimeMillis();
        Async.from(unitTestService.processDelayLongTime(seconds))
                .consume((res) -> {
                    System.out.println("processDelayLongTime执行时间 : " + (System.currentTimeMillis() - start));
                });
        return "ok";
    }

}
