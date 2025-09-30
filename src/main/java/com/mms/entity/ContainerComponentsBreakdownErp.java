package com.mms.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

/**
 * 工艺分解ERP代码实体类
 * 用于存储工艺分解结果中每个组件的ERP代码信息
 */
@Data
@Entity
@Table(name = "container_components_breakdown_erp")
@EqualsAndHashCode(callSuper = true)
public class ContainerComponentsBreakdownErp extends BaseEntity {
    
    /**
     * 关联的工艺分解记录
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "breakdown_id", nullable = false)
    @JsonIgnore
    private ContainerComponentsBreakdown breakdown;
    
    /**
     * ERP代码
     */
    @Column(name = "erp_code", length = 255)
    private String erpCode;
    
    /**
     * 备注信息
     */
    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;
}
