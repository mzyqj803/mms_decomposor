package com.mms.repository;

import com.mms.entity.Containers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
