package com.mola.rpc.core.remoting.netty.pool;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mola.rpc.core.util.RemotingUtil;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

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
    private final Map<String, ChannelFutureWrapper> channelWrapperMap = Maps.newConcurrentMap();

    /**
     * key : client指定的key
     * value : 远端地址-》channel映射
     */
    private final Map<String, Map<String, ChannelWrapper>> reverseChannelsKeyMap = Maps.newConcurrentMap();

    public Channel getChannel(String address) {
        ChannelFutureWrapper channelWrapper = channelWrapperMap.get(address);
        if (channelWrapper != null && channelWrapper.isOk()) {
            return channelWrapper.getChannel();
        }
        return null;
    }

    public ChannelFutureWrapper getChannelFutureWrapper(String address) {
        return channelWrapperMap.get(address);
    }

    public void addChannelWrapper(String address, ChannelFutureWrapper channelWrapper) {
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

    /**
     * 注册反向调用的tcp通道
     * @param serviceKey
     * @param channelWrapper
     */
    public void registerReverseInvokeChannel(String serviceKey, ChannelWrapper channelWrapper) {
        if (!reverseChannelsKeyMap.containsKey(serviceKey)) {
            reverseChannelsKeyMap.put(serviceKey, Maps.newConcurrentMap());
        }
        Map<String, ChannelWrapper> addressChannelMap = reverseChannelsKeyMap.get(serviceKey);
        String remoteAddress = channelWrapper.getRemoteAddress();
        ChannelWrapper pre = addressChannelMap.get(remoteAddress);
        if (pre == null) {
            log.warn("channel register successful, ignore, remote address = " + remoteAddress + ", key = " + serviceKey);
            addressChannelMap.put(remoteAddress, channelWrapper);
            return;
        }
        if (!pre.isOk()) {
            log.warn("channel is exist but not writable, change it, remote address = " + channelWrapper.getChannel().remoteAddress().toString());
            pre.closeChannel();
            addressChannelMap.put(remoteAddress, channelWrapper);
            return;
        }
        pre.setLastAliveTime(System.currentTimeMillis());
        log.warn("channel has been registered, remote address = " + channelWrapper.getRemoteAddress());
    }

    public void clearBadReverseChannels() {
        // 移除所有有问题的连接
        for (Map<String, ChannelWrapper> cwm : reverseChannelsKeyMap.values()) {
            Assert.isTrue(cwm instanceof ConcurrentHashMap, "channelWrapperMap not support concurrent modify");
            cwm.forEach((address, cw) -> {
                if (!cw.isOk()) {
                    log.warn("remove channel " + cw.getChannel() + " due to its bad status");
                    cwm.remove(address);
                    cw.closeChannel();
                }
            });
        }
    }

    public void removeReverseChannel(String serviceKey, String remoteAddress) {
        Map<String, ChannelWrapper> addressChannelMap = reverseChannelsKeyMap.get(serviceKey);
        if (addressChannelMap.containsKey(remoteAddress)) {
            addressChannelMap.remove(remoteAddress);
        }
    }

    public void removeClosedReverseChannel(String remoteAddress) {
        for (Map<String, ChannelWrapper> map : reverseChannelsKeyMap.values()) {
            ChannelWrapper cw = map.get(remoteAddress);
            if (cw != null && !cw.isOk()) {
                cw.closeChannel();
                map.remove(remoteAddress);
            }
        }
    }

    public Channel getReverseInvokeChannel(String serviceKey) {
        Map<String, ChannelWrapper> addressChannelMap = reverseChannelsKeyMap.get(serviceKey);
        if (addressChannelMap == null || addressChannelMap.size() == 0) {
            log.warn("there are no available channel to be use, key = " + serviceKey);
            return null;
        }
        // 优先使用指定地址
        // 随机取一个
        Random random = new Random();
        ArrayList<ChannelWrapper> channels = Lists.newArrayList(addressChannelMap.values());
        ChannelWrapper availableChannel = null;
        while (channels.size() > 0) {
            int pos = random.nextInt(channels.size());
            availableChannel = channels.get(pos);
            if (availableChannel != null && availableChannel.isOk()) {
                return availableChannel.getChannel();
            }
            channels.remove(pos);
        }
        log.error("can not get reverse invoke channel! serviceKey = " + serviceKey);
        return null;
    }

    public Map<String, Map<String, ChannelWrapper>> getReverseChannelsKeyMap() {
        return reverseChannelsKeyMap;
    }

    public void shutdown() {
        for (ChannelFutureWrapper channelWrapper : channelWrapperMap.values()) {
            if (channelWrapper != null) {
                RemotingUtil.closeChannel(channelWrapper.getChannel());
            }
        }
        for (Map<String, ChannelWrapper> map : reverseChannelsKeyMap.values()) {
            for (ChannelWrapper channelWrapper : map.values()) {
                RemotingUtil.closeChannel(channelWrapper.getChannel());
            }
            map.clear();
        }
        // for gc
        channelWrapperMap.clear();
        reverseChannelsKeyMap.clear();
    }
}
