package com.mola.rpc.core.remoting.netty;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mola.rpc.core.util.RemotingHelper;
import com.mola.rpc.core.util.RemotingUtil;
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
public class ReverseInvokeChannelPool {

    private static final Logger log = LoggerFactory.getLogger(ReverseInvokeChannelPool.class);

    /**
     * key : client指定的key
     * value : 远端地址-》channel映射
     */
    private static Map<String, Map<String, Channel>> reverseChannelsKeyMap = Maps.newConcurrentMap();

    /**
     * 注册反向调用的tcp通道
     * @param serviceKey
     * @param channel
     */
    public static void registerReverseInvokeChannel(String serviceKey, Channel channel) {
        if (!reverseChannelsKeyMap.containsKey(serviceKey)) {
            reverseChannelsKeyMap.put(serviceKey, Maps.newConcurrentMap());
        }
        Map<String, Channel> addressChannelMap = reverseChannelsKeyMap.get(serviceKey);
        String remoteAddress = RemotingHelper.parseChannelRemoteAddr(channel);
        Channel pre = addressChannelMap.get(remoteAddress);
        if (null == pre) {
            log.warn("channel register successful, ignore, remote address = " + remoteAddress + ", key = " + serviceKey);
            addressChannelMap.put(remoteAddress, channel);
            return;
        }
        if (!pre.isActive()) {
            log.warn("channel is exist but not active, change it, remote address = " + channel.remoteAddress().toString());
            RemotingUtil.closeChannel(channel);
            addressChannelMap.put(remoteAddress, channel);
            return;
        }
        log.warn("channel has been registered, ignore, remote address = " + channel.remoteAddress().toString());
    }

    public static void removeChannel(String serviceKey, Channel channel) {
        Map<String, Channel> addressChannelMap = reverseChannelsKeyMap.get(serviceKey);
        String key = RemotingHelper.parseChannelRemoteAddr(channel);
        if (addressChannelMap.containsKey(key)) {
            addressChannelMap.remove(key);
        }
    }

    public static void removeClosedChannel(String remoteAddress) {
        for (Map<String, Channel> map : reverseChannelsKeyMap.values()) {
            Channel channel = map.get(remoteAddress);
            if (null != channel && !channel.isActive()) {
                map.remove(remoteAddress);
            }
        }
    }

    public static Channel getReverseInvokeChannel(String serviceKey) {
        Assert.isTrue(reverseChannelsKeyMap.containsKey(serviceKey), "in consumer , reverseChannelsKeyMap not contains key : "
                + serviceKey + ", perhaps provider not register it ");
        Map<String, Channel> addressChannelMap = reverseChannelsKeyMap.get(serviceKey);
        Assert.isTrue(addressChannelMap.size() > 0, "there are no available channel to be use, key = " + serviceKey);
        // 随机取一个
        Random random = new Random();
        ArrayList<Channel> channels = Lists.newArrayList(addressChannelMap.values());
        int pos = random.nextInt(channels.size());
        return channels.get(pos);
    }
}
