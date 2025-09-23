package com.mms.controller;

import com.mms.entity.Components;
import com.mms.service.ComponentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/components")
@RequiredArgsConstructor
public class ComponentsController {
    
    private final ComponentsService componentsService;
    
    /**
     * 获取零部件列表（支持分页和搜索）
     */
    @GetMapping
    public ResponseEntity<Page<Components>> getComponents(
            @RequestParam(required = false) String componentCode,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String categoryCode,
            Pageable pageable) {
        
        Page<Components> components = componentsService.getComponents(componentCode, name, categoryCode, pageable);
        return ResponseEntity.ok(components);
    }
    
    /**
     * 获取零部件详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Components> getComponent(@PathVariable Long id) {
        Components component = componentsService.getComponentById(id);
        return ResponseEntity.ok(component);
    }
    
    /**
     * 创建零部件
     */
    @PostMapping
    public ResponseEntity<Components> createComponent(@Valid @RequestBody Components component) {
        Components createdComponent = componentsService.createComponent(component);
        return ResponseEntity.ok(createdComponent);
    }
    
    /**
     * 更新零部件
     */
    @PutMapping("/{id}")
    public ResponseEntity<Components> updateComponent(@PathVariable Long id, @Valid @RequestBody Components component) {
        Components updatedComponent = componentsService.updateComponent(id, component);
        return ResponseEntity.ok(updatedComponent);
    }
    
    /**
     * 删除零部件
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComponent(@PathVariable Long id) {
        componentsService.deleteComponent(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 搜索零部件
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Components>> searchComponents(
            @RequestParam String keyword,
            Pageable pageable) {
        
        Page<Components> components = componentsService.searchComponents(keyword, pageable);
        return ResponseEntity.ok(components);
    }
    
    /**
     * 获取零部件分类列表
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getComponentCategories() {
        List<String> categories = componentsService.getComponentCategories();
        return ResponseEntity.ok(categories);
    }
}
