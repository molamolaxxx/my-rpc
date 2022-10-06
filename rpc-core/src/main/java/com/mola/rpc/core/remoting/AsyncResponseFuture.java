package com.mola.rpc.core.remoting;


import com.mola.rpc.core.remoting.protocol.RemotingCommand;
import com.mola.rpc.core.util.BytesUtil;
import com.mola.rpc.core.util.RemotingSerializableUtil;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 异步请求应答封装
 */
public class AsyncResponseFuture<T> extends ResponseFuture{

    private Consumer<T> consumer;

    private Supplier<Void> sender;

    /**
     * consumer调用方法
     */
    private Method method;

    public AsyncResponseFuture(int opaque) {
        super(opaque);
    }

    @Override
    public void putResponse(RemotingCommand responseCommand) {
        super.putResponse(responseCommand);
        if (null != consumer) {
            // response转换成对象
            String body = (String) BytesUtil.bytesToObject(responseCommand.getBody());
            Object res = RemotingSerializableUtil.fromJson(body, method.getReturnType());
            consumer.accept((T) res);
        }
    }

    public T get(long timeout) throws InterruptedException {
        RemotingCommand remotingCommand = super.waitResponse(timeout);
        // response转换成对象
        String body = (String) BytesUtil.bytesToObject(remotingCommand.getBody());
        Object res = RemotingSerializableUtil.fromJson(body, method.getReturnType());
        return (T) res;
    }

    public void setConsumer(Consumer<T> consumer) {
        this.consumer = consumer;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
