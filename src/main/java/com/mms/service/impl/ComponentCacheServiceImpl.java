package com.mms.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mms.entity.Components;
import com.mms.repository.ComponentsRepository;
import com.mms.service.ComponentCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 零部件缓存服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ComponentCacheServiceImpl implements ComponentCacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ComponentsRepository componentsRepository;
    private final ObjectMapper objectMapper;
    
    private static final String CACHE_KEY_PREFIX = "component:";
    
    @Override
    @Transactional(readOnly = true)
    public int initializeCache() {
        log.info("开始初始化零部件缓存...");
        
        if (!isRedisAvailable()) {
            log.warn("Redis不可用，跳过缓存初始化");
            return 0;
        }
        
        try {
            // 清空现有缓存
            clearCache();
            
            // 获取所有零部件
            List<Components> allComponents = componentsRepository.findAll();
            
            int cachedCount = 0;
            for (Components component : allComponents) {
                try {
                    // 创建一个简化的组件对象，只包含基本字段，避免懒加载问题
                    ComponentForCache cacheComponent = new ComponentForCache();
                    cacheComponent.setId(component.getId());
                    cacheComponent.setCategoryCode(component.getCategoryCode());
                    cacheComponent.setComponentCode(component.getComponentCode());
                    cacheComponent.setName(component.getName());
                    cacheComponent.setComment(component.getComment());
                    cacheComponent.setProcurementFlag(component.getProcurementFlag());
                    cacheComponent.setCommonPartsFlag(component.getCommonPartsFlag());
                    cacheComponent.setEntryTs(component.getEntryTs());
                    cacheComponent.setEntryUser(component.getEntryUser());
                    cacheComponent.setLastUpdateTs(component.getLastUpdateTs());
                    cacheComponent.setLastUpdateUser(component.getLastUpdateUser());
                    
                    // 将零部件转换为JSON字符串
                    String componentJson = objectMapper.writeValueAsString(cacheComponent);
                    
                    // 存储到Redis，永不过期
                    String cacheKey = CACHE_KEY_PREFIX + component.getComponentCode();
                    redisTemplate.opsForValue().set(cacheKey, componentJson);
                    
                    cachedCount++;
                    
                    if (cachedCount % 100 == 0) {
                        log.info("已缓存 {} 个零部件", cachedCount);
                    }
                } catch (Exception e) {
                    log.error("缓存零部件失败: componentCode={}, error={}", 
                        component.getComponentCode(), e.getMessage());
                }
            }
            
            log.info("零部件缓存初始化完成，共缓存 {} 个零部件", cachedCount);
            return cachedCount;
            
        } catch (Exception e) {
            log.error("初始化零部件缓存失败: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    @Override
    public Optional<String> getComponentFromCache(String componentCode) {
        if (!isRedisAvailable()) {
            log.debug("Redis不可用，无法从缓存获取零部件: {}", componentCode);
            return Optional.empty();
        }
        
        try {
            String cacheKey = CACHE_KEY_PREFIX + componentCode;
            Object cachedValue = redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedValue != null) {
                log.debug("从缓存获取零部件成功: {}", componentCode);
                return Optional.of(cachedValue.toString());
            } else {
                log.debug("缓存中未找到零部件: {}", componentCode);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("从缓存获取零部件失败: componentCode={}, error={}", 
                componentCode, e.getMessage());
            return Optional.empty();
        }
    }
    
    @Override
    public void putComponentToCache(String componentCode, String componentJson) {
        if (!isRedisAvailable()) {
            log.debug("Redis不可用，无法存储零部件到缓存: {}", componentCode);
            return;
        }
        
        try {
            String cacheKey = CACHE_KEY_PREFIX + componentCode;
            redisTemplate.opsForValue().set(cacheKey, componentJson);
            log.debug("零部件已存储到缓存: {}", componentCode);
        } catch (Exception e) {
            log.error("存储零部件到缓存失败: componentCode={}, error={}", 
                componentCode, e.getMessage());
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
            log.debug("Redis不可用，无法清空缓存");
            return;
        }
        
        try {
            // 删除所有以component:开头的key
            redisTemplate.delete(redisTemplate.keys(CACHE_KEY_PREFIX + "*"));
            log.info("零部件缓存已清空");
        } catch (Exception e) {
            log.error("清空缓存失败: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public long getCacheSize() {
        if (!isRedisAvailable()) {
            return 0;
        }
        
        try {
            return redisTemplate.keys(CACHE_KEY_PREFIX + "*").size();
        } catch (Exception e) {
            log.error("获取缓存大小失败: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * 用于缓存的简化组件类，避免懒加载问题
     */
    private static class ComponentForCache {
        private Long id;
        private String categoryCode;
        private String componentCode;
        private String name;
        private String comment;
        private Boolean procurementFlag;
        private Boolean commonPartsFlag;
        private java.time.LocalDateTime entryTs;
        private String entryUser;
        private java.time.LocalDateTime lastUpdateTs;
        private String lastUpdateUser;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getCategoryCode() { return categoryCode; }
        public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
        
        public String getComponentCode() { return componentCode; }
        public void setComponentCode(String componentCode) { this.componentCode = componentCode; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
        
        public Boolean getProcurementFlag() { return procurementFlag; }
        public void setProcurementFlag(Boolean procurementFlag) { this.procurementFlag = procurementFlag; }
        
        public Boolean getCommonPartsFlag() { return commonPartsFlag; }
        public void setCommonPartsFlag(Boolean commonPartsFlag) { this.commonPartsFlag = commonPartsFlag; }
        
        public java.time.LocalDateTime getEntryTs() { return entryTs; }
        public void setEntryTs(java.time.LocalDateTime entryTs) { this.entryTs = entryTs; }
        
        public String getEntryUser() { return entryUser; }
        public void setEntryUser(String entryUser) { this.entryUser = entryUser; }
        
        public java.time.LocalDateTime getLastUpdateTs() { return lastUpdateTs; }
        public void setLastUpdateTs(java.time.LocalDateTime lastUpdateTs) { this.lastUpdateTs = lastUpdateTs; }
        
        public String getLastUpdateUser() { return lastUpdateUser; }
        public void setLastUpdateUser(String lastUpdateUser) { this.lastUpdateUser = lastUpdateUser; }
    }
}
