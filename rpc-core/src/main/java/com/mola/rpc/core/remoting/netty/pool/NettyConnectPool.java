package com.mola.rpc.core.remoting.netty.pool;

import com.google.common.collect.Maps;
import com.mola.rpc.common.entity.RpcMetaData;
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
    private final Map<String, ChannelFutureWrapper> channelWrapperMap = Maps.newConcurrentMap();

    /**
     * key : client指定的key
     * value : 反向代理组
     */
    private final Map<String, ReverseProviderChannelGroup> reverseChannelsKeyMap = Maps.newConcurrentMap();

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
     * @param providerRpcMeta
     * @param channelWrapper
     */
    public void registerReverseInvokeChannel(RpcMetaData providerRpcMeta, ChannelWrapper channelWrapper) {
        String serviceKey = providerRpcMeta.fetchReverseServiceKey();
        if (!reverseChannelsKeyMap.containsKey(serviceKey)) {
            reverseChannelsKeyMap.put(serviceKey, new ReverseProviderChannelGroup());
        }

        ReverseProviderChannelGroup reverseProviderChannelGroup = reverseChannelsKeyMap.get(serviceKey);
        Map<String, ChannelWrapper> reverseAddress2ChannelMap = reverseProviderChannelGroup.getReverseAddress2ChannelMap();
        Map<String, RpcMetaData> reverseAddress2ProviderMap = reverseProviderChannelGroup.getReverseAddress2ProviderMap();

        String remoteAddress = channelWrapper.getRemoteAddress();
        ChannelWrapper pre = reverseAddress2ChannelMap.get(remoteAddress);
        if (pre == null) {
            log.warn("channel register successful, ignore, remote address = " + remoteAddress + ", key = " + serviceKey);
            reverseAddress2ChannelMap.put(remoteAddress, channelWrapper);
            reverseAddress2ProviderMap.put(remoteAddress, providerRpcMeta);
            return;
        }
        if (!pre.isOk()) {
            log.warn("channel is exist but not writable, change it, remote address = " + channelWrapper.getChannel().remoteAddress().toString());
            pre.closeChannel();
            reverseAddress2ChannelMap.put(remoteAddress, channelWrapper);
            reverseAddress2ProviderMap.put(remoteAddress, providerRpcMeta);
            return;
        }
        pre.setLastAliveTime(System.currentTimeMillis());
        log.warn("channel has been registered, remote address = " + channelWrapper.getRemoteAddress());
    }

    public void clearBadReverseChannels() {
        // 移除所有有问题的连接
        reverseChannelsKeyMap.values().forEach(ReverseProviderChannelGroup::clearBadReverseChannels);
    }

    public void removeReverseChannel(String serviceKey, String remoteAddress) {
        ReverseProviderChannelGroup gp = reverseChannelsKeyMap.get(serviceKey);
        if (gp != null) {
            gp.removeReverseChannel(remoteAddress);
        }
    }

    public void removeClosedReverseChannel(String remoteAddress) {
        for (ReverseProviderChannelGroup gp : reverseChannelsKeyMap.values()) {
            gp.removeClosedReverseChannel(remoteAddress);
        }
    }

    public Channel getReverseInvokeChannel(String serviceKey, String routeTag) {
        ReverseProviderChannelGroup gp = reverseChannelsKeyMap.get(serviceKey);
        if (gp == null) {
            return null;
        }

        Channel reverseInvokeChannel = gp.getReverseInvokeChannel(routeTag);
        if (reverseInvokeChannel == null) {
            log.error("can not get reverse invoke channel! serviceKey = " + serviceKey);
            return null;
        }
        return reverseInvokeChannel;
    }

    public Map<String, ReverseProviderChannelGroup> getReverseChannelsKeyMap() {
        return reverseChannelsKeyMap;
    }

    public void shutdown() {
        for (ChannelFutureWrapper channelWrapper : channelWrapperMap.values()) {
            if (channelWrapper != null) {
                RemotingUtil.closeChannel(channelWrapper.getChannel());
            }
        }
        reverseChannelsKeyMap.values().forEach(ReverseProviderChannelGroup::shutdown);
        // for gc
        channelWrapperMap.clear();
        reverseChannelsKeyMap.clear();
    }
}
