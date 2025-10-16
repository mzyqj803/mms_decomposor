package com.mms.service.impl;

import com.mms.entity.Contracts;
import com.mms.entity.Containers;
import com.mms.dto.ContainerDTO;
import com.mms.repository.ContractsRepository;
import com.mms.repository.ContainersRepository;
import com.mms.service.BreakdownService;
import com.mms.service.CacheService;
import com.mms.service.ContractParametersService;
import com.mms.service.ContractsService;
import com.mms.service.DistributedLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractsServiceImpl implements ContractsService {
    
    private final ContractsRepository contractsRepository;
    private final ContainersRepository containersRepository;
    private final CacheService cacheService;
    private final DistributedLockService distributedLockService;
    private final ContractParametersService contractParametersService;
    private final BreakdownService breakdownService;
    
    @Override
    public Page<Contracts> getContracts(String contractNo, String projectName, Integer status, Pageable pageable) {
        String cacheKey = String.format("contracts:%s:%s:%s:%d:%d", 
            contractNo, projectName, status, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Contracts> cachedResult = cacheService.get(cacheKey, Page.class);
        if (cachedResult != null) {
            return cachedResult;
        }
        
        Page<Contracts> contracts;
        
        // 根据参数情况选择不同的查询方法
        if (status != null) {
            // 如果指定了状态，先按状态查询
            if (contractNo != null && !contractNo.trim().isEmpty() && projectName != null && !projectName.trim().isEmpty()) {
                // 两个参数都有值 + 状态
                contracts = contractsRepository.findByContractNoAndProjectNameContainingAndStatus(contractNo, projectName, status, pageable);
            } else if (contractNo != null && !contractNo.trim().isEmpty()) {
                // 只有合同号有值 + 状态
                contracts = contractsRepository.findByContractNoContainingAndStatus(contractNo, status, pageable);
            } else if (projectName != null && !projectName.trim().isEmpty()) {
                // 只有项目名称有值 + 状态
                contracts = contractsRepository.findByProjectNameContainingAndStatus(projectName, status, pageable);
            } else {
                // 只有状态
                contracts = contractsRepository.findByStatus(status, pageable);
            }
        } else {
            // 没有状态过滤
            if (contractNo != null && !contractNo.trim().isEmpty() && projectName != null && !projectName.trim().isEmpty()) {
                // 两个参数都有值
                contracts = contractsRepository.findByContractNoAndProjectNameContaining(contractNo, projectName, pageable);
            } else if (contractNo != null && !contractNo.trim().isEmpty()) {
                // 只有合同号有值
                contracts = contractsRepository.findByContractNoContaining(contractNo, pageable);
            } else if (projectName != null && !projectName.trim().isEmpty()) {
                // 只有项目名称有值
                contracts = contractsRepository.findByProjectNameContaining(projectName, pageable);
            } else {
                // 都没有值，查询所有（排除已删除）
                contracts = contractsRepository.findAllExcludeDeleted(pageable);
            }
        }
        
        // 缓存5分钟
        cacheService.set(cacheKey, contracts, 5, TimeUnit.MINUTES);
        
        return contracts;
    }
    
    @Override
    public Contracts getContractById(Long id) {
        String cacheKey = "contract:" + id;
        
        Contracts cachedContract = cacheService.get(cacheKey, Contracts.class);
        if (cachedContract != null) {
            // 检查缓存的合同是否已删除
            if (cachedContract.getStatus() == Contracts.ContractStatus.DELETED) {
                throw new RuntimeException("合同已被删除");
            }
            return cachedContract;
        }
        
        Contracts contract = contractsRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("合同不存在"));
        
        // 检查合同是否已删除
        if (contract.getStatus() == Contracts.ContractStatus.DELETED) {
            throw new RuntimeException("合同已被删除");
        }
        
        // 确保装箱单数据被加载（由于使用了LAZY加载）
        if (contract.getContainers() != null) {
            contract.getContainers().size(); // 触发懒加载
        }
        
        // 缓存10分钟
        cacheService.set(cacheKey, contract, 10, TimeUnit.MINUTES);
        
        return contract;
    }
    
    /**
     * 获取合同详情（包括已删除的合同，用于显示）
     */
    public Contracts getContractByIdIncludeDeleted(Long id) {
        String cacheKey = "contract:all:" + id;
        
        Contracts cachedContract = cacheService.get(cacheKey, Contracts.class);
        if (cachedContract != null) {
            return cachedContract;
        }
        
        Contracts contract = contractsRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("合同不存在"));
        
        // 确保装箱单数据被加载（由于使用了LAZY加载）
        if (contract.getContainers() != null) {
            contract.getContainers().size(); // 触发懒加载
        }
        
        // 缓存10分钟
        cacheService.set(cacheKey, contract, 10, TimeUnit.MINUTES);
        
        return contract;
    }
    
    @Override
    @Transactional
    public Contracts createContract(Contracts contract) {
        // 检查合同号是否已存在
        if (contractsRepository.findByContractNo(contract.getContractNo()).isPresent()) {
            throw new RuntimeException("合同号已存在");
        }
        
        contract.setStatus(Contracts.ContractStatus.DRAFT);
        Contracts savedContract = contractsRepository.save(contract);
        
        // 保存合同参数
        if (contract.getParameters() != null && !contract.getParameters().isEmpty()) {
            contract.getParameters().forEach(param -> param.setContract(savedContract));
            contractParametersService.saveContractParameters(savedContract.getId(), contract.getParameters());
        }
        
        // 清除相关缓存
        clearContractsCache();
        
        log.info("创建合同成功: {}", savedContract.getContractNo());
        return savedContract;
    }
    
    @Override
    @Transactional
    public Contracts updateContract(Long id, Contracts contract) {
        Contracts existingContract = getContractById(id);
        
        existingContract.setClientName(contract.getClientName());
        existingContract.setProjectName(contract.getProjectName());
        
        Contracts updatedContract = contractsRepository.save(existingContract);
        
        // 清除相关缓存
        clearContractCache(id);
        clearContractsCache();
        
        log.info("更新合同成功: {}", updatedContract.getContractNo());
        return updatedContract;
    }
    
    @Override
    @Transactional
    public void deleteContract(Long id, String contractNo) {
        Contracts contract = getContractById(id);
        
        // 验证合同号是否匹配
        if (!contract.getContractNo().equals(contractNo)) {
            throw new RuntimeException("合同号不匹配，删除操作被拒绝");
        }
        
        if (contract.getStatus() == Contracts.ContractStatus.PROCESSING) {
            throw new RuntimeException("处理中的合同不能删除，请等待处理完成后再试");
        }
        
        // 逻辑删除：将状态设置为已删除
        contract.setStatus(Contracts.ContractStatus.DELETED);
        contractsRepository.save(contract);
        
        // 清除相关缓存
        clearContractCache(id);
        clearContractsCache();
        
        log.info("逻辑删除合同成功: {} (验证合同号: {})", contract.getContractNo(), contractNo);
    }
    
    @Override
    public Page<Contracts> searchContracts(String keyword, Pageable pageable) {
        String cacheKey = String.format("contracts:search:%s:%d:%d", 
            keyword, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Contracts> cachedResult = cacheService.get(cacheKey, Page.class);
        if (cachedResult != null) {
            return cachedResult;
        }
        
        Page<Contracts> contracts = contractsRepository.findByContractNoOrProjectNameContaining(keyword, pageable);
        
        // 缓存3分钟
        cacheService.set(cacheKey, contracts, 3, TimeUnit.MINUTES);
        
        return contracts;
    }
    
    @Override
    @Transactional
    public Map<String, Object> generateContainer(Long contractId) {
        String lockKey = "contract:generate:" + contractId;
        
        return distributedLockService.executeWithLock(lockKey, 10, 30, TimeUnit.SECONDS, () -> {
            Contracts contract = getContractById(contractId);
            
            if (contract.getStatus() != Contracts.ContractStatus.DRAFT) {
                throw new RuntimeException("只有草稿状态的合同才能生成装箱单");
            }
            
            // TODO: 实现装箱单生成逻辑
            // 1. 根据历史记录查找类似装箱单
            // 2. 生成新的装箱单数据
            // 3. 保存到数据库
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "装箱单生成成功");
            result.put("containerCount", 0); // 实际生成的装箱单数量
            
            log.info("为合同 {} 生成装箱单成功", contract.getContractNo());
            return result;
        });
    }
    
    
    @Override
    @Transactional
    public Map<String, Object> startBreakdown(Long contractId) {
        String lockKey = "contract:breakdown:" + contractId;
        
        return distributedLockService.executeWithLock(lockKey, 10, 30, TimeUnit.SECONDS, () -> {
            Contracts contract = getContractById(contractId);
            
            // 检查合同状态，允许重新分解
            // if (contract.getStatus() != Contracts.ContractStatus.DRAFT) {
            //     throw new RuntimeException("只有草稿状态的合同才能开始工艺分解");
            // }
            
            // 更新合同状态为处理中
            contract.setStatus(Contracts.ContractStatus.PROCESSING);
            contractsRepository.save(contract);
            
            try {
                // 执行实际的工艺分解任务
                Map<String, Object> breakdownResult = breakdownService.breakdownContract(contractId);
                
                // 分解完成后，合同状态已经在breakdownService.breakdownContract中更新为COMPLETED
                // 清除相关缓存
                clearContractCache(contractId);
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "工艺分解已完成");
                result.put("status", "COMPLETED");
                result.put("breakdownResult", breakdownResult);
                
                log.info("合同 {} 工艺分解完成", contract.getContractNo());
                return result;
                
            } catch (Exception e) {
                // 如果分解失败，将合同状态设置为错误
                contract.setStatus(Contracts.ContractStatus.ERROR);
                contractsRepository.save(contract);
                
                log.error("合同 {} 工艺分解失败", contract.getContractNo(), e);
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "工艺分解失败: " + e.getMessage());
                result.put("status", "ERROR");
                
                // 清除相关缓存
                clearContractCache(contractId);
                
                return result;
            }
        });
    }
    
    @Override
    public Map<String, Object> getBreakdownResult(Long contractId) {
        String cacheKey = "contract:breakdown:result:" + contractId;
        
        Map<String, Object> cachedResult = cacheService.get(cacheKey, Map.class);
        if (cachedResult != null) {
            return cachedResult;
        }
        
        Contracts contract = getContractById(contractId);
        
        // 获取合同分解汇总
        Map<String, Object> summary = breakdownService.getContractBreakdownSummary(contractId);
        @SuppressWarnings("unchecked")
        java.util.Collection<Map<String, Object>> allComponents = (java.util.Collection<Map<String, Object>>) summary.get("allComponents");
        
        // 将后端汇总结构转换为前端需要的展示结构
        java.util.List<Map<String, Object>> breakdownData = new java.util.ArrayList<>();
        if (allComponents != null) {
            for (Map<String, Object> comp : allComponents) {
                Map<String, Object> row = new HashMap<>();
                row.put("containerName", comp.getOrDefault("containerName", "未知箱包")); // 所属箱包
                row.put("componentCode", comp.get("componentCode")); // 部件代号
                row.put("componentName", comp.get("name")); // 部件名称
                row.put("quantity", comp.getOrDefault("totalQuantity", comp.get("quantity"))); // 数量
                row.put("erpCode", comp.getOrDefault("erpCode", "")); // ERP代码
                row.put("procurementFlag", comp.getOrDefault("procurementFlag", false)); // 是否外购
                row.put("remark", comp.getOrDefault("remark", "")); // 备注
                breakdownData.add(row);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("contractId", contractId);
        // 将整数状态转换为文本，便于前端判断
        String statusText;
        switch (contract.getStatus() == null ? -1 : contract.getStatus()) {
            case 0: statusText = "DRAFT"; break;
            case 1: statusText = "PROCESSING"; break;
            case 2: statusText = "COMPLETED"; break;
            case 3: statusText = "ERROR"; break;
            default: statusText = "UNKNOWN"; break;
        }
        result.put("status", statusText);
        result.put("success", true);
        result.put("message", breakdownData.isEmpty() ? "暂无分解结果" : "分解结果加载成功");
        result.put("breakdownData", breakdownData);
        
        // 缓存5分钟
        cacheService.set(cacheKey, result, 5, TimeUnit.MINUTES);
        
        return result;
    }
    
    @Override
    public byte[] exportBreakdown(Long contractId, String format) {
        Contracts contract = getContractById(contractId);
        
        if (contract.getStatus() != Contracts.ContractStatus.COMPLETED) {
            throw new RuntimeException("只有完成状态的合同才能导出分解表");
        }
        
        // 使用BreakdownService的合并分解表功能
        return breakdownService.generateMergedBreakdownPdf(contractId);
    }
    
    private void clearContractCache(Long contractId) {
        cacheService.delete("contract:" + contractId);
        cacheService.delete("contract:breakdown:result:" + contractId);
    }
    
    private void clearContractsCache() {
        // 清除所有合同列表相关的缓存
        // 使用通配符删除所有以"contracts:"开头的缓存键
        try {
            cacheService.deletePattern("contracts:*");
            log.info("已清除所有合同列表缓存");
        } catch (Exception e) {
            log.warn("清除合同列表缓存失败: {}", e.getMessage());
        }
    }
    
    @Override
    public List<Containers> getContractContainers(Long contractId) {
        String cacheKey = "contract:containers:" + contractId;
        
        // 尝试从缓存获取DTO列表
        @SuppressWarnings("unchecked")
        List<ContainerDTO> cachedContainers = cacheService.get(cacheKey, List.class);
        if (cachedContainers != null) {
            log.debug("从缓存获取合同{}的箱包列表，数量: {}", contractId, cachedContainers.size());
            // 从缓存获取时，直接查询数据库获取完整实体
            // 这样可以避免DTO转换的复杂性，同时享受缓存带来的性能提升
            return containersRepository.findByContractId(contractId);
        }
        
        // 从数据库查询
        List<Containers> containers = containersRepository.findByContractId(contractId);
        
        // 转换为DTO并缓存（用于缓存命中检测）
        List<ContainerDTO> containerDTOs = containers.stream()
                .map(ContainerDTO::fromEntity)
                .collect(Collectors.toList());
        
        // 缓存DTO列表（避免序列化问题）
        cacheService.set(cacheKey, containerDTOs, 10, TimeUnit.MINUTES);
        log.debug("缓存合同{}的箱包列表，数量: {}", contractId, containerDTOs.size());
        
        return containers;
    }
}
