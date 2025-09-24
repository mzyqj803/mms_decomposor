package com.mms.service;

import java.util.Map;

public interface BreakdownService {
    
    /**
     * 对单个箱包进行工艺分解
     * @param containerId 箱包ID
     * @return 分解结果
     */
    Map<String, Object> breakdownContainer(Long containerId);
    
    /**
     * 对合同的所有箱包进行工艺分解
     * @param contractId 合同ID
     * @return 分解结果
     */
    Map<String, Object> breakdownContract(Long contractId);
    
    /**
     * 获取箱包的工艺分解结果
     * @param containerId 箱包ID
     * @return 分解结果
     */
    Map<String, Object> getContainerBreakdown(Long containerId);
    
    /**
     * 获取合同的工艺分解汇总结果
     * @param contractId 合同ID
     * @return 分解汇总结果
     */
    Map<String, Object> getContractBreakdownSummary(Long contractId);
    
    /**
     * 导出工艺分解表
     * @param contractId 合同ID
     * @param format 导出格式 (excel, pdf)
     * @return 文件字节数组
     */
    byte[] exportBreakdown(Long contractId, String format);
}
