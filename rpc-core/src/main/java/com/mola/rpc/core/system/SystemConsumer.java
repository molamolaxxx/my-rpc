package com.mola.rpc.core.system;

import com.alibaba.fastjson.JSONObject;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.core.proto.RpcInvoker;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 系统级别消费者
 * @date : 2023-03-06 10:53
 **/
public class SystemConsumer<T> {

    private T consumerInner;

    private RpcMetaData rpcMetaData;

    private SystemConsumer(Class<T> consumerInterface) {
        String consumerName = "systemConsumer";
        this.rpcMetaData = new RpcMetaData();
        this.consumerInner = RpcInvoker.consumer(consumerInterface, rpcMetaData, consumerName);
    }
    public static class Multipart {
        public static SystemConsumer<ReverseInvokerCaller> reverseInvokerCaller = new SystemConsumer(ReverseInvokerCaller.class);
    }

    public T fetch() {
        return consumerInner;
    }

    public void setAppointedAddress(List<String> address) {
        Assert.notEmpty(address, "changeAppointedAddress failed, address is empty, meta = " + JSONObject.toJSONString(rpcMetaData));
        this.rpcMetaData.setAppointedAddress(address);
    }

    public interface ReverseInvokerCaller {
        void register(String channelKey);
    }

}
