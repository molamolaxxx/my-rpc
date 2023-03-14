package com.mola.rpc.core.remoting.netty;

import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.core.proxy.InvokeMethod;
import com.mola.rpc.core.remoting.AsyncResponseFuture;
import com.mola.rpc.core.remoting.ResponseFuture;
import com.mola.rpc.core.remoting.handler.*;
import com.mola.rpc.core.remoting.protocol.RemotingCommand;
import com.mola.rpc.core.util.RemotingHelper;
import com.mola.rpc.core.util.RemotingUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-08-06 11:12
 **/
public class NettyRemoteClient {

    private static final Logger log = LoggerFactory.getLogger(NettyRemoteClient.class);

    /**
     * 启动器
     */
    private final Bootstrap clientBootstrap;

    /**
     * worker-group
     */
    private final EventLoopGroup eventLoopGroupWorker;

    /**
     * 默认执行pipeline中事件的线程组
     */
    private DefaultEventExecutorGroup defaultEventExecutorGroup;

    /**
     * 响应回调管理
     */
    private ResponseFutureManager responseFutureManager;

    private NettyConnectPool nettyConnectPool;

    private RpcContext rpcContext;

    private AtomicBoolean startFlag = new AtomicBoolean(false);

    /**
     * pipeline上的请求处理器，用于provider的反射调用和结果写回(in)
     */
    private NettyRpcRequestHandler requestHandler;

    /**
     * pipeline上的响应处理器，用于consumer的结果同步(in)
     */
    private NettyRpcResponseHandler responseHandler;


