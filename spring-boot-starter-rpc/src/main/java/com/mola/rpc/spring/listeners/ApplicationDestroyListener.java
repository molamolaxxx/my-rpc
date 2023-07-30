package com.mola.rpc.spring.listeners;

import com.mola.rpc.core.proto.ProtoRpcConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-07-30 18:21
 **/
public class ApplicationDestroyListener {

    private static final Logger log = LoggerFactory.getLogger(ApplicationDestroyListener.class);

    private final ProtoRpcConfigFactory protoRpcConfigFactory;

    public ApplicationDestroyListener(ProtoRpcConfigFactory protoRpcConfigFactory) {
        this.protoRpcConfigFactory = protoRpcConfigFactory;
    }

    @PreDestroy
    public void close() {
        // 删除配置中心数据
        this.protoRpcConfigFactory.shutdown();
    }
}
