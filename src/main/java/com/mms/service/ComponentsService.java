package com.mms.service;

import com.mms.entity.Components;
import com.mms.entity.ComponentsSpec;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ComponentsService {
    
    /**
     * 获取零部件列表（支持分页和搜索）
     */
    Page<Components> getComponents(String componentCode, String name, String categoryCode, Pageable pageable);
    
    /**
     * 根据ID获取零部件
     */
    Components getComponentById(Long id);
    
    /**
     * 创建零部件
     */
    Components createComponent(Components component);
    
    /**
     * 更新零部件
     */
    Components updateComponent(Long id, Components component);
    
    /**
     * 删除零部件
     */
    void deleteComponent(Long id);
    
    /**
     * 搜索零部件
     */
    Page<Components> searchComponents(String keyword, Pageable pageable);
    
    /**
     * 获取零部件分类列表
     */
    java.util.List<String> getComponentCategories();
    
    /**
     * 根据组件编号获取组件规格
     */
    List<ComponentsSpec> getComponentSpecsByCode(String componentCode);
}
