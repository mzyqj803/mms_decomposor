package com.mms.repository;

import com.mms.entity.ComponentsSpec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComponentsSpecRepository extends JpaRepository<ComponentsSpec, Long> {
    
    /**
     * 根据组件ID查找规格
     */
    List<ComponentsSpec> findByComponentId(Long componentId);
    
    /**
     * 根据组件ID删除规格
     */
    void deleteByComponentId(Long componentId);
}

