package com.mms.service;

import com.mms.entity.ContainerComponentsBreakdownErp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 工艺分解ERP代码服务接口
 */
public interface ContainerComponentsBreakdownErpService {
    
    /**
     * 创建ERP代码记录
     */
    ContainerComponentsBreakdownErp create(ContainerComponentsBreakdownErp erpRecord);
    
    /**
     * 根据ID查找ERP代码记录
     */
    Optional<ContainerComponentsBreakdownErp> findById(Long id);
    
    /**
     * 更新ERP代码记录
     */
    ContainerComponentsBreakdownErp update(ContainerComponentsBreakdownErp erpRecord);
    
    /**
     * 删除ERP代码记录
     */
    void deleteById(Long id);
    
    /**
     * 根据工艺分解ID查找ERP代码记录列表
     */
    List<ContainerComponentsBreakdownErp> findByBreakdownId(Long breakdownId);
    
    /**
     * 根据ERP代码查找记录列表
     */
    List<ContainerComponentsBreakdownErp> findByErpCode(String erpCode);
    
    /**
     * 根据工艺分解ID和ERP代码查找记录
     */
    Optional<ContainerComponentsBreakdownErp> findByBreakdownIdAndErpCode(Long breakdownId, String erpCode);
    
    /**
     * 分页查询ERP代码记录（支持多条件搜索）
     */
    Page<ContainerComponentsBreakdownErp> findByConditions(Long breakdownId, String erpCode, String comments, Pageable pageable);
    
    /**
     * 根据合同ID查找所有相关的ERP代码记录
     */
    List<ContainerComponentsBreakdownErp> findByContractId(Long contractId);
    
    /**
     * 根据装箱单ID查找所有相关的ERP代码记录
     */
    List<ContainerComponentsBreakdownErp> findByContainerId(Long containerId);
    
    /**
     * 批量创建ERP代码记录
     */
    List<ContainerComponentsBreakdownErp> batchCreate(List<ContainerComponentsBreakdownErp> erpRecords);
    
    /**
     * 批量更新ERP代码记录
     */
    List<ContainerComponentsBreakdownErp> batchUpdate(List<ContainerComponentsBreakdownErp> erpRecords);
    
    /**
     * 批量删除指定工艺分解ID的所有ERP代码记录
     */
    void batchDeleteByBreakdownId(Long breakdownId);
    
    /**
     * 统计指定工艺分解ID的ERP代码记录数量
     */
    long countByBreakdownId(Long breakdownId);
    
    /**
     * 为工艺分解结果自动生成ERP代码记录
     * 使用FastenerErpCodeFinder工具类自动查找ERP代码
     */
    List<ContainerComponentsBreakdownErp> generateErpCodesForBreakdown(Long breakdownId);
    
    /**
     * 为合同的所有工艺分解结果生成ERP代码记录
     */
    List<ContainerComponentsBreakdownErp> generateErpCodesForContract(Long contractId);
}
