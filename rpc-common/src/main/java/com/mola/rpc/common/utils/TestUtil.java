package com.mola.rpc.common.utils;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 判断是否是单元测试环境
 * @date : 2023-03-07 01:55
 **/
public class TestUtil {
    public static boolean isJUnitTest() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.getClassName().startsWith("org.junit.")) {
                return true;
            }
        }
        return false;
    }
}
