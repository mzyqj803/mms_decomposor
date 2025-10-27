package com.mms.repository;

import com.mms.entity.ComponentsRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComponentsRelationshipRepository extends JpaRepository<ComponentsRelationship, Long> {
    
    /**
     * 根据父组件ID查找所有子组件关系（只返回 active 的组件）
     */
    @Query("SELECT cr FROM ComponentsRelationship cr JOIN FETCH cr.child c WHERE cr.parent.id = :parentId AND cr.parent.status = 1 AND c.status = 1")
    List<ComponentsRelationship> findByParentId(@Param("parentId") Long parentId);
    
    /**
     * 根据子组件ID查找所有父组件关系（只返回 active 的组件）
     */
    @Query("SELECT cr FROM ComponentsRelationship cr WHERE cr.child.id = :childId AND cr.parent.status = 1 AND cr.child.status = 1")
    List<ComponentsRelationship> findByChildId(@Param("childId") Long childId);
    
    /**
     * 根据父组件编号查找所有子组件关系
     */
    @Query("SELECT cr FROM ComponentsRelationship cr WHERE cr.parent.componentCode = :parentCode")
    List<ComponentsRelationship> findByParentComponentCode(@Param("parentCode") String parentCode);
    
    /**
     * 根据子组件编号查找所有父组件关系
     */
    @Query("SELECT cr FROM ComponentsRelationship cr WHERE cr.child.componentCode = :childCode")
    List<ComponentsRelationship> findByChildComponentCode(@Param("childCode") String childCode);
    
    /**
     * 根据子组件ID删除关系
     */
    void deleteByChildId(Long childId);
    
    /**
     * 根据父组件ID删除关系
     */
    void deleteByParentId(Long parentId);
}
