package com.mola.rpc.core.remoting.netty.pool;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.core.util.RemotingUtil;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-10-11 15:19
 **/
public class ReverseProviderChannelGroup {

    private static final Logger log = LoggerFactory.getLogger(ReverseProviderChannelGroup.class);

    private Map<String, ChannelWrapper> reverseAddress2ChannelMap = Maps.newConcurrentMap();

    private Map<String, RpcMetaData> reverseAddress2ProviderMap = Maps.newConcurrentMap();


    public Map<String, ChannelWrapper> getReverseAddress2ChannelMap() {
        return reverseAddress2ChannelMap;
    }

    public Map<String, RpcMetaData> getReverseAddress2ProviderMap() {
        return reverseAddress2ProviderMap;
    }

    public void clearBadReverseChannels() {
        // 移除所有有问题的连接
        reverseAddress2ChannelMap.forEach((address, cw) -> {
            if (!cw.isOk()) {
                log.warn("remove channel " + cw.getChannel() + " due to its bad status");
                reverseAddress2ChannelMap.remove(address);
                reverseAddress2ProviderMap.remove(address);
                cw.closeChannel();
            }
        });
    }

    public void removeReverseChannel(String remoteAddress) {
        reverseAddress2ChannelMap.remove(remoteAddress);
        reverseAddress2ProviderMap.remove(remoteAddress);
    }

    public void removeClosedReverseChannel(String remoteAddress) {
        ChannelWrapper cw = reverseAddress2ChannelMap.get(remoteAddress);
        if (cw != null && !cw.isOk()) {
            cw.closeChannel();
            reverseAddress2ChannelMap.remove(remoteAddress);
            reverseAddress2ProviderMap.remove(remoteAddress);
        }
    }

    public void shutdown() {
        for (ChannelWrapper channelWrapper : reverseAddress2ChannelMap.values()) {
            RemotingUtil.closeChannel(channelWrapper.getChannel());
        }
        reverseAddress2ChannelMap.clear();
        reverseAddress2ProviderMap.clear();
    }

    public Channel getReverseInvokeChannel(String routeTag) {
        if (reverseAddress2ChannelMap == null || reverseAddress2ChannelMap.size() == 0) {
            return null;
        }
        List<ChannelWrapper> channels = Lists.newArrayList();
        // channel筛选
        reverseAddress2ChannelMap.forEach((remoteAddress, cw) -> {
            if (routeTag != null && routeTag.trim().length() != 0) {
                RpcMetaData rpcMetaData = reverseAddress2ProviderMap.get(remoteAddress);
                if (rpcMetaData == null) {
                    return;
                }

                Set<String> routeTags = rpcMetaData.getRouteTags();
                if (routeTags != null && routeTags.contains(routeTag)) {
                    channels.add(cw);
                }
            } else {
                channels.add(cw);
            }
        });


        // 随机取一个
        Random random = new Random();
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
