package com.mms.service;

import com.mms.entity.Containers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ContainersService {
    
    /**
     * 获取装箱单列表（支持搜索和分页）
     */
    Page<Containers> getContainers(String containerNo, String contractNo, String projectName, Pageable pageable);
    
    /**
     * 根据ID获取装箱单
     */
    Containers getContainerById(Long id);
    
    /**
     * 获取装箱单内的组件列表
     */
    List<Map<String, Object>> getContainerComponents(Long containerId);
    
    /**
     * 更新装箱单内的组件
     */
    void updateContainerComponent(Long containerId, Long componentId, Map<String, Object> componentData);
    
    /**
     * 删除装箱单内的组件
     */
    void deleteContainerComponent(Long containerId, Long componentId);
    
    /**
     * 删除装箱单
     */
    void deleteContainer(Long id);
}
