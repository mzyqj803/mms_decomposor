package com.mms.controller;

import com.mms.entity.Containers;
import com.mms.service.ContainerUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/contracts/{contractId}/containers")
@RequiredArgsConstructor
public class ContainerUploadController {
    
    private final ContainerUploadService containerUploadService;
    
    /**
     * 上传装箱单Excel文件
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadContainerFile(
            @PathVariable Long contractId,
            @RequestParam("file") MultipartFile file) {
        
        try {
            // 验证文件
            if (file.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "请选择要上传的文件");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 验证文件类型
            String fileName = file.getOriginalFilename();
            if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "请上传Excel文件(.xlsx或.xls格式)");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 解析并创建装箱单
            List<Containers> containers = containerUploadService.parseAndCreateContainers(contractId, file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "装箱单上传成功");
            response.put("data", containers);
            response.put("count", containers.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "上传失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 查找类似的装箱单
     */
    @GetMapping("/similar")
    public ResponseEntity<Map<String, Object>> findSimilarContainers(@PathVariable Long contractId) {
        try {
            List<Containers> containers = containerUploadService.findSimilarContainers(contractId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", containers);
            response.put("count", containers.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查找失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 克隆装箱单
     */
    @PostMapping("/clone")
    public ResponseEntity<Map<String, Object>> cloneContainers(
            @PathVariable Long contractId,
            @RequestParam Long sourceContractId) {
        
        try {
            List<Containers> containers = containerUploadService.cloneContainers(sourceContractId, contractId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "装箱单克隆成功");
            response.put("data", containers);
            response.put("count", containers.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "克隆失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
