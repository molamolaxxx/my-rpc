package com.mola.rpc.core.excutors.task;

import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.core.proto.ObjectFetcher;
import com.mola.rpc.core.proxy.InvokeMethod;
import com.mola.rpc.core.remoting.protocol.RemotingCommand;
import com.mola.rpc.core.util.BytesUtil;
import com.mola.rpc.core.util.RemotingSerializableUtil;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.net.SocketAddress;

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
        // todo 拦截器链执行
        this.runTask();
        // todo 后置处理器
    }

    protected Object runTask() {
        throw new NotImplementedException();
    }

    /**
     * 构建协议包
     * @param result 返回的结果
     * @return
     */
    protected RemotingCommand buildRemotingCommand(RemotingCommand request, Object result, int commandCode, String remark) {
        RemotingCommand response = RemotingCommand.createResponseCommand(commandCode, remark);
        response.setOpaque(request.getOpaque());
        // 1、构建body
        byte[] responseBody = null;
        try {
            responseBody = BytesUtil.objectToBytes(result);
        } catch (Throwable e) {
            log.error("[NettyServerHandler]: objectToBytes error"
                    + ", result:" + RemotingSerializableUtil.toJson(result, false), e);
            return null;
        }
        if(responseBody == null) {
            log.error("[NettyServerHandler]: responseBody is null"
                    + ", result:" + RemotingSerializableUtil.toJson(result, false));
            return null;
        }
        response.setCode(commandCode);
        response.setBody(responseBody);
        return response;
    }

    /**
     * 发送响应报文
     * @param response
     */
    protected void sendResponse(RemotingCommand response) {
        final String responseStr = response.toString();
        final SocketAddress remoteAddress = channel.remoteAddress();
        // 写入channel,发送返回到客户端
        channel.writeAndFlush(response).addListener(future -> {
            // 返回结果成功
            if (future.isSuccess()) {
                return;
            }
            Throwable cause = future.cause();
            // 返回结果失败
            log.warn("send a request command to channel <" + remoteAddress + "> failed. response = " + responseStr, cause);
        });
    }
}
