package com.base.common.lock;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * 功能描述：锁对象
 *
 * @Author: shigf
 * @Date: 2020/8/7 10:09
 */
public class KeyLocks {
    private Map<String, SoftReference<Object>> lockMap = new HashMap();

    public KeyLocks() {
    }

    public Object lockByKey(String key){
        Object lock;
        synchronized (this){
            SoftReference<Object> objectSoftReference = lockMap.get(key);
            if(objectSoftReference == null || (lock = objectSoftReference.get()) == null){
                lock = new Object();
                lockMap.put(key, new SoftReference<>(lock));
            }
        }
        return lock;
    }
}
