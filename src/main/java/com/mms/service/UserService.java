package com.mms.service;

import com.mms.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    
    /**
     * 获取用户列表（分页）
     */
    Page<User> getUsers(String keyword, Pageable pageable);
    
    /**
     * 根据ID获取用户
     */
    User getUserById(Long id);
    
    /**
     * 根据用户名获取用户
     */
    User getUserByUsername(String username);
    
    /**
     * 创建用户
     */
    User createUser(User user, List<Long> roleIds);
    
    /**
     * 更新用户
     */
    User updateUser(Long id, User user, List<Long> roleIds);
    
    /**
     * 删除用户
     */
    void deleteUser(Long id);
    
    /**
     * 启用/禁用用户
     */
    User toggleUserEnabled(Long id);
    
    /**
     * 修改密码
     */
    void changePassword(Long id, String oldPassword, String newPassword);
    
    /**
     * 重置密码
     */
    void resetPassword(Long id, String newPassword);
}

