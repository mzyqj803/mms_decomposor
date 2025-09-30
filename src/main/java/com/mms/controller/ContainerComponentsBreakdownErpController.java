package com.mms.controller;

import com.mms.entity.ContainerComponentsBreakdownErp;
import com.mms.service.ContainerComponentsBreakdownErpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 工艺分解ERP代码管理控制器
 */
@RestController
@RequestMapping("/api/breakdown-erp")
@RequiredArgsConstructor
@Slf4j
public class ContainerComponentsBreakdownErpController {
    
    private final ContainerComponentsBreakdownErpService erpService;
    
    /**
     * 创建ERP代码记录
     */
    @PostMapping
    public ResponseEntity<ContainerComponentsBreakdownErp> create(@RequestBody ContainerComponentsBreakdownErp erpRecord) {
        try {
            ContainerComponentsBreakdownErp created = erpService.create(erpRecord);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            log.error("创建ERP代码记录失败", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 根据ID获取ERP代码记录
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContainerComponentsBreakdownErp> getById(@PathVariable Long id) {
        Optional<ContainerComponentsBreakdownErp> erpRecord = erpService.findById(id);
        return erpRecord.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 更新ERP代码记录
     */
    @PutMapping("/{id}")
    public ResponseEntity<ContainerComponentsBreakdownErp> update(@PathVariable Long id, @RequestBody ContainerComponentsBreakdownErp erpRecord) {
        try {
            erpRecord.setId(id);
            ContainerComponentsBreakdownErp updated = erpService.update(erpRecord);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("更新ERP代码记录失败: id={}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 删除ERP代码记录
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            erpService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("删除ERP代码记录失败: id={}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 根据工艺分解ID获取ERP代码记录列表
     */
    @GetMapping("/breakdown/{breakdownId}")
    public ResponseEntity<List<ContainerComponentsBreakdownErp>> getByBreakdownId(@PathVariable Long breakdownId) {
        List<ContainerComponentsBreakdownErp> records = erpService.findByBreakdownId(breakdownId);
        return ResponseEntity.ok(records);
    }
    
    /**
     * 根据ERP代码获取记录列表
     */
    @GetMapping("/erp-code/{erpCode}")
    public ResponseEntity<List<ContainerComponentsBreakdownErp>> getByErpCode(@PathVariable String erpCode) {
        List<ContainerComponentsBreakdownErp> records = erpService.findByErpCode(erpCode);
        return ResponseEntity.ok(records);
    }
    
    /**
     * 分页查询ERP代码记录（支持多条件搜索）
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ContainerComponentsBreakdownErp>> search(
            @RequestParam(required = false) Long breakdownId,
            @RequestParam(required = false) String erpCode,
            @RequestParam(required = false) String comments,
            Pageable pageable) {
        Page<ContainerComponentsBreakdownErp> page = erpService.findByConditions(breakdownId, erpCode, comments, pageable);
        return ResponseEntity.ok(page);
    }
    
    /**
     * 根据合同ID获取所有相关的ERP代码记录
     */
    @GetMapping("/contract/{contractId}")
    public ResponseEntity<List<ContainerComponentsBreakdownErp>> getByContractId(@PathVariable Long contractId) {
        List<ContainerComponentsBreakdownErp> records = erpService.findByContractId(contractId);
        return ResponseEntity.ok(records);
    }
    
    /**
     * 根据装箱单ID获取所有相关的ERP代码记录
     */
    @GetMapping("/container/{containerId}")
    public ResponseEntity<List<ContainerComponentsBreakdownErp>> getByContainerId(@PathVariable Long containerId) {
        List<ContainerComponentsBreakdownErp> records = erpService.findByContainerId(containerId);
        return ResponseEntity.ok(records);
    }
    
    /**
     * 批量创建ERP代码记录
     */
    @PostMapping("/batch")
    public ResponseEntity<List<ContainerComponentsBreakdownErp>> batchCreate(@RequestBody List<ContainerComponentsBreakdownErp> erpRecords) {
        try {
            List<ContainerComponentsBreakdownErp> created = erpService.batchCreate(erpRecords);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            log.error("批量创建ERP代码记录失败", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 批量更新ERP代码记录
     */
    @PutMapping("/batch")
    public ResponseEntity<List<ContainerComponentsBreakdownErp>> batchUpdate(@RequestBody List<ContainerComponentsBreakdownErp> erpRecords) {
        try {
            List<ContainerComponentsBreakdownErp> updated = erpService.batchUpdate(erpRecords);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("批量更新ERP代码记录失败", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 批量删除指定工艺分解ID的所有ERP代码记录
     */
    @DeleteMapping("/breakdown/{breakdownId}")
    public ResponseEntity<Void> batchDeleteByBreakdownId(@PathVariable Long breakdownId) {
        try {
            erpService.batchDeleteByBreakdownId(breakdownId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("批量删除工艺分解ERP代码记录失败: breakdownId={}", breakdownId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 统计指定工艺分解ID的ERP代码记录数量
     */
    @GetMapping("/breakdown/{breakdownId}/count")
    public ResponseEntity<Map<String, Long>> countByBreakdownId(@PathVariable Long breakdownId) {
        long count = erpService.countByBreakdownId(breakdownId);
        return ResponseEntity.ok(Map.of("count", count));
    }
    
    /**
     * 为工艺分解结果自动生成ERP代码记录
     */
    @PostMapping("/generate/breakdown/{breakdownId}")
    public ResponseEntity<List<ContainerComponentsBreakdownErp>> generateErpCodesForBreakdown(@PathVariable Long breakdownId) {
        try {
            List<ContainerComponentsBreakdownErp> generated = erpService.generateErpCodesForBreakdown(breakdownId);
            return ResponseEntity.ok(generated);
        } catch (Exception e) {
            log.error("为工艺分解生成ERP代码记录失败: breakdownId={}", breakdownId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 为合同的所有工艺分解结果生成ERP代码记录
     */
    @PostMapping("/generate/contract/{contractId}")
    public ResponseEntity<List<ContainerComponentsBreakdownErp>> generateErpCodesForContract(@PathVariable Long contractId) {
        try {
            List<ContainerComponentsBreakdownErp> generated = erpService.generateErpCodesForContract(contractId);
            return ResponseEntity.ok(generated);
        } catch (Exception e) {
            log.error("为合同生成ERP代码记录失败: contractId={}", contractId, e);
            return ResponseEntity.badRequest().build();
        }
    }
}
