package com.mms.service;

import com.mms.entity.Containers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
     * 删除装箱单
     */
    void deleteContainer(Long id);
}
