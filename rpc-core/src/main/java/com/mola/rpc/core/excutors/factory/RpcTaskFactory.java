package com.mola.rpc.core.excutors.factory;

import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.common.enums.SystemInvokerClazzInterfaceEnum;
import com.mola.rpc.core.excutors.task.ProviderInvokeTask;
import com.mola.rpc.core.excutors.task.RegisterReverseChannelTask;
import com.mola.rpc.core.excutors.task.RpcTask;
import com.mola.rpc.core.proto.ObjectFetcher;
import com.mola.rpc.core.proxy.InvokeMethod;
import com.mola.rpc.core.remoting.protocol.RemotingCommand;
import io.netty.channel.Channel;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-04-22 09:39
 **/
public class RpcTaskFactory {

    /**
     * 获取不同的task
     * 1、provider执行器
     * 2、系统请求处理器
     * @param providerFetcher
     * @param invokeMethod
     * @param request
     * @param providerMeta
     * @param channel
     * @return
     */
    public RpcTask getTask(ObjectFetcher providerFetcher, InvokeMethod invokeMethod,
                           RemotingCommand request, RpcMetaData providerMeta, Channel channel) {
        // 注册反向代理
        if (SystemInvokerClazzInterfaceEnum.REVERSE_INVOKER_CLAZZ_NAME.is(invokeMethod.getInterfaceClazz())) {
            return new RegisterReverseChannelTask(
                    providerFetcher,
                    invokeMethod,
                    request,
                    providerMeta,
                    channel
            );
        }
        return new ProviderInvokeTask(
                providerFetcher,
                invokeMethod,
                request,
                providerMeta,
                channel
        );
    }
}
