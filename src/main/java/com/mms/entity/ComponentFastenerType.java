package com.mms.entity;

import lombok.Data;

import jakarta.persistence.*;
import java.io.Serializable;

/**
 * 工件装配类型查询结果实体
 * 用于封装查询工件是产线装配还是仓库装箱紧固件的结果
 */
@Data
@Entity
@Table(name = "components_spec")
public class ComponentFastenerType implements Serializable {
    
    @Id
    @Column(name = "component_id")
    private Long componentId;
    
    @Column(name = "assembly_type")
    private String assemblyType; // "产线装配" 或 "仓库装箱"
    
    public ComponentFastenerType() {}
    
    public ComponentFastenerType(Long componentId, String assemblyType) {
        this.componentId = componentId;
        this.assemblyType = assemblyType;
    }
}
