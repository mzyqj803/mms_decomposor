package com.mms.security;

import com.mms.config.ApiPermissionConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * API权限拦截过滤器
 * 根据api-permission-mapping.yml配置进行权限检查
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class ApiPermissionFilter extends OncePerRequestFilter {
    
    private final ApiPermissionConfig apiPermissionConfig;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    @Override
    protected void doFilterInternal(@jakarta.annotation.Nonnull HttpServletRequest request,
                                    @jakarta.annotation.Nonnull HttpServletResponse response,
                                    @jakarta.annotation.Nonnull FilterChain filterChain) throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        
        // 获取当前用户认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // 如果未认证或匿名用户，跳过权限检查（由JWT过滤器处理）
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 查找匹配的权限配置（支持带context-path和不带context-path的路径）
        ApiPermissionConfig.ApiPermissionMapping matchedMapping = findMatchingMapping(requestPath, method);
        
        if (matchedMapping != null) {
            // 需要权限检查
            List<String> requiredPermissions = matchedMapping.getPermissions();
            
            if (requiredPermissions != null && !requiredPermissions.isEmpty()) {
                // 获取用户权限
                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
                Set<String> userPermissions = authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());
                
                // 检查用户是否拥有所需权限
                boolean hasPermission = requiredPermissions.stream()
                        .anyMatch(userPermissions::contains);
                
                if (!hasPermission) {
                    log.warn("用户 {} 尝试访问 {} {} 但缺少权限: {} (当前权限: {})", 
                            authentication.getName(), method, requestPath, requiredPermissions, userPermissions);
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":false,\"message\":\"权限不足，无法访问该资源\",\"required\":\"" + 
                            String.join(",", requiredPermissions) + "\"}");
                    return;
                }
                
                log.debug("用户 {} 访问 {} {} 权限验证通过", 
                        authentication.getName(), method, requestPath);
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * 查找匹配的权限映射配置
     * 支持Ant路径模式匹配，如 /api/contracts/{id}
     */
    private ApiPermissionConfig.ApiPermissionMapping findMatchingMapping(String requestPath, String method) {
        // 规范化请求路径（移除context-path前缀，如果存在）
        String normalizedPath = requestPath;
        if (normalizedPath.startsWith("/api/")) {
            normalizedPath = normalizedPath.substring(4); // 移除 /api
        }
        if (!normalizedPath.startsWith("/")) {
            normalizedPath = "/" + normalizedPath;
        }
        
        for (ApiPermissionConfig.ApiPermissionMapping mapping : apiPermissionConfig.getApiPermissions()) {
            String configPath = mapping.getPath();
            
            // 移除配置路径中的 /api 前缀（如果存在）
            String normalizedConfigPath = configPath;
            if (normalizedConfigPath.startsWith("/api/")) {
                normalizedConfigPath = normalizedConfigPath.substring(4);
            }
            if (!normalizedConfigPath.startsWith("/")) {
                normalizedConfigPath = "/" + normalizedConfigPath;
            }
            
            // 将配置中的 {id} 等路径变量转换为 Ant 模式 **
            String antPattern = normalizedConfigPath.replaceAll("\\{[^}]+\\}", "**");
            
            // 匹配原始路径或规范化后的路径
            boolean pathMatches = pathMatcher.match(antPattern, requestPath) || 
                                 pathMatcher.match(antPattern, normalizedPath) ||
                                 pathMatcher.match(configPath, requestPath) ||
                                 pathMatcher.match(normalizedConfigPath, normalizedPath);
            
            if (pathMatches) {
                // 检查HTTP方法是否匹配
                if (mapping.getMethods() == null || mapping.getMethods().isEmpty() || 
                    mapping.getMethods().contains(method)) {
                    return mapping;
                }
            }
        }
        return null;
    }
}

