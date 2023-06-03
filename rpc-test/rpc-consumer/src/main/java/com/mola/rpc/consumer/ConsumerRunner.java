package com.mola.rpc.consumer;

import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.common.lifecycle.ConsumerLifeCycle;
import com.mola.rpc.common.lifecycle.ConsumerLifeCycleHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-07-25 23:45
 **/
@SpringBootApplication
public class ConsumerRunner {
    public static void main(String[] args) {
        ConsumerLifeCycle.fetch().addListener(new ConsumerLifeCycleHandler() {
            @Override
            public void afterInitialize(RpcMetaData consumerMetaData) {
                System.out.println("afterInitialize  " + consumerMetaData );
            }

            @Override
            public void afterAddressChange(RpcMetaData consumerMetaData) {
                System.out.println("afterAddressChange  " + consumerMetaData );
            }
        });
        SpringApplication.run(ConsumerRunner.class, args);
    }
}
