package com.mola.rpc.consumer.test;

import com.google.common.collect.Lists;
import com.mola.rpc.client.Order;
import com.mola.rpc.client.ServerResponse;
import com.mola.rpc.client.SpecialObject;
import com.mola.rpc.client.UnitTestService;
import com.mola.rpc.common.entity.AddressInfo;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.common.utils.AssertUtil;
import com.mola.rpc.common.utils.JSONUtil;
import com.mola.rpc.core.remoting.Async;
import com.mola.rpc.data.config.manager.RpcDataManager;
import com.mola.rpc.data.config.spring.RpcProviderDataInitBean;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.nio.file.AccessMode;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 单测：
 * spring
 * 1、同步调用成功，检测参数类型
 * 2、同步调用失败，检测异常
 * 3、异步调用成功，检测参数类型
 * 4、异步调用失败，检测异常
 * 5、异步转同步，调用成功、失败，检测
 * proto同上
 * @date : 2023-01-22 16:43
 **/
@SpringBootTest(classes = ConsumerTestContext.class)
@RunWith(SpringRunner.class)
public class ConsumerInvokeTest {

    private static final Logger log = LoggerFactory.getLogger(ConsumerInvokeTest.class);

    @Resource
    protected RpcProviderDataInitBean rpcProviderDataInitBean;

    @Resource
    protected UnitTestService unitTestService;

    @Resource
    protected UnitTestService unitTestServiceAsync;

    @Before
    public void before() {
        RpcDataManager<RpcMetaData> rpcDataManager = rpcProviderDataInitBean.getRpcDataManager();
        AssertUtil.isTrue(rpcDataManager.isProviderAvailable("com.mola.rpc.client.UnitTestService", "default", "1.0.0", "public"), "UnitTestService的provider不存在，请检查是否启动");
        List<AddressInfo> remoteProviderAddress = rpcDataManager.getRemoteProviderAddress("com.mola.rpc.client.UnitTestService", "default", "1.0.0", "public");
        AssertUtil.notEmpty(remoteProviderAddress, "UnitTestService的provider不存在可用地址，请检查是否启动");
    }


    @Test
    public void syncInvokeTest() {
        String input1 = "input1:" + System.currentTimeMillis();
        ServerResponse<String> response = unitTestService.test001(input1);
    }

    @Test
    public void syncInvokeTest001() {
        String input1 = "input1:" + System.currentTimeMillis();
        String input2 = "input2:" + System.currentTimeMillis();
        String input3[] = new String[]{"input3" + System.currentTimeMillis() ,"input3" + System.currentTimeMillis()};
        ServerResponse<String> response = unitTestService.test001(input1);
        AssertUtil.isTrue(response != null && response.isSuccess(), "001-case1测试失败,调用失败");
        AssertUtil.isTrue(input1.equals(response.getData()), "001-case1测试失败");
        response = unitTestService.test001(input1, input2);
        AssertUtil.isTrue(response != null && response.isSuccess(), "001-case2测试失败,调用失败");
        AssertUtil.isTrue((input1 + input2).equals(response.getData()), "001-case2测试失败");
        response = unitTestService.test001(input1, input2, input3);
        AssertUtil.isTrue(response != null && response.isSuccess(), "001-case3测试失败,调用失败");
        AssertUtil.isTrue((input1 + input2 + String.join("", input3)).equals(response.getData()), "001-case3测试失败");
    }

    @Test
    public void syncInvokeTest002() {
        Date now = new Date();
        Order order = new Order("002-id", now, "测试输入", "002-code");
        Order order2 = new Order("002-id-2", now, "测试输入2", "002-code-2");
        ServerResponse<Order> response = unitTestService.test002(order);
        AssertUtil.isTrue(response != null && response.isSuccess(), "002-case1测试失败,调用失败");
        AssertUtil.isTrue(order.equals(response.getData()), "002-case1测试失败");
        response = unitTestService.test002(order, "002-code-modified");
        AssertUtil.isTrue(response != null && response.isSuccess(), "002-case2测试失败,调用失败");
        AssertUtil.isTrue(response.getData().getCode().equals("002-code-modified"), "002-case2测试失败");
        ServerResponse<List<Order>> listServerResponse = unitTestService.test002(order, order2, "002-code-modified-1", "002-code-modified-2");
        AssertUtil.isTrue(listServerResponse != null && listServerResponse.isSuccess(), "002-case3测试失败,调用失败");
        List<Order> data = listServerResponse.getData();
        order.setCode("002-code-modified-1");
        AssertUtil.isTrue(data.get(0).equals(order), "002-case3测试失败, get(0)");
        order2.setCode("002-code-modified-2");
        AssertUtil.isTrue(data.get(1).equals(order2), "002-case3测试失败, get(1)");
        listServerResponse = unitTestService.test002(new Order[]{order, order2}, new Order[]{order, order2});
        data = listServerResponse.getData();
        AssertUtil.isTrue(data.get(0).equals(order), "002-case4测试失败, get(0)");
        AssertUtil.isTrue(data.get(1).equals(order2), "002-case4测试失败, get(1)");
        AssertUtil.isTrue(data.get(2).equals(order), "002-case4测试失败, get(2)");
        AssertUtil.isTrue(data.get(3).equals(order2), "002-case4测试失败, get(3)");
    }

