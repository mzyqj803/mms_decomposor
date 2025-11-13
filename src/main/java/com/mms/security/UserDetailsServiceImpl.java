package com.mms.security;

import com.mms.entity.Permission;
import com.mms.entity.Role;
import com.mms.entity.User;
import com.mms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameAndEnabledTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在或已被禁用: " + username));
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(getAuthorities(user))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.getEnabled())
                .build();
    }
    
    /**
     * 获取用户权限
     * 包括角色权限和具体权限
     * 
     * 注意：为了避免Hibernate PersistentSet的hashCode循环调用问题，
     * 需要将集合转换为普通集合后再遍历
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        
        // 将Hibernate的PersistentSet转换为普通ArrayList，避免hashCode循环调用
        List<Role> roles = new ArrayList<>(user.getRoles());
        
        // 添加角色权限（格式：ROLE_ADMIN, ROLE_USER）
        roles.stream()
                .filter(role -> role.getEnabled() != null && role.getEnabled())
                .forEach(role -> {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode().toUpperCase()));
                    
                    // 将Hibernate的PersistentSet转换为普通ArrayList，避免hashCode循环调用
                    List<Permission> permissions = new ArrayList<>(role.getPermissions());
                    
                    // 添加角色的具体权限（格式：CONTRACT:CREATE, USER:DELETE等）
                    permissions.stream()
                            .filter(permission -> permission.getEnabled() != null && permission.getEnabled())
                            .forEach(permission -> 
                                authorities.add(new SimpleGrantedAuthority(permission.getCode()))
                            );
                });
        
        return authorities;
    }
}

