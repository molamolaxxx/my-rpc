package com.mola.rpc.webmanager.service;

import com.mola.rpc.spring.autoconfig.RpcSpringConfigurationProperties;
import com.mola.rpc.webmanager.entity.GatewayMappingEntity;
import com.mola.rpc.webmanager.repo.GatewayMappingRepository;
import com.mola.rpc.webmanager.test.CsTestContext;
import junit.framework.TestCase;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@SpringBootTest(classes = {CsTestContext.class})
@RunWith(SpringRunner.class)
public class GatewayMappingServiceTest extends TestCase {

    @Mock
    private GatewayMappingRepository gatewayMappingRepository;

    @Resource
    private RpcSpringConfigurationProperties rpcSpringConfigurationProperties;

    @InjectMocks
    private GatewayMappingService gatewayMappingService;

    @Before
    public void before() {
//        MockitoAnnotations.initMocks(this);
//        // 将 userDaoMock 添加到 Spring 容器中
//        ReflectionTestUtils.setField(gatewayMappingService, "gatewayMappingRepository", gatewayMappingRepository);
        GatewayMappingEntity entity = new GatewayMappingEntity();
        entity.setId(1L);
        entity.setRequestMapping("/unit/test");
        entity.setEnvironment(rpcSpringConfigurationProperties.getEnvironment());
        Mockito.when(gatewayMappingRepository.findAll())
                .thenReturn(Lists.newArrayList(entity));
    }

    @Test
    public void loadGatewayMappingToCacheTest() {
        gatewayMappingService.loadGatewayMappingToCache();
    }
}