package com.mola.rpc.core.remoting.netty;

import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.core.proxy.InvokeMethod;
import com.mola.rpc.core.remoting.AsyncResponseFuture;
import com.mola.rpc.core.remoting.ResponseFuture;
import com.mola.rpc.core.remoting.handler.*;
import com.mola.rpc.core.remoting.netty.pool.ChannelFutureWrapper;
import com.mola.rpc.core.remoting.netty.pool.NettyConnectPool;
import com.mola.rpc.core.remoting.protocol.RemotingCommand;
import com.mola.rpc.core.util.MultiKeyReentrantLock;
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
import com.mola.rpc.common.utils.AssertUtil;

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
    private final ResponseFutureManager responseFutureManager;

    private NettyConnectPool nettyConnectPool;

    private RpcContext rpcContext;

    private final AtomicBoolean startFlag = new AtomicBoolean(false);

    private final MultiKeyReentrantLock channelCreateLock = new MultiKeyReentrantLock(128);

    /**
     * pipeline上的请求处理器，用于provider的反射调用和结果写回(in)
     */
    private final NettyRpcRequestHandler requestHandler;

    /**
     * pipeline上的响应处理器，用于consumer的结果同步(in)
     */
    private final NettyRpcResponseHandler responseHandler;


    public NettyRemoteClient(NettyRpcRequestHandler requestHandler, NettyRpcResponseHandler responseHandler) {
        AssertUtil.notNull(requestHandler, "requestHandler is required");
        AssertUtil.notNull(responseHandler, "responseHandler is required");
        this.requestHandler = requestHandler;
        this.responseHandler = responseHandler;
        this.responseHandler.setNettyRemoteClient(this);
        this.clientBootstrap = new Bootstrap();
        // worker 线程池
        this.eventLoopGroupWorker = new NioEventLoopGroup(10, new ThreadFactory() {
            private final AtomicInteger threadIndex = new AtomicInteger(0);
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
        // 初始化pipeline的执行线程池
        this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(
                4,
                new ThreadFactory() {
                    final AtomicInteger threadIndex = new AtomicInteger(0);
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
                                new IdleStateHandler(300, 300, 300),//闲置时间
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
        if (channel != null) {
            return channel;
        }

        ChannelFutureWrapper channelFutureWrapper = nettyConnectPool.getChannelFutureWrapper(address);
        if (channelFutureWrapper != null && !channelFutureWrapper.getChannelFuture().isDone()) {
            throw new RuntimeException("[connecting]: connect host " + address + " is connecting, not available");
        }

        channelCreateLock.lock(address);
        try {
            channelFutureWrapper = nettyConnectPool.getChannelFutureWrapper(address);
            if (channelFutureWrapper != null) {
                return channelFutureWrapper.getChannel();
            }
            ChannelFuture future = this.clientBootstrap.connect(RemotingHelper.string2SocketAddress(address));
            log.info("createChannel: begin to connect remote host[{" + address + "}] asynchronously");
            channelFutureWrapper = ChannelFutureWrapper.of(future);
            // 等待连接完成，输出日志
            ChannelFuture channelFuture = channelFutureWrapper.getChannelFuture();
            if (channelFuture.awaitUninterruptibly(3000)) {
                // 同步等待完成
                if (channelFutureWrapper.isOk()) {
                    log.info("connect host {} success, channel is {}", address, channelFutureWrapper.getChannel());
                    nettyConnectPool.addChannelWrapper(address, channelFutureWrapper);
                } else {
                    log.info("connect host {} failed, channel is {}", address, channelFutureWrapper.getChannel());
                }
            } else {
                log.warn("createChannel: connect remote host[{" + address + "}] timeout 3000 ms, {" + channelFuture.toString() + "}");
            }
            return channelFutureWrapper.getChannel();
        } finally {
            channelCreateLock.unlock(address);
        }
    }

    /**
     * 同步调用远程服务
     * @param address
     * @param request
     * @return
     */
    public RemotingCommand syncInvoke(String address, RemotingCommand request, InvokeMethod invokeMethod,Method method, long timeout) {
        try {
            Channel channel = this.getAvailableChannel(address);
            return syncInvokeWithChannel(channel, request, invokeMethod, method, timeout);
        } catch (Exception e) {
            log.error("syncInvoke failed to remote host[{" + address + "}], request" + request , e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 发送请求给服务端
     * @param channel  发送使用的通道
     * @param request 请求报文
     * @param invokeMethod 执行方法包装
     * @param method 具体方法
     * @param timeout 客户端超时时间
     * @return
     */
    public RemotingCommand syncInvokeWithChannel(Channel channel, RemotingCommand request, InvokeMethod invokeMethod, Method method, long timeout) {
        try {
            ResponseFuture responseFuture = new ResponseFuture(request.getOpaque());
            responseFuture.setMethod(method);
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
                if (!RemotingUtil.channelIsAvailable(channel)) {
                    RemotingUtil.closeChannel(channel);
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
            if (response == null) {
                // 发送请求成功，读取应答超时
                if (responseFuture.isSendRequestOK()) {
                    if (!RemotingUtil.channelIsAvailable(channel)) {
                        RemotingUtil.closeChannel(channel);
                    }
                    throw new RuntimeException("provider time out, method = "
                            + invokeMethod.getInterfaceClazz() + "@" + invokeMethod.getMethodName() + " channel is " + channel.toString());
                }
                if (responseFuture.getCause() != null) {
                    throw new RuntimeException(responseFuture.getCause());
                }
                throw new RuntimeException("unknown exception");
            }
            return response;
        } catch (Exception e) {
            log.error("syncInvokeWithChannel failed in channel: [" + channel.toString() + "], request" + request , e);
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
            return asyncInvokeWithChannel(channel, request, invokeMethod, method, timeout);
        } catch (Exception e) {
            log.warn("asyncInvoke failed to remote host[{" + address + "}], request" + request , e);
            throw new RuntimeException(e);
        }
    }

    public AsyncResponseFuture asyncInvokeWithChannel(Channel channel, RemotingCommand request, InvokeMethod invokeMethod, Method method, long timeout) {
        try {
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
                if (!RemotingUtil.channelIsAvailable(channel)) {
                    RemotingUtil.closeChannel(channel);
                }
                responseFuture.setSendRequestOK(false);
                this.responseFutureManager.removeAsyncResponseFuture(request.getOpaque());
                responseFuture.setCause(future.cause());
                responseFuture.putResponse(null);
                log.warn("send a request command to channel <" + remoteAddress + "> failed. request = " + request);
            });
            return responseFuture;
        } catch (Exception e) {
            log.error("asyncInvoke failed in channel[" + channel + "], request" + request , e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 单向调用远程服务
     * @param address
     * @param request
     * @return
     */
    public void onewayInvoke(String address, RemotingCommand request, InvokeMethod invokeMethod, Method method, long timeout) {
        try {
            Channel channel = this.getAvailableChannel(address);
            onewayInvokeWithChannel(channel, request, invokeMethod, method, timeout);
        } catch (Exception e) {
            log.warn("onewayInvoke failed to remote host[{" + address + "}], request" + request , e);
            throw new RuntimeException(e);
        }
    }


    public void onewayInvokeWithChannel(Channel channel, RemotingCommand request, InvokeMethod invokeMethod, Method method, long timeout) {
        try {
            // 写入channel
            final SocketAddress remoteAddress = channel.remoteAddress();
            channel.writeAndFlush(request).addListener(future -> {
                // 调用成功
                if (future.isSuccess()) {
                    return;
                }
                // 调用失败
                if (!RemotingUtil.channelIsAvailable(channel)) {
                    RemotingUtil.closeChannel(channel);
                }
                log.warn("send a request command to channel <" + remoteAddress + "> failed. request = " + request);
            });
        } catch (Exception e) {
            log.error("onewayInvoke failed in channel[" + channel + "], request" + request , e);
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
            if (channel == null) {
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
            if (channel == null) {
                return;
            }
            final String addressRemote = address == null ? RemotingHelper.parseChannelRemoteAddress(channel) : address;
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
        if (responseFuture != null) {
            responseFuture.putResponse(responseCommand);
            return;
        }
        // 异步缓存
        AsyncResponseFuture<?> asyncResponseFuture = this.responseFutureManager.getAsyncResponseFuture(responseCommand.getOpaque());
        if (asyncResponseFuture != null) {
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
