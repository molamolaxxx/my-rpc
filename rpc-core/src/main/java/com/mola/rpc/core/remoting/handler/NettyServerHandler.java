package com.mola.rpc.core.remoting.handler;

import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.core.biz.BizProcessAsyncExecutor;
import com.mola.rpc.core.proxy.InvokeMethod;
import com.mola.rpc.core.remoting.protocol.RemotingCommand;
import com.mola.rpc.core.remoting.protocol.RemotingCommandCode;
import com.mola.rpc.core.spring.RpcProperties;
import com.mola.rpc.core.util.BytesUtil;
import com.mola.rpc.core.util.RemotingSerializableUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import java.net.SocketAddress;

/**
 * @author : molamola
 * @Project: InvincibleSchedulerEngine
 * @Description:
 * @date : 2020-09-11 16:55
 **/
public class NettyServerHandler extends SimpleChannelInboundHandler<RemotingCommand> {

    private static final Logger log = LoggerFactory.getLogger(NettyServerHandler.class);

    private RpcContext rpcContext;

    private ApplicationContext applicationContext;

    private BizProcessAsyncExecutor bizProcessAsyncExecutor;

    public NettyServerHandler(RpcContext rpcContext, ApplicationContext applicationContext, RpcProperties rpcProperties) {
        this.rpcContext = rpcContext;
        this.applicationContext = applicationContext;
        this.bizProcessAsyncExecutor = new BizProcessAsyncExecutor(rpcProperties);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RemotingCommand request) throws Exception {
        InvokeMethod invokeMethod = InvokeMethod.newInstance((String) BytesUtil.bytesToObject(request.getBody()));
        RpcMetaData providerMeta = rpcContext.getProviderMeta(invokeMethod.getInterfaceClazz(), request.getGroup(), request.getVersion());
        Assert.notNull(providerMeta, "providerMeta not found");
        // 反射调用服务
        this.bizProcessAsyncExecutor.process(
                () -> {
                    // todo 前置过滤器
                    log.warn("biz-process-thread:" + Thread.currentThread().getName());
                    RemotingCommand response = null;
                    // 反射调用
                    try {
                        Object providerBean = this.applicationContext.getBean(providerMeta.getProviderBeanName());
                        Object result = invokeMethod.invoke(providerBean);
                        response = buildRemotingCommand(request, result, RemotingCommandCode.SUCCESS, null);
                    } catch (Exception e) {
                        response = buildRemotingCommand(request, null, RemotingCommandCode.SYSTEM_ERROR, e.getMessage());
                        log.error("server system error !, message = " + request.toString(), e);
                    }
                    Assert.notNull(response, "response is null" + request.toString());
                    Channel channel = ctx.channel();
                    final String responseStr = response.toString();
                    final SocketAddress remoteAddress = channel.remoteAddress();
                    // 写入channel,发送返回到客户端
                    channel.writeAndFlush(response).addListener(future -> {
                        // 返回结果成功
                        if (future.isSuccess()) {
                            return;
                        }
                        // 返回结果失败
                        log.warn("send a request command to channel <" + remoteAddress + "> failed. response = " + responseStr);
                    });
                }, providerMeta.getInFiber()
        );


    }
    /**
     * 构建协议包
     * @param result 返回的结果
     * @return
     */
    private RemotingCommand buildRemotingCommand(RemotingCommand request, Object result, int commandCode, String remark) {
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
        if(null == responseBody) {
            log.error("[NettyServerHandler]: responseBody is null"
                    + ", result:" + RemotingSerializableUtil.toJson(result, false));
            return null;
        }
        response.setCode(commandCode);
        response.setBody(responseBody);

        return response;
    }

}
