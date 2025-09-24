package com.mms.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
    
    // 合同状态：0=DRAFT, 1=PROCESSING, 2=COMPLETED, 3=ERROR
    @Column(name = "status")
    private Integer status = 0;
    
    // 一对多关系：装箱单
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Containers> containers;
    
    // 一对多关系：合同参数
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ContractParameters> parameters;
    
    // 一对多关系：装箱组件汇总
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ContainersComponentsSummary> summaries;
    
    public static class ContractStatus {
        public static final int DRAFT = 0;          // 草稿
        public static final int PROCESSING = 1;      // 处理中
        public static final int COMPLETED = 2;       // 完成
        public static final int ERROR = 3;           // 错误
        
        public static String getStatusText(Integer status) {
            if (status == null) return "未知";
            switch (status) {
                case DRAFT: return "草稿";
                case PROCESSING: return "处理中";
                case COMPLETED: return "完成";
                case ERROR: return "错误";
                default: return "未知";
            }
        }
        
        public static String getStatusType(Integer status) {
            if (status == null) return "info";
            switch (status) {
                case DRAFT: return "info";
                case PROCESSING: return "warning";
                case COMPLETED: return "success";
                case ERROR: return "danger";
                default: return "info";
            }
        }
    }
}
