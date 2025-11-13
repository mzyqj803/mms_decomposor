package com.mms.service.impl;

import com.mms.entity.Role;
import com.mms.entity.User;
import com.mms.repository.RoleRepository;
import com.mms.repository.UserRepository;
import com.mms.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional(readOnly = true)
    public Page<User> getUsers(String keyword, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            
            if (StringUtils.hasText(keyword)) {
                Predicate usernamePredicate = cb.like(cb.lower(root.get("username")), 
                    "%" + keyword.toLowerCase() + "%");
                Predicate namePredicate = cb.like(cb.lower(root.get("name")), 
                    "%" + keyword.toLowerCase() + "%");
                Predicate emailPredicate = cb.like(cb.lower(root.get("email")), 
                    "%" + keyword.toLowerCase() + "%");
                predicate = cb.and(predicate, cb.or(usernamePredicate, namePredicate, emailPredicate));
            }
            
            return predicate;
        };
        
        return userRepository.findAll(spec, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + id));
    }
    
    @Override
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
    }
    
    @Override
    @Transactional
    public User createUser(User user, List<Long> roleIds) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在: " + user.getUsername());
        }
        
        // 检查邮箱是否已存在
        if (StringUtils.hasText(user.getEmail()) && userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已存在: " + user.getEmail());
        }
        
        // 加密密码
        // BCryptPasswordEncoder会自动为每次加密生成随机salt，确保相同密码产生不同哈希
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 设置角色
        if (roleIds != null && !roleIds.isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIds));
            user.setRoles(roles);
        }
        
        // 设置默认值
        if (user.getEnabled() == null) {
            user.setEnabled(true);
        }
        
        return userRepository.save(user);
    }
    
    @Override
    @Transactional
    public User updateUser(Long id, User user, List<Long> roleIds) {
        User existingUser = getUserById(id);
        
        // 检查用户名是否已被其他用户使用
        if (!existingUser.getUsername().equals(user.getUsername()) && 
            userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在: " + user.getUsername());
        }
        
        // 检查邮箱是否已被其他用户使用
        if (StringUtils.hasText(user.getEmail()) && 
            !user.getEmail().equals(existingUser.getEmail()) && 
            userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已存在: " + user.getEmail());
        }
        
        // 更新用户信息
        existingUser.setUsername(user.getUsername());
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setAvatar(user.getAvatar());
        existingUser.setEnabled(user.getEnabled());
        
        // 更新密码（如果提供了新密码）
        if (StringUtils.hasText(user.getPassword())) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        // 更新角色
        if (roleIds != null) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIds));
            existingUser.setRoles(roles);
        }
        
        return userRepository.save(existingUser);
    }
    
    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }
    
    @Override
    @Transactional
    public User toggleUserEnabled(Long id) {
        User user = getUserById(id);
        user.setEnabled(!user.getEnabled());
        return userRepository.save(user);
    }
    
    @Override
    @Transactional
    public void changePassword(Long id, String oldPassword, String newPassword) {
        User user = getUserById(id);
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }
        
        // 设置新密码（BCrypt会自动生成新的随机salt）
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    @Override
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User user = getUserById(id);
        // 重置密码（BCrypt会自动生成新的随机salt）
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}

