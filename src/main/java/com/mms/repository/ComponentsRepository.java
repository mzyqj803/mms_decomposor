package com.mms.repository;

import com.mms.entity.Components;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComponentsRepository extends JpaRepository<Components, Long> {
    
    Optional<Components> findByComponentCode(String componentCode);
    
    List<Components> findByCategoryCode(String categoryCode);
    
    @Query("SELECT c FROM Components c WHERE c.componentCode LIKE %:code% OR c.name LIKE %:name%")
    List<Components> findByComponentCodeOrNameContaining(@Param("code") String code, 
                                                        @Param("name") String name);
    
    List<Components> findByProcurementFlag(Boolean procurementFlag);
    
    List<Components> findByCommonPartsFlag(Boolean commonPartsFlag);
}
