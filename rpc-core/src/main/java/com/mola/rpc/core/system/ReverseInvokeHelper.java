package com.mola.rpc.core.system;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.core.proto.ProtoRpcConfigFactory;
import com.mola.rpc.core.remoting.netty.NettyConnectPool;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

    private ReverseInvokeHelper(){}
    public static class Singleton{
        private static ReverseInvokeHelper reverseInvokeHelper = new ReverseInvokeHelper();
    }

    public static ReverseInvokeHelper instance() {
        return Singleton.reverseInvokeHelper;
    }

    public void startMonitor() {
        ProtoRpcConfigFactory protoRpcConfigFactory = ProtoRpcConfigFactory.get();
        RpcContext rpcContext = protoRpcConfigFactory.getRpcContext();
        NettyConnectPool nettyConnectPool = protoRpcConfigFactory.getNettyConnectPool();
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
                // 获取所有提供服务信息
                for (RpcMetaData rpcMetaData : rpcContext.getProviderMetaMap().values()) {
                    if (!rpcMetaData.getReverseMode()) {
                        continue;
                    }
                    List<String> reverseModeConsumerAddress = rpcMetaData.getReverseModeConsumerAddress();
                    for (String consumerAddress : reverseModeConsumerAddress) {
                        Channel channel = nettyConnectPool.getChannel(consumerAddress);
                        if (null == channel || !channel.isActive() || !channel.isOpen()) {
                            log.error("remote address " + consumerAddress +" 's channel exception! processing re-register");
                            this.registerProviderProxyToServer(rpcMetaData);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("reverseProviderConnectMonitorThread schedule failed!", e);
            }

        },10, 30, TimeUnit.SECONDS);
    }

    public void shutdownMonitor() {
        if (null != reverseProviderConnectMonitorThread) {
            reverseProviderConnectMonitorThread.shutdown();
        }
    }

    public String getServiceKey(RpcMetaData rpcMetaData, boolean isConsumer) {
        return String.format("%s:%s:%s", rpcMetaData.getInterfaceClazz().getName(),
                rpcMetaData.getGroup(), rpcMetaData.getVersion());
    }

    public void registerProviderProxyToServer(RpcMetaData providerMeta) {
        // 反向代理模式下，向consumer端注册provider的key
        if (Boolean.TRUE.equals(providerMeta.getReverseMode())) {
            Assert.notEmpty(providerMeta.getReverseModeConsumerAddress(),
                    "provider in reverse mode, reverseModeConsumerAddress can not be empty! " + JSONObject.toJSONString(providerMeta));
            SystemConsumer<SystemConsumer.ReverseInvokerCaller> systemConsumer = SystemConsumer.Multipart.reverseInvokerCaller;
            for (String reverseModeConsumerAddress : providerMeta.getReverseModeConsumerAddress()) {
                systemConsumer.setAppointedAddress(Lists.newArrayList(reverseModeConsumerAddress));
                systemConsumer.fetch().register(this.getServiceKey(providerMeta, false));
            }
        }
    }
}
