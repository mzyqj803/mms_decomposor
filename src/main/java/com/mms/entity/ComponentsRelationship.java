package com.mms.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

@Data
@Entity
@Table(name = "components_relationship")
@EqualsAndHashCode(callSuper = true)
public class ComponentsRelationship extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    @JsonIgnore
    private Components parent;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    @JsonIgnore
    private Components child;
    
    @Column(name = "quantity")
    private Integer quantity = 1; // 默认数量为1
}
