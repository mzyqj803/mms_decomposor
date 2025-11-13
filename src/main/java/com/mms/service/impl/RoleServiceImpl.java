package com.mms.service.impl;

import com.mms.entity.Permission;
import com.mms.entity.Role;
import com.mms.repository.PermissionRepository;
import com.mms.repository.RoleRepository;
import com.mms.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {
    
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    
    @Override
    @Transactional(readOnly = true)
    public Page<Role> getRoles(String keyword, Pageable pageable) {
        Specification<Role> spec = (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            
            if (StringUtils.hasText(keyword)) {
                Predicate namePredicate = cb.like(cb.lower(root.get("name")), 
                    "%" + keyword.toLowerCase() + "%");
                Predicate codePredicate = cb.like(cb.lower(root.get("code")), 
                    "%" + keyword.toLowerCase() + "%");
                Predicate descriptionPredicate = cb.like(cb.lower(root.get("description")), 
                    "%" + keyword.toLowerCase() + "%");
                predicate = cb.and(predicate, cb.or(namePredicate, codePredicate, descriptionPredicate));
            }
            
            return predicate;
        };
        
        return roleRepository.findAll(spec, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("角色不存在: " + id));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Role getRoleByCode(String code) {
        return roleRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("角色不存在: " + code));
    }
    
    @Override
    @Transactional
    public Role createRole(Role role, List<Long> permissionIds) {
        // 检查角色代码是否已存在
        if (roleRepository.existsByCode(role.getCode())) {
            throw new RuntimeException("角色代码已存在: " + role.getCode());
        }
        
        // 检查角色名称是否已存在
        if (roleRepository.existsByName(role.getName())) {
            throw new RuntimeException("角色名称已存在: " + role.getName());
        }
        
        // 设置权限
        if (permissionIds != null && !permissionIds.isEmpty()) {
            Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(permissionIds));
            role.setPermissions(permissions);
        }
        
        // 设置默认值
        if (role.getEnabled() == null) {
            role.setEnabled(true);
        }
        
        return roleRepository.save(role);
    }
    
    @Override
    @Transactional
    public Role updateRole(Long id, Role role, List<Long> permissionIds) {
        Role existingRole = getRoleById(id);
        
        // 检查角色代码是否已被其他角色使用
        if (!existingRole.getCode().equals(role.getCode()) && 
            roleRepository.existsByCode(role.getCode())) {
            throw new RuntimeException("角色代码已存在: " + role.getCode());
        }
        
        // 检查角色名称是否已被其他角色使用
        if (!existingRole.getName().equals(role.getName()) && 
            roleRepository.existsByName(role.getName())) {
            throw new RuntimeException("角色名称已存在: " + role.getName());
        }
        
        // 更新角色信息
        existingRole.setName(role.getName());
        existingRole.setCode(role.getCode());
        existingRole.setDescription(role.getDescription());
        existingRole.setEnabled(role.getEnabled());
        
        // 更新权限
        if (permissionIds != null) {
            Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(permissionIds));
            existingRole.setPermissions(permissions);
        }
        
        return roleRepository.save(existingRole);
    }
    
    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = getRoleById(id);
        roleRepository.delete(role);
    }
    
    @Override
    @Transactional
    public Role toggleRoleEnabled(Long id) {
        Role role = getRoleById(id);
        role.setEnabled(!role.getEnabled());
        return roleRepository.save(role);
    }
    
    @Override
    @Transactional
    public Role updateRolePermissions(Long id, List<Long> permissionIds) {
        Role role = getRoleById(id);
        
        if (permissionIds != null) {
            Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(permissionIds));
            role.setPermissions(permissions);
        } else {
            role.setPermissions(new HashSet<>());
        }
        
        return roleRepository.save(role);
    }
}

