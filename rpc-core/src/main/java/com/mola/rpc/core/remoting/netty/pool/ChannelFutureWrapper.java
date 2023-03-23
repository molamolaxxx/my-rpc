package com.mola.rpc.core.remoting.netty.pool;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-08-06 11:43
 **/
public class ChannelFutureWrapper extends AbstractChannelWrapper {

    private ChannelFuture channelFuture;

    public ChannelFutureWrapper(ChannelFuture channelFuture) {
        this.channelFuture = channelFuture;
    }

    public ChannelFuture getChannelFuture() {
        return channelFuture;
    }

    @Override
    public Channel getChannel() {
        return channelFuture.channel();
    }

    @Override
    public boolean isOk() {
        return null != channelFuture && channelFuture.channel().isActive() && channelFuture.channel().isWritable();
    }

    public static final ChannelFutureWrapper of(ChannelFuture channelFuture) {
        return new ChannelFutureWrapper(channelFuture);
    }
}
