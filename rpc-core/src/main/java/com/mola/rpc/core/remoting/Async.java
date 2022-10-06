package com.mola.rpc.core.remoting;

import org.springframework.util.Assert;

import java.util.function.Consumer;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 异步调用器
 * @date : 2022-10-06 15:34
 **/
public class Async<T> {

    private static final ThreadLocal<AsyncResponseFuture> asyncFutureThreadLocal = new ThreadLocal<>();

    /**
     * 生成异步器
     * @param result
     * @param <T>
     * @return
     */
    public static <T> Async<T> from(T result) {
        if (null != result) {
            throw new RuntimeException("please check if this method is an async method!");
        }
        return new Async();
    }

    public static void addFuture(AsyncResponseFuture responseFuture) {
        asyncFutureThreadLocal.set(responseFuture);
    }

    /**
     * 注册监听器
     * @param consumer
     */
    public void register(Consumer<T> consumer) {
        try {
            AsyncResponseFuture<T> responseFuture = asyncFutureThreadLocal.get();
            Assert.notNull(responseFuture, "responseFuture is null");
            responseFuture.setConsumer(consumer);
        } finally {
            asyncFutureThreadLocal.remove();
        }
    }

    public T sync(long timeout) {
        try {
            AsyncResponseFuture<T> responseFuture = asyncFutureThreadLocal.get();
            Assert.notNull(responseFuture, "responseFuture is null");
            return responseFuture.get(timeout);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            asyncFutureThreadLocal.remove();
        }
    }
}
