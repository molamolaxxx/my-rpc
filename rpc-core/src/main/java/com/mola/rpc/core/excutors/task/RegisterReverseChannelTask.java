package com.mola.rpc.core.excutors.task;

import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.core.proto.ObjectFetcher;
import com.mola.rpc.core.proto.ProtoRpcConfigFactory;
import com.mola.rpc.core.proxy.InvokeMethod;
import com.mola.rpc.core.remoting.netty.pool.ChannelWrapper;
import com.mola.rpc.core.remoting.netty.pool.NettyConnectPool;
import com.mola.rpc.core.remoting.protocol.RemotingCommand;
import com.mola.rpc.core.remoting.protocol.RemotingCommandCode;
import com.mola.rpc.core.util.RemotingUtil;
import io.netty.channel.Channel;
import com.mola.rpc.common.utils.AssertUtil;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 注册反向代理channel的task
 * @date : 2023-04-17 00:01
 **/
public class RegisterReverseChannelTask extends RpcTask {

    public RegisterReverseChannelTask(ObjectFetcher providerObjFetcher, InvokeMethod invokeMethod,
                                      RemotingCommand request, RpcMetaData providerMeta, Channel channel) {
        super(providerObjFetcher, invokeMethod, request, providerMeta, channel);
    }

    @Override
    protected Object runTask() {
        // 解析出reverseKey
        Object[] args = invokeMethod.fetchArgs();
        AssertUtil.isTrue(args[0] instanceof String,"handleReverseInvokeCommand args[0] require String Type, " + invokeMethod.toString());
        String reverseKey = (String) args[0];
        NettyConnectPool nettyConnectPool = ProtoRpcConfigFactory.fetch().getNettyConnectPool();
        // 写入连接池
        nettyConnectPool.registerReverseInvokeChannel(reverseKey, ChannelWrapper.of(channel));
        if (request.isOnewayInvoke()) {
            return null;
        }
        // 构建响应
        RemotingCommand response = RemotingCommand.build(request, null, RemotingCommandCode.SUCCESS, null);
        // 发送响应
        RemotingUtil.sendResponse(response, channel);
        return null;
    }
}
