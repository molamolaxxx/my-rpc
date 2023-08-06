package com.mola.rpc.consumer.test;

import com.mola.rpc.common.ext.ExtensionRegistryManager;
import com.mola.rpc.common.interceptor.ReverseProxyRegisterInterceptor;
import com.mola.rpc.consumer.test.interceptors.TestInterceptor;
import com.mola.rpc.core.proto.ProtoRpcConfigFactory;
import com.mola.rpc.test.tool.PitchInvokeContext;
import com.mola.rpc.test.tool.PitchingContext;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import com.mola.rpc.common.utils.AssertUtil;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-06-03 14:12
 **/
@SpringBootTest(classes = ConsumerTestContext.class)
@RunWith(SpringRunner.class)
public class ExtensionRegistryManagerTest {
    
    @Resource
    private ExtensionRegistryManager extensionRegistryManager;

    public ExtensionRegistryManagerTest() {
        PitchingContext.start();
    }

    @Test
    public void testInterceptors() {
        ProtoRpcConfigFactory fetch = ProtoRpcConfigFactory.fetch();
        ExtensionRegistryManager extensionRegistryManager = fetch.getExtensionRegistryManager();

        List<PitchInvokeContext> getInterceptorsCtx = PitchingContext.fetchInvokeContext(
                "com.mola.rpc.common.ext.ExtensionRegistryManager", "getInterceptors");
        List<TestInterceptor> interceptors = extensionRegistryManager.getInterceptors(TestInterceptor.class);
        AssertUtil.isTrue(interceptors.size() == 1, "size is error");

        List<ReverseProxyRegisterInterceptor> testReverseInterceptor = extensionRegistryManager.getInterceptors(ReverseProxyRegisterInterceptor.class);
        AssertUtil.isTrue(testReverseInterceptor.size() == 3, "size is error");
        AssertUtil.isTrue(testReverseInterceptor.get(0).priority() < testReverseInterceptor.get(1).priority()
                , "priority is error");
        AssertUtil.isTrue(testReverseInterceptor.get(1).priority() < testReverseInterceptor.get(2).priority()
                , "priority is error");
    }

    @After
    public void after() {
        PitchingContext.finish();
    }
}
