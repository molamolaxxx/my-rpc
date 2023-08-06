package com.mola.rpc.webmanager.test;

import com.google.common.collect.Lists;
import com.mola.rpc.webmanager.entity.ProviderInfoEntity;
import com.mola.rpc.webmanager.repo.ProviderInfoRepository;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProviderMetaServiceTest extends TestCase {

    @Mock
    private ProviderInfoRepository providerInfoRepository;

    @Test
    public void testGetById() {
        // 构造被测试对象

        // 使用 Mockito 模拟依赖对象的行为
        when(providerInfoRepository.findAll())
                .thenReturn(Lists.newArrayList(new ProviderInfoEntity()));

        List<ProviderInfoEntity> all = providerInfoRepository.findAll();
        System.out.println("d");
    }
}