package com.mms.controller;

import com.mms.dto.FastenerSimilarityResult;
import com.mms.service.FastenerLuceneIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 紧固件相似度搜索控制器
 */
@RestController
@RequestMapping("/fastener-similarity")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class FastenerSimilarityController {

    private final FastenerLuceneIndexService fastenerLuceneIndexService;

    /**
     * 根据查询文本搜索相似紧固件
     * 
     * @param query 查询文本
     * @param limit 返回结果数量限制，默认10
     * @return 相似紧固件列表
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchSimilarFasteners(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            log.info("搜索相似紧固件: query={}, limit={}", query, limit);
            
            List<FastenerSimilarityResult> results = fastenerLuceneIndexService
                    .searchSimilarFasteners(query, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", results);
            response.put("total", results.size());
            response.put("query", query);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("搜索相似紧固件失败: query={}", query, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "搜索失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 根据紧固件ID搜索相似紧固件
     * 
     * @param id 紧固件ID
     * @param limit 返回结果数量限制，默认10
     * @return 相似紧固件列表
     */
    @GetMapping("/search-by-id/{id}")
    public ResponseEntity<Map<String, Object>> searchSimilarFastenersById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            log.info("根据ID搜索相似紧固件: id={}, limit={}", id, limit);
            
            List<FastenerSimilarityResult> results = fastenerLuceneIndexService
                    .searchSimilarFastenersById(id, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", results);
            response.put("total", results.size());
            response.put("sourceId", id);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("根据ID搜索相似紧固件失败: id={}", id, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "搜索失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 重新构建索引
     * 
     * @return 操作结果
     */
    @PostMapping("/rebuild-index")
    public ResponseEntity<Map<String, Object>> rebuildIndex() {
        try {
            log.info("开始重新构建紧固件索引");
            
            fastenerLuceneIndexService.rebuildIndex();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "索引重建完成");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("重新构建索引失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "索引重建失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取索引状态信息
     * 
     * @return 索引状态
     */
    @GetMapping("/index-status")
    public ResponseEntity<Map<String, Object>> getIndexStatus() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "索引服务运行正常");
            response.put("indexDir", "lucene-index");
            response.put("fieldName", "fastener_factors");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取索引状态失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取索引状态失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}

