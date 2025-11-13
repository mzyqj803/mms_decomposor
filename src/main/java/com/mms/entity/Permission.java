package com.mms.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Entity
@Table(name = "permissions")
public class Permission extends BaseEntity {
    
    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;
    
    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;
    
    @Column(name = "description", length = 255)
    private String description;
    
    @Column(name = "resource", length = 50)
    private String resource; // 资源类型，如：CONTRACT, CONTAINER, COMPONENT, USER, ROLE
    
    @Column(name = "action", length = 50)
    private String action; // 操作类型，如：CREATE, UPDATE, DELETE, VIEW, EXECUTE
    
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
    
    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles = new HashSet<>();
}


