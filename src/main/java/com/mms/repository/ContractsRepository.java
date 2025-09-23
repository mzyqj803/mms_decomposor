package com.mms.repository;

import com.mms.entity.Contracts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractsRepository extends JpaRepository<Contracts, Long> {
    
    Optional<Contracts> findByContractNo(String contractNo);
    
    // 按合同号模糊查询
    @Query("SELECT c FROM Contracts c WHERE c.contractNo LIKE %:contractNo%")
    Page<Contracts> findByContractNoContaining(@Param("contractNo") String contractNo, Pageable pageable);
    
    // 按项目名称模糊查询
    @Query("SELECT c FROM Contracts c WHERE c.projectName LIKE %:projectName%")
    Page<Contracts> findByProjectNameContaining(@Param("projectName") String projectName, Pageable pageable);
    
    // 按合同号和项目名称模糊查询
    @Query("SELECT c FROM Contracts c WHERE c.contractNo LIKE %:contractNo% AND c.projectName LIKE %:projectName%")
    Page<Contracts> findByContractNoAndProjectNameContaining(@Param("contractNo") String contractNo, 
                                                            @Param("projectName") String projectName,
                                                            Pageable pageable);
    
    // 按合同号或项目名称模糊查询（用于搜索功能）
    @Query("SELECT c FROM Contracts c WHERE c.contractNo LIKE %:keyword% OR c.projectName LIKE %:keyword%")
    Page<Contracts> findByContractNoOrProjectNameContaining(@Param("keyword") String keyword, Pageable pageable);
    
    // 按状态查询
    List<Contracts> findByStatus(Contracts.ContractStatus status);
    
    // 按状态分页查询
    Page<Contracts> findByStatus(Contracts.ContractStatus status, Pageable pageable);
    
    // 按合同号模糊查询 + 状态
    @Query("SELECT c FROM Contracts c WHERE c.contractNo LIKE %:contractNo% AND c.status = :status")
    Page<Contracts> findByContractNoContainingAndStatus(@Param("contractNo") String contractNo, 
                                                        @Param("status") Contracts.ContractStatus status, 
                                                        Pageable pageable);
    
    // 按项目名称模糊查询 + 状态
    @Query("SELECT c FROM Contracts c WHERE c.projectName LIKE %:projectName% AND c.status = :status")
    Page<Contracts> findByProjectNameContainingAndStatus(@Param("projectName") String projectName, 
                                                         @Param("status") Contracts.ContractStatus status, 
                                                         Pageable pageable);
    
    // 按合同号和项目名称模糊查询 + 状态
    @Query("SELECT c FROM Contracts c WHERE c.contractNo LIKE %:contractNo% AND c.projectName LIKE %:projectName% AND c.status = :status")
    Page<Contracts> findByContractNoAndProjectNameContainingAndStatus(@Param("contractNo") String contractNo, 
                                                                      @Param("projectName") String projectName,
                                                                      @Param("status") Contracts.ContractStatus status,
                                                                      Pageable pageable);
}
