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
import com.mola.rpc.core.util.RemotingSerializableUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author : molamola
 * @Project: InvincibleSchedulerEngine
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
        this.rpcExecutorFactory = ProtoRpcConfigFactory.get().getRpcExecutorFactory();
        this.rpcTaskFactory = ProtoRpcConfigFactory.get().getRpcTaskFactory();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RemotingCommand request) {
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
        if(responseBody == null) {
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
