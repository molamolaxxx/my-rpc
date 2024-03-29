package com.mola.rpc.core.util;

import com.mola.rpc.common.utils.AssertUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-03-24 15:12
 **/
public class MultiKeyReentrantLock {

    private final ConcurrentHashMap<Integer, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    /**
     * 桶容量
     */
    private final int capacity;

    public MultiKeyReentrantLock(int capacity) {
        AssertUtil.isTrue(capacity > 0, "capacity can not smaller than zero");
        this.capacity = capacity;
    }

    public void lock(String key) {
        Integer hash = getHash(key);
        ReentrantLock reentrantLock = lockMap.putIfAbsent(hash, new ReentrantLock());
        if (reentrantLock == null) {
            reentrantLock = lockMap.get(hash);
        }
        reentrantLock.lock();
    }

    public void unlock(String key) {
        ReentrantLock reentrantLock = lockMap.get(getHash(key));
        AssertUtil.notNull(reentrantLock, "reentrantLock not exist");
        reentrantLock.unlock();
    }

    private int getHash(String key) {
        return HashUtil.getHash(key) % capacity;
    }
}
