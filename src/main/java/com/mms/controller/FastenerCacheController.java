package com.mms.controller;

import com.mms.service.FastenerWarehouseCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 紧固件仓库缓存管理控制器
 * 提供缓存管理相关的API接口
 */
@RestController
@RequestMapping("/api/fastener-cache")
@RequiredArgsConstructor
@Slf4j
public class FastenerCacheController {
    
    private final FastenerWarehouseCacheService fastenerWarehouseCacheService;
    
    /**
     * 检查缓存状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getCacheStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            boolean redisAvailable = fastenerWarehouseCacheService.isRedisAvailable();
            long cacheSize = fastenerWarehouseCacheService.getCacheSize();
            
            status.put("redisAvailable", redisAvailable);
            status.put("cacheSize", cacheSize);
            status.put("status", "success");
            status.put("message", "缓存状态获取成功");
            
            log.info("缓存状态查询成功: Redis可用={}, 缓存大小={}", redisAvailable, cacheSize);
            
        } catch (Exception e) {
            log.error("获取缓存状态失败", e);
            status.put("status", "error");
            status.put("message", "获取缓存状态失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * 初始化缓存
     */
    @PostMapping("/initialize")
    public ResponseEntity<Map<String, Object>> initializeCache() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("开始初始化紧固件缓存...");
            int cachedCount = fastenerWarehouseCacheService.initializeCache();
            
            result.put("status", "success");
            result.put("message", "缓存初始化成功");
            result.put("cachedCount", cachedCount);
            
            log.info("紧固件缓存初始化完成，共缓存 {} 个紧固件", cachedCount);
            
        } catch (Exception e) {
            log.error("初始化紧固件缓存失败", e);
            result.put("status", "error");
            result.put("message", "初始化缓存失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 清空缓存
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearCache() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            fastenerWarehouseCacheService.clearCache();
            
            result.put("status", "success");
            result.put("message", "缓存清空成功");
            
            log.info("紧固件缓存已清空");
            
        } catch (Exception e) {
            log.error("清空紧固件缓存失败", e);
            result.put("status", "error");
            result.put("message", "清空缓存失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 重新加载指定产品代码的缓存
     */
    @PostMapping("/reload/{productCode}")
    public ResponseEntity<Map<String, Object>> reloadCacheForProductCode(@PathVariable String productCode) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            int reloadedCount = fastenerWarehouseCacheService.reloadCacheForProductCode(productCode);
            
            result.put("status", "success");
            result.put("message", "产品代码缓存重新加载成功");
            result.put("productCode", productCode);
            result.put("reloadedCount", reloadedCount);
            
            log.info("产品代码 {} 的缓存重新加载完成，数量: {}", productCode, reloadedCount);
            
        } catch (Exception e) {
            log.error("重新加载产品代码缓存失败: {}", productCode, e);
            result.put("status", "error");
            result.put("message", "重新加载缓存失败: " + e.getMessage());
            result.put("productCode", productCode);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 测试从缓存获取紧固件
     */
    @GetMapping("/test/{productCode}")
    public ResponseEntity<Map<String, Object>> testGetFasteners(@PathVariable String productCode) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            var fasteners = fastenerWarehouseCacheService.getFastenersByProductCodeContaining(productCode);
            
            result.put("status", "success");
            result.put("message", "从缓存获取紧固件成功");
            result.put("productCode", productCode);
            result.put("fastenerCount", fasteners.size());
            result.put("fasteners", fasteners);
            
            log.info("测试从缓存获取紧固件成功: {}, 数量: {}", productCode, fasteners.size());
            
        } catch (Exception e) {
            log.error("测试从缓存获取紧固件失败: {}", productCode, e);
            result.put("status", "error");
            result.put("message", "获取紧固件失败: " + e.getMessage());
            result.put("productCode", productCode);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取缓存统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            boolean redisAvailable = fastenerWarehouseCacheService.isRedisAvailable();
            long cacheSize = fastenerWarehouseCacheService.getCacheSize();
            
            stats.put("redisAvailable", redisAvailable);
            stats.put("cacheSize", cacheSize);
            stats.put("cacheKeyPrefix", "fastener:product:");
            stats.put("status", "success");
            stats.put("message", "缓存统计信息获取成功");
            
            if (redisAvailable) {
                stats.put("performance", "Redis缓存可用，查询性能优化");
            } else {
                stats.put("performance", "Redis不可用，降级到数据库查询");
            }
            
        } catch (Exception e) {
            log.error("获取缓存统计信息失败", e);
            stats.put("status", "error");
            stats.put("message", "获取统计信息失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(stats);
    }
}
