package com.mola.rpc.core.remoting.netty;

import com.google.common.collect.Maps;
import io.netty.channel.Channel;

import java.util.Map;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-07-30 22:31
 **/
public class NettyConnectPool {

    /**
     * 缓存 addr -> wrapper
     */
    private Map<String, ChannelWrapper> channelWrapperMap = Maps.newConcurrentMap();

    public Channel getChannel(String address) {
        ChannelWrapper channelWrapper = channelWrapperMap.get(address);
        if (null != channelWrapper && channelWrapper.isOk()) {
            return channelWrapper.getChannel();
        }
        return null;
    }

    public ChannelWrapper getChannelWrapper(String address) {
        return channelWrapperMap.get(address);
    }

    public void addChannelWrapper(String address, ChannelWrapper channelWrapper) {
        this.channelWrapperMap.put(address, channelWrapper);
    }

    public void removeChannel(String address, Channel channel) {
        if (!channelWrapperMap.containsKey(address)) {
            throw new RuntimeException("address not found");
        }
        if (channel != channelWrapperMap.get(address)) {
            throw new RuntimeException("channel has been changed");
        }
        channelWrapperMap.remove(address);
    }
}
