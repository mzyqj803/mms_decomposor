package com.mms.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "container_components_breakdown_problems")
@EqualsAndHashCode(callSuper = true)
public class ContainerComponentsBreakdownProblems extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "container_id", nullable = false)
    @JsonIgnore
    private Containers container;
    
    @Column(name = "component_no", length = 255)
    private String componentNo;
    
    @Column(name = "quantity")
    private Integer quantity;
    
    @Column(name = "entry_ts")
    private LocalDateTime entryTs;
    
    @Column(name = "entry_user", length = 50)
    private String entryUser;
    
    @Column(name = "last_update_ts")
    private LocalDateTime lastUpdateTs;
    
    @Column(name = "last_update_user", length = 50)
    private String lastUpdateUser;
}
