package com.mola.rpc.core.remoting.handler;

import com.mola.rpc.core.remoting.netty.NettyRemoteClient;
import com.mola.rpc.core.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2020-09-11 16:55
 **/
@ChannelHandler.Sharable
public class NettyRpcResponseHandler extends SimpleChannelInboundHandler<RemotingCommand> {

    private static final Logger log = LoggerFactory.getLogger(NettyRpcResponseHandler.class);

    private NettyRemoteClient nettyRemoteClient;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RemotingCommand msg) throws Exception {
        // 此处是response处理器，如果是request类型则不处理
        if (!msg.isResponseType()) {
            ctx.fireChannelRead(msg);
            return;
        }
        Assert.notNull(nettyRemoteClient, "require nettyRemoteClient");
        this.nettyRemoteClient.putResponse(msg);
    }

    public void setNettyRemoteClient(NettyRemoteClient nettyRemoteClient) {
        this.nettyRemoteClient = nettyRemoteClient;
    }
}
