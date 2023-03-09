package com.mola.rpc.core.remoting.netty;

import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.core.properties.RpcProperties;
import com.mola.rpc.core.remoting.handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-08-06 11:12
 **/
public class NettyRemoteServer {

    private static final Logger log = LoggerFactory.getLogger(NettyRemoteServer.class);

    /**
     * 启动器
     */
    private final ServerBootstrap serverBootstrap;

    /**
     * worker-group
     */
    private final EventLoopGroup eventLoopGroupWorker;

    /**
     * boss-group
     */
    private final EventLoopGroup eventLoopGroupBoss;

    /**
     * 默认执行pipeline中事件的线程组
     */
    private DefaultEventExecutorGroup defaultEventExecutorGroup;

    private RpcProperties rpcProperties;

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

    private Channel serverChannel;

    public NettyRemoteServer(NettyRpcRequestHandler requestHandler, NettyRpcResponseHandler responseHandler) {
        Assert.notNull(requestHandler, "requestHandler is required");
        Assert.notNull(responseHandler, "responseHandler is required");
        this.requestHandler = requestHandler;
        this.responseHandler  = responseHandler;
        this.serverBootstrap = new ServerBootstrap();
        // 初始化boss，负责建立连接
        this.eventLoopGroupBoss = new NioEventLoopGroup(1, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("netty-boss-selector-%d", this.threadIndex.incrementAndGet()));
            }
        });

        // 初始化worker，负责监听读写事件
        this.eventLoopGroupWorker = new NioEventLoopGroup(10, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("netty-worker-selector-%d-%d", 10,
                        this.threadIndex.incrementAndGet()));
            }
        });
    }

    public void start() {
        if (ignoreNettyServerStart()) {
            log.info("[NettyRemoteServer]:there are no provider or reverse consumer registered, netty server will not start");
            return;
        }
        defaultEventExecutorGroup = new DefaultEventExecutorGroup(
                8,
                new ThreadFactory() {
                    AtomicInteger threadIndex = new AtomicInteger(0);
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "netty-server-pipeline-thread-" + this.threadIndex.incrementAndGet());
                    }
                }
        );

        ServerBootstrap bootstrap = this.serverBootstrap.group(this.eventLoopGroupBoss, this.eventLoopGroupWorker)
                .channel(NioServerSocketChannel.class)
                // 保存完成三次握手的连接队列数，即accept queue大小，超过队列最大值将不再进行三次握手
                .option(ChannelOption.SO_BACKLOG, 1024)
                // 端口释放后不用等待可立刻使用
                .option(ChannelOption.SO_REUSEADDR, true)
                // 关闭tcp内置心跳检测,使用netty自带handler
                .option(ChannelOption.SO_KEEPALIVE, false)
                // 禁用了Nagle算法，允许小包发送（否则会因为tcp缓存导致rt延时）
                .childOption(ChannelOption.TCP_NODELAY, true)
                // 发送缓冲区大小
                .option(ChannelOption.SO_SNDBUF, 65535)
                // 接受缓冲区大小
                .option(ChannelOption.SO_RCVBUF, 65535)
                .localAddress(new InetSocketAddress(rpcProperties.getServerPort()))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                defaultEventExecutorGroup,
                                new NettyEncoder(),
                                new NettyDecoder(),
                                new IdleStateHandler(0, 0, 120),
                                new NettyServerConnectManageHandler(), // 监听与连接相关的事件
                                requestHandler, // in
                                responseHandler // in
                        );
                    }
                });


        // 启动netty
        try {
            ChannelFuture sync = this.serverBootstrap.bind().sync();
            InetSocketAddress address = (InetSocketAddress) sync.channel().localAddress();
            log.info("[NettyRemoteServer]: netty server start at " + address);
//            this.serverChannel = sync.channel();
        }
        catch (InterruptedException e1) {
            throw new RuntimeException("this.serverBootstrap.bind().sync() InterruptedException", e1);
        }
        this.startFlag.compareAndSet(false, true);
    }

    public void shutdown() {
//        if (serverChannel != null) {
//            log.info("[NettyRemoteServer]: netty server close");
//            serverChannel.close();
//            serverChannel = null;
//        }
        if (eventLoopGroupBoss != null) {
            eventLoopGroupBoss.shutdownGracefully();
        }
        if (eventLoopGroupWorker != null) {
            eventLoopGroupWorker.shutdownGracefully();
        }
        if (defaultEventExecutorGroup != null) {
            defaultEventExecutorGroup.shutdownGracefully();
        }
    }

    private boolean ignoreNettyServerStart() {
        Map<String, RpcMetaData> consumerMetaMap = rpcContext.getConsumerMetaMap();
        for (RpcMetaData rpcMetaData : consumerMetaMap.values()) {
            if (rpcMetaData.getReverseMode()) {
                return false;
            }
        }
        return rpcContext.getProviderMetaMap().size() == 0;
    }

    public RpcProperties getRpcProperties() {
        return rpcProperties;
    }

    public void setRpcProperties(RpcProperties rpcProperties) {
        this.rpcProperties = rpcProperties;
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
