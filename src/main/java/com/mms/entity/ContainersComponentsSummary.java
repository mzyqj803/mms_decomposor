package com.mms.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;

@Data
@Entity
@Table(name = "containers_components_summary")
@EqualsAndHashCode(callSuper = true)
public class ContainersComponentsSummary extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contracts contract;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id", nullable = false)
    private Components component;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "container_id", nullable = false)
    private Containers container;
    
    @Column(name = "quantity")
    private Integer quantity;
}
