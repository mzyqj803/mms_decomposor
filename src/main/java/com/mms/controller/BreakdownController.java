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
@RequestMapping("/breakdown")
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
     * 删除箱包的分解结果
     */
    @DeleteMapping("/container/{containerId}")
    public ResponseEntity<Map<String, Object>> deleteContainerBreakdown(@PathVariable Long containerId) {
        try {
            log.info("删除箱包分解结果: containerId={}", containerId);
            Map<String, Object> result = breakdownService.deleteContainerBreakdown(containerId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("删除箱包分解结果失败: containerId={}, error={}", containerId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "删除分解结果失败: " + e.getMessage()));
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
            
            // 获取合同信息以生成正确的文件名
            String contractNo = breakdownService.getContractNoById(contractId);
            String fileName = String.format("%s_breakdown_merge.%s", contractNo, format);
            
            HttpHeaders headers = new HttpHeaders();
            if ("pdf".equals(format)) {
                headers.setContentType(MediaType.APPLICATION_PDF);
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            headers.setContentDispositionFormData("attachment", fileName);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(fileBytes);
        } catch (Exception e) {
            log.error("导出工艺分解表失败: contractId={}, format={}, error={}", 
                contractId, format, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 导出工艺分解表（带文件名）
     */
    @GetMapping("/contract/{contractId}/export/{fileName}")
    public ResponseEntity<byte[]> exportBreakdownWithFileName(
            @PathVariable Long contractId,
            @PathVariable String fileName,
            @RequestParam(defaultValue = "excel") String format) {
        try {
            log.info("导出工艺分解表: contractId={}, fileName={}, format={}", contractId, fileName, format);
            byte[] fileBytes = breakdownService.exportBreakdown(contractId, format);
            
            // URL解码文件名
            String decodedFileName = java.net.URLDecoder.decode(fileName, "UTF-8");
            
            HttpHeaders headers = new HttpHeaders();
            if ("pdf".equals(format)) {
                headers.setContentType(MediaType.APPLICATION_PDF);
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            headers.setContentDispositionFormData("attachment", decodedFileName);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(fileBytes);
        } catch (Exception e) {
            log.error("导出工艺分解表失败: contractId={}, fileName={}, format={}, error={}", 
                contractId, fileName, format, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 合并分解表
     */
    @PostMapping("/merge")
    public ResponseEntity<Map<String, Object>> mergeBreakdownTables(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            java.util.List<Integer> containerIds = (java.util.List<Integer>) request.get("containerIds");
            
            log.info("开始合并分解表: containerIds={}", containerIds);
            Map<String, Object> result = breakdownService.mergeBreakdownTables(containerIds);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("合并分解表失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "合并分解表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 下载合并分解表PDF
     */
    @GetMapping("/merged/{contractId}/download")
    public ResponseEntity<byte[]> downloadMergedBreakdownPdf(@PathVariable Long contractId) {
        try {
            log.info("下载合并分解表PDF: contractId={}", contractId);
            byte[] pdfBytes = breakdownService.generateMergedBreakdownPdf(contractId);
            
            // 获取合同信息以生成正确的文件名
            String contractNo = breakdownService.getContractNoById(contractId);
            String fileName = String.format("%s_合并分解表.pdf", contractNo);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            // 使用URL编码处理中文文件名
            String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8");
            headers.set("Content-Disposition", String.format("attachment; filename=\"%s\"; filename*=UTF-8''%s", fileName, encodedFileName));
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
        } catch (Exception e) {
            log.error("下载合并分解表PDF失败: contractId={}, error={}", contractId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 下载合并分解表PDF（带文件名）
     */
    @GetMapping("/merged/{contractId}/download/{fileName}")
    public ResponseEntity<byte[]> downloadMergedBreakdownPdfWithFileName(
            @PathVariable Long contractId, 
            @PathVariable String fileName) {
        try {
            log.info("下载合并分解表PDF: contractId={}, fileName={}", contractId, fileName);
            byte[] pdfBytes = breakdownService.generateMergedBreakdownPdf(contractId);
            
            // URL解码文件名
            String decodedFileName = java.net.URLDecoder.decode(fileName, "UTF-8");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", decodedFileName);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
        } catch (Exception e) {
            log.error("下载合并分解表PDF失败: contractId={}, fileName={}, error={}", 
                contractId, fileName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
