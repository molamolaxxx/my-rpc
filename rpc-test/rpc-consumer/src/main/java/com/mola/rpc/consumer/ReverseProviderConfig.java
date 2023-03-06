package com.mola.rpc.consumer;

import com.google.common.collect.Lists;
import com.mola.rpc.client.UserService;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.core.proto.RpcInvoker;

import javax.annotation.PostConstruct;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-07-25 23:37
 **/
//@Configuration
public class ReverseProviderConfig {

    @PostConstruct
    public void init() {
        // 配置
        RpcMetaData rpcMetaData = new RpcMetaData();
        rpcMetaData.setReverseMode(Boolean.TRUE);
        rpcMetaData.setReverseModeConsumerAddress(Lists.newArrayList("127.0.0.1:9003"));
        RpcInvoker.provider(
                UserService.class,
                new UserService() {
                    @Override
                    public String queryUserName(String id) {
                        return id;
                    }
                },
                rpcMetaData
        );
    }

}
