package com.mms.service;

import com.mms.entity.User;

import java.util.Map;

public interface AuthService {
    
    /**
     * 用户登录
     */
    Map<String, Object> login(String username, String password);
    
    /**
     * 获取当前登录用户信息
     */
    User getCurrentUser();
    
    /**
     * 用户登出
     */
    void logout();
}

