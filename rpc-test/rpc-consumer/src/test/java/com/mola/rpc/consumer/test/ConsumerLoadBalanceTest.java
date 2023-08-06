package com.mola.rpc.consumer.test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mola.rpc.client.ServerResponse;
import com.mola.rpc.client.UnitTestService;
import com.mola.rpc.common.context.InvokeContext;
import com.mola.rpc.core.loadbalance.LoadBalance;
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
import java.util.Map;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 负载均衡测试
 * @date : 2023-05-28 13:06
 **/
@SpringBootTest(classes = ConsumerTestContext.class)
@RunWith(SpringRunner.class)
public class ConsumerLoadBalanceTest {

    @Resource
    protected UnitTestService unitTestServiceUseRandom;

    @Resource
    protected UnitTestService unitTestServiceUseRoundRobin;

    @Resource
    protected UnitTestService unitTestServiceUseHash;

    @Resource
    protected UnitTestService unitTestServiceUseHashAppoint;

    @Resource
    protected UnitTestService unitTestServiceRoundRobinAppoint;

    @Resource
    protected LoadBalance loadBalance;


    public ConsumerLoadBalanceTest() {
        PitchingContext.start();
    }

    @Test
    public void testRandom() {
        String input1 = "input1:" + System.currentTimeMillis();

        for (int i = 0; i < 1000; i++) {
            ServerResponse<String> response = unitTestServiceUseRandom.test001(input1);
        }

        List<PitchInvokeContext> getTargetProviderAddress = PitchingContext
                .fetchInvokeContext("com.mola.rpc.core.loadbalance.LoadBalance",
                        "getTargetProviderAddress");
        AssertUtil.isTrue(getTargetProviderAddress.size() == 1000, "InvokeContext error");
        Map<String, Integer> cntMap = Maps.newHashMap();
        for (PitchInvokeContext invokeContext : getTargetProviderAddress) {
            String address = (String) invokeContext.getResult();
            cntMap.putIfAbsent(address, 0);
            cntMap.computeIfPresent(address, (k, v) -> v + 1);
        }
        for (Integer value : cntMap.values()) {
            AssertUtil.isTrue(value > 1000 / (cntMap.size() + 1), "cnt is error");
        }
    }

    @Test
    public void testRoundRobin() {
        String input1 = "input1:" + System.currentTimeMillis();

        for (int i = 0; i < 1000; i++) {
            ServerResponse<String> response = unitTestServiceUseRoundRobin.test001(input1);
        }

        List<PitchInvokeContext> getTargetProviderAddress = PitchingContext
                .fetchInvokeContext("com.mola.rpc.core.loadbalance.LoadBalance",
                        "getTargetProviderAddress");
        AssertUtil.isTrue(getTargetProviderAddress.size() == 1000, "InvokeContext error");
        Map<String, Integer> cntMap = Maps.newHashMap();
        for (PitchInvokeContext invokeContext : getTargetProviderAddress) {
            String address = (String) invokeContext.getResult();
            cntMap.putIfAbsent(address, 0);
            cntMap.computeIfPresent(address, (k, v) -> v + 1);
        }
        for (Integer value : cntMap.values()) {
            AssertUtil.isTrue(value >= 1000 / cntMap.size(), "cnt is error");
            AssertUtil.isTrue(value <= (1000 / cntMap.size()) + 1, "cnt is error");
        }
    }

    @Test
    public void testHash() {
        String input1 = "input1:" + System.currentTimeMillis();
        for (int i = 0; i < 200; i++) {
            ServerResponse<String> response = unitTestServiceUseHash.test001(input1);
        }

        List<PitchInvokeContext> getTargetProviderAddress = PitchingContext
                .fetchInvokeContext("com.mola.rpc.core.loadbalance.LoadBalance",
                        "getTargetProviderAddress");

        AssertUtil.isTrue(getTargetProviderAddress.size() == 200, "InvokeContext error");
        Map<String, Integer> cntMap = Maps.newHashMap();
        for (PitchInvokeContext invokeContext : getTargetProviderAddress) {
            String address = (String) invokeContext.getResult();
            cntMap.putIfAbsent(address, 0);
            cntMap.computeIfPresent(address, (k, v) -> v + 1);
        }
        AssertUtil.isTrue(cntMap.size() == 1, "cntMap is error");
        for (Integer value : cntMap.values()) {
            AssertUtil.isTrue(value == 200, "cntMap value is error");
        }
    }

    @Test
    public void testStaticAppoint() {
        String input1 = "input1:" + System.currentTimeMillis();
        for (int i = 0; i < 200; i++) {
            try {
                ServerResponse<String> response = unitTestServiceUseHashAppoint.test001(input1);
            } catch (Exception ignore) {}
        }

        List<PitchInvokeContext> getTargetProviderAddress = PitchingContext
                .fetchInvokeContext("com.mola.rpc.core.loadbalance.LoadBalance",
                        "getTargetProviderAddress");

        AssertUtil.isTrue(getTargetProviderAddress.size() == 200, "InvokeContext error");
        Map<String, Integer> cntMap = Maps.newHashMap();
        for (PitchInvokeContext invokeContext : getTargetProviderAddress) {
            String address = (String) invokeContext.getResult();
            cntMap.putIfAbsent(address, 0);
            cntMap.computeIfPresent(address, (k, v) -> v + 1);
        }
        AssertUtil.isTrue(cntMap.size() == 1, "cntMap is error");
        for (Integer value : cntMap.values()) {
            AssertUtil.isTrue(value == 200, "cntMap value is error");
        }
    }

    @Test
    public void testAppoint() {
        String input1 = "input1:" + System.currentTimeMillis();

        for (int i = 0; i < 225; i++) {
            try {
                InvokeContext.appointProviderAddressList(Lists.newArrayList(
                        "127.0.0.1:9238", "127.0.0.1:9239", "127.0.0.1:9211",
                        "127.0.0.1:9248", "127.0.0.1:9339", "127.0.0.1:9212",
                        "127.0.0.1:9258", "127.0.0.1:9439", "127.0.0.1:9213",
                        "127.0.0.1:9268", "127.0.0.1:9539", "127.0.0.1:9214",
                        "127.0.0.1:9278", "127.0.0.1:9639", "127.0.0.1:9215"
                ));
                ServerResponse<String> response = unitTestServiceRoundRobinAppoint.test001(input1);
            } catch (Exception ignore) {}
        }

        List<PitchInvokeContext> getTargetProviderAddress = PitchingContext
                .fetchInvokeContext("com.mola.rpc.core.loadbalance.LoadBalance",
                        "getTargetProviderAddress");

        AssertUtil.isTrue(getTargetProviderAddress.size() == 225, "InvokeContext error");
        Map<String, Integer> cntMap = Maps.newHashMap();
        for (PitchInvokeContext invokeContext : getTargetProviderAddress) {
            String address = (String) invokeContext.getResult();
            cntMap.putIfAbsent(address, 0);
            cntMap.computeIfPresent(address, (k, v) -> v + 1);
        }


        for (Integer value : cntMap.values()) {
            AssertUtil.isTrue(value == 15, "cnt is error");
        }
    }

    @After
    public void after() {
        PitchingContext.finish();
    }
}
