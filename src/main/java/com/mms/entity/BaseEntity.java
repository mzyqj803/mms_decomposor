package com.mms.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @CreatedDate
    @Column(name = "entry_ts", nullable = false, updatable = false)
    private LocalDateTime entryTs;
    
    @Column(name = "entry_user", length = 50, nullable = false, updatable = false)
    private String entryUser = "SYS_USER";
    
    @LastModifiedDate
    @Column(name = "last_update_ts")
    private LocalDateTime lastUpdateTs;
    
    @Column(name = "last_update_user", length = 50)
    private String lastUpdateUser = "SYS_USER";
}
