package com.mms.controller;

import com.mms.entity.Containers;
import com.mms.dto.ContainerDTO;
import com.mms.service.ContainersService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/containers")
@RequiredArgsConstructor
public class ContainersController {
    
    private final ContainersService containersService;
    
    /**
     * 获取装箱单列表（支持搜索和分页）
     */
    @GetMapping
    public ResponseEntity<Page<ContainerDTO>> getContainers(
            @RequestParam(required = false) String containerNo,
            @RequestParam(required = false) String contractNo,
            @RequestParam(required = false) String projectName,
            Pageable pageable) {
        
        Page<Containers> containers = containersService.getContainers(containerNo, contractNo, projectName, pageable);
        
        // 转换为DTO
        Page<ContainerDTO> containerDTOs = containers.map(ContainerDTO::fromEntity);
        
        return ResponseEntity.ok(containerDTOs);
    }
    
    /**
     * 获取装箱单详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Containers> getContainer(@PathVariable Long id) {
        Containers container = containersService.getContainerById(id);
        return ResponseEntity.ok(container);
    }
    
    /**
     * 获取装箱单内的组件列表
     */
    @GetMapping("/{id}/components")
    public ResponseEntity<List<Map<String, Object>>> getContainerComponents(@PathVariable Long id) {
        try {
            List<Map<String, Object>> components = containersService.getContainerComponents(id);
            return ResponseEntity.ok(components);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 更新装箱单内的组件
     */
    @PutMapping("/{containerId}/components/{componentId}")
    public ResponseEntity<Map<String, Object>> updateContainerComponent(
            @PathVariable Long containerId,
            @PathVariable Long componentId,
            @RequestBody Map<String, Object> componentData) {
        try {
            containersService.updateContainerComponent(containerId, componentId, componentData);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "组件更新成功"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "更新失败: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 删除装箱单内的组件
     */
    @DeleteMapping("/{containerId}/components/{componentId}")
    public ResponseEntity<Map<String, Object>> deleteContainerComponent(
            @PathVariable Long containerId,
            @PathVariable Long componentId) {
        try {
            containersService.deleteContainerComponent(containerId, componentId);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "组件删除成功"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "删除失败: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 删除装箱单
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteContainer(@PathVariable Long id) {
        try {
            containersService.deleteContainer(id);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "装箱单删除成功"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "删除失败: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
}
