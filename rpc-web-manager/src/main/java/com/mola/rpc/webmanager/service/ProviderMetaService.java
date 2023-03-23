package com.mola.rpc.webmanager.service;

import com.mola.rpc.common.entity.ProviderConfigData;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.data.config.manager.RpcDataManager;
import com.mola.rpc.data.config.spring.RpcProviderDataInitBean;
import com.mola.rpc.webmanager.entity.ProviderInfoEntity;
import com.mola.rpc.webmanager.repo.ProviderInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-01-25 21:36
 **/
@Service
public class ProviderMetaService {

    private static final Logger log = LoggerFactory.getLogger(ProviderMetaService.class);

    @Resource
    private RpcProviderDataInitBean rpcProviderDataInitBean;

    @Resource
    private ProviderInfoRepository providerInfoRepository;

    public Map<String, RpcMetaData> queryAllMetaDataFromConfigServer() {
        RpcDataManager<RpcMetaData> rpcDataManager = rpcProviderDataInitBean.getRpcDataManager();
        return rpcDataManager.getAllProviderMetaData();
    }

    public List<ProviderConfigData> getAllProviderConfigData(String interfaceClazz, String group, String version, String environment) {
        RpcDataManager<RpcMetaData> rpcDataManager = rpcProviderDataInitBean.getRpcDataManager();
        return rpcDataManager.getAllProviderConfigData(interfaceClazz, group, version, environment);
    }

    public void updateProviderInfoStorage(String providerKey, RpcMetaData rpcMetaData) {
        String[] splitRes = providerKey.split(":");
        if (splitRes== null || splitRes.length != 4) {
            log.error("ProviderRefreshAllHandler run failed, provider key illegal! " + providerKey);
            return;
        }
        // 2、同步更新数据库
        ProviderInfoEntity entity = new ProviderInfoEntity();
        entity.setInterfaceClazzName(splitRes[0]);
        entity.setProviderGroup(splitRes[1]);
        entity.setProviderVersion(splitRes[2]);
        entity.setProviderEnvironment(splitRes[3]);
        List<ProviderConfigData> allProviderConfigData = getAllProviderConfigData(entity.getInterfaceClazzName(),
                entity.getProviderGroup(), entity.getProviderVersion(), entity.getProviderEnvironment());
        Set<String> appNameSet = allProviderConfigData.stream().map(e -> e.getAppName()).collect(Collectors.toSet());
        entity.setProviderAppName(String.join(",", appNameSet));
        entity.setInFiber(rpcMetaData.getInFiber());
        entity.setOnline(!CollectionUtils.isEmpty(allProviderConfigData));
        entity.setProviderBeanClazz(rpcMetaData.getProviderBeanClazz());
        entity.setProviderBeanName(rpcMetaData.getProviderBeanName());
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("id","providerBeanClazz",
                        "providerAppName","inFiber", "providerBeanName", "online");
        Optional<ProviderInfoEntity> optional = providerInfoRepository.findOne(Example.of(entity, matcher));
        if (optional.isPresent()) {
            ProviderInfoEntity fromDb = optional.get();
            entity.setId(fromDb.getId());
        }
        providerInfoRepository.save(entity);
    }
}
