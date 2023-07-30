package com.mola.rpc.core.excutors.factory;

import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.common.enums.SystemInvokerClazzInterfaceEnum;
import com.mola.rpc.core.excutors.RpcExecutor;
import com.mola.rpc.core.excutors.impl.CommonRpcProviderExecutor;
import com.mola.rpc.core.excutors.impl.FiberRpcProviderExecutor;
import com.mola.rpc.core.excutors.impl.SystemRpcProviderExecutor;
import com.mola.rpc.core.properties.RpcProperties;
import com.mola.rpc.core.proto.ObjectFetcher;
import com.mola.rpc.core.proxy.InvokeMethod;
import org.springframework.util.Assert;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-04-16 23:29
 **/
public class RpcExecutorFactory {

    private final RpcExecutor commonRpcProviderExecutor;

    private final RpcExecutor fiberRpcProviderExecutor;

    private final RpcExecutor systemRpcProviderExecutor;

    private ObjectFetcher providerObjFetcher;

    public RpcExecutorFactory(RpcProperties rpcProperties, RpcContext rpcContext) {
        this.commonRpcProviderExecutor = new CommonRpcProviderExecutor(rpcProperties);
        this.systemRpcProviderExecutor = new SystemRpcProviderExecutor(rpcProperties);
        this.fiberRpcProviderExecutor = new FiberRpcProviderExecutor(rpcProperties);
    }

    /**
     * 获取不同的provider处理器
     * 1、公用线程池处理器
     * 2、自定义线程池处理器
     * 3、fiber处理器
     * @param invokeMethod
     * @return
     */
    public RpcExecutor getProviderExecutor(InvokeMethod invokeMethod, RpcMetaData providerMeta) {
        // 判断是否是系统内部调用
        if (SystemInvokerClazzInterfaceEnum.has(invokeMethod.getInterfaceClazz())) {
            return systemRpcProviderExecutor;
        }
        Assert.notNull(providerMeta, "providerMeta not found");
        if (Boolean.TRUE.equals(providerMeta.getInFiber())) {
            return fiberRpcProviderExecutor;
        }
        return commonRpcProviderExecutor;
    }
}
