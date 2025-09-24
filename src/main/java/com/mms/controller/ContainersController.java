package com.mms.controller;

import com.mms.entity.Containers;
import com.mms.service.ContainersService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Page<Containers>> getContainers(
            @RequestParam(required = false) String containerNo,
            @RequestParam(required = false) String contractNo,
            @RequestParam(required = false) String projectName,
            Pageable pageable) {
        
        Page<Containers> containers = containersService.getContainers(containerNo, contractNo, projectName, pageable);
        return ResponseEntity.ok(containers);
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
