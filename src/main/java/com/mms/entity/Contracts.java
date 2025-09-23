package com.mms.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "contracts")
@EqualsAndHashCode(callSuper = true)
public class Contracts extends BaseEntity {
    
    @Column(name = "contract_no", length = 255, unique = true)
    private String contractNo;
    
    @Column(name = "client_name", length = 511)
    private String clientName;
    
    @Column(name = "project_name", length = 511)
    private String projectName;
    
    @Column(name = "quantity")
    private Integer quantity;
    
    // 合同状态：DRAFT, PROCESSING, COMPLETED, ERROR
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private ContractStatus status = ContractStatus.DRAFT;
    
    // 一对多关系：装箱单
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Containers> containers;
    
    // 一对多关系：合同参数
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ContractParameters> parameters;
    
    // 一对多关系：装箱组件汇总
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContainersComponentsSummary> summaries;
    
    public enum ContractStatus {
        DRAFT,          // 草稿
        PROCESSING,     // 处理中
        COMPLETED,      // 完成
        ERROR           // 错误
    }
}
