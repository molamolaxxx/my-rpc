package com.mola.rpc.core.remoting.netty;

import com.mola.rpc.core.remoting.AsyncResponseFuture;
import com.mola.rpc.core.remoting.ResponseFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 异步回调管理
 * @date : 2022-10-09 23:47
 **/
public class ResponseFutureManager {

    private static final Logger log = LoggerFactory.getLogger(ResponseFutureManager.class);

    /**
     * 缓存对外所有同步请求
     * opaque -> ResponseFuture
     */
    protected final Map<Integer, ResponseFuture> responseMap = new ConcurrentHashMap<>(256);

    /**
     * 缓存对外所有异步请求
     * opaque -> ResponseFuture
     */
    protected final Map<Integer, AsyncResponseFuture> asyncResponseMap = new ConcurrentHashMap<>(256);

    /**
     * 定时拉取服务端配置
     */
    private ScheduledExecutorService responseFutureMonitorService;

    public void initResponseFutureMonitor() {
        this.responseFutureMonitorService = Executors.newScheduledThreadPool(1,
                new ThreadFactory() {
                    AtomicInteger threadIndex = new AtomicInteger(0);
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "response-future-monitor-thread-" + this.threadIndex.incrementAndGet());
                    }
                });
        this.responseFutureMonitorService.scheduleAtFixedRate(() -> {
            // 同步map检查，大于一分钟删除
            delTimeoutFutures(responseMap, 60 * 1000);
            // 异步map检查，大于三分钟删除
            delTimeoutFutures(asyncResponseMap, 180 * 1000);
        },60, 60, TimeUnit.SECONDS);
    }

    /**
     * 删除超时的future
     * @param responseMap
     */
    private void delTimeoutFutures(Map<Integer, ? extends ResponseFuture> responseMap, Integer timeout) {
        long currentTime = System.currentTimeMillis();
        List<ResponseFuture> needDelFutures = new ArrayList<>();
        for (ResponseFuture future : responseMap.values()) {
            if (currentTime - future.getBeginTimestamp() > timeout) {
                needDelFutures.add(future);
            }
        }
        for (ResponseFuture needDelFuture : needDelFutures) {
            log.warn("task has been remove by future monitor! task is " + needDelFuture);
            responseMap.remove(needDelFuture.getOpaque());
        }
    }

    public void putSyncResponseFuture(Integer opaque, ResponseFuture responseFuture) {
        this.responseMap.put(opaque, responseFuture);
    }

    public void putAsyncResponseFuture(Integer opaque, AsyncResponseFuture responseFuture) {
        this.asyncResponseMap.put(opaque, responseFuture);
    }

    public ResponseFuture getSyncResponseFuture(Integer opaque) {
        return this.responseMap.get(opaque);
    }

    public AsyncResponseFuture getAsyncResponseFuture(Integer opaque) {
        return this.asyncResponseMap.get(opaque);
    }

    public void removeSyncResponseFuture(Integer opaque) {
        this.responseMap.remove(opaque);
    }

    public void removeAsyncResponseFuture(Integer opaque) {
        this.asyncResponseMap.remove(opaque);
    }
}
