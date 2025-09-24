package com.mms.repository;

import com.mms.entity.Containers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ContainersRepository extends JpaRepository<Containers, Long> {
    
    /**
     * 根据合同ID查找所有装箱单
     */
    @Query("SELECT c FROM Containers c WHERE c.contract.id = :contractId")
    List<Containers> findByContractId(@Param("contractId") Long contractId);
    
    /**
     * 根据合同ID和装箱单号查找装箱单
     */
    @Query("SELECT c FROM Containers c WHERE c.contract.id = :contractId AND c.containerNo = :containerNo")
    Containers findByContractIdAndContainerNo(@Param("contractId") Long contractId, @Param("containerNo") String containerNo);
    
    /**
     * 分页查询装箱单（支持多条件搜索）
     */
    @Query("SELECT c FROM Containers c LEFT JOIN FETCH c.contract WHERE " +
           "(:containerNo IS NULL OR c.containerNo LIKE %:containerNo%) AND " +
           "(:contractNo IS NULL OR c.contract.contractNo LIKE %:contractNo%) AND " +
           "(:projectName IS NULL OR c.contract.projectName LIKE %:projectName%)")
    Page<Containers> findByConditions(@Param("containerNo") String containerNo,
                                      @Param("contractNo") String contractNo,
                                      @Param("projectName") String projectName,
                                      Pageable pageable);
    
    /**
     * 根据合同ID删除所有装箱单
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Containers c WHERE c.contract.id = :contractId")
    void deleteByContractId(@Param("contractId") Long contractId);
}
