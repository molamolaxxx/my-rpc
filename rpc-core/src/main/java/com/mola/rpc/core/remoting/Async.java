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
     * 单次异步调用的future
     */
    private AsyncResponseFuture asyncResponseFuture;

    public Async(AsyncResponseFuture asyncResponseFuture) {
        this.asyncResponseFuture = asyncResponseFuture;
    }

    /**
     * 生成异步器
     * @param result
     * @param <T>
     * @return
     */
    public static <T> Async<T> from(T result) {
        try {
            AsyncResponseFuture asyncResponseFuture = asyncFutureThreadLocal.get();
            Assert.notNull(asyncResponseFuture, "responseFuture is null, please check if this method is an async method!");
            return new Async(asyncResponseFuture);
        } finally {
            asyncFutureThreadLocal.remove();
        }
    }

    public static void addFuture(AsyncResponseFuture responseFuture) {
        asyncFutureThreadLocal.set(responseFuture);
    }

    /**
     * 注册监听器
     * 如果consumer执行时间小于1ms，可能会导致putResponse在setConsumer之前，不执行consumer的内容。可以使用get获取结果
     * @param consumer
     */
    public void consume(Consumer<T> consumer) {
        AsyncResponseFuture<T> responseFuture = this.asyncResponseFuture;
        responseFuture.setConsumer(consumer);
        responseFuture.processConsumerIfReady();
    }

    public T get() {
        try {
            AsyncResponseFuture<T> responseFuture = this.asyncResponseFuture;
            return responseFuture.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
