package com.mms.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "container_components")
@EqualsAndHashCode(callSuper = true)
public class ContainerComponents extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "container_id", nullable = false)
    @JsonBackReference
    private Containers container;
    
    @Column(name = "component_no", length = 255)
    private String componentNo;
    
    @Column(name = "component_name", length = 511)
    private String componentName;
    
    @Column(name = "unit_code", length = 50)
    private String unitCode;
    
    @Column(name = "quantity")
    private Integer quantity;
    
    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;
    
    // 一对多关系：装箱组件分解
    @OneToMany(mappedBy = "containerComponent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContainerComponentsBreakdown> breakdowns;
}
