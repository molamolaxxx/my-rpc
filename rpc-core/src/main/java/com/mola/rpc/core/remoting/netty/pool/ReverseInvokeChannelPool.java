package com.mola.rpc.core.remoting.netty.pool;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-07-30 22:31
 **/
@Deprecated
public class ReverseInvokeChannelPool {

    private static final Logger log = LoggerFactory.getLogger(ReverseInvokeChannelPool.class);

    /**
     * key : client指定的key
     * value : 远端地址-》channel映射
     */
    private static Map<String, Map<String, ChannelWrapper>> reverseChannelsKeyMap = Maps.newConcurrentMap();

    /**
     * 注册反向调用的tcp通道
     * @param serviceKey
     * @param channelWrapper
     */
    public static void registerReverseInvokeChannel(String serviceKey, ChannelWrapper channelWrapper) {
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
        log.warn("channel has been registered, remote address = " + channelWrapper.getRemoteAddress());
    }

    public static void removeChannel(String serviceKey, String remoteAddress) {
        Map<String, ChannelWrapper> addressChannelMap = reverseChannelsKeyMap.get(serviceKey);
        if (addressChannelMap.containsKey(remoteAddress)) {
            addressChannelMap.remove(remoteAddress);
        }
    }

    public static void removeClosedChannel(String remoteAddress) {
        for (Map<String, ChannelWrapper> map : reverseChannelsKeyMap.values()) {
            ChannelWrapper cw = map.get(remoteAddress);
            if (cw != null && !cw.isOk()) {
                cw.closeChannel();
                map.remove(remoteAddress);
            }
        }
    }

    public static Channel getReverseInvokeChannel(String serviceKey) {
        Assert.isTrue(reverseChannelsKeyMap.containsKey(serviceKey), "in consumer , reverseChannelsKeyMap not contains key : "
                + serviceKey + ", perhaps provider not register it ");
        Map<String, ChannelWrapper> addressChannelMap = reverseChannelsKeyMap.get(serviceKey);
        Assert.isTrue(addressChannelMap.size() > 0, "there are no available channel to be use, key = " + serviceKey);
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
        return null;
    }
}
