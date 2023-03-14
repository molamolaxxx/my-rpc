package com.mola.rpc.core.remoting.netty;

import com.google.common.collect.Maps;
import com.mola.rpc.core.util.RemotingUtil;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 客户端连接管理
 * @date : 2022-07-30 22:31
 **/
public class NettyConnectPool {

    private static final Logger log = LoggerFactory.getLogger(NettyConnectPool.class);

    /**
     * 缓存 远端addr -> wrapper
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
        if (channel != channelWrapperMap.get(address).getChannel()) {
            throw new RuntimeException("channel has been changed");
        }
        channelWrapperMap.remove(address);
    }

    public void shutdown() {
        for (ChannelWrapper channelWrapper : channelWrapperMap.values()) {
            if (channelWrapper != null) {
                RemotingUtil.closeChannel(channelWrapper.getChannel());
            }
        }
        // for gc
        channelWrapperMap = Maps.newConcurrentMap();
    }
}
