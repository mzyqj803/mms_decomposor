package com.mms.service.impl;

import com.mms.repository.ComponentFastenerRepository;
import com.mms.service.CacheService;
import com.mms.service.ComponentFastenerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 工件装配类型服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ComponentFastenerServiceImpl implements ComponentFastenerService {
    
    private final ComponentFastenerRepository componentFastenerRepository;
    private final CacheService cacheService;
    
    @Override
    @SuppressWarnings("unchecked")
    public List<Long> getAssembledFasteners() {
        String cacheKey = "fastener:assembled:list";
        
        List<Long> cachedResult = cacheService.get(cacheKey, List.class);
        if (cachedResult != null) {
            log.debug("从缓存返回产线装配紧固件列表，共{}个", cachedResult.size());
            return cachedResult;
        }
        
        List<Long> assembledFasteners = componentFastenerRepository.findAssembledFasteners();
        
        // 缓存10分钟
        cacheService.set(cacheKey, assembledFasteners, 10, TimeUnit.MINUTES);
        
        log.info("查询产线装配紧固件列表成功，共{}个", assembledFasteners.size());
        return assembledFasteners;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<Long> getUnassembledFasteners() {
        String cacheKey = "fastener:unassembled:list";
        
        List<Long> cachedResult = cacheService.get(cacheKey, List.class);
        if (cachedResult != null) {
            log.debug("从缓存返回仓库装箱紧固件列表，共{}个", cachedResult.size());
            return cachedResult;
        }
        
        List<Long> unassembledFasteners = componentFastenerRepository.findUnassembledFasteners();
        
        // 缓存10分钟
        cacheService.set(cacheKey, unassembledFasteners, 10, TimeUnit.MINUTES);
        
        log.info("查询仓库装箱紧固件列表成功，共{}个", unassembledFasteners.size());
        return unassembledFasteners;
    }
    
    @Override
    public boolean isAssembledFastener(Long componentId) {
        String cacheKey = "fastener:assembled:" + componentId;
        
        Boolean cachedResult = cacheService.get(cacheKey, Boolean.class);
        if (cachedResult != null) {
            log.debug("从缓存返回工件{}是否产线装配紧固件: {}", componentId, cachedResult);
            return cachedResult;
        }
        
        boolean isAssembled = componentFastenerRepository.isAssembledFastener(componentId);
        
        // 缓存15分钟
        cacheService.set(cacheKey, isAssembled, 15, TimeUnit.MINUTES);
        
        log.debug("查询工件{}是否产线装配紧固件: {}", componentId, isAssembled);
        return isAssembled;
    }
    
    @Override
    public boolean isUnassembledFastener(Long componentId) {
        String cacheKey = "fastener:unassembled:" + componentId;
        
        Boolean cachedResult = cacheService.get(cacheKey, Boolean.class);
        if (cachedResult != null) {
            log.debug("从缓存返回工件{}是否仓库装箱紧固件: {}", componentId, cachedResult);
            return cachedResult;
        }
        
        boolean isUnassembled = componentFastenerRepository.isUnassembledFastener(componentId);
        
        // 缓存15分钟
        cacheService.set(cacheKey, isUnassembled, 15, TimeUnit.MINUTES);
        
        log.debug("查询工件{}是否仓库装箱紧固件: {}", componentId, isUnassembled);
        return isUnassembled;
    }
    
    @Override
    public String getAssemblyType(Long componentId) {
        String cacheKey = "fastener:type:" + componentId;
        
        String cachedType = cacheService.get(cacheKey, String.class);
        if (cachedType != null) {
            log.debug("从缓存返回工件{}装配类型: {}", componentId, cachedType);
            return cachedType;
        }
        
        String assemblyType = componentFastenerRepository.getAssemblyType(componentId);
        
        // 缓存15分钟
        cacheService.set(cacheKey, assemblyType, 15, TimeUnit.MINUTES);
        
        log.debug("查询工件{}装配类型: {}", componentId, assemblyType);
        return assemblyType;
    }
}
