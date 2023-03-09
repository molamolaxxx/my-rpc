package com.mola.rpc.spring;

import com.mola.rpc.core.proto.ProtoRpcConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-07-30 18:21
 **/
@Configuration
public class ApplicationDestroyHookConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ApplicationDestroyHookConfiguration.class);

    private ProtoRpcConfigFactory protoRpcConfigFactory;


    public ApplicationDestroyHookConfiguration(@Autowired ProtoRpcConfigFactory protoRpcConfigFactory) {
        this.protoRpcConfigFactory = protoRpcConfigFactory;
    }

    @PreDestroy
    public void close() {
        // 删除配置中心数据
        this.protoRpcConfigFactory.shutdown();
    }
}
