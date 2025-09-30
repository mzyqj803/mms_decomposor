package com.mms.utils;

import com.mms.dto.FastenerParseResult;
import com.mms.entity.FastenerWarehouse;
import com.mms.repository.ComponentFastenerRepository;
import com.mms.service.FastenerWarehouseCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 紧固件ERP代码查找工具类
 * 根据component_code和name查找对应的fastenerWarehouse中的ERP_Code
 * 
 * 工作流程：
 * 1. 判断component_id是否存在于紧固件视图中
 * 2. 调用FastenerParser解析component_code和name
 * 3. 按照productCode -> specs -> level -> surfaceTreatment的顺序匹配
 * 4. 返回匹配的ERP_Code
 */
@Component
public class FastenerErpCodeFinder {
    
    @Autowired
    private ComponentFastenerRepository componentFastenerRepository;
    
    @Autowired
    private FastenerWarehouseCacheService fastenerWarehouseCacheService;
    
    @Autowired
    private FastenerParser fastenerParser;
    
    /**
     * 查找紧固件的ERP代码
     * 
     * @param componentId 工件ID
     * @param componentCode 工件代码
     * @param name 工件名称
     * @return ERP代码查找结果
     */
    public ErpCodeResult findErpCode(Long componentId, String componentCode, String name) {
        // 1. 检查是否为紧固件
        if (!isFastener(componentId)) {
            return ErpCodeResult.notFastener(componentId, componentCode, name);
        }
        
        // 2. 解析紧固件信息
        FastenerParseResult parseResult = fastenerParser.parse(componentCode, name);
        if (!parseResult.isSuccess()) {
            return ErpCodeResult.parseError(componentId, componentCode, name, parseResult.getErrorMessage());
        }
        
        // 3. 按优先级匹配ERP代码
        return matchErpCode(componentId, componentCode, name, parseResult);
    }
    
    /**
     * 检查是否为紧固件
     */
    private boolean isFastener(Long componentId) {
        return componentFastenerRepository.isAssembledFastener(componentId) || 
               componentFastenerRepository.isUnassembledFastener(componentId);
    }
    
    /**
     * 按优先级匹配ERP代码
     * 匹配顺序：productCode -> specs -> level -> surfaceTreatment
     */
    private ErpCodeResult matchErpCode(Long componentId, String componentCode, String name, FastenerParseResult parseResult) {
        String productCode = parseResult.getProductCode();
        String specs = parseResult.getSpecs();
        String level = parseResult.getLevel();
        String surfaceTreatment = parseResult.getSurfaceTreatment();
        
        // 1. 先查找productCode是否存在（优先从Redis缓存查找）
        List<FastenerWarehouse> productCodeMatches = fastenerWarehouseCacheService.getFastenersByProductCodeContaining(productCode);
        if (productCodeMatches.isEmpty()) {
            return ErpCodeResult.unknownMaterial(componentId, componentCode, name, productCode);
        }
        
        // 2. 在productCode匹配的基础上，按优先级进一步匹配
        List<FastenerWarehouse> currentMatches = productCodeMatches;
        
        // 2.1 匹配specs
        if (specs != null && !specs.trim().isEmpty()) {
            List<FastenerWarehouse> specsMatches = filterBySpecs(currentMatches, specs);
            if (!specsMatches.isEmpty()) {
                currentMatches = specsMatches;
            }
        }
        
        // 2.2 匹配level
        if (level != null && !level.trim().isEmpty()) {
            List<FastenerWarehouse> levelMatches = filterByLevel(currentMatches, level);
            if (!levelMatches.isEmpty()) {
                currentMatches = levelMatches;
            }
        }
        
        // 2.3 匹配surfaceTreatment
        if (surfaceTreatment != null && !surfaceTreatment.trim().isEmpty()) {
            List<FastenerWarehouse> surfaceMatches = filterBySurfaceTreatment(currentMatches, surfaceTreatment);
            if (!surfaceMatches.isEmpty()) {
                currentMatches = surfaceMatches;
            }
        }
        
        // 3. 返回第一个匹配的ERP代码
        if (!currentMatches.isEmpty()) {
            FastenerWarehouse matched = currentMatches.get(0);
            return ErpCodeResult.success(componentId, componentCode, name, matched.getErpCode(), 
                                       productCode, specs, level, surfaceTreatment);
        }
        
        // 4. 如果所有匹配都失败，返回productCode匹配的第一个
        return ErpCodeResult.success(componentId, componentCode, name, productCodeMatches.get(0).getErpCode(),
                                   productCode, specs, level, surfaceTreatment);
    }
    
