package com.mms.repository;

import com.mms.entity.FastenerWarehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FastenerWarehouseRepository extends JpaRepository<FastenerWarehouse, Long> {
    
    /**
     * 根据产品代码查找紧固件
     */
    Optional<FastenerWarehouse> findByProductCode(String productCode);
    
    /**
     * 根据产品代码模糊查找紧固件
     */
    List<FastenerWarehouse> findByProductCodeContaining(String productCode);
    
    /**
     * 根据ERP代码查找紧固件
     */
    Optional<FastenerWarehouse> findByErpCode(String erpCode);
    
    /**
     * 根据默认标志查找紧固件
     */
    List<FastenerWarehouse> findByDefaultFlag(Boolean defaultFlag);
    
    /**
     * 根据规格查找紧固件
     */
    List<FastenerWarehouse> findBySpecsContaining(String specs);
    
    /**
     * 根据材料查找紧固件
     */
    List<FastenerWarehouse> findByMaterial(String material);
    
    /**
     * 根据表面处理查找紧固件
     */
    List<FastenerWarehouse> findBySurfaceTreatment(String surfaceTreatment);
    
    /**
     * 根据等级查找紧固件
     */
    List<FastenerWarehouse> findByLevel(String level);
    
    /**
     * 分页查询紧固件（支持多条件搜索）
     */
    @Query("SELECT fw FROM FastenerWarehouse fw WHERE " +
           "(:productCode IS NULL OR fw.productCode LIKE %:productCode%) AND " +
           "(:erpCode IS NULL OR fw.erpCode LIKE %:erpCode%) AND " +
           "(:name IS NULL OR fw.name LIKE %:name%) AND " +
           "(:specs IS NULL OR fw.specs LIKE %:specs%) AND " +
           "(:material IS NULL OR fw.material LIKE %:material%) AND " +
           "(:surfaceTreatment IS NULL OR fw.surfaceTreatment LIKE %:surfaceTreatment%) AND " +
           "(:level IS NULL OR fw.level = :level) AND " +
           "(:defaultFlag IS NULL OR fw.defaultFlag = :defaultFlag)")
    Page<FastenerWarehouse> findByConditions(@Param("productCode") String productCode,
                                              @Param("erpCode") String erpCode,
                                              @Param("name") String name,
                                              @Param("specs") String specs,
                                              @Param("material") String material,
                                              @Param("surfaceTreatment") String surfaceTreatment,
                                              @Param("level") String level,
                                              @Param("defaultFlag") Boolean defaultFlag,
                                              Pageable pageable);
    
    /**
     * 分页搜索紧固件（关键词搜索）
     */
    @Query("SELECT fw FROM FastenerWarehouse fw WHERE " +
           "fw.productCode LIKE %:keyword% OR " +
           "fw.erpCode LIKE %:keyword% OR " +
           "fw.name LIKE %:keyword% OR " +
           "fw.specs LIKE %:keyword% OR " +
           "fw.material LIKE %:keyword% OR " +
           "fw.surfaceTreatment LIKE %:keyword%")
    Page<FastenerWarehouse> findByKeywordContaining(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 获取所有材料（去重）
     */
    @Query("SELECT DISTINCT fw.material FROM FastenerWarehouse fw WHERE fw.material IS NOT NULL ORDER BY fw.material")
    List<String> findDistinctMaterials();
    
    /**
     * 获取所有表面处理（去重）
     */
    @Query("SELECT DISTINCT fw.surfaceTreatment FROM FastenerWarehouse fw WHERE fw.surfaceTreatment IS NOT NULL ORDER BY fw.surfaceTreatment")
    List<String> findDistinctSurfaceTreatments();
    
    /**
     * 获取所有等级（去重）
     */
    @Query("SELECT DISTINCT fw.level FROM FastenerWarehouse fw WHERE fw.level IS NOT NULL ORDER BY fw.level")
    List<String> findDistinctLevels();
}
