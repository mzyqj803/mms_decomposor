package com.mms.controller;

import com.mms.entity.Role;
import com.mms.service.RoleService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {
    
    private final RoleService roleService;
    
    /**
     * 获取角色列表（分页）
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getRoles(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? 
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Role> roles = roleService.getRoles(keyword, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", roles.getContent());
        response.put("totalElements", roles.getTotalElements());
        response.put("page", roles.getNumber());
        response.put("size", roles.getSize());
        response.put("totalPages", roles.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 根据ID获取角色
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getRoleById(@PathVariable Long id) {
        try {
            Role role = roleService.getRoleById(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", role);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    /**
     * 创建角色
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createRole(@RequestBody CreateRoleRequest request) {
        try {
            Role role = new Role();
            role.setName(request.getName());
            role.setCode(request.getCode());
            role.setDescription(request.getDescription());
            role.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
            
            Role createdRole = roleService.createRole(role, request.getPermissionIds());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "角色创建成功");
            response.put("data", createdRole);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateRole(
            @PathVariable Long id,
            @RequestBody UpdateRoleRequest request) {
        try {
            Role role = new Role();
            role.setName(request.getName());
            role.setCode(request.getCode());
            role.setDescription(request.getDescription());
            role.setEnabled(request.getEnabled());
            
            Role updatedRole = roleService.updateRole(id, role, request.getPermissionIds());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "角色更新成功");
            response.put("data", updatedRole);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteRole(@PathVariable Long id) {
        try {
            roleService.deleteRole(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "角色删除成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    /**
     * 启用/禁用角色
     */
    @PutMapping("/{id}/toggle-enabled")
    public ResponseEntity<Map<String, Object>> toggleRoleEnabled(@PathVariable Long id) {
        try {
            Role role = roleService.toggleRoleEnabled(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", role.getEnabled() ? "角色已启用" : "角色已禁用");
            response.put("data", role);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    /**
     * 获取角色权限
     */
    @GetMapping("/{id}/permissions")
    public ResponseEntity<Map<String, Object>> getRolePermissions(@PathVariable Long id) {
        try {
            Role role = roleService.getRoleById(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", role.getPermissions());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    /**
     * 更新角色权限
     */
    @PutMapping("/{id}/permissions")
    public ResponseEntity<Map<String, Object>> updateRolePermissions(
            @PathVariable Long id,
            @RequestBody UpdateRolePermissionsRequest request) {
        try {
            Role role = roleService.updateRolePermissions(id, request.getPermissionIds());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "角色权限更新成功");
            response.put("data", role);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    @Data
    public static class CreateRoleRequest {
        private String name;
        private String code;
        private String description;
        private Boolean enabled;
        private List<Long> permissionIds;
    }
    
    @Data
    public static class UpdateRoleRequest {
        private String name;
        private String code;
        private String description;
        private Boolean enabled;
        private List<Long> permissionIds;
    }
    
    @Data
    public static class UpdateRolePermissionsRequest {
        private List<Long> permissionIds;
    }
}

