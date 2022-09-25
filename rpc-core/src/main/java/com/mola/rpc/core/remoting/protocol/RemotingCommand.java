package com.mola.rpc.core.remoting.protocol;


import com.mola.rpc.common.constants.RemotingCommandType;
import com.mola.rpc.core.util.RemotingSerializableUtil;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * ise客户端与服务端通信协议
 * Remoting模块中，服务器与客户端通过传递RemotingCommand来交互
 */
public class RemotingCommand {

    private static AtomicInteger RequestId = new AtomicInteger(0);

    /**
     * 0000:request
     * 0010:oneway request
     * 0001:response
     * */
    private static final int RPC_TYPE = 0; // 0, REQUEST_COMMAND  1, RESPONSE_COMMAND

    private static final int RPC_ONEWAY = 1; // 0, RPC   1, Oneway

    /**
     * Header 部分
     */
    private int code;
    private int version = 0;

    /**
     * request内部标示，保证每一个r都有唯一编号对应
     * AtomicInteger加到max_value会重置为-max_value
     */
    private int opaque = RequestId.getAndIncrement();
    private int flag = 0;
    private String remark;
    private HashMap<String, String> extFields;

    /**
     * Body 部分
     */
    private transient byte[] body;


    public RemotingCommand() {
    }

    public static RemotingCommand createResponseCommand(int code, String remark) {
        RemotingCommand cmd = new RemotingCommand();
        cmd.markResponseType();
        cmd.setCode(code);
        cmd.setRemark(remark);

        return cmd;
    }

    private byte[] buildHeader() {
        return RemotingSerializableUtil.encode(this);
    }


    public ByteBuffer encode() {
        // 头部长度占用4字节
        int length = 4;
        // header data length
        byte[] headerData = this.buildHeader();
        length += headerData.length;
        // body data length
        if (this.body != null) {
            length += body.length;
        }
        // 主体内容
        ByteBuffer result = ByteBuffer.allocate(4 + length);
        // 1、报文总长度
        result.putInt(length);
        // 2、头部长度
        result.putInt(headerData.length);
        // 3、报文头
        result.put(headerData);
        // 4、报文体
        if (this.body != null) {
            result.put(this.body);
        }
        result.flip();
        return result;
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

        ByteBuffer result = ByteBuffer.allocate(4 + length - bodyLength);

        // length，头部32位整形（4byte）表示长度，用于tcp分包
        result.putInt(length);

        // header length
        result.putInt(headerData.length);

        // header data
        result.put(headerData);

        result.flip();

        return result;
    }


    public static RemotingCommand decode(final byte[] array) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(array);
        return decode(byteBuffer);
    }


    public static RemotingCommand decode(final ByteBuffer byteBuffer) {
        int length = byteBuffer.limit();

        // 在netty-decoder中，bytebuffer已经被读取了4位长度域，所以下一个整数为header长度值
        int headerLength = byteBuffer.getInt();

        byte[] headerData = new byte[headerLength];
        byteBuffer.get(headerData);

        // limit取数组长度，需要减去长度域
        int bodyLength = length - 4 - headerLength;
        byte[] bodyData = null;
        if (bodyLength > 0) {
            bodyData = new byte[bodyLength];
            byteBuffer.get(bodyData);
        }

        RemotingCommand cmd = RemotingSerializableUtil.decode(headerData, RemotingCommand.class);
        cmd.body = bodyData;

        return cmd;
    }

    public static RemotingCommand decodeBody(final ByteBuffer byteBuffer) {
        RemotingCommand cmd = new RemotingCommand();
        cmd.body = byteBuffer.array();
        return cmd;
    }

    public void markResponseType() {
        int bits = 1 << RPC_TYPE;
        this.flag |= bits;
    }

    public boolean isResponseType() {
        int bits = 1 << RPC_TYPE;
        return (this.flag & bits) == bits;
    }

    public void markOnewayRPC() {
        int bits = 1 << RPC_ONEWAY;
        this.flag |= bits;
    }

    public boolean isOnewayRPC() {
        int bits = 1 << RPC_ONEWAY;
        return (this.flag & bits) == bits;
    }

    public int getCode() {
        return code;
    }


    public void setCode(int code) {
        this.code = code;
    }

    public RemotingCommandType getType() {
        if (this.isResponseType()) {
            return RemotingCommandType.RESPONSE_COMMAND;
        }

        return RemotingCommandType.REQUEST_COMMAND;
    }


    public int getVersion() {
        return version;
    }


    public void setVersion(int version) {
        this.version = version;
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
        if (null == extFields) {
            extFields = new HashMap<String, String>();
        }
        extFields.put(key, value);
    }


    @Override
    public String toString() {
        return "RemotingCommand [code=" + code + ", version=" + version
                + ", opaque=" + opaque + ", flag(B)=" + Integer.toBinaryString(flag) + ", remark=" + remark
                + ", extFields=" + extFields + "]";
    }

}
