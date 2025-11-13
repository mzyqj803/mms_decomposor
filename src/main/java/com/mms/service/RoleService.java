package com.mms.service;

import com.mms.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RoleService {
    
    /**
     * 获取角色列表（分页）
     */
    Page<Role> getRoles(String keyword, Pageable pageable);
    
    /**
     * 根据ID获取角色
     */
    Role getRoleById(Long id);
    
    /**
     * 根据代码获取角色
     */
    Role getRoleByCode(String code);
    
    /**
     * 创建角色
     */
    Role createRole(Role role, List<Long> permissionIds);
    
    /**
     * 更新角色
     */
    Role updateRole(Long id, Role role, List<Long> permissionIds);
    
    /**
     * 删除角色
     */
    void deleteRole(Long id);
    
    /**
     * 启用/禁用角色
     */
    Role toggleRoleEnabled(Long id);
    
    /**
     * 更新角色权限
     */
    Role updateRolePermissions(Long id, List<Long> permissionIds);
}

