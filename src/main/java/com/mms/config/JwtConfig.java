package com.mms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtConfig {
    
    /**
     * JWT密钥
     */
    private String secret = "mms-decomposor-secret-key-change-in-production-environment-2024";
    
    /**
     * Token过期时间（秒），默认1小时
     */
    private Long expiration = 3600L;
    
    /**
     * Token前缀
     */
    private String tokenPrefix = "Bearer ";
    
    /**
     * Token请求头名称
     */
    private String headerName = "Authorization";
}

