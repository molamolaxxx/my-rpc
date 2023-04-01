package com.mola.rpc.webmanager.config;

import com.mola.rpc.data.config.spring.RpcProviderDataInitBean;
import com.mola.rpc.webmanager.entity.RpcWatcher;
import com.mola.rpc.webmanager.service.ProviderMetaService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 监听configServer服务变更
 * @date : 2023-03-25 23:24
 **/
@Configuration
public class AddressChangeListenerConfig implements InitializingBean {

    @Resource
    private RpcProviderDataInitBean rpcProviderDataInitBean;

    @Resource
    private ProviderMetaService providerMetaService;

    @Override
    public void afterPropertiesSet() throws Exception {
        rpcProviderDataInitBean.addAddressChangeListener(rpcMetaData -> {
            if (!(rpcMetaData instanceof RpcWatcher)) {
                return;
            }
            RpcWatcher rpcWatcher = (RpcWatcher) rpcMetaData;
            providerMetaService.updateProviderInfoStorage(rpcWatcher.getProviderInfoEntity());
        });
    }
}
