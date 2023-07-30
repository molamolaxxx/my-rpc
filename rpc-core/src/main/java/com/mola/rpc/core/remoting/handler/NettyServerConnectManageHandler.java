package com.mola.rpc.core.remoting.handler;

import com.mola.rpc.core.proto.ProtoRpcConfigFactory;
import com.mola.rpc.core.remoting.netty.pool.NettyConnectPool;
import com.mola.rpc.core.util.RemotingHelper;
import com.mola.rpc.core.util.RemotingUtil;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 连接管理器
 * @date : 2020-09-04 10:50
 **/
public class NettyServerConnectManageHandler extends ChannelDuplexHandler {

    private static final Logger log = LoggerFactory.getLogger(NettyServerConnectManageHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddress = RemotingHelper.parseChannelRemoteAddress(ctx.channel());
        log.info("NETTY SERVER PIPELINE: channelActive, the channel[{" + remoteAddress + "}]");
        super.channelActive(ctx);
        // 连接成功
        log.info("[ServerChannelEventListener]: onChannelConnect {" + remoteAddress + "}");
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddress = RemotingHelper.parseChannelRemoteAddress(ctx.channel());
        log.info("NETTY SERVER PIPELINE: channelInactive, the channel[{" + remoteAddress + "}]");
        super.channelInactive(ctx);
        // 连接关闭
        log.warn("[ServerChannelEventListener]: onChannelClose {" + remoteAddress + "}");
        NettyConnectPool nettyConnectPool = ProtoRpcConfigFactory.fetch().getNettyConnectPool();
        nettyConnectPool.removeClosedReverseChannel(remoteAddress);
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.ALL_IDLE)) {
                final String remoteAddress = RemotingHelper.parseChannelRemoteAddress(ctx.channel());
                // 连接空闲
                log.warn("[ServerChannelEventListener]: onChannelIdle {" + remoteAddress + "}");
            }
        }
        ctx.fireUserEventTriggered(evt);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        final String remoteAddress = RemotingHelper.parseChannelRemoteAddress(ctx.channel());
        // 连接异常
        RemotingUtil.closeChannel(ctx.channel());
        log.warn("[ServerChannelEventListener]: onChannelException {" + remoteAddress + "}");
    }
}
