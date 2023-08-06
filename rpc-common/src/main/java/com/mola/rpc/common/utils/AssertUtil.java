package com.mola.rpc.common.utils;

import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Map;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-08-06 16:21
 **/
public class AssertUtil {

    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new RuntimeException(message);
        }
    }

    public static void notNull(@Nullable Object object, String message) {
        if (object == null) {
            throw new RuntimeException(message);
        }
    }

    public static void notEmpty(@Nullable Collection<?> collection, String message) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new RuntimeException(message);
        }
    }

    public static void hasText(@Nullable String text, String message) {
        if (!StringUtils.hasText(text)) {
            throw new RuntimeException(message);
        }
    }

    public static void notEmpty(@Nullable Map<?, ?> map, String message) {
        if (CollectionUtils.isEmpty(map)) {
            throw new RuntimeException(message);
        }
    }
}
