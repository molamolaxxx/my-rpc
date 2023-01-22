package com.mola.rpc.provider;

import com.mola.rpc.client.UnitTestService;
import com.mola.rpc.common.annotation.RpcProvider;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 单元测试provider
 * @date : 2023-01-22 21:41
 **/
@RpcProvider(interfaceClazz = UnitTestService.class)
public class UnitTestServiceImpl implements UnitTestService {
}
