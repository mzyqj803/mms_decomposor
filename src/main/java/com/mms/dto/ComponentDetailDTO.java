package com.mms.dto;

import com.mms.entity.ComponentsProcesses;
import com.mms.entity.ComponentsSpec;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 零部件详情DTO
 * 用于返回完整的零部件信息，包括关联关系
 */
@Data
public class ComponentDetailDTO {
    
    private Long id;
    private String categoryCode;
    private String componentCode;
    private String name;
    private String comment;
    private Boolean procurementFlag;
    private Integer commonPartsFlag;
    private Integer status;
    private LocalDateTime entryTs;
    
    // 规格信息
    private List<ComponentsSpec> specs = new ArrayList<>();
    
    // 工艺信息
    private List<ComponentsProcesses> processes = new ArrayList<>();
    
    // 子组件关系
    private List<RelationshipDTO> children = new ArrayList<>();
    
    // 父组件关系
    private List<RelationshipDTO> parents = new ArrayList<>();
    
    /**
     * 关联关系DTO
     */
    @Data
    public static class RelationshipDTO {
        private Long id;
        private Integer quantity;
        
        // 关联的组件信息
        private ComponentInfo child;
        private ComponentInfo parent;
        
        /**
         * 组件基本信息
         */
        @Data
        public static class ComponentInfo {
            private Long id;
            private String componentCode;
            private String name;
            private String categoryCode;
        }
    }
}

