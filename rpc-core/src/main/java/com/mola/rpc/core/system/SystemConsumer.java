package com.mola.rpc.core.system;

import com.mola.rpc.common.annotation.OnewayInvoke;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.common.utils.ClazzUtil;
import com.mola.rpc.core.proto.RpcInvoker;

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
        rpcMetaData.setOnewayExecuteMethods(
                ClazzUtil.getMethodNameFilterByAnnotation(consumerInterface, OnewayInvoke.class));
        this.consumerInner = RpcInvoker.consumer(consumerInterface, rpcMetaData, consumerName);
    }

    public static class Multipart {
        public static SystemConsumer<ReverseInvokerCaller> reverseInvokerCaller =
                new SystemConsumer(ReverseInvokerCaller.class);
    }

    public T fetch() {
        return consumerInner;
    }

    public void setAppointedAddress(List<String> address) {
        this.rpcMetaData.setAppointedAddress(address);
    }

    public interface ReverseInvokerCaller {

        @OnewayInvoke
        void register(String channelKey);
    }
}
