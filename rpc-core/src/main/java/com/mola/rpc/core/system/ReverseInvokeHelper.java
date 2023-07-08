package com.mola.rpc.core.system;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.common.ext.ExtensionRegistryManager;
import com.mola.rpc.common.interceptor.ReverseProxyRegisterInterceptor;
import com.mola.rpc.core.proto.ProtoRpcConfigFactory;
import com.mola.rpc.core.remoting.netty.pool.ChannelWrapper;
import com.mola.rpc.core.remoting.netty.pool.NettyConnectPool;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 反向代理调用
 * 场景：client与server建立连接，client端提供provider，server需要调用client的provider
 * client：
 * 1、因为server端不维护client建立的连接，所以需要client主动向server进行注册，维护注册key和channel对象的关联，服务端单独维护连接
 * 2、启动provider
 * server：
 * 1、server收到注册提醒，维护channel，并检测是否可用
 * 2、启动consumer
 * 3、在server端指定consumer的channel-key，consumer会自动在channel中进行选择
 * @date : 2023-03-05 18:55
 **/
public class ReverseInvokeHelper {

    private static final Logger log = LoggerFactory.getLogger(ReverseInvokeHelper.class);

    /**
     * 定时上报服务端代理连接
     */
    private ScheduledExecutorService reverseProviderConnectMonitorThread;

    private AtomicBoolean startFlag = new AtomicBoolean(false);

    private ReverseInvokeHelper(){}
    private static class Singleton{
        private static ReverseInvokeHelper reverseInvokeHelper = new ReverseInvokeHelper();
    }

    public static ReverseInvokeHelper instance() {
        return Singleton.reverseInvokeHelper;
    }

    public void startMonitor() {
        if (startFlag.get()) {
            return;
        }
        ProtoRpcConfigFactory protoRpcConfigFactory = ProtoRpcConfigFactory.fetch();
        RpcContext rpcContext = protoRpcConfigFactory.getRpcContext();
        NettyConnectPool nettyConnectPool = protoRpcConfigFactory.getNettyConnectPool();
        ExtensionRegistryManager extensionRegistryManager = protoRpcConfigFactory.getExtensionRegistryManager();

        this.reverseProviderConnectMonitorThread = Executors.newScheduledThreadPool(1,
                new ThreadFactory() {
                    AtomicInteger threadIndex = new AtomicInteger(0);
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r, "reverse-provider-connect-monitor-thread-" + this.threadIndex.incrementAndGet());
                        thread.setDaemon(true);
                        return thread;
                    }
                });

        this.reverseProviderConnectMonitorThread.scheduleAtFixedRate(() -> {
            try {
                List<ReverseProxyRegisterInterceptor> interceptors = extensionRegistryManager
                        .getInterceptors(ReverseProxyRegisterInterceptor.class);

                // 单个代理注册方法句柄
                Consumer<RpcMetaData> singleRegisterMethodHandle = (rpcMetaData) -> {
                    if (!rpcMetaData.getReverseMode()) {
                        return;
                    }
                    for (ReverseProxyRegisterInterceptor interceptor : interceptors) {
                        if (interceptor.intercept(rpcMetaData)) {
                            return;
                        }
                    }
                    List<String> reverseModeConsumerAddress = rpcMetaData.getReverseModeConsumerAddress();
                    for (String consumerAddress : reverseModeConsumerAddress) {
                        Channel channel = nettyConnectPool.getChannel(consumerAddress);
                        try {
                            this.registerProviderToServer(rpcMetaData);
                        } catch (Exception e) {
                            log.error("remote address " + (channel == null ? consumerAddress : channel.toString()) +" 's channel heart beat exception! ", e);
                        }
                    }
                };

                // 获取所有提供服务信息
                rpcContext.getProviderMetaMap().values().forEach(singleRegisterMethodHandle);

                // 清除坏连接
                nettyConnectPool.clearBadReverseChannels();
            } catch (Exception e) {
                log.error("reverseProviderConnectMonitorThread schedule failed!", e);
            }

        },0, 45, TimeUnit.SECONDS);
        startFlag.compareAndSet(false, true);
    }

    public void shutdownMonitor() {
        if (reverseProviderConnectMonitorThread != null) {
            reverseProviderConnectMonitorThread.shutdown();
        }
    }

    public String getServiceKey(RpcMetaData rpcMetaData, boolean isConsumer) {
        return String.format("%s:%s:%s", rpcMetaData.getInterfaceClazz().getName(),
                rpcMetaData.getGroup(), rpcMetaData.getVersion());
    }

    /**
     * 获取代理服务
     * @param serviceKey
     * @return
     */
    public Map<String, ChannelWrapper> fetchAvailableProxyService(String serviceKey) {
        ProtoRpcConfigFactory protoRpcConfigFactory = ProtoRpcConfigFactory.fetch();
        NettyConnectPool nettyConnectPool = protoRpcConfigFactory.getNettyConnectPool();
        Map<String, Map<String, ChannelWrapper>> reverseChannelsKeyMap = nettyConnectPool.getReverseChannelsKeyMap();
        if (reverseChannelsKeyMap == null) {
            return null;
        }
        return reverseChannelsKeyMap.get(serviceKey);
    }

    public void registerProviderToServer(RpcMetaData providerMeta) {
        // 反向代理模式下，向consumer端注册provider的key
        Assert.notEmpty(providerMeta.getReverseModeConsumerAddress(),
                "provider in reverse mode, reverseModeConsumerAddress can not be empty! " + JSONObject.toJSONString(providerMeta));
        SystemConsumer<SystemConsumer.ReverseInvokerCaller> systemConsumer = SystemConsumer.Multipart.reverseInvokerCaller;
        for (String reverseModeConsumerAddress : providerMeta.getReverseModeConsumerAddress()) {
            systemConsumer.setAppointedAddress(Lists.newArrayList(reverseModeConsumerAddress));
            systemConsumer.fetch().register(getServiceKey(providerMeta, false));
        }
    }
}
