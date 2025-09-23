package com.mms.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;

@Entity
@Table(name = "contract_parameters")
@Data
@EqualsAndHashCode(callSuper = true)
public class ContractParameters extends BaseEntity {
    
    @Column(name = "param_name", length = 255)
    private String paramName;
    
    @Column(name = "param_value", length = 511)
    private String paramValue;
    
    // 多对一关系：合同参数属于一个合同
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    @JsonBackReference
    private Contracts contract;
}