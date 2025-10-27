package com.mms.entity;

import lombok.Data;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@MappedSuperclass
public abstract class BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 移除 @CreatedDate 避免与数据库 DEFAULT CURRENT_TIMESTAMP() 冲突
    // 完全依赖数据库默认值来设置创建时间，避免并发写入时的时间戳冲突
    @Column(name = "entry_ts", nullable = false, insertable = false, updatable = false)
    private LocalDateTime entryTs;
    
    @Column(name = "entry_user", length = 50, nullable = false, updatable = false)
    private String entryUser = "SYS_USER";
    
    // 移除 @LastModifiedDate 避免与数据库 ON UPDATE CURRENT_TIMESTAMP() 冲突
    // 完全依赖数据库触发器来设置更新时间，避免并发写入时的时间戳冲突
    @Column(name = "last_update_ts", insertable = false, updatable = false)
    private LocalDateTime lastUpdateTs;
    
    @Column(name = "last_update_user", length = 50)
    private String lastUpdateUser = "SYS_USER";
}
