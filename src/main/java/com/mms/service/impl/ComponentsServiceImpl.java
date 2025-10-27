package com.mms.service.impl;

import com.mms.dto.ComponentDetailDTO;
import com.mms.entity.Components;
import com.mms.entity.ComponentsSpec;
import com.mms.entity.ComponentsRelationship;
import com.mms.repository.ComponentsRepository;
import com.mms.repository.ComponentsSpecRepository;
import com.mms.repository.ComponentsRelationshipRepository;
import com.mms.service.CacheService;
import com.mms.service.ComponentsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComponentsServiceImpl implements ComponentsService {
    
    private final ComponentsRepository componentsRepository;
    private final ComponentsSpecRepository componentsSpecRepository;
    private final ComponentsRelationshipRepository componentsRelationshipRepository;
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
    @Transactional(readOnly = true)
    public ComponentDetailDTO getComponentDetail(Long id) {
        String cacheKey = "component:detail:" + id;
        
        ComponentDetailDTO cachedDetail = cacheService.get(cacheKey, ComponentDetailDTO.class);
        if (cachedDetail != null) {
            return cachedDetail;
        }
        
        // 获取零部件基本信息
        Components component = componentsRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("零部件不存在"));
        
        ComponentDetailDTO detailDTO = new ComponentDetailDTO();
        detailDTO.setId(component.getId());
        detailDTO.setCategoryCode(component.getCategoryCode());
        detailDTO.setComponentCode(component.getComponentCode());
        detailDTO.setName(component.getName());
        detailDTO.setComment(component.getComment());
        detailDTO.setProcurementFlag(component.getProcurementFlag());
        detailDTO.setCommonPartsFlag(component.getCommonPartsFlag());
        detailDTO.setStatus(component.getStatus());
        detailDTO.setEntryTs(component.getEntryTs());
        
        // 获取规格信息
        try {
            List<ComponentsSpec> specs = component.getSpecs();
            if (specs != null && !specs.isEmpty()) {
                detailDTO.setSpecs(new ArrayList<>(specs));
            }
        } catch (Exception e) {
            log.warn("获取零部件规格信息失败: {}", e.getMessage());
        }
        
        // 获取工艺信息
        try {
            if (component.getProcesses() != null && !component.getProcesses().isEmpty()) {
                detailDTO.setProcesses(new ArrayList<>(component.getProcesses()));
            }
        } catch (Exception e) {
            log.warn("获取零部件工艺信息失败: {}", e.getMessage());
        }
        
        // 获取子组件关系
        try {
            List<ComponentsRelationship> childRelationships = componentsRelationshipRepository.findByParentId(id);
            if (childRelationships != null && !childRelationships.isEmpty()) {
                List<ComponentDetailDTO.RelationshipDTO> children = new ArrayList<>();
                for (ComponentsRelationship rel : childRelationships) {
                    ComponentDetailDTO.RelationshipDTO relationshipDTO = new ComponentDetailDTO.RelationshipDTO();
                    relationshipDTO.setId(rel.getId());
                    relationshipDTO.setQuantity(rel.getQuantity());
                    
                    // 设置子组件信息
                    if (rel.getChild() != null) {
                        ComponentDetailDTO.RelationshipDTO.ComponentInfo childInfo = 
                            new ComponentDetailDTO.RelationshipDTO.ComponentInfo();
                        childInfo.setId(rel.getChild().getId());
                        childInfo.setComponentCode(rel.getChild().getComponentCode());
                        childInfo.setName(rel.getChild().getName());
                        childInfo.setCategoryCode(rel.getChild().getCategoryCode());
                        relationshipDTO.setChild(childInfo);
                    }
                    
                    children.add(relationshipDTO);
                }
                detailDTO.setChildren(children);
            }
        } catch (Exception e) {
            log.warn("获取子组件关系失败: {}", e.getMessage());
        }
        
        // 获取父组件关系
        try {
            List<ComponentsRelationship> parentRelationships = componentsRelationshipRepository.findByChildId(id);
            if (parentRelationships != null && !parentRelationships.isEmpty()) {
                List<ComponentDetailDTO.RelationshipDTO> parents = new ArrayList<>();
                for (ComponentsRelationship rel : parentRelationships) {
                    ComponentDetailDTO.RelationshipDTO relationshipDTO = new ComponentDetailDTO.RelationshipDTO();
                    relationshipDTO.setId(rel.getId());
                    relationshipDTO.setQuantity(rel.getQuantity());
                    
                    // 设置父组件信息
                    if (rel.getParent() != null) {
                        ComponentDetailDTO.RelationshipDTO.ComponentInfo parentInfo = 
                            new ComponentDetailDTO.RelationshipDTO.ComponentInfo();
                        parentInfo.setId(rel.getParent().getId());
                        parentInfo.setComponentCode(rel.getParent().getComponentCode());
                        parentInfo.setName(rel.getParent().getName());
                        parentInfo.setCategoryCode(rel.getParent().getCategoryCode());
                        relationshipDTO.setParent(parentInfo);
                    }
                    
                    parents.add(relationshipDTO);
                }
                detailDTO.setParents(parents);
            }
        } catch (Exception e) {
            log.warn("获取父组件关系失败: {}", e.getMessage());
        }
        
        // 缓存10分钟
        cacheService.set(cacheKey, detailDTO, 10, TimeUnit.MINUTES);
        
        return detailDTO;
    }
    
    @Override
    @Transactional
    public Components createComponent(Components component) {
        // 检查零部件代号是否已存在（只检查 active 的组件，允许重用已删除组件的代号）
        if (componentsRepository.findActiveByComponentCode(component.getComponentCode()).isPresent()) {
            throw new RuntimeException("零部件代号已存在");
        }
        
        // 设置新组件为 active 状态
        if (component.getStatus() == null) {
            component.setStatus(1);
        }
        
        // 保存零部件基本信息
        Components savedComponent = componentsRepository.save(component);
        
        // 处理规格数据
        if (component.getSpecs() != null && !component.getSpecs().isEmpty()) {
            for (ComponentsSpec spec : component.getSpecs()) {
                spec.setComponent(savedComponent);
                componentsSpecRepository.save(spec);
            }
        }
        
        // 处理父工件关系
        if (component.getParentComponentId() != null && !component.getParentComponentId().trim().isEmpty()) {
            // 查找父工件（只查找 active 的组件）
            Components parentComponent = componentsRepository.findActiveByComponentCode(component.getParentComponentId())
                .orElseThrow(() -> new RuntimeException("父工件不存在或已被删除: " + component.getParentComponentId()));
            
            // 创建父子关系
            ComponentsRelationship relationship = new ComponentsRelationship();
            relationship.setParent(parentComponent);
            relationship.setChild(savedComponent);
            relationship.setQuantity(1); // 默认数量为1
            
            componentsRelationshipRepository.save(relationship);
            
            log.info("创建父子关系成功: 父工件={}, 子工件={}", 
                    parentComponent.getComponentCode(), savedComponent.getComponentCode());
        }
        
        // 清除相关缓存
        clearComponentsCache();
        
        log.info("创建零部件成功: {}, 规格数量: {}, 父工件: {}", 
                savedComponent.getComponentCode(), 
                component.getSpecs() != null ? component.getSpecs().size() : 0,
                component.getParentComponentId());
        return savedComponent;
    }
    
    @Override
    @Transactional
    public Components updateComponent(Long id, Components component) {
        Components existingComponent = getComponentById(id);
        
        // 检查零部件代号是否被其他记录使用（只检查 active 的组件）
        if (!existingComponent.getComponentCode().equals(component.getComponentCode())) {
            if (componentsRepository.findActiveByComponentCode(component.getComponentCode()).isPresent()) {
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
        
        // 处理规格数据更新
        if (component.getSpecs() != null) {
            // 删除现有规格
            componentsSpecRepository.deleteByComponentId(id);
            
            // 添加新规格
            for (ComponentsSpec spec : component.getSpecs()) {
                spec.setComponent(updatedComponent);
                componentsSpecRepository.save(spec);
            }
        }
        
        // 清除相关缓存
        clearComponentCache(id);
        clearComponentsCache();
        
        log.info("更新零部件成功: {}, 规格数量: {}", updatedComponent.getComponentCode(), 
                component.getSpecs() != null ? component.getSpecs().size() : 0);
        return updatedComponent;
    }
    
    @Override
    @Transactional
    public void deleteComponent(Long id) {
        Components component = componentsRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("零部件不存在"));
        
        // 软删除：将 status 设置为 0 (deleted)
        component.setStatus(0);
        componentsRepository.save(component);
        
        // 清除相关缓存
        clearComponentCache(id);
        clearComponentsCache();
        
        log.info("软删除零部件成功: {}, status设置为0", component.getComponentCode());
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
        
        // 根据组件编号查找组件（只查找 active 的组件）
        Components component = componentsRepository.findActiveByComponentCode(componentCode)
            .orElseThrow(() -> new RuntimeException("零部件不存在或已被删除: " + componentCode));
        
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
