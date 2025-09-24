package com.mms.repository;

import com.mms.entity.ContainerComponentsBreakdown;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ContainerComponentsBreakdownRepository extends JpaRepository<ContainerComponentsBreakdown, Long> {
    
    /**
     * 根据装箱单ID查找所有分解记录
     */
    @Query("SELECT ccb FROM ContainerComponentsBreakdown ccb WHERE ccb.container.id = :containerId")
    List<ContainerComponentsBreakdown> findByContainerId(@Param("containerId") Long containerId);
    
    /**
     * 根据合同ID查找所有分解记录
     */
    @Query("SELECT ccb FROM ContainerComponentsBreakdown ccb WHERE ccb.container.contract.id = :contractId")
    List<ContainerComponentsBreakdown> findByContractId(@Param("contractId") Long contractId);
    
    /**
     * 根据合同ID删除所有分解记录
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ContainerComponentsBreakdown ccb WHERE ccb.container.contract.id = :contractId")
    void deleteByContractId(@Param("contractId") Long contractId);
}
