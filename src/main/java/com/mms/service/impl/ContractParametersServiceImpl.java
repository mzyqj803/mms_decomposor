package com.mms.service.impl;

import com.mms.entity.ContractParameters;
import com.mms.entity.Contracts;
import com.mms.repository.ContractParametersRepository;
import com.mms.repository.ContractsRepository;
import com.mms.service.ContractParametersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractParametersServiceImpl implements ContractParametersService {
    
    private final ContractParametersRepository contractParametersRepository;
    private final ContractsRepository contractsRepository;
    
    @Override
    public List<ContractParameters> getContractParameters(Long contractId) {
        return contractParametersRepository.findByContractId(contractId);
    }
    
    @Override
    @Transactional
    public ContractParameters saveContractParameter(ContractParameters parameter) {
        ContractParameters savedParameter = contractParametersRepository.save(parameter);
        log.info("保存合同参数成功: 合同ID={}, 参数名={}", 
            parameter.getContract().getId(), parameter.getParamName());
        return savedParameter;
    }
    
    @Override
    @Transactional
    public List<ContractParameters> saveContractParameters(Long contractId, List<ContractParameters> parameters) {
        // 获取合同实体
        Contracts contract = contractsRepository.findById(contractId)
            .orElseThrow(() -> new RuntimeException("合同不存在"));
        
        // 设置合同关系
        parameters.forEach(param -> param.setContract(contract));
        
        List<ContractParameters> savedParameters = contractParametersRepository.saveAll(parameters);
        log.info("批量保存合同参数成功: 合同ID={}, 参数数量={}", contractId, parameters.size());
        return savedParameters;
    }
    
    @Override
    @Transactional
    public ContractParameters updateContractParameter(Long id, ContractParameters parameter) {
        ContractParameters existingParameter = contractParametersRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("合同参数不存在"));
        
        existingParameter.setParamName(parameter.getParamName());
        existingParameter.setParamValue(parameter.getParamValue());
        
        ContractParameters updatedParameter = contractParametersRepository.save(existingParameter);
        log.info("更新合同参数成功: ID={}, 参数名={}", id, parameter.getParamName());
        return updatedParameter;
    }
    
    @Override
    @Transactional
    public void deleteContractParameter(Long id) {
        ContractParameters parameter = contractParametersRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("合同参数不存在"));
        
        contractParametersRepository.deleteById(id);
        log.info("删除合同参数成功: ID={}, 参数名={}", id, parameter.getParamName());
    }
    
    @Override
    @Transactional
    public void deleteContractParameters(Long contractId) {
        contractParametersRepository.deleteByContractId(contractId);
        log.info("删除合同所有参数成功: 合同ID={}", contractId);
    }
    
    @Override
    public String getParameterValue(Long contractId, String paramName) {
        ContractParameters parameter = contractParametersRepository.findByContractIdAndParamName(contractId, paramName);
        return parameter != null ? parameter.getParamValue() : null;
    }
    
    @Override
    @Transactional
    public ContractParameters setParameterValue(Long contractId, String paramName, String paramValue) {
        ContractParameters existingParameter = contractParametersRepository.findByContractIdAndParamName(contractId, paramName);
        
        if (existingParameter != null) {
            // 更新现有参数
            existingParameter.setParamValue(paramValue);
            ContractParameters updatedParameter = contractParametersRepository.save(existingParameter);
            log.info("更新合同参数值: 合同ID={}, 参数名={}", contractId, paramName);
            return updatedParameter;
        } else {
            // 创建新参数
            Contracts contract = contractsRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("合同不存在"));
            
            ContractParameters newParameter = new ContractParameters();
            newParameter.setContract(contract);
            newParameter.setParamName(paramName);
            newParameter.setParamValue(paramValue);
            ContractParameters savedParameter = contractParametersRepository.save(newParameter);
            log.info("创建合同参数: 合同ID={}, 参数名={}", contractId, paramName);
            return savedParameter;
        }
    }
    
    @Override
    @Transactional
    public List<ContractParameters> setParameterValues(Long contractId, Map<String, String> parameters) {
        return parameters.entrySet().stream()
            .map(entry -> setParameterValue(contractId, entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }
}
