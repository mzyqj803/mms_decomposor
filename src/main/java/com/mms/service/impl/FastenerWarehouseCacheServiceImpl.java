package com.mms.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mms.entity.FastenerWarehouse;
import com.mms.repository.FastenerWarehouseRepository;
import com.mms.service.FastenerWarehouseCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 紧固件仓库缓存服务实现
 * 优先从Redis缓存查找fastenerWarehouse，缓存key为productCode
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FastenerWarehouseCacheServiceImpl implements FastenerWarehouseCacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final FastenerWarehouseRepository fastenerWarehouseRepository;
    private final ObjectMapper objectMapper;
    
    private static final String CACHE_KEY_PREFIX = "fastener:product:";
    
    @Override
    @Transactional(readOnly = true)
    public int initializeCache() {
        log.info("开始初始化紧固件仓库缓存...");
        
        if (!isRedisAvailable()) {
            log.warn("Redis不可用，跳过紧固件缓存初始化");
            return 0;
        }
        
        try {
            // 清空现有缓存
            clearCache();
            
            // 获取所有紧固件
            List<FastenerWarehouse> allFasteners = fastenerWarehouseRepository.findAll();
            
            // 按产品代码分组
            java.util.Map<String, List<FastenerWarehouse>> groupedByProductCode = 
                allFasteners.stream()
                    .filter(fw -> fw.getProductCode() != null && !fw.getProductCode().trim().isEmpty())
                    .collect(java.util.stream.Collectors.groupingBy(FastenerWarehouse::getProductCode));
            
            int cachedCount = 0;
            for (java.util.Map.Entry<String, List<FastenerWarehouse>> entry : groupedByProductCode.entrySet()) {
                String productCode = entry.getKey();
                List<FastenerWarehouse> fasteners = entry.getValue();
                
                try {
                    // 将紧固件列表转换为JSON字符串
                    String fastenersJson = objectMapper.writeValueAsString(fasteners);
                    
                    // 存储到Redis，永不过期
                    String cacheKey = CACHE_KEY_PREFIX + productCode;
                    redisTemplate.opsForValue().set(cacheKey, fastenersJson);
                    
                    cachedCount += fasteners.size();
                    
                    if (cachedCount % 100 == 0) {
                        log.info("已缓存 {} 个紧固件", cachedCount);
                    }
                } catch (Exception e) {
                    log.error("缓存紧固件失败: productCode={}, error={}", 
                        productCode, e.getMessage());
                }
            }
            
            log.info("紧固件仓库缓存初始化完成，共缓存 {} 个紧固件，涉及 {} 个产品代码", 
                cachedCount, groupedByProductCode.size());
            return cachedCount;
            
        } catch (Exception e) {
            log.error("初始化紧固件仓库缓存失败: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    @Override
    public List<FastenerWarehouse> getFastenersByProductCode(String productCode) {
        if (productCode == null || productCode.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        if (!isRedisAvailable()) {
            log.debug("Redis不可用，从数据库获取紧固件: {}", productCode);
            return fastenerWarehouseRepository.findByProductCodeContaining(productCode);
        }
        
        try {
            String cacheKey = CACHE_KEY_PREFIX + productCode;
            Object cachedValue = redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedValue != null) {
                log.debug("从缓存获取紧固件成功: {}", productCode);
                return objectMapper.readValue(cachedValue.toString(), new TypeReference<List<FastenerWarehouse>>() {});
            } else {
                log.debug("缓存中未找到紧固件，从数据库获取: {}", productCode);
                List<FastenerWarehouse> fasteners = fastenerWarehouseRepository.findByProductCodeContaining(productCode);
                
                // 将查询结果存储到缓存
                if (!fasteners.isEmpty()) {
                    putFastenersToCache(productCode, fasteners);
                }
                
                return fasteners;
            }
        } catch (Exception e) {
            log.error("从缓存获取紧固件失败: productCode={}, error={}", 
                productCode, e.getMessage());
            // 降级到数据库查询
            return fastenerWarehouseRepository.findByProductCodeContaining(productCode);
        }
    }
    
    @Override
    public List<FastenerWarehouse> getFastenersByProductCodeContaining(String productCode) {
        if (productCode == null || productCode.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        if (!isRedisAvailable()) {
            log.debug("Redis不可用，从数据库模糊查询紧固件: {}", productCode);
            return fastenerWarehouseRepository.findByProductCodeContaining(productCode);
        }
        
        try {
            // 先尝试精确匹配
            List<FastenerWarehouse> exactMatch = getFastenersByProductCode(productCode);
            if (!exactMatch.isEmpty()) {
                return exactMatch;
            }
            
            // 如果精确匹配没有结果，尝试模糊匹配
            // 获取所有缓存的产品代码
            Set<String> cacheKeys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
            List<FastenerWarehouse> allMatchingFasteners = new ArrayList<>();
            
            for (String cacheKey : cacheKeys) {
                try {
                    Object cachedValue = redisTemplate.opsForValue().get(cacheKey);
                    if (cachedValue != null) {
                        List<FastenerWarehouse> fasteners = objectMapper.readValue(
                            cachedValue.toString(), new TypeReference<List<FastenerWarehouse>>() {});
                        
                        // 检查是否有匹配的产品代码
                        boolean hasMatch = fasteners.stream()
                            .anyMatch(fw -> fw.getProductCode() != null && 
                                fw.getProductCode().contains(productCode));
                        
                        if (hasMatch) {
                            allMatchingFasteners.addAll(fasteners);
                        }
                    }
                } catch (Exception e) {
                    log.warn("处理缓存键失败: {}, error={}", cacheKey, e.getMessage());
                }
            }
            
            if (!allMatchingFasteners.isEmpty()) {
                log.debug("从缓存模糊匹配获取紧固件成功: {}, 找到 {} 个", 
                    productCode, allMatchingFasteners.size());
                return allMatchingFasteners;
            }
            
            // 如果缓存中没有找到，从数据库查询
            log.debug("缓存中未找到匹配的紧固件，从数据库模糊查询: {}", productCode);
            List<FastenerWarehouse> fasteners = fastenerWarehouseRepository.findByProductCodeContaining(productCode);
            
            // 将查询结果存储到缓存
            if (!fasteners.isEmpty()) {
                putFastenersToCache(productCode, fasteners);
            }
            
            return fasteners;
            
        } catch (Exception e) {
            log.error("从缓存模糊查询紧固件失败: productCode={}, error={}", 
                productCode, e.getMessage());
            // 降级到数据库查询
            return fastenerWarehouseRepository.findByProductCodeContaining(productCode);
        }
    }
    
    @Override
    public void putFastenersToCache(String productCode, List<FastenerWarehouse> fasteners) {
        if (productCode == null || productCode.trim().isEmpty() || fasteners == null || fasteners.isEmpty()) {
            return;
        }
        
        if (!isRedisAvailable()) {
            log.debug("Redis不可用，无法存储紧固件到缓存: {}", productCode);
            return;
        }
        
        try {
            String fastenersJson = objectMapper.writeValueAsString(fasteners);
            String cacheKey = CACHE_KEY_PREFIX + productCode;
            redisTemplate.opsForValue().set(cacheKey, fastenersJson);
            log.debug("紧固件已存储到缓存: {}, 数量: {}", productCode, fasteners.size());
        } catch (Exception e) {
            log.error("存储紧固件到缓存失败: productCode={}, error={}", 
                productCode, e.getMessage());
        }
    }
    
    @Override
    public boolean isRedisAvailable() {
        try {
            // 尝试执行一个简单的Redis操作来检查连接
            redisTemplate.opsForValue().get("test:connection");
            return true;
        } catch (Exception e) {
            log.debug("Redis连接检查失败: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public void clearCache() {
        if (!isRedisAvailable()) {
            log.debug("Redis不可用，无法清空紧固件缓存");
            return;
        }
        
        try {
            // 删除所有以fastener:product:开头的key
            Set<String> keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("紧固件缓存已清空，删除了 {} 个缓存键", keys.size());
            }
        } catch (Exception e) {
            log.error("清空紧固件缓存失败: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public long getCacheSize() {
        if (!isRedisAvailable()) {
            return 0;
        }
        
        try {
            Set<String> keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
            return keys != null ? keys.size() : 0;
        } catch (Exception e) {
            log.error("获取紧固件缓存大小失败: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public int reloadCacheForProductCode(String productCode) {
        if (productCode == null || productCode.trim().isEmpty()) {
            return 0;
        }
        
        if (!isRedisAvailable()) {
            log.warn("Redis不可用，无法重新加载紧固件缓存: {}", productCode);
            return 0;
        }
        
        try {
            // 从数据库重新查询
            List<FastenerWarehouse> fasteners = fastenerWarehouseRepository.findByProductCodeContaining(productCode);
            
            // 更新缓存
            putFastenersToCache(productCode, fasteners);
            
            log.info("重新加载紧固件缓存完成: {}, 数量: {}", productCode, fasteners.size());
            return fasteners.size();
            
        } catch (Exception e) {
            log.error("重新加载紧固件缓存失败: productCode={}, error={}", 
                productCode, e.getMessage(), e);
            return 0;
        }
    }
}
