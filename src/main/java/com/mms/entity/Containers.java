package com.mms.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "containers")
@EqualsAndHashCode(callSuper = true)
public class Containers extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contracts contract;
    
    @Column(name = "container_no", length = 255)
    private String containerNo;
    
    @Column(name = "name", length = 511)
    private String name;
    
    @Column(name = "container_size", length = 50)
    private String containerSize;
    
    @Column(name = "container_weight", length = 50)
    private String containerWeight;
    
    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;
    
    // 一对多关系：装箱组件
    @OneToMany(mappedBy = "container", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContainerComponents> components;
    
    // 一对多关系：装箱组件汇总
    @OneToMany(mappedBy = "container", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContainersComponentsSummary> summaries;
}
