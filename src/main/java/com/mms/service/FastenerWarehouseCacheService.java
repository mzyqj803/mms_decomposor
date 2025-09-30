package com.mms.service;

import com.mms.entity.FastenerWarehouse;

import java.util.List;

/**
 * 紧固件仓库缓存服务接口
 * 优先从Redis缓存查找fastenerWarehouse，缓存key为productCode
 */
public interface FastenerWarehouseCacheService {
    
    /**
     * 初始化缓存 - 将所有紧固件仓库数据加载到Redis缓存
     * @return 加载的紧固件数量
     */
    int initializeCache();
    
    /**
     * 根据产品代码从缓存中获取紧固件列表
     * @param productCode 产品代码
     * @return 紧固件列表
     */
    List<FastenerWarehouse> getFastenersByProductCode(String productCode);
    
    /**
     * 根据产品代码模糊匹配从缓存中获取紧固件列表
     * @param productCode 产品代码（支持模糊匹配）
     * @return 紧固件列表
     */
    List<FastenerWarehouse> getFastenersByProductCodeContaining(String productCode);
    
    /**
     * 将紧固件信息存储到缓存
     * @param productCode 产品代码
     * @param fasteners 紧固件列表
     */
    void putFastenersToCache(String productCode, List<FastenerWarehouse> fasteners);
    
    /**
     * 检查Redis是否可用
     * @return true如果Redis可用，false否则
     */
    boolean isRedisAvailable();
    
    /**
     * 清空所有紧固件缓存
     */
    void clearCache();
    
    /**
     * 获取缓存中的紧固件数量
     * @return 缓存中的紧固件数量
     */
    long getCacheSize();
    
    /**
     * 重新加载指定产品代码的缓存
     * @param productCode 产品代码
     * @return 重新加载的紧固件数量
     */
    int reloadCacheForProductCode(String productCode);
}
