package com.mola.rpc.core.remoting.netty.pool;

import com.mola.rpc.core.util.RemotingUtil;
import io.netty.channel.Channel;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 反向调用维护在consumer端的wrapper
 * @date : 2022-08-06 11:43
 **/
public class ChannelWrapper extends AbstractChannelWrapper {

    private Channel channel;

    private long lastAliveTime;

    private static final long MAX_ALIVE_TIME_INTERVAL = 120 * 1000;

    public ChannelWrapper(Channel channel) {
        this.channel = channel;
        this.lastAliveTime = System.currentTimeMillis();
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public boolean isOk() {
        return RemotingUtil.channelIsAvailable(channel) && System.currentTimeMillis() - lastAliveTime < MAX_ALIVE_TIME_INTERVAL;
    }

    public static final ChannelWrapper of(Channel channel) {
        return new ChannelWrapper(channel);
    }

    public void setLastAliveTime(long lastAliveTime) {
        this.lastAliveTime = lastAliveTime;
    }
}