    public NettyRemoteClient(NettyRpcRequestHandler requestHandler, NettyRpcResponseHandler responseHandler) {
        Assert.notNull(requestHandler, "requestHandler is required");
        Assert.notNull(responseHandler, "responseHandler is required");
        this.requestHandler = requestHandler;
        this.responseHandler = responseHandler;
        this.responseHandler.setNettyRemoteClient(this);
        this.clientBootstrap = new Bootstrap();
        // worker 线程池
        this.eventLoopGroupWorker = new NioEventLoopGroup(10, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("netty-client-selector-%d", this.threadIndex.incrementAndGet()));
            }
        });
        this.responseFutureManager = new ResponseFutureManager();
        this.responseFutureManager.initResponseFutureMonitor();
    }

    /**
     * 启动client
     * @param forceStart 是否强制启动
     */
    public void start(boolean forceStart) {
        if (rpcContext.getConsumerMetaMap().size() == 0 && !forceStart) {
            log.info("[NettyRemoteClient]:there are no consumer registered, netty client will not start");
            return;
        }
        final NettyRemoteClient self = this;
        // 初始化pipeline的执行线程池
        this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(
                4,
                new ThreadFactory() {
                    AtomicInteger threadIndex = new AtomicInteger(0);
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "netty-client-worker-thread-" + this.threadIndex.incrementAndGet());
                    }
                }
        );
        // 初始化bootstrap
        Bootstrap handler = this.clientBootstrap.group(this.eventLoopGroupWorker).channel(NioSocketChannel.class)
                // 关闭nagle
                .option(ChannelOption.TCP_NODELAY, true)
                // 不使用tcp心跳
                .option(ChannelOption.SO_KEEPALIVE, false)
                // 发送端缓存
                .option(ChannelOption.SO_SNDBUF, 65535)
                // 接收端缓存
                .option(ChannelOption.SO_RCVBUF, 65535)
                // 处理器
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                defaultEventExecutorGroup, // 执行pipe的线程池
                                new NettyEncoder(), //out
                                new NettyDecoder(), //in
                                new IdleStateHandler(0, 0, 120),//闲置时间
                                new NettyClientConnectManageHandler(nettyConnectPool), //Duplex
                                requestHandler, // in
                                responseHandler // in
                        ); //in
                    }
                });
        log.info("[NettyRemoteClient]: netty client start");
        this.startFlag.compareAndSet(false, true);
    }

    public void shutdown() {
        if (eventLoopGroupWorker != null) {
            eventLoopGroupWorker.shutdownGracefully();
        }
        if (eventLoopGroupWorker != null) {
            eventLoopGroupWorker.shutdownGracefully();
        }
        if (defaultEventExecutorGroup != null) {
            defaultEventExecutorGroup.shutdownGracefully();
        }
        if (nettyConnectPool != null) {
            nettyConnectPool.shutdown();
        }
    }

    /**
     * 与服务端创建连接，采用异步连接的方式
     * @param address
     * @return
     */
    public Channel createChannel(final String address) {
        // 1、从缓存中获取
        Channel channel = nettyConnectPool.getChannel(address);
        if (null != channel) {
            return channel;
        }

        ChannelWrapper channelWrapper = nettyConnectPool.getChannelWrapper(address);
        if (null != channelWrapper && !channelWrapper.getChannelFuture().isDone()) {
            log.info("[connecting]: connect host {} connecting", address);
            return channelWrapper.getChannel();
        }

        ChannelFuture future = this.clientBootstrap.connect(RemotingHelper.string2SocketAddress(address));
        log.info("createChannel: begin to connect remote host[{" + address + "}] asynchronously");
        nettyConnectPool.addChannelWrapper(address, ChannelWrapper.of(future));
        channelWrapper = nettyConnectPool.getChannelWrapper(address);
        // 等待连接完成，输出日志
        if (null != channelWrapper) {
            ChannelFuture channelFuture = channelWrapper.getChannelFuture();
            if (channelFuture.awaitUninterruptibly(3000)) {
                // 同步等待完成
                if (channelWrapper.isOk()) {
                    log.info("connect host {} success, channel is {}", address, channelWrapper.getChannel());
                } else {
                    log.info("connect host {} failed, channel is {}", address, channelWrapper.getChannel());
                }
            } else {
                log.warn("createChannel: connect remote host[{" + address + "}] timeout 3000 ms, {" + channelFuture.toString() + "}");
            }
            return channelWrapper.getChannel();
        }
        return null;
    }

    /**
     * 同步调用远程服务
     * @param address
     * @param request
     * @return
     */
    public RemotingCommand syncInvoke(String address, RemotingCommand request, InvokeMethod invokeMethod, long timeout) {
        try {
            Channel channel = this.getAvailableChannel(address);
            return syncInvokeWithChannel(channel, request, invokeMethod, timeout);
        } catch (Exception e) {
            log.error("syncInvoke failed remote host[{" + address + "}], request" + request , e);
            throw new RuntimeException(e);
        }
    }

    public RemotingCommand syncInvokeWithChannel(Channel channel, RemotingCommand request, InvokeMethod invokeMethod, long timeout) {
        try {
            ResponseFuture responseFuture = new ResponseFuture(request.getOpaque());
            // 缓存对外请求
            this.responseFutureManager.putSyncResponseFuture(request.getOpaque(), responseFuture);
            // 写入channel
            final SocketAddress remoteAddress = channel.remoteAddress();
            channel.writeAndFlush(request).addListener(future -> {
                // 调用成功
                if (future.isSuccess()) {
                    responseFuture.setSendRequestOK(true);
                    return;
                }
                // 调用失败
                responseFuture.setSendRequestOK(false);
                this.responseFutureManager.removeSyncResponseFuture(request.getOpaque());
                responseFuture.setCause(future.cause());
                responseFuture.putResponse(null);
                log.warn("send a request command to channel <" + remoteAddress + "> failed. request = " + request);
            });

            // 基于发令枪，同步等待服务端返回
            RemotingCommand response = responseFuture.waitResponse(timeout);
            if (null == response) {
                // 发送请求成功，读取应答超时
                if (responseFuture.isSendRequestOK()) {
                    throw new RuntimeException("provider time out, method = "
                            + invokeMethod.getInterfaceClazz() + "@" + invokeMethod.getMethodName() + " channel is " + channel.toString());
                }
                if (null != responseFuture.getCause()) {
                    throw new RuntimeException(responseFuture.getCause());
                }
                throw new RuntimeException("unknown exception");
            }
            return response;
        } catch (Exception e) {
            log.warn("syncInvokeWithChannel failed with channel: [" + channel.toString() + "], request" + request , e);
            throw new RuntimeException(e);
        } finally {
            this.responseFutureManager.removeSyncResponseFuture(request.getOpaque());
        }
    }

    /**
     * 异步调用远程服务
     * @param address
     * @param request
     * @return
     */
    public AsyncResponseFuture asyncInvoke(String address, RemotingCommand request, InvokeMethod invokeMethod, Method method, long timeout) {
        try {
            Channel channel = this.getAvailableChannel(address);
            AsyncResponseFuture responseFuture = new AsyncResponseFuture(request.getOpaque(), timeout);
            responseFuture.setMethod(method);
            // 缓存对外请求
            this.responseFutureManager.putAsyncResponseFuture(request.getOpaque(), responseFuture);
            // 写入channel
            final SocketAddress remoteAddress = channel.remoteAddress();
            channel.writeAndFlush(request).addListener(future -> {
                // 调用成功
                if (future.isSuccess()) {
                    responseFuture.setSendRequestOK(true);
                    return;
                }
                // 调用失败
                responseFuture.setSendRequestOK(false);
                this.responseFutureManager.removeAsyncResponseFuture(request.getOpaque());
                responseFuture.setCause(future.cause());
                responseFuture.putResponse(null);
                log.warn("send a request command to channel <" + remoteAddress + "> failed. request = " + request);
            });
            return responseFuture;
        } catch (Exception e) {
            log.warn("asyncInvoke failed remote host[{" + address + "}], request" + request , e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取一个可用的通道
     * @param address 远程地址
     * @return
     */
    public Channel getAvailableChannel(String address) {
        Channel channel = null;
        int retryCount = 0;
        while (!RemotingUtil.channelIsAvailable(channel) && ++retryCount < 10) {
            channel = nettyConnectPool.getChannel(address);
            if (null == channel) {
                channel = createChannel(address);
            }
            // 连接有问题，关闭连接，抛出异常
            if (!RemotingUtil.channelIsAvailable(channel)) {
                this.closeChannel(address, channel);
                log.error("channel is exception, retry count = "+retryCount+", address = " + address);
            }
        }
        if (retryCount == 10) {
            throw new RuntimeException("all retry failed! address = " + address);
        }
        return channel;
    }

    /**
     * 关闭channel，从缓存中移除wrapper
     * @param address
     * @param channel
     */
    public void closeChannel(final String address, final Channel channel) {
        try {
            if (null == channel) {
                return;
            }
            final String addressRemote = null == address ? RemotingHelper.parseChannelRemoteAddr(channel) : address;
            // 关闭channel
            RemotingUtil.closeChannel(channel);
            // 移除channel
            nettyConnectPool.removeChannel(addressRemote, channel);
            log.info("closeChannel: begin close the channel[{" + address + "}] Found: {" + (channel != null) + "}");
        } catch (Exception e) {
            log.error("closeChannel exception", e);
        }
    }

    public void setNettyConnectPool(NettyConnectPool nettyConnectPool) {
        this.nettyConnectPool = nettyConnectPool;
    }

    /**
     * 设置返回值
     * @param responseCommand
     */
    public void putResponse(RemotingCommand responseCommand) {
        // 同步缓存
        ResponseFuture responseFuture = this.responseFutureManager.getSyncResponseFuture(responseCommand.getOpaque());
        if (null != responseFuture) {
            responseFuture.putResponse(responseCommand);
            return;
        }
        // 异步缓存
        AsyncResponseFuture asyncResponseFuture = this.responseFutureManager.getAsyncResponseFuture(responseCommand.getOpaque());
        if (null != asyncResponseFuture) {
            asyncResponseFuture.putResponse(responseCommand);
            this.responseFutureManager.removeAsyncResponseFuture(responseCommand.getOpaque());
        }
    }

    public RpcContext getRpcContext() {
        return rpcContext;
    }

    public void setRpcContext(RpcContext rpcContext) {
        this.rpcContext = rpcContext;
    }

    public boolean isStart() {
        return this.startFlag.get();
    }
}
