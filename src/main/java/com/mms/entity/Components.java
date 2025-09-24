package com.mms.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "components")
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({"specs", "processes", "children", "parents"})
public class Components extends BaseEntity {
    
    @Column(name = "category_code", length = 50)
    private String categoryCode;
    
    @Column(name = "component_code", length = 50, unique = true)
    private String componentCode;
    
    @Column(name = "name", length = 511)
    private String name;
    
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;
    
    @Column(name = "procurement_flag")
    private Boolean procurementFlag = false;
    
    @Column(name = "common_parts_flag")
    private Boolean commonPartsFlag = false;
    
    // 一对多关系：组件规格
    @OneToMany(mappedBy = "component", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ComponentsSpec> specs;
    
    // 一对多关系：组件工艺
    @OneToMany(mappedBy = "component", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ComponentsProcesses> processes;
    
    // 一对多关系：父组件关系
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ComponentsRelationship> children;
    
    // 一对多关系：子组件关系
    @OneToMany(mappedBy = "child", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ComponentsRelationship> parents;
}
