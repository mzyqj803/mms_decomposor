package com.mms.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;

@Data
@Entity
@Table(name = "components_processes")
@EqualsAndHashCode(callSuper = true)
public class ComponentsProcesses extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id", nullable = false)
    private Components component;
    
    @Column(name = "seq_no")
    private Integer seqNo;
    
    @Column(name = "name", length = 511)
    private String name;
    
    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;
}
