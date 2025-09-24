package com.mms.service.impl;

import com.mms.entity.Containers;
import com.mms.repository.ContainersRepository;
import com.mms.service.ContainersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContainersServiceImpl implements ContainersService {
    
    private final ContainersRepository containersRepository;
    
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
    @Transactional
    public void deleteContainer(Long id) {
        Containers container = getContainerById(id);
        containersRepository.delete(container);
        log.info("删除装箱单: ID={}, 装箱单号={}", id, container.getContainerNo());
    }
}
