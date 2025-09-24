package com.mms.repository;

import com.mms.entity.ContainerComponents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ContainerComponentsRepository extends JpaRepository<ContainerComponents, Long> {
    
    /**
     * 根据装箱单ID查找所有组件
     */
    @Query("SELECT cc FROM ContainerComponents cc WHERE cc.container.id = :containerId")
    List<ContainerComponents> findByContainerId(@Param("containerId") Long containerId);
    
    /**
     * 根据装箱单ID和组件编号查找组件
     */
    @Query("SELECT cc FROM ContainerComponents cc WHERE cc.container.id = :containerId AND cc.componentNo = :componentNo")
    ContainerComponents findByContainerIdAndComponentNo(@Param("containerId") Long containerId, 
                                                       @Param("componentNo") String componentNo);
    
    /**
     * 根据合同ID查找所有组件
     */
    @Query("SELECT cc FROM ContainerComponents cc WHERE cc.container.contract.id = :contractId")
    List<ContainerComponents> findByContractId(@Param("contractId") Long contractId);
    
    /**
     * 根据合同ID删除所有组件
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ContainerComponents cc WHERE cc.container.contract.id = :contractId")
    void deleteByContractId(@Param("contractId") Long contractId);
}
