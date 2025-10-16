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
    
    // 查询所有合同（排除已删除）
    @Query("SELECT c FROM Contracts c WHERE c.status != 4")
    Page<Contracts> findAllExcludeDeleted(Pageable pageable);
    
    // 查询所有合同（排除已删除）
    @Query("SELECT c FROM Contracts c WHERE c.status != 4")
    List<Contracts> findAllExcludeDeleted();
    
    // 按合同号模糊查询（排除已删除）
    @Query("SELECT c FROM Contracts c WHERE c.contractNo LIKE %:contractNo% AND c.status != 4")
    Page<Contracts> findByContractNoContaining(@Param("contractNo") String contractNo, Pageable pageable);
    
    // 按项目名称模糊查询（排除已删除）
    @Query("SELECT c FROM Contracts c WHERE c.projectName LIKE %:projectName% AND c.status != 4")
    Page<Contracts> findByProjectNameContaining(@Param("projectName") String projectName, Pageable pageable);
    
    // 按合同号和项目名称模糊查询（排除已删除）
    @Query("SELECT c FROM Contracts c WHERE c.contractNo LIKE %:contractNo% AND c.projectName LIKE %:projectName% AND c.status != 4")
    Page<Contracts> findByContractNoAndProjectNameContaining(@Param("contractNo") String contractNo, 
                                                            @Param("projectName") String projectName,
                                                            Pageable pageable);
    
    // 按合同号或项目名称模糊查询（用于搜索功能，排除已删除）
    @Query("SELECT c FROM Contracts c WHERE (c.contractNo LIKE %:keyword% OR c.projectName LIKE %:keyword%) AND c.status != 4")
    Page<Contracts> findByContractNoOrProjectNameContaining(@Param("keyword") String keyword, Pageable pageable);
    
    // 按状态查询（排除已删除）
    @Query("SELECT c FROM Contracts c WHERE c.status = :status AND c.status != 4")
    List<Contracts> findByStatus(@Param("status") Integer status);
    
    // 按状态分页查询（排除已删除）
    @Query("SELECT c FROM Contracts c WHERE c.status = :status AND c.status != 4")
    Page<Contracts> findByStatus(@Param("status") Integer status, Pageable pageable);
    
    // 按合同号模糊查询 + 状态（排除已删除）
    @Query("SELECT c FROM Contracts c WHERE c.contractNo LIKE %:contractNo% AND c.status = :status AND c.status != 4")
    Page<Contracts> findByContractNoContainingAndStatus(@Param("contractNo") String contractNo, 
                                                        @Param("status") Integer status, 
                                                        Pageable pageable);
    
    // 按项目名称模糊查询 + 状态（排除已删除）
    @Query("SELECT c FROM Contracts c WHERE c.projectName LIKE %:projectName% AND c.status = :status AND c.status != 4")
    Page<Contracts> findByProjectNameContainingAndStatus(@Param("projectName") String projectName, 
                                                         @Param("status") Integer status, 
                                                         Pageable pageable);
    
    // 按合同号和项目名称模糊查询 + 状态（排除已删除）
    @Query("SELECT c FROM Contracts c WHERE c.contractNo LIKE %:contractNo% AND c.projectName LIKE %:projectName% AND c.status = :status AND c.status != 4")
    Page<Contracts> findByContractNoAndProjectNameContainingAndStatus(@Param("contractNo") String contractNo, 
                                                                      @Param("projectName") String projectName,
                                                                      @Param("status") Integer status,
                                                                      Pageable pageable);
}
