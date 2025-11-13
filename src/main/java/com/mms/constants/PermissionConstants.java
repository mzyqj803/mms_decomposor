package com.mms.constants;

/**
 * 权限常量定义
 * 定义系统中所有的权限代码和名称
 */
public class PermissionConstants {
    
    // 合同相关权限
    public static final String CONTRACT_CREATE = "CONTRACT:CREATE";
    public static final String CONTRACT_UPDATE = "CONTRACT:UPDATE";
    public static final String CONTRACT_DELETE = "CONTRACT:DELETE";
    public static final String CONTRACT_VIEW = "CONTRACT:VIEW";
    
    // 装箱单相关权限
    public static final String CONTAINER_CREATE = "CONTAINER:CREATE";
    public static final String CONTAINER_UPDATE = "CONTAINER:UPDATE";
    public static final String CONTAINER_DELETE = "CONTAINER:DELETE";
    public static final String CONTAINER_VIEW = "CONTAINER:VIEW";
    
    // 工艺分解相关权限
    public static final String BREAKDOWN_EXECUTE = "BREAKDOWN:EXECUTE";
    public static final String BREAKDOWN_VIEW = "BREAKDOWN:VIEW";
    
    // 零部件相关权限
    public static final String COMPONENT_CREATE = "COMPONENT:CREATE";
    public static final String COMPONENT_UPDATE = "COMPONENT:UPDATE";
    public static final String COMPONENT_DELETE = "COMPONENT:DELETE";
    public static final String COMPONENT_VIEW = "COMPONENT:VIEW";
    
    // 用户相关权限
    public static final String USER_CREATE = "USER:CREATE";
    public static final String USER_UPDATE = "USER:UPDATE";
    public static final String USER_DELETE = "USER:DELETE";
    public static final String USER_VIEW = "USER:VIEW";
    
    // 角色相关权限
    public static final String ROLE_CREATE = "ROLE:CREATE";
    public static final String ROLE_UPDATE = "ROLE:UPDATE";
    public static final String ROLE_DELETE = "ROLE:DELETE";
    public static final String ROLE_VIEW = "ROLE:VIEW";
    
    // 权限名称映射
    public static String getPermissionName(String code) {
        return switch (code) {
            case CONTRACT_CREATE -> "创建合同";
            case CONTRACT_UPDATE -> "编辑合同";
            case CONTRACT_DELETE -> "删除合同";
            case CONTRACT_VIEW -> "查看合同";
            case CONTAINER_CREATE -> "创建装箱单";
            case CONTAINER_UPDATE -> "编辑装箱单";
            case CONTAINER_DELETE -> "删除装箱单";
            case CONTAINER_VIEW -> "查看装箱单";
            case BREAKDOWN_EXECUTE -> "执行工艺分解";
            case BREAKDOWN_VIEW -> "查看工艺分解";
            case COMPONENT_CREATE -> "创建零部件";
            case COMPONENT_UPDATE -> "编辑零部件";
            case COMPONENT_DELETE -> "删除零部件";
            case COMPONENT_VIEW -> "查看零部件";
            case USER_CREATE -> "创建用户";
            case USER_UPDATE -> "编辑用户";
            case USER_DELETE -> "删除用户";
            case USER_VIEW -> "查看用户";
            case ROLE_CREATE -> "创建角色";
            case ROLE_UPDATE -> "编辑角色";
            case ROLE_DELETE -> "删除角色";
            case ROLE_VIEW -> "查看角色";
            default -> "未知权限";
        };
    }
}


