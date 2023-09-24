package com.mola.rpc.core.remoting.handler;

import com.mola.rpc.core.remoting.netty.NettyRemoteClient;
import com.mola.rpc.core.remoting.protocol.RemotingCommand;
import com.mola.rpc.core.remoting.protocol.RemotingCommandCode;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mola.rpc.common.utils.AssertUtil;

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
        // 直接在consumer端抛出异常
        if (!msg.crc32Check()) {
            log.error("response crc32 check failed, msg = " + msg);
            msg.setCode(RemotingCommandCode.SYSTEM_ERROR);
            msg.setRemark("response crc32 check failed");
        }
        AssertUtil.notNull(nettyRemoteClient, "require nettyRemoteClient");
        this.nettyRemoteClient.putResponse(msg);
    }

    public void setNettyRemoteClient(NettyRemoteClient nettyRemoteClient) {
        this.nettyRemoteClient = nettyRemoteClient;
    }
}
