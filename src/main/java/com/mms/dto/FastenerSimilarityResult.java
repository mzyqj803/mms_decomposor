package com.mms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 紧固件相似度搜索结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FastenerSimilarityResult {
    
    /**
     * 紧固件ID
     */
    private Long id;
    
    /**
     * ERP代码
     */
    private String erpCode;
    
    /**
     * 规格
     */
    private String specs;
    
    /**
     * 产品代码
     */
    private String productCode;
    
    /**
     * 名称
     */
    private String name;
    
    /**
     * 等级
     */
    private String level;
    
    /**
     * 材料
     */
    private String material;
    
    /**
     * 表面处理
     */
    private String surfaceTreatment;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 相似度分数
     */
    private Float similarityScore;
    
    /**
     * 是否为默认紧固件
     */
    private Boolean defaultFlag;
}


