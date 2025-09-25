package com.mms.repository;

import com.mms.entity.ContainerComponentsBreakdownProblems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ContainerComponentsBreakdownProblemsRepository extends JpaRepository<ContainerComponentsBreakdownProblems, Long> {
    
    /**
     * 根据箱包ID查找所有问题部件记录
     */
    @Query("SELECT ccbp FROM ContainerComponentsBreakdownProblems ccbp WHERE ccbp.container.id = :containerId")
    List<ContainerComponentsBreakdownProblems> findByContainerId(@Param("containerId") Long containerId);
    
    /**
     * 根据箱包ID删除所有问题部件记录
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ContainerComponentsBreakdownProblems ccbp WHERE ccbp.container.id = :containerId")
    void deleteByContainerId(@Param("containerId") Long containerId);
    
    /**
     * 根据箱包ID列表删除所有问题部件记录
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ContainerComponentsBreakdownProblems ccbp WHERE ccbp.container.id IN :containerIds")
    void deleteByContainerIdIn(@Param("containerIds") List<Long> containerIds);
    
    /**
     * 根据合同ID查找所有问题部件记录
     */
    @Query("SELECT ccbp FROM ContainerComponentsBreakdownProblems ccbp WHERE ccbp.container.contract.id = :contractId")
    List<ContainerComponentsBreakdownProblems> findByContractId(@Param("contractId") Long contractId);
    
    /**
     * 根据合同ID删除所有问题部件记录
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ContainerComponentsBreakdownProblems ccbp WHERE ccbp.container.contract.id = :contractId")
    void deleteByContractId(@Param("contractId") Long contractId);
}