    @Test
    public void syncInvokeTest003() {
        int i = unitTestService.test003(1);
        AssertUtil.isTrue(i == 2, "003-case1测试失败");
        char[] as = unitTestService.test003(1, 'a', 20000L, (short) 3, true);
        String newStr = new String(as);
        AssertUtil.isTrue("1;a;20000;3;true".equals(newStr), "003-case2测试失败");
        int res = unitTestService.test003(new int[]{1, 2, 3, 4});
        AssertUtil.isTrue(res == 10, "003-case3测试失败");
        int[][] dataMap = new int[10][10];
        int[][] ints = unitTestService.test003(dataMap, 5, 5, 25231);
        AssertUtil.isTrue(ints[5][5] == 25231, "003-case4测试失败");
    }

    @Test
    public void specialObjectTransformTest() {
        BigDecimal bigDecimal = new BigDecimal("3.1415926");
        Date date = new Date();
        SpecialObject specialObject = unitTestService.specialObjectTransform(new SpecialObject(bigDecimal, date, AccessMode.READ, null));
        AssertUtil.isTrue(bigDecimal.compareTo(specialObject.getBigDecimal()) == 0, "specialObjectTransform-case1测试失败");
        AssertUtil.isTrue(date.compareTo(specialObject.getDate()) == 0, "specialObjectTransform-case2测试失败");
        AssertUtil.isTrue(AccessMode.READ.equals(specialObject.getAccessMode()), "specialObjectTransform-case3测试失败");
    }

    @Test
    public void syncNullResultTest() {
        ServerResponse<Order> response = unitTestService.nullResult(new Order());
        AssertUtil.isTrue(response == null, "syncNullResultTest-case1测试失败,调用失败");
    }

    @Test
    public void testException() {
        try {
            ServerResponse serverResponse = unitTestService.throwException();
            throw new RuntimeException("no expect success");
        } catch (Exception e) {
            AssertUtil.isTrue(e.getMessage().contains("throwException:服务端抛出运行时异常"), "testException-case1测试失败");
        }
    }

    @Test
    public void testPerformance() {
        Order order = new Order("002-id", new Date(), "测试输入", "002-code");
        long totalCost = 0L;
        for (int i = 0; i < 5000; i++) {
            long start = System.currentTimeMillis();
            ServerResponse<Order> response = unitTestService.test002(order);
            totalCost += (System.currentTimeMillis() - start);
        }
        AssertUtil.isTrue(totalCost < 7000, "testPerformance冷启动调用，cost = " + totalCost);
        log.info("testPerformance冷启动调用在正常时间范围内，cost = " + totalCost);
        totalCost = 0L;
        for (int i = 0; i < 5000; i++) {
            long start = System.currentTimeMillis();
            ServerResponse<Order> response = unitTestService.test002(order);
            totalCost += (System.currentTimeMillis() - start);
        }
        AssertUtil.isTrue(totalCost < 6000, "testPerformance热调用，cost = " + totalCost);
        log.info("testPerformance热调用在正常时间范围内，cost = " + totalCost);
    }

    @Test
    public void asyncInvokeTest() throws InterruptedException {
        String input1 = "async1:" + System.currentTimeMillis();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Async.from(unitTestServiceAsync.test001(input1)).consume((res) -> {
            AssertUtil.isTrue(res != null && res.isSuccess(), "asyncInvokeTest-case1测试失败,调用失败");
            AssertUtil.isTrue(input1.equals(res.getData()), "asyncInvokeTest-case1测试失败");
            countDownLatch.countDown();
        });
        Async<char[]> from = Async.from(unitTestServiceAsync.test003(1, 'a', 20000L, (short) 3, true));
        char[] chars = from.get();
        AssertUtil.isTrue("1;a;20000;3;true".equals(new String(chars)), "asyncInvokeTest-case2测试失败");
        countDownLatch.await(3000, TimeUnit.MILLISECONDS);
        AssertUtil.isTrue(countDownLatch.getCount() == 0, "asyncInvokeTest-case2测试失败");
        CountDownLatch countDownLatch2 = new CountDownLatch(1);
        Async.from(unitTestServiceAsync.test003(1)).consume(res -> {
            AssertUtil.isTrue(res == 2, "asyncInvokeTest-case3测试失败");
            countDownLatch2.countDown();
        });
        countDownLatch2.await(3000, TimeUnit.MILLISECONDS);
        AssertUtil.isTrue(countDownLatch2.getCount() == 0, "asyncInvokeTest-case2测试失败");
        // 同步调用，报异常
        try {
            Async.from(unitTestService.test003(1)).consume(res -> {
            });
            throw new RuntimeException("no expect success");
        } catch (Exception e) {
            AssertUtil.isTrue(e.getMessage().contains("please check if this method is an async method"), "asyncInvokeTest-case5测试失败");
        }
        // 同步调用，报异常
        try {
            Integer integer = Async.from(unitTestService.test003(1)).get();
            throw new RuntimeException("no expect success");
        } catch (Exception e) {
            AssertUtil.isTrue(e.getMessage().contains("please check if this method is an async method"), "asyncInvokeTest-case6测试失败");
        }
    }

