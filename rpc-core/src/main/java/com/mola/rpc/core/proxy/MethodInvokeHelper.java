package com.mola.rpc.core.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : molamola
 * @Project: InvincibleSchedulerEngine
 * @Description: 动态方法调用
 * @date : 2020-08-30 20:38
 **/
public class MethodInvokeHelper {

    private static final Logger logger = LoggerFactory.getLogger(MethodInvokeHelper.class);

    /**
     * 方法缓存
     */
    private static final Map<MethodKey, Method> METHOD_CACHE = new HashMap<MethodKey, Method>();


    /**
     * 调用方法
     * @param object
     * @param methodName
     * @param parameterTypes
     * @param arguments
     * @return
     */
    public static Object invokeMethod(Object object, String methodName, Class<?>[] parameterTypes, Object[] arguments) {
        Method method = getMethod(object, methodName, parameterTypes);
        if(null == method) {
            throw new RuntimeException("no such method");
        }
        Object result = null;
        try {
            method.setAccessible(true);
            result = method.invoke(object, arguments);
        } catch (Throwable e) {
            logger.error("[ProxyService$invokeMethod]: error, methodName:" + methodName, e);
            throw new RuntimeException(e.getCause().getMessage());
        }
        return result;
    }

    /**
     * 获取方法
     * @param object
     * @param methodName
     * @param parameterTypes
     * @return
     */
    private static Method getMethod(Object object, String methodName, Class<?>[] parameterTypes) {
        MethodKey classKey = new MethodKey(object, methodName, parameterTypes);
        Method method = METHOD_CACHE.get(classKey);
        if(method != null) {
            return method;
        }
        method = tryFindMethod(object, methodName, parameterTypes);
        if(method != null) {
            /** 缓存方法 */
            METHOD_CACHE.put(classKey, method);
        }
        return method;
    }

    /**
     * 尝试查找方法
     * @param object
     * @param methodName
     * @param parameterTypes
     * @return
     */
    private static Method tryFindMethod(Object object, String methodName, Class<?>[] parameterTypes) {
        Method method = null;
        try {
            method = object.getClass().getDeclaredMethod(methodName, parameterTypes);
        } catch (Throwable e) {
            logger.error("[ProxyService$tryFindMethod]: failed, methodName:" + methodName, e);
            throw new RuntimeException(e);
        }
        return method;
    }
}
