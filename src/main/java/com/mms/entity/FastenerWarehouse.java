package com.mms.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;

@Data
@Entity
@Table(name = "fastener_warehouse")
@EqualsAndHashCode(callSuper = true)
public class FastenerWarehouse extends BaseEntity {
    
    @Column(name = "specs", length = 511)
    private String specs;
    
    @Column(name = "product_code", length = 255)
    private String productCode;
    
    @Column(name = "erp_code", length = 255)
    private String erpCode;
    
    @Column(name = "name", length = 511)
    private String name;
    
    @Column(name = "level", length = 50)
    private String level;
    
    @Column(name = "material", length = 255)
    private String material;
    
    @Column(name = "surface_treatment", length = 255)
    private String surfaceTreatment;
    
    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;
    
    @Column(name = "default_flag")
    private Boolean defaultFlag = false;
}
