package com.mms.service.impl;

import com.mms.entity.ContainerComponentsBreakdown;
import com.mms.entity.ContainerComponentsBreakdownErp;
import com.mms.entity.Components;
import com.mms.repository.ContainerComponentsBreakdownErpRepository;
import com.mms.repository.ContainerComponentsBreakdownRepository;
import com.mms.service.ContainerComponentsBreakdownErpService;
import com.mms.utils.FastenerErpCodeFinder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 工艺分解ERP代码服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContainerComponentsBreakdownErpServiceImpl implements ContainerComponentsBreakdownErpService {
    
    private final ContainerComponentsBreakdownErpRepository erpRepository;
    private final ContainerComponentsBreakdownRepository breakdownRepository;
    private final FastenerErpCodeFinder fastenerErpCodeFinder;
    
    @Override
    @Transactional
    public ContainerComponentsBreakdownErp create(ContainerComponentsBreakdownErp erpRecord) {
        log.info("创建ERP代码记录: breakdownId={}, erpCode={}", 
                erpRecord.getBreakdown().getId(), erpRecord.getErpCode());
        
        return erpRepository.save(erpRecord);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ContainerComponentsBreakdownErp> findById(Long id) {
        return erpRepository.findById(id);
    }
    
    @Override
    @Transactional
    public ContainerComponentsBreakdownErp update(ContainerComponentsBreakdownErp erpRecord) {
        log.info("更新ERP代码记录: id={}, erpCode={}", 
                erpRecord.getId(), erpRecord.getErpCode());
        
        return erpRepository.save(erpRecord);
    }
    
    @Override
    @Transactional
    public void deleteById(Long id) {
        log.info("删除ERP代码记录: id={}", id);
        erpRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ContainerComponentsBreakdownErp> findByBreakdownId(Long breakdownId) {
        return erpRepository.findByBreakdownId(breakdownId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ContainerComponentsBreakdownErp> findByErpCode(String erpCode) {
        return erpRepository.findByErpCode(erpCode);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ContainerComponentsBreakdownErp> findByBreakdownIdAndErpCode(Long breakdownId, String erpCode) {
        return erpRepository.findByBreakdownIdAndErpCode(breakdownId, erpCode);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ContainerComponentsBreakdownErp> findByConditions(Long breakdownId, String erpCode, String comments, Pageable pageable) {
        return erpRepository.findByConditions(breakdownId, erpCode, comments, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ContainerComponentsBreakdownErp> findByContractId(Long contractId) {
        return erpRepository.findByContractId(contractId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ContainerComponentsBreakdownErp> findByContainerId(Long containerId) {
        return erpRepository.findByContainerId(containerId);
    }
    
    @Override
    @Transactional
    public List<ContainerComponentsBreakdownErp> batchCreate(List<ContainerComponentsBreakdownErp> erpRecords) {
        log.info("批量创建ERP代码记录，数量: {}", erpRecords.size());
        return erpRepository.saveAll(erpRecords);
    }
    
    @Override
    @Transactional
    public List<ContainerComponentsBreakdownErp> batchUpdate(List<ContainerComponentsBreakdownErp> erpRecords) {
        log.info("批量更新ERP代码记录，数量: {}", erpRecords.size());
        return erpRepository.saveAll(erpRecords);
    }
    
    @Override
    @Transactional
    public void batchDeleteByBreakdownId(Long breakdownId) {
        log.info("批量删除工艺分解ID的ERP代码记录: {}", breakdownId);
        erpRepository.deleteByBreakdownId(breakdownId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countByBreakdownId(Long breakdownId) {
        return erpRepository.countByBreakdownId(breakdownId);
    }
    
    @Override
    @Transactional
    public List<ContainerComponentsBreakdownErp> generateErpCodesForBreakdown(Long breakdownId) {
        log.info("为工艺分解结果生成ERP代码记录: {}", breakdownId);
        
        Optional<ContainerComponentsBreakdown> breakdownOpt = breakdownRepository.findById(breakdownId);
        if (breakdownOpt.isEmpty()) {
            log.warn("工艺分解记录不存在: {}", breakdownId);
            return new ArrayList<>();
        }
        
        ContainerComponentsBreakdown breakdown = breakdownOpt.get();
        Components subComponent = breakdown.getSubComponent();
        
        if (subComponent == null) {
            log.warn("工艺分解记录中没有子组件信息: {}", breakdownId);
            return new ArrayList<>();
        }
        
        // 使用FastenerErpCodeFinder查找ERP代码
        FastenerErpCodeFinder.ErpCodeResult result = fastenerErpCodeFinder.findErpCode(
                subComponent.getId(), 
                subComponent.getComponentCode(), 
                subComponent.getName()
        );
        
        List<ContainerComponentsBreakdownErp> erpRecords = new ArrayList<>();
        
        if (result.isSuccess()) {
            ContainerComponentsBreakdownErp erpRecord = new ContainerComponentsBreakdownErp();
            erpRecord.setBreakdown(breakdown);
            erpRecord.setErpCode(result.getErpCode());
            erpRecord.setComments(String.format("自动生成 - 紧固件组件: %s, 匹配产品代码: %s, 规格: %s, 等级: %s, 表面处理: %s",
                    result.isFastenerComponent() ? "是" : "否",
                    result.getMatchedProductCode(),
                    result.getMatchedSpecs(),
                    result.getMatchedLevel(),
                    result.getMatchedSurfaceTreatment()));
            
            erpRecords.add(erpRecord);
            log.info("成功生成ERP代码记录: breakdownId={}, erpCode={}, fastenerComponent={}", 
                    breakdownId, result.getErpCode(), result.isFastenerComponent());
        } else {
            // 即使查找失败，也创建一个记录用于标记
            ContainerComponentsBreakdownErp erpRecord = new ContainerComponentsBreakdownErp();
            erpRecord.setBreakdown(breakdown);
            erpRecord.setErpCode(null);
            erpRecord.setComments(String.format("ERP代码查找失败: %s, 紧固件组件: %s", 
                    result.getErrorMessage(),
                    result.isFastenerComponent() ? "是" : "否"));
            
            erpRecords.add(erpRecord);
            log.warn("ERP代码查找失败: breakdownId={}, error={}, fastenerComponent={}", 
                    breakdownId, result.getErrorMessage(), result.isFastenerComponent());
        }
        
        return batchCreate(erpRecords);
    }
    
    @Override
    @Transactional
    public List<ContainerComponentsBreakdownErp> generateErpCodesForContract(Long contractId) {
        log.info("为合同的所有工艺分解结果生成ERP代码记录: {}", contractId);
        
        List<ContainerComponentsBreakdownErp> allErpRecords = new ArrayList<>();
        
        // 获取合同的所有工艺分解记录
        List<ContainerComponentsBreakdownErp> existingRecords = findByContractId(contractId);
        
        // 获取所有工艺分解ID
        List<Long> breakdownIds = existingRecords.stream()
                .map(record -> record.getBreakdown().getId())
                .distinct()
                .toList();
        
        // 为每个工艺分解记录生成ERP代码
        for (Long breakdownId : breakdownIds) {
            List<ContainerComponentsBreakdownErp> erpRecords = generateErpCodesForBreakdown(breakdownId);
            allErpRecords.addAll(erpRecords);
        }
        
        log.info("为合同生成ERP代码记录完成: contractId={}, 总数量={}", contractId, allErpRecords.size());
        return allErpRecords;
    }
}
