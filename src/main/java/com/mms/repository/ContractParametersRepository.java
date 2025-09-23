package com.mms.repository;

import com.mms.entity.ContractParameters;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractParametersRepository extends JpaRepository<ContractParameters, Long> {
    
    // 根据合同ID查找所有参数
    @Query("SELECT cp FROM ContractParameters cp WHERE cp.contract.id = :contractId")
    List<ContractParameters> findByContractId(@Param("contractId") Long contractId);
    
    // 根据合同ID删除所有参数
    @Modifying
    @Query("DELETE FROM ContractParameters cp WHERE cp.contract.id = :contractId")
    void deleteByContractId(@Param("contractId") Long contractId);
    
    // 根据合同ID和参数名查找参数
    @Query("SELECT cp FROM ContractParameters cp WHERE cp.contract.id = :contractId AND cp.paramName = :paramName")
    ContractParameters findByContractIdAndParamName(@Param("contractId") Long contractId, @Param("paramName") String paramName);
    
    // 检查合同是否存在指定名称的参数
    @Query("SELECT COUNT(cp) > 0 FROM ContractParameters cp WHERE cp.contract.id = :contractId AND cp.paramName = :paramName")
    boolean existsByContractIdAndParamName(@Param("contractId") Long contractId, @Param("paramName") String paramName);
}
