package com.mola.rpc.core.remoting.netty.pool;

import com.mola.rpc.core.util.RemotingHelper;
import com.mola.rpc.core.util.RemotingUtil;
import io.netty.channel.Channel;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 包装了channel对象
 * 1、channel探活
 * 2、channel获取
 * @date : 2023-03-23 12:34
 **/
public abstract class AbstractChannelWrapper {

    /**
     * channel是否可用
     * @return
     */
    protected abstract boolean isOk();

    /**
     * 获取channel
     * @return
     */
    protected abstract Channel getChannel();

    public void closeChannel() {
        Channel channel = this.getChannel();
        if (channel != null) {
            RemotingUtil.closeChannel(channel);
        }
    }

    public String getRemoteAddress() {
        return RemotingHelper.parseChannelRemoteAddr(this.getChannel());
    }
}
