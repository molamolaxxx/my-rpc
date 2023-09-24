package com.mola.rpc.core.remoting.protocol;


import com.mola.rpc.common.constants.RemotingCommandType;
import com.mola.rpc.core.util.BytesUtil;
import com.mola.rpc.core.util.RemotingSerializableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.CRC32;


/**
 * 客户端与服务端通信协议
 * Remoting模块中，服务器与客户端通过传递RemotingCommand来交互
 */
public class RemotingCommand {

    protected static final Logger log = LoggerFactory.getLogger(RemotingCommand.class);

    private static AtomicInteger RequestId = new AtomicInteger(0);


    private int flag = 0;

    /**
     * 0000:request
     * 0010:oneway request
     * */
    private static final int RPC_ONEWAY_FLAG = 2;

    /**
     * Header 部分
     */
    private int code;

    /**
     * request内部标示，保证每一个r都有唯一编号对应
     * AtomicInteger加到max_value会重置为-max_value
     */
    private int opaque = RequestId.getAndIncrement();
    private String remark;
    private HashMap<String, String> extFields;

    /**
     * Body 部分
     */
    private transient byte[] body;


    /**
     * crc校验 部分
     */
    private transient long crc32;


    public RemotingCommand() {
    }


    private byte[] buildHeader() {
        return RemotingSerializableUtil.encode(this);
    }


    public ByteBuffer encodeHeader() {
        return encodeHeader(this.body != null ? this.body.length : 0);
    }


    /**
     * 只打包Header，body部分独立传输
     */
    public ByteBuffer encodeHeader(final int bodyLength) {
        // 1> header length size
        int length = 4;

        // 2> header data length
        byte[] headerData = this.buildHeader();
        length += headerData.length;

        // 3> body data length
        length += bodyLength;

        // 4> crc32
        length += (bodyLength == 0 ? 0 : 8);

        // 4（总长度int）+ 4（header长度int）+ header
        ByteBuffer result = ByteBuffer.allocate(8 + headerData.length);

        // length，头部32位整形（4byte）表示长度，用于tcp分包
        result.putInt(length);

        // header length
        result.putInt(headerData.length);

        // header data
        result.put(headerData);

        result.flip();

        return result;
    }


    public static RemotingCommand decode(final ByteBuffer byteBuffer) {
        int length = byteBuffer.limit();

        // 在netty-decoder中，bytebuffer已经被读取了4位长度域，所以下一个整数为header长度值
        int headerLength = byteBuffer.getInt();

        byte[] headerData = new byte[headerLength];
        byteBuffer.get(headerData);

        RemotingCommand cmd = RemotingSerializableUtil.decode(headerData, RemotingCommand.class);

        // limit取数组长度，需要减去长度域（12字节 = crc32(8字节) + 记录headerLength的int位）
        int bodyLength = length - 12 - headerLength;
        byte[] bodyData = null;
        if (bodyLength > 0) {
            bodyData = new byte[bodyLength];
            byteBuffer.get(bodyData);

            // 报文体
            cmd.body = bodyData;

            // crc校验码
            cmd.crc32 = byteBuffer.getLong();
        }

        return cmd;
    }

    public boolean crc32Check() {
        if (body == null || body.length == 0) {
            return true;
        }
        CRC32 crc32 = new CRC32();
        crc32.update(body);
        return crc32.getValue() == this.crc32;
    }

    /**
     * 构建协议包
     * @param result 返回的结果
     * @return
     */
    public static RemotingCommand build(RemotingCommand request, Object result, int commandCode, String remark) {
        RemotingCommand cmd = new RemotingCommand();
        cmd.setCode(commandCode);
        cmd.setRemark(remark);
        cmd.setOpaque(request.getOpaque());
        // 1、构建body
        byte[] body = null;
        try {
            body = BytesUtil.objectToBytes(result);
        } catch (Throwable e) {
            log.error("[NettyServerHandler]: objectToBytes error"
                    + ", result:" + RemotingSerializableUtil.toJson(result, false), e);
            throw e;
        }

        cmd.setCode(commandCode);
        cmd.setBody(body);
        return cmd;
    }

    public boolean isResponseType() {
        return this.code != RemotingCommandCode.NORMAL;
    }

    public int getCode() {
        return code;
    }


    public void setCode(int code) {
        this.code = code;
    }

    public void markOnewayInvoke() {
        this.flag |= RPC_ONEWAY_FLAG;
    }

    public boolean isOnewayInvoke() {
        return (this.flag & RPC_ONEWAY_FLAG) != 0;
    }

    public RemotingCommandType getType() {
        if (this.isResponseType()) {
            return RemotingCommandType.RESPONSE_COMMAND;
        }

        return RemotingCommandType.REQUEST_COMMAND;
    }

    public int getOpaque() {
        return opaque;
    }


    public void setOpaque(int opaque) {
        this.opaque = opaque;
    }


    public int getFlag() {
        return flag;
    }


    public void setFlag(int flag) {
        this.flag = flag;
    }


    public String getRemark() {
        return remark;
    }


    public void setRemark(String remark) {
        this.remark = remark;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public long getCrc32() {
        return crc32;
    }

    public HashMap<String, String> getExtFields() {
        return extFields;
    }


    public void setExtFields(HashMap<String, String> extFields) {
        this.extFields = extFields;
    }


    public static int createNewRequestId() {
        return RequestId.incrementAndGet();
    }


    public void addExtField(String key, String value) {
        if (extFields == null) {
            extFields = new HashMap<String, String>();
        }
        extFields.put(key, value);
    }

    @Override
    public String toString() {
        return "RemotingCommand [code=" + code
                + ", opaque=" + opaque + ", flag(B)=" + Integer.toBinaryString(flag) + ", remark=" + remark
                + ", extFields=" + extFields + "]";
    }
}
