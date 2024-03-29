package com.mola.rpc.core.excutors.task;

import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.core.proto.ObjectFetcher;
import com.mola.rpc.core.proxy.InvokeMethod;
import com.mola.rpc.core.remoting.protocol.RemotingCommand;
import com.mola.rpc.core.remoting.protocol.RemotingCommandCode;
import com.mola.rpc.core.util.RemotingUtil;
import io.netty.channel.Channel;
import com.mola.rpc.common.utils.AssertUtil;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-04-17 00:01
 **/
public class ProviderInvokeTask extends RpcTask {

    public ProviderInvokeTask(ObjectFetcher providerObjFetcher, InvokeMethod invokeMethod,
                              RemotingCommand request, RpcMetaData providerMeta, Channel channel) {
        super(providerObjFetcher, invokeMethod, request, providerMeta, channel);
    }

    @Override
    protected Object runTask() {
        AssertUtil.notNull(providerMeta, "providerMeta not found in ProviderInvokeTask");
        RemotingCommand response = null;
        Object result = null;
        // 反射调用
        try {
            Object providerBean = providerObjFetcher.getObject(providerMeta);
            result = invokeMethod.invoke(providerBean);
            if (request.isOnewayInvoke()) {
                return null;
            }
            response = RemotingCommand.build(request, result, RemotingCommandCode.SUCCESS, null);
        } catch (Exception e) {
            response = RemotingCommand.build(request, null, RemotingCommandCode.SYSTEM_ERROR, e.getMessage());
            log.error("server system error!, message = " + request.toString(), e);
        }
        AssertUtil.notNull(response, "response is null" + request.toString());
        // 发送响应
        RemotingUtil.sendResponse(response, channel);
        return result;
    }
}
