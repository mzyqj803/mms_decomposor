package com.mms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    
    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;
    
    @Column(name = "password", length = 255, nullable = false)
    @JsonIgnore
    private String password;
    
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    
    @Column(name = "email", length = 100)
    private String email;
    
    @Column(name = "avatar", length = 255)
    private String avatar;
    
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}

