package com.mms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * API权限映射配置
 * 从api-permission-mapping.yml加载配置
 * 通过application.yml中的spring.config.import导入
 * 
 * 优先使用 @ConfigurationProperties 自动绑定，如果失败则使用编程方式加载
 * 
 * 注意：YAML结构是 api-permissions: [...]
 * 由于YAML中 api-permissions 本身就是一个列表，prefix设为空，
 * Spring Boot 会尝试将 api-permissions 列表绑定到 apiPermissions 字段
 */
@Configuration
@ConfigurationProperties
@Data
public class ApiPermissionConfig {
    
    /**
     * API权限映射列表
     * Spring Boot 会自动将 api-permissions 列表绑定到此字段
     * 如果自动绑定失败，ApiPermissionConfigLoader 会使用编程方式加载
     */
    private List<ApiPermissionMapping> apiPermissions = new ArrayList<>();
    
    @Data
    public static class ApiPermissionMapping {
        /**
         * API路径（支持路径变量，如 /api/contracts/{id}）
         */
        private String path;
        
        /**
         * HTTP方法列表
         */
        private List<String> methods;
        
        /**
         * 所需权限列表（用户需要拥有其中任意一个权限即可）
         */
        private List<String> permissions;
        
        /**
         * 描述
         */
        private String description;
    }
}


