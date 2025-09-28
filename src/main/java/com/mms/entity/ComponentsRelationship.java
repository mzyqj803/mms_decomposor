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
    @JoinColumn(name = "Parent_ID", nullable = false)
    @JsonIgnore
    private Components parent;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Child_ID", nullable = false)
    @JsonIgnore
    private Components child;
    
    @Column(name = "quantity")
    private Integer quantity = 1; // 默认数量为1
}
