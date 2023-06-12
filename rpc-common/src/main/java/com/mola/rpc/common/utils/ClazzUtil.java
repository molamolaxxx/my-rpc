package com.mola.rpc.common.utils;

import com.google.common.collect.Sets;
import com.mola.rpc.common.annotation.AsyncInvoke;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-06-12 21:22
 **/
public class ClazzUtil {

    public static Set<String> getAllAsyncInvokeMethodName(Class<?> clazz) {
        Assert.notNull(clazz, "clazz is null");
        Method[] methods = clazz.getMethods();
        if (methods == null || methods.length == 0) {
            return Sets.newHashSet();
        }
        Set<String> result = Sets.newHashSet();
        for (Method method : methods) {
            if (method.getAnnotation(AsyncInvoke.class) != null) {
                result.add(method.getName());
            }
        }
        return result;
    }
}
