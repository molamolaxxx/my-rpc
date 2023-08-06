package com.mola.rpc.webmanager.service;

import com.mola.rpc.common.utils.JSONUtil;
import com.mola.rpc.webmanager.entity.GatewayMappingEntity;
import com.mola.rpc.webmanager.repo.GatewayMappingRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-01-25 21:36
 **/
@Service
public class GatewayMappingService {

    @Resource
    private GatewayMappingRepository gatewayMappingRepository;

    public void loadGatewayMappingToCache() {
        // code
        List<GatewayMappingEntity> all = gatewayMappingRepository.findAll();
        System.out.println(JSONUtil.toJSONString(all));
    }
}
