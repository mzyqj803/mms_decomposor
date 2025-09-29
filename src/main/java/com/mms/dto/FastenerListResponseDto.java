package com.mms.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 紧固件列表响应DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FastenerListResponseDto {
    
    /**
     * 产线装配紧固件列表
     */
    private List<ComponentFastenerInfo> assembledFasteners;
    
    /**
     * 仓库装箱紧固件列表
     */
    private List<ComponentFastenerInfo> unassembledFasteners;
    
    /**
     * 产线装配紧固件数量
     */
    private Integer assembledCount;
    
    /**
     * 仓库装箱紧固件数量
     */
    private Integer unassembledCount;
    
    /**
     * 紧固件详细信息
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ComponentFastenerInfo {
        private Long componentId;
        private String componentCode;
        private String name;
        private String assemblyType;
    }
}
