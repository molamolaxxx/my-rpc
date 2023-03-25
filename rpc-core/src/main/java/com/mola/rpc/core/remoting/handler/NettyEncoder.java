package com.mola.rpc.core.remoting.handler;

import com.mola.rpc.core.remoting.protocol.RemotingCommand;
import com.mola.rpc.core.util.RemotingHelper;
import com.mola.rpc.core.util.RemotingUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * @author : molamola
 * @Description: 编码器，输出时将RemotingCommand对象转化成字节流
 * 仅作用于对外输出
 **/
public class NettyEncoder extends MessageToByteEncoder<RemotingCommand> {

    private static final Logger log = LoggerFactory.getLogger(NettyEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, RemotingCommand remotingCommand, ByteBuf out) throws Exception {

        try {
            // 序列化头部并发送
            ByteBuffer header = remotingCommand.encodeHeader();
            out.writeBytes(header);
            // 获取报文体并发送
            byte[] body = remotingCommand.getBody();
            if (body != null) {
                out.writeBytes(body);
            }
        } catch (Throwable t) {
            log.error("encode exception, " + RemotingHelper.parseChannelRemoteAddr(ctx.channel()), t);
            if (remotingCommand != null) {
                log.error(remotingCommand.toString());
            }
            // 这里关闭后， 会在pipeline中产生事件，通过具体的close事件来清理数据结构
            RemotingUtil.closeChannel(ctx.channel());
        }
    }
}