    @Test
    public void asyncInvokeTestInAnnotation() throws InterruptedException {
        CountDownLatch countDownLatch3 = new CountDownLatch(1);
        Async.from(unitTestServiceAsync.asyncInvokeInAnnotation()).consume(res -> {
            AssertUtil.isTrue(res.equals("ok"), "asyncInvokeTest-case7测试失败");
            countDownLatch3.countDown();
        });
        countDownLatch3.await(3000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void onewayInvokeTestInAnnotation() {
        long start = System.currentTimeMillis();
        String s = unitTestService.onewayTest();
        AssertUtil.isTrue(s == null, "onewayInvokeTestInAnnotation-case1测试失败");
        AssertUtil.isTrue(System.currentTimeMillis() - start < 500, "onewayInvokeTestInAnnotation-case2测试失败");
    }


    @Test
    public void testAsyncException() {
        try {
            ServerResponse serverResponse = Async.from(unitTestServiceAsync.throwException()).get();
            throw new RuntimeException("no expect success");
        } catch (Exception e) {
            AssertUtil.isTrue(e.getMessage().contains("throwException:服务端抛出运行时异常"), "testException-case1测试失败");
        }
    }

    @Test
    public void testAsyncConcurrentTimeLimit() {
        long start = System.currentTimeMillis();
        String input = "async:" + System.currentTimeMillis();
        List<Async<ServerResponse<String>>> asyncList = Lists.newArrayList();
        for (int i = 0; i < 20; i++) {
            String in = input + i;
            asyncList.add(Async.from(unitTestServiceAsync.test001(in)));
        }

        for (int i = 0; i < asyncList.size(); i++) {
            ServerResponse<String> res = asyncList.get(i).get();
            AssertUtil.isTrue(res != null && res.isSuccess(), "testAsyncConcurrentTimeLimit测试失败,调用失败, res = " + JSONUtil.toJSONString(res));
            AssertUtil.isTrue((input + i).equals(res.getData()), "testAsyncConcurrentTimeLimit-case1测试失败");
        }
        long cost = System.currentTimeMillis() - start;
        log.info("20次异步调用总时长，cost = " + cost);
        AssertUtil.isTrue(cost < 2500, "testAsyncConcurrentTimeLimit-case2测试失败");
    }

    @Test
    public void testMultiThreadInvoke() {
        ExecutorService executorService = Executors.newFixedThreadPool(16);
        CountDownLatch cdl = new CountDownLatch(10000);
        for (int i = 0; i < 10000; i++) {
            executorService.submit(
                () -> {
                    String input1 = System.currentTimeMillis() + "";
                    ServerResponse<String> response = unitTestService.test001(input1);
                    AssertUtil.isTrue(response != null && response.isSuccess(), "001-case1测试失败,调用失败");
                    AssertUtil.isTrue(input1.equals(response.getData()), "001-case1测试失败");
                    BigDecimal bigDecimal = new BigDecimal("3.1415926");
                    Date date = new Date();
                    SpecialObject specialObject = unitTestService.specialObjectTransform(new SpecialObject(bigDecimal, date, AccessMode.READ, null));
                    AssertUtil.isTrue(bigDecimal.compareTo(specialObject.getBigDecimal()) == 0, "specialObjectTransform-case1测试失败");
                    AssertUtil.isTrue(date.compareTo(specialObject.getDate()) == 0, "specialObjectTransform-case2测试失败");
                    AssertUtil.isTrue(AccessMode.READ.equals(specialObject.getAccessMode()), "specialObjectTransform-case3测试失败");
                    cdl.countDown();
                }
            );
        }

        try {
            cdl.await(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }
        AssertUtil.isTrue(cdl.getCount() == 0, "001-case1测试失败, cdl = " + cdl.getCount());
    }
}
