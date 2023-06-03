package com.mola.rpc.core.remoting.handler;

import com.alibaba.fastjson.JSONObject;
import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.core.excutors.RpcExecutor;
import com.mola.rpc.core.excutors.factory.RpcExecutorFactory;
import com.mola.rpc.core.excutors.factory.RpcTaskFactory;
import com.mola.rpc.core.proto.ObjectFetcher;
import com.mola.rpc.core.proto.ProtoRpcConfigFactory;
import com.mola.rpc.core.proxy.InvokeMethod;
import com.mola.rpc.core.remoting.protocol.RemotingCommand;
import com.mola.rpc.core.util.BytesUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2020-09-11 16:55
 **/
@ChannelHandler.Sharable
public class NettyRpcRequestHandler extends SimpleChannelInboundHandler<RemotingCommand> {

    private static final Logger log = LoggerFactory.getLogger(NettyRpcRequestHandler.class);

    private RpcContext rpcContext;

    private ObjectFetcher providerFetcher;

    private RpcExecutorFactory rpcExecutorFactory;

    private RpcTaskFactory rpcTaskFactory;

    public NettyRpcRequestHandler(RpcContext rpcContext, ObjectFetcher providerFetcher) {
        this.rpcContext = rpcContext;
        this.providerFetcher = providerFetcher;
        this.rpcExecutorFactory = ProtoRpcConfigFactory.fetch().getRpcExecutorFactory();
        this.rpcTaskFactory = ProtoRpcConfigFactory.fetch().getRpcTaskFactory();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RemotingCommand request) {
        // 此处是request处理器，如果是response类型则不处理
        if (request.isResponseType()) {
            ctx.fireChannelRead(request);
            return;
        }
        InvokeMethod invokeMethod = InvokeMethod.newInstance((String) BytesUtil.bytesToObject(request.getBody()));
        RpcMetaData providerMeta = rpcContext.getProviderMeta(invokeMethod.getInterfaceClazz(), invokeMethod.getGroup(), invokeMethod.getVersion());
        // 获取执行器
        RpcExecutor executor = rpcExecutorFactory.getProviderExecutor(invokeMethod, providerMeta);
        if (Objects.isNull(executor)) {
            throw new RuntimeException("executor not found, invokeMethod is " + JSONObject.toJSONString(invokeMethod));
        }
        // 反射调用服务
        executor.process(rpcTaskFactory.getTask(
                providerFetcher,
                invokeMethod,
                request,
                providerMeta,
                ctx.channel()
        ));
    }

    public void setProviderFetcher(ObjectFetcher providerFetcher) {
        this.providerFetcher = providerFetcher;
    }
}
