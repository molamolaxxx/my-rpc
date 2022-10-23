package com.mola.rpc.spring;

import com.mola.rpc.common.context.RpcContext;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.data.config.manager.RpcDataManager;
import com.mola.rpc.data.config.spring.RpcProviderDataInitBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.util.Collection;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-07-30 18:21
 **/
@Configuration
public class ApplicationDestroyHookConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ApplicationDestroyHookConfiguration.class);

    private RpcProviderDataInitBean rpcProviderDataInitBean;
    public ApplicationDestroyHookConfiguration(@Autowired RpcProviderDataInitBean rpcProviderDataInitBean) {
        this.rpcProviderDataInitBean = rpcProviderDataInitBean;
    }

    /**
     * 删除配置中心数据
     * @param rpcProviderDataInitBean
     */
    private void deleteConfigServerData(RpcProviderDataInitBean rpcProviderDataInitBean) {
        RpcContext rpcContext = rpcProviderDataInitBean.getRpcContext();
        RpcDataManager rpcDataManager = rpcProviderDataInitBean.getRpcDataManager();
        Collection<RpcMetaData> providerMetaDataCollection = rpcContext.getProviderMetaMap().values();
        for (RpcMetaData providerMetaData : providerMetaDataCollection) {
            rpcDataManager.deleteRemoteProviderData(providerMetaData,
                    rpcProviderDataInitBean.getEnvironment(), rpcProviderDataInitBean.getAppName(), rpcContext.getProviderAddress());
        }
        log.info("delete config server data success!");
    }

    @PreDestroy
    public void close() {
        // 删除配置中心数据
        deleteConfigServerData(rpcProviderDataInitBean);
    }
}
