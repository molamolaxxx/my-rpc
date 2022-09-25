package com.mola.rpc.data.config.listener;

import com.alibaba.fastjson.JSONObject;
import com.mola.rpc.common.entity.AddressInfo;
import com.mola.rpc.common.entity.RpcMetaData;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: zk监听器实现
 * @date : 2022-09-25 21:23
 **/
public class ZkProviderConfigChangeListenerImpl implements IZkChildListener {

    private static final Logger log = LoggerFactory.getLogger(ZkProviderConfigChangeListenerImpl.class);

    private RpcMetaData consumerMetaData;

    private ZkClient zkClient;

    public ZkProviderConfigChangeListenerImpl(RpcMetaData consumerMetaData, ZkClient zkClient) {
        this.consumerMetaData = consumerMetaData;
        this.zkClient = zkClient;
    }

    @Override
    public void handleChildChange(String parentPath, List<String> childList) throws Exception {
        log.warn(this.consumerMetaData.getInterfaceClazz().getName() + ":" + JSONObject.toJSONString(childList));
        // 遍历子节点
        consumerMetaData
                .setAddressList(childList.stream()
                .map(path -> new AddressInfo(path))
                        .collect(Collectors.toList()));
    }
}
