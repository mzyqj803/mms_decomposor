package com.mms.controller;

import com.mms.service.ComponentCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存测试控制器
 */
@RestController
@RequestMapping("/cache")
@RequiredArgsConstructor
@Slf4j
public class CacheTestController {
    
    private final ComponentCacheService componentCacheService;
    
    /**
     * 检查Redis连接状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getCacheStatus() {
        Map<String, Object> response = new HashMap<>();
        
        boolean redisAvailable = componentCacheService.isRedisAvailable();
        long cacheSize = componentCacheService.getCacheSize();
        
        response.put("redisAvailable", redisAvailable);
        response.put("cacheSize", cacheSize);
        response.put("message", redisAvailable ? "Redis连接正常" : "Redis连接失败");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 重新初始化缓存
     */
    @PostMapping("/reload")
    public ResponseEntity<Map<String, Object>> reloadCache() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            int cachedCount = componentCacheService.initializeCache();
            response.put("success", true);
            response.put("cachedCount", cachedCount);
            response.put("message", "缓存重新加载成功");
            
            log.info("手动重新加载缓存完成，共缓存 {} 个零部件", cachedCount);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "缓存重新加载失败: " + e.getMessage());
            log.error("手动重新加载缓存失败", e);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 清空缓存
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearCache() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            componentCacheService.clearCache();
            response.put("success", true);
            response.put("message", "缓存已清空");
            
            log.info("手动清空缓存完成");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "清空缓存失败: " + e.getMessage());
            log.error("手动清空缓存失败", e);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 测试从缓存获取零部件
     */
    @GetMapping("/component/{componentCode}")
    public ResponseEntity<Map<String, Object>> getComponentFromCache(@PathVariable String componentCode) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            var componentJson = componentCacheService.getComponentFromCache(componentCode);
            
            if (componentJson.isPresent()) {
                response.put("success", true);
                response.put("found", true);
                response.put("componentCode", componentCode);
                response.put("componentJson", componentJson.get());
                response.put("message", "从缓存中找到零部件");
            } else {
                response.put("success", true);
                response.put("found", false);
                response.put("componentCode", componentCode);
                response.put("message", "缓存中未找到该零部件");
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取零部件失败: " + e.getMessage());
            log.error("从缓存获取零部件失败: componentCode={}", componentCode, e);
        }
        
        return ResponseEntity.ok(response);
    }
}
