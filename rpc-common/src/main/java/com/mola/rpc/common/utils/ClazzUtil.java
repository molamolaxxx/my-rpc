package com.mola.rpc.common.utils;

import com.google.common.collect.Sets;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-06-12 21:22
 **/
public class ClazzUtil {

    public static Set<String> getMethodNameFilterByAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
        AssertUtil.notNull(clazz, "clazz is null");
        Method[] methods = clazz.getMethods();
        if (methods == null || methods.length == 0) {
            return Sets.newHashSet();
        }
        Set<String> result = Sets.newHashSet();
        for (Method method : methods) {
            if (method.getAnnotation(annotation) != null) {
                result.add(method.getName());
            }
        }
        return result;
    }
}
