package com.mms.controller;

import com.mms.service.ContainerUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/containers")
@RequiredArgsConstructor
public class ContainerPreviewController {
    
    private final ContainerUploadService containerUploadService;
    
    /**
     * 预览Excel文件内容（不保存到数据库）
     */
    @PostMapping("/preview")
    public ResponseEntity<Map<String, Object>> previewExcelFile(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件
            if (file.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "请选择要预览的文件");
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
            
            Map<String, Object> previewData = containerUploadService.previewExcelFile(file);
            return ResponseEntity.ok(previewData);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "预览失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
