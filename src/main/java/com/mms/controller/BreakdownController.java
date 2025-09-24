package com.mms.controller;

import com.mms.service.BreakdownService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/breakdown")
@RequiredArgsConstructor
@Slf4j
public class BreakdownController {
    
    private final BreakdownService breakdownService;
    
    /**
     * 对单个箱包进行工艺分解
     */
    @PostMapping("/container/{containerId}")
    public ResponseEntity<Map<String, Object>> breakdownContainer(@PathVariable Long containerId) {
        try {
            log.info("开始对箱包进行工艺分解: containerId={}", containerId);
            Map<String, Object> result = breakdownService.breakdownContainer(containerId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("箱包工艺分解失败: containerId={}, error={}", containerId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "工艺分解失败: " + e.getMessage()));
        }
    }
    
    /**
     * 对合同的所有箱包进行工艺分解
     */
    @PostMapping("/contract/{contractId}")
    public ResponseEntity<Map<String, Object>> breakdownContract(@PathVariable Long contractId) {
        try {
            log.info("开始对合同进行工艺分解: contractId={}", contractId);
            Map<String, Object> result = breakdownService.breakdownContract(contractId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("合同工艺分解失败: contractId={}, error={}", contractId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "工艺分解失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取箱包的工艺分解结果
     */
    @GetMapping("/container/{containerId}")
    public ResponseEntity<Map<String, Object>> getContainerBreakdown(@PathVariable Long containerId) {
        try {
            Map<String, Object> result = breakdownService.getContainerBreakdown(containerId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取箱包分解结果失败: containerId={}, error={}", containerId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "获取分解结果失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取合同的工艺分解汇总结果
     */
    @GetMapping("/contract/{contractId}/summary")
    public ResponseEntity<Map<String, Object>> getContractBreakdownSummary(@PathVariable Long contractId) {
        try {
            Map<String, Object> result = breakdownService.getContractBreakdownSummary(contractId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取合同分解汇总失败: contractId={}, error={}", contractId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "获取分解汇总失败: " + e.getMessage()));
        }
    }
    
    /**
     * 导出工艺分解表
     */
    @GetMapping("/contract/{contractId}/export")
    public ResponseEntity<byte[]> exportBreakdown(
            @PathVariable Long contractId,
            @RequestParam(defaultValue = "excel") String format) {
        try {
            log.info("导出工艺分解表: contractId={}, format={}", contractId, format);
            byte[] fileBytes = breakdownService.exportBreakdown(contractId, format);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                String.format("breakdown_%d.%s", contractId, format));
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(fileBytes);
        } catch (Exception e) {
            log.error("导出工艺分解表失败: contractId={}, format={}, error={}", 
                contractId, format, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
