package com.mms.service;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class DistributedLockService {
    
    @Autowired
    private RedissonClient redissonClient;
    
    /**
     * 获取分布式锁
     * @param lockKey 锁的key
     * @param waitTime 等待时间
     * @param leaseTime 锁的持有时间
     * @param timeUnit 时间单位
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, timeUnit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    /**
     * 释放分布式锁
     * @param lockKey 锁的key
     */
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
    
    /**
     * 执行带锁的操作
     * @param lockKey 锁的key
     * @param waitTime 等待时间
     * @param leaseTime 锁的持有时间
     * @param timeUnit 时间单位
     * @param action 要执行的操作
     * @return 操作结果
     */
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit, LockAction<T> action) {
        if (tryLock(lockKey, waitTime, leaseTime, timeUnit)) {
            try {
                return action.execute();
            } finally {
                unlock(lockKey);
            }
        }
        throw new RuntimeException("Failed to acquire lock: " + lockKey);
    }
    
    @FunctionalInterface
    public interface LockAction<T> {
        T execute();
    }
}
