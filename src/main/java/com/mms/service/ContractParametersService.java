package com.mms.service;

import com.mms.entity.ContractParameters;

import java.util.List;
import java.util.Map;

public interface ContractParametersService {
    
    /**
     * 获取合同的所有参数
     */
    List<ContractParameters> getContractParameters(Long contractId);
    
    /**
     * 保存合同参数
     */
    ContractParameters saveContractParameter(ContractParameters parameter);
    
    /**
     * 批量保存合同参数
     */
    List<ContractParameters> saveContractParameters(Long contractId, List<ContractParameters> parameters);
    
    /**
     * 更新合同参数
     */
    ContractParameters updateContractParameter(Long id, ContractParameters parameter);
    
    /**
     * 删除合同参数
     */
    void deleteContractParameter(Long id);
    
    /**
     * 删除合同的所有参数
     */
    void deleteContractParameters(Long contractId);
    
    /**
     * 根据参数名获取参数值
     */
    String getParameterValue(Long contractId, String paramName);
    
    /**
     * 设置参数值（如果不存在则创建，存在则更新）
     */
    ContractParameters setParameterValue(Long contractId, String paramName, String paramValue);
    
    /**
     * 批量设置参数值
     */
    List<ContractParameters> setParameterValues(Long contractId, Map<String, String> parameters);
}
