package com.mola.rpc.core.remoting;


import com.mola.rpc.core.remoting.protocol.RemotingCommand;
import com.mola.rpc.core.util.BytesUtil;

import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * 异步请求应答封装
 */
public class AsyncResponseFuture<T> extends ResponseFuture{

    private Consumer<T> consumer;

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
            consumer.accept((T) BytesUtil.bytesToObject(responseCommand.getBody()));
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
        return (T) BytesUtil.bytesToObject(remotingCommand.getBody());
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
