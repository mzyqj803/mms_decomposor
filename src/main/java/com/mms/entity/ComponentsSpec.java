package com.mms.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;

@Data
@Entity
@Table(name = "components_spec")
@EqualsAndHashCode(callSuper = true)
public class ComponentsSpec extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id", nullable = false)
    private Components component;
    
    @Column(name = "spec_code", length = 50)
    private String specCode;
    
    @Column(name = "spec_value", length = 511)
    private String specValue;
    
    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;
}
