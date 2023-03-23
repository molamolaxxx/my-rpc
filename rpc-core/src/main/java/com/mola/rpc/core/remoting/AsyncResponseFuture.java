package com.mola.rpc.core.remoting;


import com.mola.rpc.core.remoting.protocol.RemotingCommand;
import com.mola.rpc.core.remoting.protocol.RemotingCommandCode;
import com.mola.rpc.core.util.BytesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * 异步请求应答封装
 */
public class AsyncResponseFuture<T> extends ResponseFuture {

    private static final Logger log = LoggerFactory.getLogger(AsyncResponseFuture.class);

    private Consumer<T> consumer;

    private long timeout;

    public AsyncResponseFuture(int opaque, long timeout) {
        super(opaque);
        this.timeout = timeout;
    }

    @Override
    public void putResponse(RemotingCommand responseCommand) {
        super.putResponse(responseCommand);
        // 服务端执行异常
        if (responseCommand.getCode() == RemotingCommandCode.SYSTEM_ERROR) {
            log.error("async invoke failed, server throw exception, client will not consume result! remark = " + responseCommand.getRemark());
            return;
        }
        if (null != consumer) {
            // response转换成对象
            consumer.accept((T) BytesUtil.bytesToObject(responseCommand.getBody(), method.getReturnType()));
        }
    }

    public T get() throws InterruptedException {
        RemotingCommand remotingCommand = getResponseCommand();
        if (getResponseCommand() == null) {
            remotingCommand = super.waitResponse(timeout);
        }
        if (remotingCommand == null) {
            return null;
        }
        // 服务端执行异常
        if (remotingCommand.getCode() == RemotingCommandCode.SYSTEM_ERROR) {
            throw new RuntimeException(remotingCommand.getRemark());
        }
        // response转换成对象
        return (T) BytesUtil.bytesToObject(remotingCommand.getBody(), method.getReturnType());
    }

    public void setConsumer(Consumer<T> consumer) {
        this.consumer = consumer;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
