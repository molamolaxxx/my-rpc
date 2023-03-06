package com.mola.rpc.core.system;

import com.mola.rpc.common.entity.RpcMetaData;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 反向代理调用
 * 场景：client与server建立连接，client端提供provider，server需要调用client的provider
 * client：
 * 1、因为server端不维护client建立的连接，所以需要client主动向server进行注册，维护注册key和channel对象的关联，服务端单独维护连接
 * 2、启动provider
 * server：
 * 1、server收到注册提醒，维护channel，并检测是否可用
 * 2、启动consumer
 * 3、在server端指定consumer的channel-key，consumer会自动在channel中进行选择
 * @date : 2023-03-05 18:55
 **/
public class ReverseInvokeHelper {

    public static String getServiceKey(RpcMetaData rpcMetaData, boolean isConsumer) {
        return String.format("%s:%s:%s", rpcMetaData.getInterfaceClazz().getName(),
                rpcMetaData.getGroup(), rpcMetaData.getVersion());
    }
}
