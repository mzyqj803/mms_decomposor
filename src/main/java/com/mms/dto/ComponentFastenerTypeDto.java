package com.mms.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 工件装配类型查询结果DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComponentFastenerTypeDto {
    
    /**
     * 工件ID
     */
    private Long componentId;
    
    /**
     * 装配类型：null-无装配信息，"产线装配"-产线装配，"仓库装箱"-仓库装箱
     */
    private String assemblyType;
    
    /**
     * 工件的详细信息
     */
    private ComponentInfo componentInfo;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ComponentInfo {
        private String componentCode;
        private String name;
        private String categoryCode;
    }
}
