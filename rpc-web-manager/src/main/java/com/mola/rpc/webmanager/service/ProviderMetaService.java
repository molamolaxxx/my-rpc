package com.mola.rpc.webmanager.service;

import com.mola.rpc.common.utils.JSONUtil;
import com.mola.rpc.common.entity.ProviderConfigData;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.core.properties.RpcProperties;
import com.mola.rpc.data.config.manager.RpcDataManager;
import com.mola.rpc.data.config.spring.RpcProviderDataInitBean;
import com.mola.rpc.webmanager.entity.ProviderInfoEntity;
import com.mola.rpc.webmanager.repo.ProviderInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
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

    @Resource
    private RpcProperties rpcProperties;

    public Map<String, RpcMetaData> queryAllMetaDataFromConfigServer() {
        RpcDataManager<RpcMetaData> rpcDataManager = rpcProviderDataInitBean.getRpcDataManager();
        return rpcDataManager.getAllProviderMetaData();
    }

    public List<ProviderConfigData> getAllProviderConfigData(String interfaceClazz, String group, String version, String environment) {
        RpcDataManager<RpcMetaData> rpcDataManager = rpcProviderDataInitBean.getRpcDataManager();
        return rpcDataManager.getAllProviderConfigData(interfaceClazz, group, version, environment);
    }

    public ProviderInfoEntity updateProviderInfoStorage(String providerKey, RpcMetaData rpcMetaData) {
        String[] splitRes = providerKey.split(":");
        if (splitRes== null || splitRes.length != 3) {
            log.error("ProviderRefreshAllHandler run failed, provider key illegal! " + providerKey);
            return null;
        }
        // 2、同步更新数据库
        ProviderInfoEntity entity = new ProviderInfoEntity();
        entity.setInterfaceClazzName(splitRes[0]);
        entity.setProviderGroup(splitRes[1]);
        entity.setProviderVersion(splitRes[2]);
        entity.setProviderEnvironment(rpcProperties.getEnvironment());
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
            entity.setCreator(fromDb.getCreator());
            entity.setGmtCreate(fromDb.getGmtCreate());
        } else {
            entity.setCreator("sys");
            entity.setGmtCreate(new Date());
        }
        entity.setModifier("sys");
        entity.setGmtModify(new Date());
        providerInfoRepository.save(entity);
        return entity;
    }

    public ProviderInfoEntity updateProviderInfoStorage(ProviderInfoEntity entity) {
        Assert.notNull(entity, "entity is null");
        Optional<ProviderInfoEntity> optional = providerInfoRepository.findById(entity.getId());
        Assert.isTrue(optional.isPresent(), "db is null, entity = " + JSONUtil.toJSONString(entity));
        ProviderInfoEntity fromDb = optional.get();
        entity.setId(fromDb.getId());
        entity.setModifier("sys");
        entity.setGmtModify(new Date());
        List<ProviderConfigData> allProviderConfigData = getAllProviderConfigData(entity.getInterfaceClazzName(),
                entity.getProviderGroup(), entity.getProviderVersion(), entity.getProviderEnvironment());
        Set<String> appNameSet = allProviderConfigData.stream().map(e -> e.getAppName()).collect(Collectors.toSet());
        entity.setProviderAppName(String.join(",", appNameSet));
        entity.setOnline(!CollectionUtils.isEmpty(allProviderConfigData));
        providerInfoRepository.save(entity);
        return entity;
    }
}
