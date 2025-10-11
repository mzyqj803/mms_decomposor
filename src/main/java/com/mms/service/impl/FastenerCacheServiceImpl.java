package com.mms.service.impl;

import com.mms.repository.ComponentFastenerRepository;
import com.mms.service.CacheService;
import com.mms.service.FastenerCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * 紧固件缓存服务实现
 * 用于预加载紧固件相关的缓存数据
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FastenerCacheServiceImpl implements FastenerCacheService {
    
    private final ComponentFastenerRepository componentFastenerRepository;
    private final CacheService cacheService;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String ASSEMBLED_CACHE_KEY_PREFIX = "fastener:assembled:";
    private static final String UNASSEMBLED_CACHE_KEY_PREFIX = "fastener:unassembled:";
    private static final String ASSEMBLED_LIST_CACHE_KEY = "fastener:assembled:list";
    private static final String UNASSEMBLED_LIST_CACHE_KEY = "fastener:unassembled:list";
    
    @Override
    @Transactional(readOnly = true)
    public int initializeFastenerCache() {
        log.info("开始初始化紧固件缓存...");
        
        if (!isRedisAvailable()) {
            log.warn("Redis不可用，跳过紧固件缓存初始化");
            return 0;
        }
        
        try {
            // 清空现有紧固件缓存
            clearFastenerCache();
            
            int totalCached = 0;
            
            // 1. 加载产线装配紧固件列表
            List<Long> assembledFasteners = componentFastenerRepository.findAssembledFasteners();
            log.info("找到 {} 个产线装配紧固件", assembledFasteners.size());
            
            for (Long componentId : assembledFasteners) {
                try {
                    // 缓存每个紧固件的状态（永不过期）
                    String cacheKey = ASSEMBLED_CACHE_KEY_PREFIX + componentId;
                    cacheService.set(cacheKey, true);
                    totalCached++;
                } catch (Exception e) {
                    log.error("缓存产线装配紧固件失败: componentId={}, error={}", 
                        componentId, e.getMessage());
                }
            }
            
            // 缓存产线装配紧固件列表（永不过期）
            cacheService.set(ASSEMBLED_LIST_CACHE_KEY, assembledFasteners);
            log.info("已缓存产线装配紧固件列表，共 {} 个", assembledFasteners.size());
            
            // 2. 加载仓库装箱紧固件列表
            List<Long> unassembledFasteners = componentFastenerRepository.findUnassembledFasteners();
            log.info("找到 {} 个仓库装箱紧固件", unassembledFasteners.size());
            
            for (Long componentId : unassembledFasteners) {
                try {
                    // 缓存每个紧固件的状态（永不过期）
                    String cacheKey = UNASSEMBLED_CACHE_KEY_PREFIX + componentId;
                    cacheService.set(cacheKey, true);
                    totalCached++;
                } catch (Exception e) {
                    log.error("缓存仓库装箱紧固件失败: componentId={}, error={}", 
                        componentId, e.getMessage());
                }
            }
            
            // 缓存仓库装箱紧固件列表（永不过期）
            cacheService.set(UNASSEMBLED_LIST_CACHE_KEY, unassembledFasteners);
            log.info("已缓存仓库装箱紧固件列表，共 {} 个", unassembledFasteners.size());
            
            log.info("紧固件缓存初始化完成，共缓存 {} 个紧固件状态", totalCached);
            return totalCached;
            
        } catch (Exception e) {
            log.error("初始化紧固件缓存失败: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    @Override
    public boolean isRedisAvailable() {
        try {
            redisTemplate.opsForValue().get("test");
            return true;
        } catch (Exception e) {
            log.debug("Redis连接检查失败: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public void clearFastenerCache() {
        if (!isRedisAvailable()) {
            log.warn("Redis不可用，无法清空紧固件缓存");
            return;
        }
        
        try {
            // 删除所有紧固件相关的缓存键
            Set<String> keys = redisTemplate.keys(ASSEMBLED_CACHE_KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("已删除 {} 个产线装配紧固件缓存", keys.size());
            }
            
            keys = redisTemplate.keys(UNASSEMBLED_CACHE_KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("已删除 {} 个仓库装箱紧固件缓存", keys.size());
            }
            
            // 删除列表缓存
            cacheService.delete(ASSEMBLED_LIST_CACHE_KEY);
            cacheService.delete(UNASSEMBLED_LIST_CACHE_KEY);
            
            log.info("紧固件缓存清空完成");
        } catch (Exception e) {
            log.error("清空紧固件缓存失败: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public long getFastenerCacheSize() {
        if (!isRedisAvailable()) {
            return 0;
        }
        
        try {
            long assembledCount = redisTemplate.keys(ASSEMBLED_CACHE_KEY_PREFIX + "*").size();
            long unassembledCount = redisTemplate.keys(UNASSEMBLED_CACHE_KEY_PREFIX + "*").size();
            return assembledCount + unassembledCount;
        } catch (Exception e) {
            log.error("获取紧固件缓存大小失败: {}", e.getMessage(), e);
            return 0;
        }
    }
}

