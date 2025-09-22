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
    
    @Query("SELECT c FROM Contracts c WHERE c.contractNo LIKE %:contractNo% OR c.projectName LIKE %:projectName%")
    Page<Contracts> findByContractNoOrProjectNameContaining(@Param("contractNo") String contractNo, 
                                                           @Param("projectName") String projectName,
                                                           Pageable pageable);
    
    @Query("SELECT c FROM Contracts c WHERE c.contractNo LIKE %:keyword% OR c.projectName LIKE %:keyword%")
    Page<Contracts> findByContractNoOrProjectNameContaining(@Param("keyword") String keyword, Pageable pageable);
    
    List<Contracts> findByStatus(Contracts.ContractStatus status);
}
