package com.mola.rpc.core.remoting.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-08-06 11:43
 **/
public class ChannelWrapper {

    private ChannelFuture channelFuture;

    public ChannelWrapper(ChannelFuture channelFuture) {
        this.channelFuture = channelFuture;
    }

    public ChannelFuture getChannelFuture() {
        return channelFuture;
    }

    public Channel getChannel() {
        return channelFuture.channel();
    }

    public boolean isOk() {
        return null != channelFuture && channelFuture.channel().isActive();
    }

    public static final ChannelWrapper of(ChannelFuture channelFuture) {
        return new ChannelWrapper(channelFuture);
    }
}
