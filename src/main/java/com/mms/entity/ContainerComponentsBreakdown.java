package com.mms.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@Entity
@Table(name = "container_components_breakdown")
@EqualsAndHashCode(callSuper = true)
public class ContainerComponentsBreakdown extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "container_component_id", nullable = false)
    private ContainerComponents containerComponent;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_component_id", nullable = false)
    private Components subComponent;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "container_id", nullable = false)
    private Containers container;
    
    @Column(name = "quantity")
    private Integer quantity;
}
