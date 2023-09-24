package com.mola.rpc.core.excutors.task;

import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.core.proto.ObjectFetcher;
import com.mola.rpc.core.proxy.InvokeMethod;
import com.mola.rpc.core.remoting.protocol.RemotingCommand;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public abstract class RpcTask implements Runnable {

    protected static final Logger log = LoggerFactory.getLogger(RpcTask.class);

    protected ObjectFetcher providerObjFetcher;

    protected InvokeMethod invokeMethod;

    protected RemotingCommand request;

    protected RpcMetaData providerMeta;

    protected Channel channel;

    public RpcTask(ObjectFetcher providerObjFetcher, InvokeMethod invokeMethod, RemotingCommand request, RpcMetaData providerMeta, Channel channel) {
        this.providerObjFetcher = providerObjFetcher;
        this.invokeMethod = invokeMethod;
        this.request = request;
        this.providerMeta = providerMeta;
        this.channel = channel;
    }


    @Override
    public void run() {
        log.info("thread:" + Thread.currentThread().getName());
        // todo 拦截器链执行
        runTask();
        // todo 后置处理器
    }

    protected Object runTask() {
        throw new NotImplementedException();
    }

}
