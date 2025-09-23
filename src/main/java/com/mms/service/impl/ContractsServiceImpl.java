package com.mms.service.impl;

import com.mms.entity.Contracts;
import com.mms.repository.ContractsRepository;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractsServiceImpl implements ContractsService {
    
    private final ContractsRepository contractsRepository;
    private final CacheService cacheService;
    private final DistributedLockService distributedLockService;
    private final ContractParametersService contractParametersService;
    
    @Override
    public Page<Contracts> getContracts(String contractNo, String projectName, Contracts.ContractStatus status, Pageable pageable) {
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
                // 都没有值，查询所有
                contracts = contractsRepository.findAll(pageable);
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
            return cachedContract;
        }
        
        Contracts contract = contractsRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("合同不存在"));
        
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
        existingContract.setQuantity(contract.getQuantity());
        
        Contracts updatedContract = contractsRepository.save(existingContract);
        
        // 清除相关缓存
        clearContractCache(id);
        clearContractsCache();
        
        log.info("更新合同成功: {}", updatedContract.getContractNo());
        return updatedContract;
    }
    
    @Override
    @Transactional
    public void deleteContract(Long id) {
        Contracts contract = getContractById(id);
        
        if (contract.getStatus() == Contracts.ContractStatus.PROCESSING) {
            throw new RuntimeException("处理中的合同不能删除");
        }
        
        contractsRepository.deleteById(id);
        
        // 清除相关缓存
        clearContractCache(id);
        clearContractsCache();
        
        log.info("删除合同成功: {}", contract.getContractNo());
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
    public Map<String, Object> uploadContainer(Long contractId, MultipartFile file) {
        String lockKey = "contract:upload:" + contractId;
        
        return distributedLockService.executeWithLock(lockKey, 10, 30, TimeUnit.SECONDS, () -> {
            Contracts contract = getContractById(contractId);
            
            if (contract.getStatus() != Contracts.ContractStatus.DRAFT) {
                throw new RuntimeException("只有草稿状态的合同才能上传装箱单");
            }
            
            // TODO: 实现装箱单上传和解析逻辑
            // 1. 验证文件格式
            // 2. 解析Excel文件内容
            // 3. 保存装箱单数据到数据库
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "装箱单上传成功");
            result.put("fileName", file.getOriginalFilename());
            
            log.info("为合同 {} 上传装箱单成功: {}", contract.getContractNo(), file.getOriginalFilename());
            return result;
        });
    }
    
    @Override
    @Transactional
    public Map<String, Object> startBreakdown(Long contractId) {
        String lockKey = "contract:breakdown:" + contractId;
        
        return distributedLockService.executeWithLock(lockKey, 10, 30, TimeUnit.SECONDS, () -> {
            Contracts contract = getContractById(contractId);
            
            if (contract.getStatus() != Contracts.ContractStatus.DRAFT) {
                throw new RuntimeException("只有草稿状态的合同才能开始工艺分解");
            }
            
            // 更新合同状态为处理中
            contract.setStatus(Contracts.ContractStatus.PROCESSING);
            contractsRepository.save(contract);
            
            // TODO: 异步执行工艺分解任务
            // 1. 获取装箱单数据
            // 2. 根据零部件关系进行分解
            // 3. 生成合并分解表
            // 4. 更新合同状态
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "工艺分解已开始");
            result.put("status", "PROCESSING");
            
            // 清除相关缓存
            clearContractCache(contractId);
            
            log.info("合同 {} 开始工艺分解", contract.getContractNo());
            return result;
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
        
        // TODO: 实现获取工艺分解结果的逻辑
        // 1. 查询分解表数据
        // 2. 组装返回结果
        
        Map<String, Object> result = new HashMap<>();
        result.put("contractId", contractId);
        result.put("status", contract.getStatus());
        result.put("breakdownData", new HashMap<>()); // 实际的分解数据
        
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
        
        // TODO: 实现导出逻辑
        // 1. 获取分解表数据
        // 2. 根据格式生成Excel或PDF文件
        // 3. 返回文件字节数组
        
        if ("excel".equals(format)) {
            // 生成Excel文件
            return generateExcelFile(contract);
        } else {
            // 生成PDF文件
            return generatePdfFile(contract);
        }
    }
    
    private byte[] generateExcelFile(Contracts contract) {
        // TODO: 使用Apache POI生成Excel文件
        return new byte[0];
    }
    
    private byte[] generatePdfFile(Contracts contract) {
        // TODO: 使用iText生成PDF文件
        return new byte[0];
    }
    
    private void clearContractCache(Long contractId) {
        cacheService.delete("contract:" + contractId);
        cacheService.delete("contract:breakdown:result:" + contractId);
    }
    
    private void clearContractsCache() {
        // 清除所有合同列表相关的缓存
        // 这里可以使用Redis的keys命令或者维护一个缓存键的集合
    }
}
