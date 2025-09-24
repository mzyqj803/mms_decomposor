package com.mms.repository;

import com.mms.entity.ContainersComponentsSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ContainersComponentsSummaryRepository extends JpaRepository<ContainersComponentsSummary, Long> {
    
    /**
     * 根据合同ID查找所有汇总记录
     */
    @Query("SELECT ccs FROM ContainersComponentsSummary ccs WHERE ccs.contract.id = :contractId")
    List<ContainersComponentsSummary> findByContractId(@Param("contractId") Long contractId);
    
    /**
     * 根据装箱单ID查找所有汇总记录
     */
    @Query("SELECT ccs FROM ContainersComponentsSummary ccs WHERE ccs.container.id = :containerId")
    List<ContainersComponentsSummary> findByContainerId(@Param("containerId") Long containerId);
    
    /**
     * 根据合同ID删除所有汇总记录
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ContainersComponentsSummary ccs WHERE ccs.contract.id = :contractId")
    void deleteByContractId(@Param("contractId") Long contractId);
}
