package com.mola.rpc.core.excutors.impl;

import co.paralleluniverse.fibers.Fiber;
import com.mola.rpc.core.excutors.RpcExecutor;
import com.mola.rpc.core.excutors.task.RpcTask;
import com.mola.rpc.core.properties.RpcProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 使用纤程执行方法
 * @date : 2022-09-13 00:25
 **/
public class FiberRpcProviderExecutor implements RpcExecutor {

    private static final Logger log = LoggerFactory.getLogger(FiberRpcProviderExecutor.class);

    private RpcProperties rpcProperties;

    public FiberRpcProviderExecutor(RpcProperties rpcProperties){
        this.rpcProperties = rpcProperties;
    }

    @Override
    public void process(RpcTask rpcTask) {
        log.info("thread:" + Thread.currentThread().getName());
        Fiber fiber = new Fiber<>(rpcTask::run);
        fiber.start();
    }
}
