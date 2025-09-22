package com.mms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        // TODO: 实现实际的登录逻辑
        // 1. 验证用户名和密码
        // 2. 生成JWT token
        // 3. 返回用户信息和token
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "登录成功");
        response.put("token", "mock-jwt-token");
        
        Map<String, Object> user = new HashMap<>();
        user.put("id", 1L);
        user.put("username", request.getUsername());
        user.put("name", "管理员");
        user.put("avatar", "");
        
        response.put("user", user);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "登出成功");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        Map<String, Object> user = new HashMap<>();
        user.put("id", 1L);
        user.put("username", "admin");
        user.put("name", "管理员");
        user.put("avatar", "");
        
        return ResponseEntity.ok(user);
    }
    
    public static class LoginRequest {
        private String username;
        private String password;
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
    }
}
