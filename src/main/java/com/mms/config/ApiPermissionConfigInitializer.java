package com.mms.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * API权限配置初始化器
 * 在系统启动时验证和加载API权限映射配置
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApiPermissionConfigInitializer {
    
    private final ApiPermissionConfig apiPermissionConfig;
    
    @PostConstruct
    public void init() {
        log.info("========== API权限映射配置加载 ==========");
        log.info("ApiPermissionConfig Bean 是否为空: {}", apiPermissionConfig == null ? "是" : "否");
        if (apiPermissionConfig != null) {
            log.info("ApiPermissionConfig 实例: {}", apiPermissionConfig.getClass().getName());
            log.info("apiPermissions 列表是否为 null: {}", apiPermissionConfig.getApiPermissions() == null ? "是" : "否");
        }
        log.info("共加载 {} 个API权限映射规则", apiPermissionConfig != null && apiPermissionConfig.getApiPermissions() != null 
                ? apiPermissionConfig.getApiPermissions().size() : 0);
        
        // 按资源类型分组统计
        long contractApis = apiPermissionConfig.getApiPermissions().stream()
                .filter(m -> m.getPath().contains("/contracts"))
                .count();
        long containerApis = apiPermissionConfig.getApiPermissions().stream()
                .filter(m -> m.getPath().contains("/containers"))
                .count();
        long componentApis = apiPermissionConfig.getApiPermissions().stream()
                .filter(m -> m.getPath().contains("/components"))
                .count();
        long breakdownApis = apiPermissionConfig.getApiPermissions().stream()
                .filter(m -> m.getPath().contains("/breakdown"))
                .count();
        long userApis = apiPermissionConfig.getApiPermissions().stream()
                .filter(m -> m.getPath().contains("/users"))
                .count();
        long roleApis = apiPermissionConfig.getApiPermissions().stream()
                .filter(m -> m.getPath().contains("/roles"))
                .count();
        
        log.info("合同管理API: {} 个", contractApis);
        log.info("装箱单管理API: {} 个", containerApis);
        log.info("零部件管理API: {} 个", componentApis);
        log.info("工艺分解API: {} 个", breakdownApis);
        log.info("用户管理API: {} 个", userApis);
        log.info("角色管理API: {} 个", roleApis);
        
        // 输出前5个配置示例
        log.info("配置示例（前5个）:");
        apiPermissionConfig.getApiPermissions().stream()
                .limit(5)
                .forEach(mapping -> {
                    log.info("  {} {} -> {}", 
                            mapping.getMethods(), 
                            mapping.getPath(), 
                            mapping.getPermissions());
                });
        
        log.info("=========================================");
    }
}


