package com.mola.rpc.core.excutors.impl;

import com.mola.rpc.core.excutors.RpcExecutor;
import com.mola.rpc.core.excutors.task.RpcTask;
import com.mola.rpc.core.properties.RpcProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-09-13 00:25
 **/
public class SystemRpcProviderExecutor implements RpcExecutor {

    private static final Logger log = LoggerFactory.getLogger(SystemRpcProviderExecutor.class);

    /**
     * 线程池
     */
    private final ThreadPoolExecutor tp;

    /**
     * 阻塞队列
     */
    private final BlockingQueue<Runnable> blockingQueue;

    public SystemRpcProviderExecutor(RpcProperties rpcProperties){
        this.blockingQueue = new LinkedBlockingDeque<>(256);
        this.tp = new ThreadPoolExecutor(rpcProperties.getSystemThreadNum(), rpcProperties.getSystemThreadNum()
                ,200, TimeUnit.MILLISECONDS, blockingQueue, new ThreadFactory() {
            private final AtomicInteger threadIndex = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("system-provider-process-thread-%d", this.threadIndex.incrementAndGet()));
            }
        });
    }

    @Override
    public void process(RpcTask rpcTask) {
        tp.submit(rpcTask);
    }
}
