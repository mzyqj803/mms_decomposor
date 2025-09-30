package com.mms.dto;

import lombok.Data;

/**
 * 紧固件解析结果DTO
 * 用于存储从component_code和name中解析出的信息
 */
@Data
public class FastenerParseResult {
    
    /**
     * 产品代码（从GB或GB/T后面提取的数字）
     * 例如：GB5783 -> 5783, GB/T6170 -> 6170
     */
    private String productCode;
    
    /**
     * 规格（M开头的规格信息）
     * 例如：M6*20, M10*35, M12
     */
    private String specs;
    
    /**
     * 等级
     * 例如：8.8, 8, 140HV, 430HV
     */
    private String level;
    
    /**
     * 表面处理
     * 例如：镀锌等（当以Z结尾时）
     */
    private String surfaceTreatment;
    
    /**
     * 原始component_code
     */
    private String originalComponentCode;
    
    /**
     * 原始name
     */
    private String originalName;
    
    /**
     * 解析是否成功
     */
    private boolean success;
    
    /**
     * 错误信息（如果解析失败）
     */
    private String errorMessage;
    
    public FastenerParseResult() {
        this.success = false;
    }
    
    public FastenerParseResult(String originalComponentCode, String originalName) {
        this.originalComponentCode = originalComponentCode;
        this.originalName = originalName;
        this.success = false;
    }
    
    /**
     * 创建成功的解析结果
     */
    public static FastenerParseResult success(String originalComponentCode, String originalName,
                                            String productCode, String specs, String level, String surfaceTreatment) {
        FastenerParseResult result = new FastenerParseResult();
        result.originalComponentCode = originalComponentCode;
        result.originalName = originalName;
        result.productCode = productCode;
        result.specs = specs;
        result.level = level;
        result.surfaceTreatment = surfaceTreatment;
        result.success = true;
        return result;
    }
    
    /**
     * 创建失败的解析结果
     */
    public static FastenerParseResult failure(String originalComponentCode, String originalName, String errorMessage) {
        FastenerParseResult result = new FastenerParseResult();
        result.originalComponentCode = originalComponentCode;
        result.originalName = originalName;
        result.errorMessage = errorMessage;
        result.success = false;
        return result;
    }
}
