package com.mms.controller;

import com.mms.entity.ContractParameters;
import com.mms.entity.Contracts;
import com.mms.repository.ContractsRepository;
import com.mms.service.ContractParametersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/contracts/{contractId}/parameters")
@RequiredArgsConstructor
public class ContractParametersController {
    
    private final ContractParametersService contractParametersService;
    private final ContractsRepository contractsRepository;
    
    /**
     * 获取合同的所有参数
     */
    @GetMapping
    public ResponseEntity<List<ContractParameters>> getContractParameters(@PathVariable Long contractId) {
        List<ContractParameters> parameters = contractParametersService.getContractParameters(contractId);
        return ResponseEntity.ok(parameters);
    }
    
    /**
     * 创建合同参数
     */
    @PostMapping
    public ResponseEntity<ContractParameters> createContractParameter(
            @PathVariable Long contractId,
            @Valid @RequestBody ContractParameters parameter) {
        // 获取合同实体
        Contracts contract = contractsRepository.findById(contractId)
            .orElseThrow(() -> new RuntimeException("合同不存在"));
        
        // 设置合同关系
        parameter.setContract(contract);
        
        ContractParameters createdParameter = contractParametersService.saveContractParameter(parameter);
        return ResponseEntity.ok(createdParameter);
    }
    
    /**
     * 批量创建合同参数
     */
    @PostMapping("/batch")
    public ResponseEntity<List<ContractParameters>> createContractParameters(
            @PathVariable Long contractId,
            @Valid @RequestBody List<ContractParameters> parameters) {
        List<ContractParameters> createdParameters = contractParametersService.saveContractParameters(contractId, parameters);
        return ResponseEntity.ok(createdParameters);
    }
    
    /**
     * 更新合同参数
     */
    @PutMapping("/{id}")
    public ResponseEntity<ContractParameters> updateContractParameter(
            @PathVariable Long contractId,
            @PathVariable Long id,
            @Valid @RequestBody ContractParameters parameter) {
        ContractParameters updatedParameter = contractParametersService.updateContractParameter(id, parameter);
        return ResponseEntity.ok(updatedParameter);
    }
    
    /**
     * 删除合同参数
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContractParameter(@PathVariable Long contractId, @PathVariable Long id) {
        contractParametersService.deleteContractParameter(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 删除合同的所有参数
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteContractParameters(@PathVariable Long contractId) {
        contractParametersService.deleteContractParameters(contractId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取指定参数名的值
     */
    @GetMapping("/{paramName}")
    public ResponseEntity<Map<String, String>> getParameterValue(
            @PathVariable Long contractId,
            @PathVariable String paramName) {
        String value = contractParametersService.getParameterValue(contractId, paramName);
        Map<String, String> result = Map.of("paramName", paramName, "paramValue", value != null ? value : "");
        return ResponseEntity.ok(result);
    }
    
    /**
     * 设置参数值
     */
    @PutMapping("/{paramName}")
    public ResponseEntity<ContractParameters> setParameterValue(
            @PathVariable Long contractId,
            @PathVariable String paramName,
            @RequestBody Map<String, String> request) {
        String paramValue = request.get("paramValue");
        ContractParameters parameter = contractParametersService.setParameterValue(contractId, paramName, paramValue);
        return ResponseEntity.ok(parameter);
    }
    
    /**
     * 批量设置参数值
     */
    @PutMapping("/batch")
    public ResponseEntity<List<ContractParameters>> setParameterValues(
            @PathVariable Long contractId,
            @RequestBody Map<String, String> parameters) {
        List<ContractParameters> result = contractParametersService.setParameterValues(contractId, parameters);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 批量更新合同参数（替换所有参数）
     */
    @PutMapping("/batch/replace")
    public ResponseEntity<List<ContractParameters>> replaceContractParameters(
            @PathVariable Long contractId,
            @Valid @RequestBody List<ContractParameters> parameters) {
        // 先删除现有参数
        contractParametersService.deleteContractParameters(contractId);
        
        // 再保存新参数
        List<ContractParameters> savedParameters = contractParametersService.saveContractParameters(contractId, parameters);
        return ResponseEntity.ok(savedParameters);
    }
}
