package com.mola.rpc.consumer.test;

import com.mola.rpc.client.Order;
import com.mola.rpc.client.ServerResponse;
import com.mola.rpc.client.UnitTestService;
import com.mola.rpc.common.entity.AddressInfo;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.data.config.manager.RpcDataManager;
import com.mola.rpc.data.config.spring.RpcProviderDataInitBean;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

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

    @Resource
    private RpcProviderDataInitBean rpcProviderDataInitBean;

    @Resource
    private UnitTestService unitTestService;

    @Before
    public void before() {
        RpcDataManager<RpcMetaData> rpcDataManager = rpcProviderDataInitBean.getRpcDataManager();
        Assert.isTrue(rpcDataManager.isProviderExist("com.mola.rpc.client.UnitTestService", "default", "1.0.0", "pre"), "UnitTestService的provider不存在，请检查是否启动");
        List<AddressInfo> remoteProviderAddress = rpcDataManager.getRemoteProviderAddress("com.mola.rpc.client.UnitTestService", "default", "1.0.0", "pre");
        Assert.notEmpty(remoteProviderAddress, "UnitTestService的provider不存在可用地址，请检查是否启动");
    }

    @Test
    public void syncInvokeTest001() {
        String input1 = "input1:" + System.currentTimeMillis();
        String input2 = "input2:" + System.currentTimeMillis();
        String input3[] = new String[]{"input3" + System.currentTimeMillis() ,"input3" + System.currentTimeMillis()};
        ServerResponse<String> response = unitTestService.test001(input1);
        Assert.isTrue(response != null && response.isSuccess(), "001-case1测试失败,调用失败");
        Assert.isTrue(input1.equals(response.getData()), "001-case1测试失败");
        response = unitTestService.test001(input1, input2);
        Assert.isTrue(response != null && response.isSuccess(), "001-case2测试失败,调用失败");
        Assert.isTrue((input1 + input2).equals(response.getData()), "001-case2测试失败");
        response = unitTestService.test001(input1, input2, input3);
        Assert.isTrue(response != null && response.isSuccess(), "001-case3测试失败,调用失败");
        Assert.isTrue((input1 + input2 + String.join("", input3)).equals(response.getData()), "001-case3测试失败");

    }

    @Test
    public void syncInvokeTest002() {
        Date now = new Date();
        Order order = new Order("002-id", now, "测试输入", "002-code");
        Order order2 = new Order("002-id-2", now, "测试输入2", "002-code-2");
        ServerResponse<Order> response = unitTestService.test002(order);
        Assert.isTrue(response != null && response.isSuccess(), "002-case1测试失败,调用失败");
        Assert.isTrue(order.equals(response.getData()), "002-case1测试失败");
        response = unitTestService.test002(order, "002-code-modified");
        Assert.isTrue(response != null && response.isSuccess(), "002-case2测试失败,调用失败");
        Assert.isTrue(response.getData().getCode().equals("002-code-modified"), "002-case2测试失败");
        ServerResponse<List<Order>> listServerResponse = unitTestService.test002(order, order2, "002-code-modified-1", "002-code-modified-2");
        Assert.isTrue(listServerResponse != null && listServerResponse.isSuccess(), "002-case3测试失败,调用失败");
        List<Order> data = listServerResponse.getData();
        order.setCode("002-code-modified-1");
        Assert.isTrue(data.get(0).equals(order), "002-case3测试失败, get(0)");
        order2.setCode("002-code-modified-2");
        Assert.isTrue(data.get(1).equals(order2), "002-case3测试失败, get(1)");
        listServerResponse = unitTestService.test002(new Order[]{order, order2}, new Order[]{order, order2});
        data = listServerResponse.getData();
        Assert.isTrue(data.get(0).equals(order), "002-case4测试失败, get(0)");
        Assert.isTrue(data.get(1).equals(order2), "002-case4测试失败, get(1)");
        Assert.isTrue(data.get(2).equals(order), "002-case4测试失败, get(2)");
        Assert.isTrue(data.get(3).equals(order2), "002-case4测试失败, get(3)");
    }

    @Test
    public void syncInvokeTest003() {
        int i = unitTestService.test003(1);
        Assert.isTrue(i == 2, "003-case1测试失败");
        char[] as = unitTestService.test003(1, 'a', 20000L, (short) 3, true);
        String newStr = new String(as);
        Assert.isTrue("1;a;20000;3;true".equals(newStr), "003-case2测试失败");
        int res = unitTestService.test003(new int[]{1, 2, 3, 4});
        Assert.isTrue(res == 10, "003-case3测试失败");
        int[][] dataMap = new int[10][10];
        int[][] ints = unitTestService.test003(dataMap, 5, 5, 25231);
        Assert.isTrue(ints[5][5] == 25231, "003-case4测试失败");
    }

    @Test
    public void testException() {
        try {
            ServerResponse serverResponse = unitTestService.throwException();
            throw new RuntimeException("no expect success");
        } catch (Exception e) {
            Assert.isTrue(e.getMessage().contains("throwException:服务端抛出运行时异常"), "testException-case1测试失败");
        }
    }
}
