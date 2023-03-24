package com.mola.rpc.core.util;

import org.springframework.util.Assert;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-03-24 15:12
 **/
public class MultiKeyReentrantLock {

    private ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public void lock(String key) {
        ReentrantLock reentrantLock = lockMap.putIfAbsent(key, new ReentrantLock());
        if (reentrantLock == null) {
            reentrantLock = lockMap.get(key);
        }
        reentrantLock.lock();
    }

    public void unlock(String key) {
        ReentrantLock reentrantLock = lockMap.get(key);
        Assert.notNull(reentrantLock, "reentrantLock not exist");
        reentrantLock.unlock();
    }
}
