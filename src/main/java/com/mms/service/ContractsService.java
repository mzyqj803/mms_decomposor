package com.mms.service;

import com.mms.entity.Contracts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface ContractsService {
    
    /**
     * 获取合同列表
     */
    Page<Contracts> getContracts(String contractNo, String projectName, Contracts.ContractStatus status, Pageable pageable);
    
    /**
     * 根据ID获取合同
     */
    Contracts getContractById(Long id);
    
    /**
     * 创建合同
     */
    Contracts createContract(Contracts contract);
    
    /**
     * 更新合同
     */
    Contracts updateContract(Long id, Contracts contract);
    
    /**
     * 删除合同
     */
    void deleteContract(Long id);
    
    /**
     * 搜索合同
     */
    Page<Contracts> searchContracts(String keyword, Pageable pageable);
    
    /**
     * 生成装箱单
     */
    Map<String, Object> generateContainer(Long contractId);
    
    
    /**
     * 开始工艺分解
     */
    Map<String, Object> startBreakdown(Long contractId);
    
    /**
     * 获取工艺分解结果
     */
    Map<String, Object> getBreakdownResult(Long contractId);
    
    /**
     * 导出分解表
     */
    byte[] exportBreakdown(Long contractId, String format);
    
    /**
     * 获取合同的所有箱包列表
     */
    java.util.List<com.mms.entity.Containers> getContractContainers(Long contractId);
}
