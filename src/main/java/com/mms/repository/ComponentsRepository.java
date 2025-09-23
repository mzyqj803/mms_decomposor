package com.mms.repository;

import com.mms.entity.Components;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    
    /**
     * 分页查询零部件（支持多条件搜索）
     */
    @Query("SELECT c FROM Components c WHERE " +
           "(:componentCode IS NULL OR c.componentCode LIKE %:componentCode%) AND " +
           "(:name IS NULL OR c.name LIKE %:name%) AND " +
           "(:categoryCode IS NULL OR c.categoryCode = :categoryCode)")
    Page<Components> findByConditions(@Param("componentCode") String componentCode,
                                      @Param("name") String name,
                                      @Param("categoryCode") String categoryCode,
                                      Pageable pageable);
    
    /**
     * 分页搜索零部件（关键词搜索）
     */
    @Query("SELECT c FROM Components c WHERE " +
           "c.componentCode LIKE %:keyword% OR " +
           "c.name LIKE %:keyword% OR " +
           "c.categoryCode LIKE %:keyword%")
    Page<Components> findByKeywordContaining(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 获取所有分类代码（去重）
     */
    @Query("SELECT DISTINCT c.categoryCode FROM Components c WHERE c.categoryCode IS NOT NULL ORDER BY c.categoryCode")
    List<String> findDistinctCategoryCodes();
}
