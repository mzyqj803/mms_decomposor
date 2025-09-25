package com.mms.service;

import java.util.Optional;

/**
 * 零部件缓存服务接口
 */
public interface ComponentCacheService {
    
    /**
     * 初始化缓存 - 将所有零部件加载到Redis缓存
     * @return 加载的零部件数量
     */
    int initializeCache();
    
    /**
     * 根据零部件编号从缓存中获取零部件
     * @param componentCode 零部件编号
     * @return 零部件信息（JSON格式）
     */
    Optional<String> getComponentFromCache(String componentCode);
    
    /**
     * 将零部件信息存储到缓存
     * @param componentCode 零部件编号
     * @param componentJson 零部件JSON信息
     */
    void putComponentToCache(String componentCode, String componentJson);
    
    /**
     * 检查Redis是否可用
     * @return true如果Redis可用，false否则
     */
    boolean isRedisAvailable();
    
    /**
     * 清空所有零部件缓存
     */
    void clearCache();
    
    /**
     * 获取缓存中的零部件数量
     * @return 缓存中的零部件数量
     */
    long getCacheSize();
}
