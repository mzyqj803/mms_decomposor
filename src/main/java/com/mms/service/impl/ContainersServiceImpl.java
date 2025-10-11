package com.mms.service.impl;

import com.mms.entity.Containers;
import com.mms.entity.ContainerComponents;
import com.mms.repository.ContainersRepository;
import com.mms.repository.ContainerComponentsRepository;
import com.mms.service.ContainersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContainersServiceImpl implements ContainersService {
    
    private final ContainersRepository containersRepository;
    private final ContainerComponentsRepository containerComponentsRepository;
    
    @Override
    public Page<Containers> getContainers(String containerNo, String contractNo, String projectName, Pageable pageable) {
        return containersRepository.findByConditions(containerNo, contractNo, projectName, pageable);
    }
    
    @Override
    public Containers getContainerById(Long id) {
        return containersRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("装箱单不存在"));
    }
    
    @Override
    public List<Map<String, Object>> getContainerComponents(Long containerId) {
        // 验证装箱单是否存在
        getContainerById(containerId);
        
        // 获取装箱单内的组件
        List<ContainerComponents> components = containerComponentsRepository.findByContainerId(containerId);
        
        // 转换为Map格式返回
        return components.stream().map(component -> {
            Map<String, Object> componentMap = new HashMap<>();
            componentMap.put("id", component.getId());
            componentMap.put("componentNo", component.getComponentNo());
            componentMap.put("componentName", component.getComponentName());
            componentMap.put("unitCode", component.getUnitCode());
            componentMap.put("quantity", component.getQuantity());
            componentMap.put("comments", component.getComments());
            return componentMap;
        }).toList();
    }
    
    @Override
    @Transactional
    public void deleteContainer(Long id) {
        Containers container = getContainerById(id);
        containersRepository.delete(container);
        log.info("删除装箱单: ID={}, 装箱单号={}", id, container.getContainerNo());
    }
}
