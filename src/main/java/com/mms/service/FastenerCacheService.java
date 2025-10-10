package com.mms.service;

/**
 * 紧固件缓存服务接口
 * 用于预加载紧固件相关的缓存数据
 */
public interface FastenerCacheService {
    
    /**
     * 初始化紧固件缓存 - 将所有紧固件数据加载到Redis缓存
     * @return 加载的紧固件数量
     */
    int initializeFastenerCache();
    
    /**
     * 检查Redis是否可用
     * @return true如果Redis可用，false否则
     */
    boolean isRedisAvailable();
    
    /**
     * 清空所有紧固件缓存
     */
    void clearFastenerCache();
    
    /**
     * 获取缓存中的紧固件数量
     * @return 缓存中的紧固件数量
     */
    long getFastenerCacheSize();
}
