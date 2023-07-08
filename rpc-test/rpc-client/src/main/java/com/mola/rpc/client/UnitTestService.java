package com.mola.rpc.client;

import com.mola.rpc.common.annotation.AsyncInvoke;
import com.mola.rpc.common.annotation.OnewayInvoke;

import java.util.List;

/**
 * 单元测试使用接口
 */
public interface UnitTestService {

    /**
     * 入参个数：1
     * 出参类型：复合
     * 入参类型：jdk
     * 期待结果：result原样返回
     * @param input
     * @return
     */
    ServerResponse<String> test001(String input);

    /**
     * 入参个数：2
     * 出参类型：复合
     * 入参类型：jdk
     * 期待结果：result拼接返回
     * @param input1
     * @param input2
     * @return
     */
    ServerResponse<String> test001(String input1, String input2);

    /**
     * 入参个数：3
     * 出参类型：复合
     * 入参类型：jdk + 数组
     * 期待结果：result拼接返回
     * @param input1
     * @param input2
     * @param input3
     * @return
     */
    ServerResponse<String> test001(String input1, String input2, String[] input3);

    /**
     * 入参个数：1
     * 出参类型：复合
     * 入参类型：自定义
     * 期待结果：result原样返回
     * @param order
     * @return
     */
    ServerResponse<Order> test002(Order order);

    /**
     * 入参个数：2
     * 出参类型：复合
     * 入参类型：复合
     * 期待结果：code填入order返回
     * @param order
     * @return
     */
    ServerResponse<Order> test002(Order order, String code);

    /**
     * 入参个数：2
     * 出参类型：复合
     * 入参类型：复合
     * 期待结果：code填入order返回，拼接成list
     * @return
     */
    ServerResponse<List<Order>> test002(Order order1, Order order2, String code1, String code2);

    /**
     * 入参个数：2
     * 出参类型：复合
     * 入参类型：复合
     * 期待结果：数组拼接成list
     * @return
     */
    ServerResponse<List<Order>> test002(Order[] order1, Order[] order2);

    /**
     * 入参个数：1
     * 出参类型：基本类型
     * 入参类型：基本类型
     * 期待结果：params + 1
     * @return
     */
    int test003(int param);

    /**
     * 入参个数：5
     * 出参类型：基本类型
     * 入参类型：基本类型数组
     * 期待结果：params + 1
     * @return
     */
    char[] test003(int param1, char param2, long param3, short param4, boolean param5);

    /**
     * 入参个数：1
     * 出参类型：基本类型
     * 入参类型：基本类型数组
     * 期待结果：paramArr加和
     * @return
     */
    int test003(int[] paramArr);

    /**
     * 入参个数：4
     * 出参类型：基本类型
     * 入参类型：基本类型数组
     * 期待结果：二位数组指定位置替换
     * @return
     */
    int[][] test003(int[][] paramArr, int x, int y, int data);

    /**
     * 服务端抛出异常
     * @return
     */
    ServerResponse throwException();

    /**
     * rpc环路测试
     * @param order
     * @return
     */
    ServerResponse<Order> loopBackTest(Order order);

    /**
     * rpc环路测试
     * @param order
     * @return
     */
    ServerResponse<Order> nullResult(Order order);

    /**
     * 特殊对象传输
     * @param specialObject
     * @return
     */
    SpecialObject specialObjectTransform(SpecialObject specialObject);

    /**
     * 反向代理环路测试
     * client(consumer1) --正向--> server(provider1) --> server(consumer2) --反向--> client(provider2)
     * @param id
     * @return
     */
    String testReverseLoopBack(String id);
    String testReverseLoopBackInSpring(String id);
    String testReverseLoopBackInAsync(String id);

    /**
     * 长时间方法执行，测试连接是否中断
     * @param second
     * @return
     */
    String processDelayLongTime(int second);

    @AsyncInvoke
    String asyncInvokeInAnnotation();

    @OnewayInvoke
    String onewayTest();
}
