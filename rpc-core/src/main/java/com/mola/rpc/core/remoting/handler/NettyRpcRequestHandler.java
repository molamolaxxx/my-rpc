package com.mola.rpc.core.remoting.handler;

import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.core.properties.RpcProperties;
import com.mola.rpc.core.biz.BizProcessAsyncExecutor;
import com.mola.rpc.core.proto.ObjectFetcher;
import com.mola.rpc.core.proxy.InvokeMethod;
import com.mola.rpc.core.remoting.netty.ReverseInvokeChannelPool;
import com.mola.rpc.core.remoting.protocol.RemotingCommand;
import com.mola.rpc.core.remoting.protocol.RemotingCommandCode;
import com.mola.rpc.core.util.BytesUtil;
import com.mola.rpc.core.util.RemotingSerializableUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.net.SocketAddress;

/**
 * @author : molamola
 * @Project: InvincibleSchedulerEngine
 * @Description:
 * @date : 2020-09-11 16:55
 **/
@ChannelHandler.Sharable
public class NettyRpcRequestHandler extends SimpleChannelInboundHandler<RemotingCommand> {

    private static final Logger log = LoggerFactory.getLogger(NettyRpcRequestHandler.class);

    private static final String REVERSE_INVOKER_CLAZZ_NAME = "com.mola.rpc.core.system.SystemConsumer$ReverseInvokerCaller";

    private RpcContext rpcContext;

    private ObjectFetcher providerFetcher;

    private BizProcessAsyncExecutor bizProcessAsyncExecutor;

    public NettyRpcRequestHandler(RpcContext rpcContext, ObjectFetcher providerFetcher, RpcProperties rpcProperties) {
        this.rpcContext = rpcContext;
        this.providerFetcher = providerFetcher;
        this.bizProcessAsyncExecutor = new BizProcessAsyncExecutor(rpcProperties);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RemotingCommand request) throws Exception {
        if (request.isResponseType()) {
            ctx.fireChannelRead(request);
            return;
        }
        InvokeMethod invokeMethod = InvokeMethod.newInstance((String) BytesUtil.bytesToObject(request.getBody()));
        // 判断是否是系统内部调用
        if (REVERSE_INVOKER_CLAZZ_NAME.equals(invokeMethod.getInterfaceClazz())) {
            handleReverseInvokeCommand(invokeMethod, ctx.channel(),request);
            return;
        }
        RpcMetaData providerMeta = rpcContext.getProviderMeta(invokeMethod.getInterfaceClazz(), request.getGroup(), request.getVersion());
        Assert.notNull(providerMeta, "providerMeta not found");
        // 反射调用服务
        this.bizProcessAsyncExecutor.process(
                () -> {
                    // 前置过滤器
                    log.info("biz-process-thread:" + Thread.currentThread().getName());
                    RemotingCommand response = null;
                    // 反射调用
                    try {
                        Object providerBean = this.providerFetcher.getObject(providerMeta);
                        Object result = invokeMethod.invoke(providerBean);
                        response = buildRemotingCommand(request, result, RemotingCommandCode.SUCCESS, null);
                    } catch (Exception e) {
                        response = buildRemotingCommand(request, null, RemotingCommandCode.SYSTEM_ERROR, e.getMessage());
                        log.error("server system error!, message = " + request.toString(), e);
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
                        Throwable cause = future.cause();
                        // 返回结果失败
                        log.error("send a request command to channel <" + remoteAddress + "> failed. response = " + responseStr);
                    });
                }, providerMeta.getInFiber()
        );
    }

    private void handleReverseInvokeCommand(InvokeMethod invokeMethod, Channel channel, RemotingCommand request) {
        // 解析出reverseKey
        Object[] args = invokeMethod.fetchArgs();
        Assert.isTrue(args[0] instanceof String,"handleReverseInvokeCommand args[0] require String Type, " + invokeMethod.toString());
        String reverseKey = (String) args[0];
        // 写入连接池
        ReverseInvokeChannelPool.registerReverseInvokeChannel(reverseKey, channel);
        RemotingCommand response = buildRemotingCommand(request, null, RemotingCommandCode.SUCCESS, null);
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

    public void setProviderFetcher(ObjectFetcher providerFetcher) {
        this.providerFetcher = providerFetcher;
    }
}