    /**
     * 按规格过滤
     */
    private List<FastenerWarehouse> filterBySpecs(List<FastenerWarehouse> candidates, String specs) {
        return candidates.stream()
                .filter(fw -> fw.getSpecs() != null && fw.getSpecs().contains(specs))
                .toList();
    }
    
    /**
     * 按等级过滤
     */
    private List<FastenerWarehouse> filterByLevel(List<FastenerWarehouse> candidates, String level) {
        return candidates.stream()
                .filter(fw -> fw.getLevel() != null && fw.getLevel().contains(level))
                .toList();
    }
    
    /**
     * 按表面处理过滤
     */
    private List<FastenerWarehouse> filterBySurfaceTreatment(List<FastenerWarehouse> candidates, String surfaceTreatment) {
        return candidates.stream()
                .filter(fw -> fw.getSurfaceTreatment() != null && fw.getSurfaceTreatment().contains(surfaceTreatment))
                .toList();
    }
    
    /**
     * ERP代码查找结果
     */
    public static class ErpCodeResult {
        private Long componentId;
        private String componentCode;
        private String name;
        private String erpCode;
        private boolean success;
        private String errorMessage;
        private String matchedProductCode;
        private String matchedSpecs;
        private String matchedLevel;
        private String matchedSurfaceTreatment;
        
        private ErpCodeResult() {}
        
        /**
         * 成功匹配
         */
        public static ErpCodeResult success(Long componentId, String componentCode, String name, String erpCode,
                                           String productCode, String specs, String level, String surfaceTreatment) {
            ErpCodeResult result = new ErpCodeResult();
            result.componentId = componentId;
            result.componentCode = componentCode;
            result.name = name;
            result.erpCode = erpCode;
            result.success = true;
            result.matchedProductCode = productCode;
            result.matchedSpecs = specs;
            result.matchedLevel = level;
            result.matchedSurfaceTreatment = surfaceTreatment;
            return result;
        }
        
        /**
         * 不是紧固件
         */
        public static ErpCodeResult notFastener(Long componentId, String componentCode, String name) {
            ErpCodeResult result = new ErpCodeResult();
            result.componentId = componentId;
            result.componentCode = componentCode;
            result.name = name;
            result.success = false;
            result.errorMessage = "不是紧固件，跳过";
            return result;
        }
        
        /**
         * 解析错误
         */
        public static ErpCodeResult parseError(Long componentId, String componentCode, String name, String errorMessage) {
            ErpCodeResult result = new ErpCodeResult();
            result.componentId = componentId;
            result.componentCode = componentCode;
            result.name = name;
            result.success = false;
            result.errorMessage = "解析错误: " + errorMessage;
            return result;
        }
        
        /**
         * 未知物料
         */
        public static ErpCodeResult unknownMaterial(Long componentId, String componentCode, String name, String productCode) {
            ErpCodeResult result = new ErpCodeResult();
            result.componentId = componentId;
            result.componentCode = componentCode;
            result.name = name;
            result.success = false;
            result.errorMessage = "未知物料: " + productCode;
            return result;
        }
        
        // Getters
        public Long getComponentId() { return componentId; }
        public String getComponentCode() { return componentCode; }
        public String getName() { return name; }
        public String getErpCode() { return erpCode; }
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public String getMatchedProductCode() { return matchedProductCode; }
        public String getMatchedSpecs() { return matchedSpecs; }
        public String getMatchedLevel() { return matchedLevel; }
        public String getMatchedSurfaceTreatment() { return matchedSurfaceTreatment; }
    }
}
