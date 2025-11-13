package com.mms.service.impl;

import com.mms.entity.User;
import com.mms.repository.UserRepository;
import com.mms.security.UserDetailsServiceImpl;
import com.mms.service.AuthService;
import com.mms.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthenticationManager authenticationManager;
    
    @Override
    public Map<String, Object> login(String username, String password) {
        try {
            // 验证用户名和密码
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            
            // 加载用户详情（内部已有@Transactional(readOnly = true)）
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            // 生成JWT token
            String token = jwtUtil.generateToken(userDetails);
            
            // 获取用户实体（使用只读事务）
            User user = getUserByUsername(username);
            
            // 提取用户权限（排除ROLE_前缀的角色权限，只保留具体权限）
            List<String> permissions = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(authority -> !authority.startsWith("ROLE_")) // 排除角色权限
                    .collect(Collectors.toList());
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("token", token);
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("name", user.getName());
            userInfo.put("email", user.getEmail());
            userInfo.put("avatar", user.getAvatar() != null ? user.getAvatar() : "");
            userInfo.put("permissions", permissions);
            
            response.put("user", userInfo);
            
            return response;
        } catch (BadCredentialsException e) {
            log.error("登录失败: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "用户名或密码错误");
            return response;
        } catch (Exception e) {
            log.error("登录异常: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "登录失败: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * 获取用户（使用只读事务）
     */
    @Transactional(readOnly = true)
    private User getUserByUsername(String username) {
        return userRepository.findByUsernameAndEnabledTrue(username)
                .orElseThrow(() -> new BadCredentialsException("用户不存在或已被禁用"));
    }
    
    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            return userRepository.findByUsernameAndEnabledTrue(username)
                    .orElse(null);
        }
        return null;
    }
    
    @Override
    public void logout() {
        SecurityContextHolder.clearContext();
    }
}

