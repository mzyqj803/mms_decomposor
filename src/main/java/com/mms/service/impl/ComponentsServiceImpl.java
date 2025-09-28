package com.mms.service.impl;

import com.mms.entity.Components;
import com.mms.entity.ComponentsSpec;
import com.mms.repository.ComponentsRepository;
import com.mms.service.CacheService;
import com.mms.service.ComponentsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComponentsServiceImpl implements ComponentsService {
    
    private final ComponentsRepository componentsRepository;
    private final CacheService cacheService;
    
    @Override
    @SuppressWarnings("unchecked")
    public Page<Components> getComponents(String componentCode, String name, String categoryCode, Pageable pageable) {
        String cacheKey = String.format("components:%s:%s:%s:%d:%d", 
            componentCode, name, categoryCode, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Components> cachedResult = cacheService.get(cacheKey, Page.class);
        if (cachedResult != null) {
            return cachedResult;
        }
        
        Page<Components> components = componentsRepository.findByConditions(componentCode, name, categoryCode, pageable);
        
        // 缓存5分钟
        cacheService.set(cacheKey, components, 5, TimeUnit.MINUTES);
        
        return components;
    }
    
    @Override
    public Components getComponentById(Long id) {
        String cacheKey = "component:" + id;
        
        Components cachedComponent = cacheService.get(cacheKey, Components.class);
        if (cachedComponent != null) {
            return cachedComponent;
        }
        
        Components component = componentsRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("零部件不存在"));
        
        // 缓存10分钟
        cacheService.set(cacheKey, component, 10, TimeUnit.MINUTES);
        
        return component;
    }
    
    @Override
    @Transactional
    public Components createComponent(Components component) {
        // 检查零部件代号是否已存在
        if (componentsRepository.findByComponentCode(component.getComponentCode()).isPresent()) {
            throw new RuntimeException("零部件代号已存在");
        }
        
        Components savedComponent = componentsRepository.save(component);
        
        // 清除相关缓存
        clearComponentsCache();
        
        log.info("创建零部件成功: {}", savedComponent.getComponentCode());
        return savedComponent;
    }
    
    @Override
    @Transactional
    public Components updateComponent(Long id, Components component) {
        Components existingComponent = getComponentById(id);
        
        // 检查零部件代号是否被其他记录使用
        if (!existingComponent.getComponentCode().equals(component.getComponentCode())) {
            if (componentsRepository.findByComponentCode(component.getComponentCode()).isPresent()) {
                throw new RuntimeException("零部件代号已存在");
            }
        }
        
        existingComponent.setCategoryCode(component.getCategoryCode());
        existingComponent.setComponentCode(component.getComponentCode());
        existingComponent.setName(component.getName());
        existingComponent.setComment(component.getComment());
        existingComponent.setProcurementFlag(component.getProcurementFlag());
        existingComponent.setCommonPartsFlag(component.getCommonPartsFlag());
        
        Components updatedComponent = componentsRepository.save(existingComponent);
        
        // 清除相关缓存
        clearComponentCache(id);
        clearComponentsCache();
        
        log.info("更新零部件成功: {}", updatedComponent.getComponentCode());
        return updatedComponent;
    }
    
    @Override
    @Transactional
    public void deleteComponent(Long id) {
        Components component = getComponentById(id);
        
        // 检查是否有关联的规格、工艺或关系数据
        if (component.getSpecs() != null && !component.getSpecs().isEmpty()) {
            throw new RuntimeException("该零部件存在规格数据，不能删除");
        }
        if (component.getProcesses() != null && !component.getProcesses().isEmpty()) {
            throw new RuntimeException("该零部件存在工艺数据，不能删除");
        }
        if (component.getChildren() != null && !component.getChildren().isEmpty()) {
            throw new RuntimeException("该零部件存在子组件关系，不能删除");
        }
        if (component.getParents() != null && !component.getParents().isEmpty()) {
            throw new RuntimeException("该零部件存在父组件关系，不能删除");
        }
        
        componentsRepository.deleteById(id);
        
        // 清除相关缓存
        clearComponentCache(id);
        clearComponentsCache();
        
        log.info("删除零部件成功: {}", component.getComponentCode());
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Page<Components> searchComponents(String keyword, Pageable pageable) {
        String cacheKey = String.format("components:search:%s:%d:%d", 
            keyword, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Components> cachedResult = cacheService.get(cacheKey, Page.class);
        if (cachedResult != null) {
            return cachedResult;
        }
        
        Page<Components> components = componentsRepository.findByKeywordContaining(keyword, pageable);
        
        // 缓存3分钟
        cacheService.set(cacheKey, components, 3, TimeUnit.MINUTES);
        
        return components;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<String> getComponentCategories() {
        String cacheKey = "components:categories";
        
        List<String> cachedCategories = cacheService.get(cacheKey, List.class);
        if (cachedCategories != null) {
            return cachedCategories;
        }
        
        List<String> categories = componentsRepository.findDistinctCategoryCodes();
        
        // 缓存30分钟
        cacheService.set(cacheKey, categories, 30, TimeUnit.MINUTES);
        
        return categories;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<ComponentsSpec> getComponentSpecsByCode(String componentCode) {
        String cacheKey = "component:specs:" + componentCode;
        
        List<ComponentsSpec> cachedSpecs = cacheService.get(cacheKey, List.class);
        if (cachedSpecs != null) {
            return cachedSpecs;
        }
        
        // 根据组件编号查找组件
        Components component = componentsRepository.findByComponentCode(componentCode)
            .orElseThrow(() -> new RuntimeException("零部件不存在: " + componentCode));
        
        // 获取组件的规格信息
        List<ComponentsSpec> specs = component.getSpecs();
        
        // 缓存10分钟
        cacheService.set(cacheKey, specs, 10, TimeUnit.MINUTES);
        
        return specs;
    }
    
    private void clearComponentCache(Long componentId) {
        cacheService.delete("component:" + componentId);
    }
    
    private void clearComponentsCache() {
        // 清除所有零部件列表相关的缓存
        try {
            // 清除所有以 "components:" 开头的缓存键
            cacheService.deletePattern("components:*");
            log.info("已清除零部件列表相关缓存");
        } catch (Exception e) {
            log.warn("清除零部件缓存失败: {}", e.getMessage());
        }
    }
}
