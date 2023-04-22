package com.mola.rpc.core.excutors;

import com.mola.rpc.core.excutors.task.RpcTask;

public interface RpcExecutor {

    /**
     * 执行器执行方法
     * @param rpcTask
     */
    void process(RpcTask rpcTask);
}
