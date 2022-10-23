package com.mola.rpc.core.biz;

import co.paralleluniverse.fibers.Fiber;
import com.mola.rpc.common.properties.RpcProperties;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-09-13 00:25
 **/
public class BizProcessAsyncExecutor {

    /**
     * 业务线程池
     */
    private ThreadPoolExecutor bizProcessThreadPool;

    /**
     * 阻塞队列
     */
    private BlockingQueue<Runnable> blockingQueue;

    public BizProcessAsyncExecutor(RpcProperties rpcProperties){
        this.blockingQueue = new LinkedBlockingDeque<>(rpcProperties.getMaxBlockingQueueSize());
        this.bizProcessThreadPool = new ThreadPoolExecutor(rpcProperties.getCoreBizThreadNum(),rpcProperties.getMaxBizThreadNum()
                ,200, TimeUnit.MILLISECONDS, blockingQueue, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("biz-process-thread-%d", this.threadIndex.incrementAndGet()));
            }
        });
    }

    /**
     * 在纤程中执行
     * @param runnable
     */
    public void process(Runnable runnable, boolean inFiber) {
        if (inFiber) {
            Fiber fiber = new Fiber<>(runnable::run);
            fiber.start();
            return;
        }
        this.bizProcessThreadPool.submit(runnable);
    }
}
