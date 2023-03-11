package com.mola.rpc.core.remoting.handler;

import com.mola.rpc.core.remoting.netty.NettyConnectPool;
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
 * @Project: InvincibleSchedulerEngine
 * @Description: 连接管理器
 * @date : 2020-09-04 10:50
 **/
public class NettyClientConnectManageHandler extends ChannelDuplexHandler {

    private NettyConnectPool nettyConnectPool;

    private static final Logger log = LoggerFactory.getLogger(NettyClientConnectManageHandler.class);

    public NettyClientConnectManageHandler(NettyConnectPool nettyConnectPool) {
        this.nettyConnectPool = nettyConnectPool;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.info("NETTY SERVER PIPELINE: channelActive, the channel[{" + remoteAddress + "}]");
        super.channelActive(ctx);
        // 连接成功
        log.info("[NettyClientConnectManageHandler]: onChannelConnect {" + remoteAddress + "}");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.info("NETTY SERVER PIPELINE: channelInactive, the channel[{" + remoteAddress + "}]");
        super.channelInactive(ctx);
        // 连接关闭
        log.warn("[NettyClientConnectManageHandler]: onChannelClose {" + remoteAddress + "}");
        nettyConnectPool.removeChannel(remoteAddress, ctx.channel());
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.ALL_IDLE)) {
                final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
                // 连接在读写上都空闲，关闭
                log.warn("[NettyClientConnectManageHandler]: onChannelIdle ,processing close {" + remoteAddress + "}");
                RemotingUtil.closeChannel(ctx.channel());
            }
        }
        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
        // 连接异常
        RemotingUtil.closeChannel(ctx.channel());
        log.warn("[NettyClientConnectManageHandler]: onChannelException {" + remoteAddress + "}");
    }
}
