package com.mola.rpc.common.utils;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 构造线程安全的Set
 * @date : 2024-03-10 21:03
 **/
public class ConcurrentHashSetBuilder {

    private final static byte MAPPED = 0;

    private ConcurrentHashSetBuilder(){}

    public static <T> Set<T> build(int size) {
        ConcurrentHashMap<T, Byte> container = new ConcurrentHashMap<>(size);
        return container.keySet(MAPPED);
    }
}
