package com.mola.rpc.core.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * @author : molamola
 * @Project: InvincibleSchedulerEngine
 * @Description:
 * @date : 2020-09-02 22:45
 **/
public class RemotingUtil {

    private static final Logger logger = LoggerFactory.getLogger(RemotingUtil.class);

    /**
     * 获取本地非回环地址
     * @return
     */
    public static String getLocalAddress() {
        try {
            // 遍历网卡，查找一个非回路ip地址并返回
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            ArrayList<String> ipv4Result = new ArrayList<>();
            ArrayList<String> ipv6Result = new ArrayList<>();
            while (enumeration.hasMoreElements()) {
                final NetworkInterface networkInterface = enumeration.nextElement();
                final Enumeration<InetAddress> en = networkInterface.getInetAddresses();
                while (en.hasMoreElements()) {
                    final InetAddress address = en.nextElement();
                    if (!address.isLoopbackAddress()) {
                        if (address instanceof Inet6Address) {
                            ipv6Result.add(normalizeHostAddress(address));
                        }
                        else {
                            ipv4Result.add(normalizeHostAddress(address));
                        }
                    }
                }
            }

            // 优先使用ipv4
            if (!ipv4Result.isEmpty()) {
                for (String ip : ipv4Result) {
                    if (ip.startsWith("127.0") || ip.startsWith("192.168")) {
                        continue;
                    }

                    if(!ip.startsWith("10.")) {
                        continue;
                    }
                    logger.warn("[RemotingUtil]: ipv4Result ip:" + ip + ", ipv4Result:" + ipv4Result);

                    return ip;
                }
                // 取最后一个
                return ipv4Result.get(ipv4Result.size() - 1);
            }
            // 然后使用ipv6
            else if (!ipv6Result.isEmpty()) {
                return ipv6Result.get(0);
            }
            // 然后使用本地ip
            final InetAddress localHost = InetAddress.getLocalHost();
            return normalizeHostAddress(localHost);
        }
        catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String normalizeHostAddress(final InetAddress localHost) {
        if (localHost instanceof Inet6Address) {
            return "[" + localHost.getHostAddress() + "]";
        }
        else {
            return localHost.getHostAddress();
        }
    }

    public static void closeChannel(Channel channel) {
        final String addrRemote = RemotingHelper.parseChannelRemoteAddr(channel);
        channel.close().addListener((ChannelFutureListener) future ->
                logger.info("closeChannel: close the connection to channel[" + channel.toString() + "] result: {" + future.isSuccess() + "}"));
    }

    public static boolean channelIsAvailable(Channel channel) {
        return channel != null && channel.isActive() && channel.isWritable();
    }

    public static String socketAddress2String(final SocketAddress addr) {
        StringBuilder sb = new StringBuilder();
        InetSocketAddress inetSocketAddress = (InetSocketAddress) addr;
        sb.append(inetSocketAddress.getAddress().getHostAddress());
        sb.append(":");
        sb.append(inetSocketAddress.getPort());
        return sb.toString();
    }
}
