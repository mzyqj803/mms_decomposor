package com.mms.repository;

import com.mms.entity.ContainerComponentsBreakdownErp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 工艺分解ERP代码Repository
 */
@Repository
public interface ContainerComponentsBreakdownErpRepository extends JpaRepository<ContainerComponentsBreakdownErp, Long> {
    
    /**
     * 根据工艺分解ID查找ERP代码记录
     */
    List<ContainerComponentsBreakdownErp> findByBreakdownId(Long breakdownId);
    
    /**
     * 根据ERP代码查找记录
     */
    List<ContainerComponentsBreakdownErp> findByErpCode(String erpCode);
    
    /**
     * 根据工艺分解ID和ERP代码查找记录
     */
    Optional<ContainerComponentsBreakdownErp> findByBreakdownIdAndErpCode(Long breakdownId, String erpCode);
    
    /**
     * 分页查询ERP代码记录（支持多条件搜索）
     */
    @Query("SELECT ccbe FROM ContainerComponentsBreakdownErp ccbe WHERE " +
           "(:breakdownId IS NULL OR ccbe.breakdown.id = :breakdownId) AND " +
           "(:erpCode IS NULL OR ccbe.erpCode LIKE %:erpCode%) AND " +
           "(:comments IS NULL OR ccbe.comments LIKE %:comments%)")
    Page<ContainerComponentsBreakdownErp> findByConditions(@Param("breakdownId") Long breakdownId,
                                                           @Param("erpCode") String erpCode,
                                                           @Param("comments") String comments,
                                                           Pageable pageable);
    
    /**
     * 根据合同ID查找所有相关的ERP代码记录
     */
    @Query("SELECT ccbe FROM ContainerComponentsBreakdownErp ccbe " +
           "JOIN ccbe.breakdown b " +
           "JOIN b.container c " +
           "WHERE c.contract.id = :contractId")
    List<ContainerComponentsBreakdownErp> findByContractId(@Param("contractId") Long contractId);
    
    /**
     * 根据装箱单ID查找所有相关的ERP代码记录
     */
    @Query("SELECT ccbe FROM ContainerComponentsBreakdownErp ccbe " +
           "JOIN ccbe.breakdown b " +
           "WHERE b.container.id = :containerId")
    List<ContainerComponentsBreakdownErp> findByContainerId(@Param("containerId") Long containerId);
    
    /**
     * 统计指定工艺分解ID的ERP代码记录数量
     */
    long countByBreakdownId(Long breakdownId);
    
    /**
     * 删除指定工艺分解ID的所有ERP代码记录
     */
    void deleteByBreakdownId(Long breakdownId);
}
