package com.mms.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * API权限配置加载器
 * 手动加载YAML配置文件
 */
@Component
@Slf4j
public class ApiPermissionConfigLoader {
    
    private final ApiPermissionConfig apiPermissionConfig;
    private final ObjectMapper yamlMapper;
    
    public ApiPermissionConfigLoader(ApiPermissionConfig apiPermissionConfig) {
        this.apiPermissionConfig = apiPermissionConfig;
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
    }
    
    @PostConstruct
    public void loadConfig() {
        try {
            ClassPathResource resource = new ClassPathResource("api-permission-mapping.yml");
            if (!resource.exists()) {
                log.warn("配置文件 api-permission-mapping.yml 不存在");
                return;
            }
            
            try (InputStream inputStream = resource.getInputStream()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> yamlData = yamlMapper.readValue(inputStream, Map.class);
                
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> permissionsList = (List<Map<String, Object>>) yamlData.get("api-permissions");
                
                if (permissionsList != null) {
                    List<ApiPermissionConfig.ApiPermissionMapping> mappings = new ArrayList<>();
                    
                    for (Map<String, Object> item : permissionsList) {
                        ApiPermissionConfig.ApiPermissionMapping mapping = new ApiPermissionConfig.ApiPermissionMapping();
                        mapping.setPath((String) item.get("path"));
                        
                        @SuppressWarnings("unchecked")
                        List<String> methods = (List<String>) item.get("methods");
                        mapping.setMethods(methods);
                        
                        @SuppressWarnings("unchecked")
                        List<String> permissions = (List<String>) item.get("permissions");
                        mapping.setPermissions(permissions);
                        
                        mapping.setDescription((String) item.get("description"));
                        
                        mappings.add(mapping);
                    }
                    
                    // 使用反射设置配置
                    apiPermissionConfig.setApiPermissions(mappings);
                    log.info("成功从 api-permission-mapping.yml 加载 {} 个API权限映射规则", mappings.size());
                } else {
                    log.warn("配置文件中未找到 api-permissions 节点");
                }
            }
        } catch (Exception e) {
            log.error("加载 api-permission-mapping.yml 配置文件失败: {}", e.getMessage(), e);
        }
    }
}

