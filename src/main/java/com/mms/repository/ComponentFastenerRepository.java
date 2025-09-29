package com.mms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 工件装配类型Repository
 * 用于查询工件是产线装配还是仓库装箱紧固件
 */
@Repository
public interface ComponentFastenerRepository extends JpaRepository<com.mms.entity.ComponentFastenerType, Long> {
    
    /**
     * 获取产线装配紧固件列表
     */
    @Query(value = "SELECT component_id FROM components_fastener_assembled", nativeQuery = true)
    List<Long> findAssembledFasteners();
    
    /**
     * 获取仓库装箱紧固件列表
     */
    @Query(value = "SELECT component_id FROM components_fastener_unassembled", nativeQuery = true)
    List<Long> findUnassembledFasteners();
    
    /**
     * 检查指定工件ID是否为产线装配紧固件
     */
    @Query(value = "SELECT COUNT(*) > 0 FROM components_fastener_assembled WHERE component_id = :componentId", nativeQuery = true)
    boolean isAssembledFastener(@Param("componentId") Long componentId);
    
    /**
     * 检查指定工件ID是否为仓库装箱紧固件
     */
    @Query(value = "SELECT COUNT(*) > 0 FROM components_fastener_unassembled WHERE component_id = :componentId", nativeQuery = true)
    boolean isUnassembledFastener(@Param("componentId") Long componentId);
    
    /**
     * 获取工件装配类型
     * @param componentId 工件ID
     * @return 装配类型：null-无装配信息，"产线装配"-产线装配，"仓库装箱"-仓库装箱
     */
    @Query("SELECT CASE " +
           "WHEN EXISTS(SELECT 1 FROM ComponentsSpec cs WHERE cs.component.id = :componentId AND cs.specCode = 'programCode' AND cs.specValue = '产线装配') " +
           "THEN '产线装配' " +
           "WHEN EXISTS(SELECT 1 FROM ComponentsSpec cs WHERE cs.component.id = :componentId AND cs.specCode = 'programCode' AND cs.specValue = '仓库装箱') " +
           "THEN '仓库装箱' " +
           "ELSE null END")
    String getAssemblyType(@Param("componentId") Long componentId);
}
