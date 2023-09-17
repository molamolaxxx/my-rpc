package com.mola.rpc.core.system;

import com.mola.rpc.core.properties.RpcProperties;
import com.mola.rpc.core.proto.ProtoRpcConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 直接使用的配置工厂
 * @date : 2022-10-23 11:38
 **/
public class SystemRpcConfigFactory extends ProtoRpcConfigFactory {

    private static final Logger log = LoggerFactory.getLogger(SystemRpcConfigFactory.class);

    protected SystemRpcConfigFactory(){}
    private static class Singleton{
        private final static SystemRpcConfigFactory systemRpcConfigFactory = new SystemRpcConfigFactory();
    }

    public static SystemRpcConfigFactory initAndGet(){
        RpcProperties rpcProperties = new RpcProperties();
        rpcProperties.setEnvironment("my-rpc-system");
        rpcProperties.setStartConfigServer(Boolean.FALSE);
        Singleton.systemRpcConfigFactory.init(rpcProperties);
        return Singleton.systemRpcConfigFactory;
    }
}
