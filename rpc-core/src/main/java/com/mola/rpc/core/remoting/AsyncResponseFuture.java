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

    private long timeout;

    public AsyncResponseFuture(int opaque, long timeout) {
        super(opaque);
        this.timeout = timeout;
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

    public T get() throws InterruptedException {
        RemotingCommand remotingCommand = getResponseCommand();
        if (null == getResponseCommand()) {
            remotingCommand = super.waitResponse(timeout);
        }
        if (null == remotingCommand) {
            return null;
        }
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

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
