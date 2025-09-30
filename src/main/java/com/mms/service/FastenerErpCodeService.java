package com.mms.service;

import com.mms.entity.FastenerWarehouse;
import com.mms.utils.FastenerErpCodeFinder;
import com.mms.utils.FastenerErpCodeFinder.ErpCodeResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 紧固件ERP代码查找服务示例
 * 展示如何使用带Redis缓存的FastenerErpCodeFinder
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FastenerErpCodeService {
    
    @Autowired
    private FastenerErpCodeFinder fastenerErpCodeFinder;
    
    @Autowired
    private FastenerWarehouseCacheService fastenerWarehouseCacheService;
    
    /**
     * 查找单个紧固件的ERP代码（带缓存）
     * 
     * @param componentId 工件ID
     * @param componentCode 工件代码
     * @param name 工件名称
     * @return ERP代码查找结果
     */
    public ErpCodeResult findErpCodeWithCache(Long componentId, String componentCode, String name) {
        log.info("开始查找紧固件ERP代码: componentId={}, componentCode={}, name={}", 
                componentId, componentCode, name);
        
        // 使用带缓存的查找器
        ErpCodeResult result = fastenerErpCodeFinder.findErpCode(componentId, componentCode, name);
        
        if (result.isSuccess()) {
            log.info("ERP代码查找成功: componentId={}, erpCode={}", 
                    componentId, result.getErpCode());
        } else {
            log.warn("ERP代码查找失败: componentId={}, error={}", 
                    componentId, result.getErrorMessage());
        }
        
        return result;
    }
    
    /**
     * 批量查找紧固件ERP代码
     * 
     * @param componentData 工件数据数组，每个元素包含[componentId, componentCode, name]
     * @return 查找结果数组
     */
    public ErpCodeResult[] batchFindErpCodes(Object[][] componentData) {
        log.info("开始批量查找紧固件ERP代码，数量: {}", componentData.length);
        
        ErpCodeResult[] results = new ErpCodeResult[componentData.length];
        
        for (int i = 0; i < componentData.length; i++) {
            Object[] data = componentData[i];
            Long componentId = (Long) data[0];
            String componentCode = (String) data[1];
            String name = (String) data[2];
            
            results[i] = findErpCodeWithCache(componentId, componentCode, name);
        }
        
        log.info("批量查找完成，成功: {}, 失败: {}", 
                countSuccess(results), countFailures(results));
        
        return results;
    }
    
    /**
     * 测试缓存功能
     */
    public void testCacheFunctionality() {
        log.info("=== 测试紧固件缓存功能 ===");
        
        // 1. 检查Redis状态
        boolean redisAvailable = fastenerWarehouseCacheService.isRedisAvailable();
        long cacheSize = fastenerWarehouseCacheService.getCacheSize();
        
        log.info("Redis状态: {}, 缓存大小: {}", redisAvailable, cacheSize);
        
        // 2. 测试查找功能
        String testProductCode = "GB5783";
        List<FastenerWarehouse> fasteners = fastenerWarehouseCacheService
                .getFastenersByProductCodeContaining(testProductCode);
        
        log.info("测试查找产品代码: {}, 找到紧固件数量: {}", testProductCode, fasteners.size());
        
        // 3. 测试ERP代码查找
        ErpCodeResult result = findErpCodeWithCache(1L, "GB5783-M6*20-8.8Z", "螺栓");
        log.info("ERP代码查找测试结果: {}", result.isSuccess() ? "成功" : "失败");
        
        log.info("=== 缓存功能测试完成 ===");
    }
    
    /**
     * 初始化缓存
     */
    public void initializeCache() {
        log.info("开始初始化紧固件缓存...");
        
        try {
            int cachedCount = fastenerWarehouseCacheService.initializeCache();
            log.info("紧固件缓存初始化完成，共缓存 {} 个紧固件", cachedCount);
        } catch (Exception e) {
            log.error("初始化紧固件缓存失败", e);
        }
    }
    
    /**
     * 获取缓存统计信息
     */
    public void printCacheStats() {
        log.info("=== 紧固件缓存统计信息 ===");
        
        boolean redisAvailable = fastenerWarehouseCacheService.isRedisAvailable();
        long cacheSize = fastenerWarehouseCacheService.getCacheSize();
        
        log.info("Redis可用: {}", redisAvailable);
        log.info("缓存大小: {}", cacheSize);
        log.info("缓存键前缀: fastener:product:");
        
        if (redisAvailable) {
            log.info("性能状态: Redis缓存可用，查询性能优化");
        } else {
            log.info("性能状态: Redis不可用，降级到数据库查询");
        }
        
        log.info("=== 统计信息完成 ===");
    }
    
    /**
     * 运行完整的演示程序
     */
    public void runDemo() {
        log.info("=== 紧固件ERP代码查找服务演示 ===");
        
        // 1. 打印缓存统计
        printCacheStats();
        
        // 2. 测试缓存功能
        testCacheFunctionality();
        
        // 3. 测试查找功能
        Object[][] testData = {
            {1L, "GB5783-M6*20-8.8Z", "螺栓"},
            {2L, "GB6170-M6-8Z", "螺母"},
            {3L, "GB97.1-M6-140HV-Z", "平垫圈"},
            {4L, "FT001", "非紧固件"},
            {5L, "INVALID_CODE", "无效代码"},
        };
        
        ErpCodeResult[] results = batchFindErpCodes(testData);
        
        // 4. 打印结果
        printResults(results);
        
        log.info("=== 演示程序完成 ===");
    }
    
    /**
     * 打印查找结果
     */
    private void printResults(ErpCodeResult[] results) {
        log.info("=== 查找结果汇总 ===");
        
        for (int i = 0; i < results.length; i++) {
            ErpCodeResult result = results[i];
            log.info("结果 {}: 工件ID={}, 工件代码={}, 工件名称={}", 
                    i + 1, result.getComponentId(), result.getComponentCode(), result.getName());
            
            if (result.isSuccess()) {
                log.info("  ✅ 成功 - ERP代码: {}, 匹配产品代码: {}", 
                        result.getErpCode(), result.getMatchedProductCode());
            } else {
                log.info("  ❌ 失败 - 错误: {}", result.getErrorMessage());
            }
        }
        
        log.info("=== 结果汇总完成 ===");
    }
    
    /**
     * 统计成功数量
     */
    private int countSuccess(ErpCodeResult[] results) {
        int count = 0;
        for (ErpCodeResult result : results) {
            if (result.isSuccess()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * 统计失败数量
     */
    private int countFailures(ErpCodeResult[] results) {
        int count = 0;
        for (ErpCodeResult result : results) {
            if (!result.isSuccess()) {
                count++;
            }
        }
        return count;
    }
}