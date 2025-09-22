package com.mms.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@Entity
@Table(name = "contract_parameters")
@EqualsAndHashCode(callSuper = true)
public class ContractParameters extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contracts contract;
    
    @Column(name = "param_name", length = 255)
    private String paramName;
    
    @Column(name = "param_value", length = 511)
    private String paramValue;
}
