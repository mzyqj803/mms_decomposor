package com.mms.controller;

import com.mms.entity.Contracts;
import com.mms.entity.Containers;
import com.mms.service.ContractsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/contracts")
@RequiredArgsConstructor
public class ContractsController {
    
    private final ContractsService contractsService;
    
    /**
     * 获取合同列表
     */
    @GetMapping
    public ResponseEntity<Page<Contracts>> getContracts(
            @RequestParam(required = false) String contractNo,
            @RequestParam(required = false) String projectName,
            @RequestParam(required = false) Contracts.ContractStatus status,
            Pageable pageable) {
        
        Page<Contracts> contracts = contractsService.getContracts(contractNo, projectName, status, pageable);
        return ResponseEntity.ok(contracts);
    }
    
    /**
     * 获取合同详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Contracts> getContract(@PathVariable Long id) {
        Contracts contract = contractsService.getContractById(id);
        return ResponseEntity.ok(contract);
    }
    
    /**
     * 创建合同
     */
    @PostMapping
    public ResponseEntity<Contracts> createContract(@Valid @RequestBody Contracts contract) {
        Contracts createdContract = contractsService.createContract(contract);
        return ResponseEntity.ok(createdContract);
    }
    
    /**
     * 更新合同
     */
    @PutMapping("/{id}")
    public ResponseEntity<Contracts> updateContract(@PathVariable Long id, @Valid @RequestBody Contracts contract) {
        Contracts updatedContract = contractsService.updateContract(id, contract);
        return ResponseEntity.ok(updatedContract);
    }
    
    /**
     * 删除合同
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContract(@PathVariable Long id) {
        contractsService.deleteContract(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 搜索合同
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Contracts>> searchContracts(
            @RequestParam String keyword,
            Pageable pageable) {
        
        Page<Contracts> contracts = contractsService.searchContracts(keyword, pageable);
        return ResponseEntity.ok(contracts);
    }
    
    /**
     * 生成装箱单
     */
    @PostMapping("/{id}/containers/generate")
    public ResponseEntity<Map<String, Object>> generateContainer(@PathVariable Long id) {
        Map<String, Object> result = contractsService.generateContainer(id);
        return ResponseEntity.ok(result);
    }
    
    
    /**
     * 开始工艺分解
     */
    @PostMapping("/{id}/breakdown/start")
    public ResponseEntity<Map<String, Object>> startBreakdown(@PathVariable Long id) {
        Map<String, Object> result = contractsService.startBreakdown(id);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取工艺分解结果
     */
    @GetMapping("/{id}/breakdown/result")
    public ResponseEntity<Map<String, Object>> getBreakdownResult(@PathVariable Long id) {
        Map<String, Object> result = contractsService.getBreakdownResult(id);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 导出分解表
     */
    @GetMapping("/{id}/breakdown/export")
    public ResponseEntity<byte[]> exportBreakdown(
            @PathVariable Long id,
            @RequestParam(defaultValue = "excel") String format) {
        
        byte[] data = contractsService.exportBreakdown(id, format);
        
        String contentType = "excel".equals(format) ? 
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" : 
            "application/pdf";
        
        String filename = "breakdown_" + id + "." + format;
        
        return ResponseEntity.ok()
                .header("Content-Type", contentType)
                .header("Content-Disposition", "attachment; filename=" + filename)
                .body(data);
    }
    
    /**
     * 获取合同的所有箱包列表
     */
    @GetMapping("/{id}/containers")
    public ResponseEntity<List<Containers>> getContractContainers(@PathVariable Long id) {
        List<Containers> containers = contractsService.getContractContainers(id);
        return ResponseEntity.ok(containers);
    }
}
