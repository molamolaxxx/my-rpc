package com.mola.rpc.spring.listeners;

import com.mola.rpc.core.properties.RpcProperties;
import com.mola.rpc.core.proto.ProtoRpcConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import javax.annotation.Resource;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-05-28 15:57
 **/
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(ApplicationStartupListener.class);

    @Resource
    private RpcProperties rpcProperties;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ProtoRpcConfigFactory protoRpcConfigFactory = ProtoRpcConfigFactory.fetch();
        if (rpcProperties.getStartConfigServer()) {
            // config server配置，上报服务
            protoRpcConfigFactory.getRpcProviderDataInitBean().init(
                    protoRpcConfigFactory.getRpcProperties()
            );
        }
        // 反向provider注册，上报反向代理服务
        protoRpcConfigFactory.registerReverseProvider();
        log.info("after application start, init config server and register reverse providers.");
    }
}

