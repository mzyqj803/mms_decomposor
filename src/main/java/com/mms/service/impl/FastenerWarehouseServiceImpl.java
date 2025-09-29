package com.mms.service.impl;

import com.mms.entity.FastenerWarehouse;
import com.mms.repository.FastenerWarehouseRepository;
import com.mms.service.CacheService;
import com.mms.service.FastenerWarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unchecked")
public class FastenerWarehouseServiceImpl implements FastenerWarehouseService {
    
    private final FastenerWarehouseRepository fastenerRepository;
    private final CacheService cacheService;
    
    @Override
    public Page<FastenerWarehouse> getFasteners(String productCode, String erpCode, String name, 
                                                String specs, String material, String surfaceTreatment, 
                                                String level, Boolean defaultFlag, Pageable pageable) {
        String cacheKey = String.format("fastener_warehouse:%s:%s:%s:%s:%s:%s:%s:%s:%d:%d", 
            productCode, erpCode, name, specs, material, surfaceTreatment, level, defaultFlag,
            pageable.getPageNumber(), pageable.getPageSize());
        
        Page<FastenerWarehouse> cachedResult = (Page<FastenerWarehouse>) cacheService.get(cacheKey, Page.class);
        if (cachedResult != null) {
            return cachedResult;
        }
        
        Page<FastenerWarehouse> fasteners = fastenerRepository.findByConditions(
            productCode, erpCode, name, specs, material, surfaceTreatment, level, defaultFlag, pageable);
        
        // 缓存5分钟
        cacheService.set(cacheKey, fasteners, 5, TimeUnit.MINUTES);
        
        return fasteners;
    }
    
    @Override
    public FastenerWarehouse getFastenerById(Long id) {
        String cacheKey = "fastener_warehouse:" + id;
        
        FastenerWarehouse cachedFastener = cacheService.get(cacheKey, FastenerWarehouse.class);
        if (cachedFastener != null) {
            return cachedFastener;
        }
        
        FastenerWarehouse fastener = fastenerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("紧固件不存在"));
        
        // 缓存10分钟
        cacheService.set(cacheKey, fastener, 10, TimeUnit.MINUTES);
        
        return fastener;
    }
    
    @Override
    @Transactional
    public FastenerWarehouse createFastener(FastenerWarehouse fastener) {
        // 检查产品代码是否已存在
        if (fastener.getProductCode() != null && 
            fastenerRepository.findByProductCode(fastener.getProductCode()).isPresent()) {
            throw new RuntimeException("产品代码已存在");
        }
        
        // 检查ERP代码是否已存在
        if (fastener.getErpCode() != null && 
            fastenerRepository.findByErpCode(fastener.getErpCode()).isPresent()) {
            throw new RuntimeException("ERP代码已存在");
        }
        
        FastenerWarehouse savedFastener = fastenerRepository.save(fastener);
        
        // 清除相关缓存
        clearFastenersCache();
        
        log.info("创建紧固件成功: {}", savedFastener.getProductCode());
        return savedFastener;
    }
    
    @Override
    @Transactional
    public FastenerWarehouse updateFastener(Long id, FastenerWarehouse fastener) {
        FastenerWarehouse existingFastener = getFastenerById(id);
        
        // 检查产品代码是否被其他记录使用
        if (fastener.getProductCode() != null && 
            !fastener.getProductCode().equals(existingFastener.getProductCode())) {
            if (fastenerRepository.findByProductCode(fastener.getProductCode()).isPresent()) {
                throw new RuntimeException("产品代码已存在");
            }
        }
        
        // 检查ERP代码是否被其他记录使用
        if (fastener.getErpCode() != null && 
            !fastener.getErpCode().equals(existingFastener.getErpCode())) {
            if (fastenerRepository.findByErpCode(fastener.getErpCode()).isPresent()) {
                throw new RuntimeException("ERP代码已存在");
            }
        }
        
        existingFastener.setSpecs(fastener.getSpecs());
        existingFastener.setProductCode(fastener.getProductCode());
        existingFastener.setErpCode(fastener.getErpCode());
        existingFastener.setName(fastener.getName());
        existingFastener.setLevel(fastener.getLevel());
        existingFastener.setMaterial(fastener.getMaterial());
        existingFastener.setSurfaceTreatment(fastener.getSurfaceTreatment());
        existingFastener.setRemark(fastener.getRemark());
        existingFastener.setDefaultFlag(fastener.getDefaultFlag());
        
        FastenerWarehouse updatedFastener = fastenerRepository.save(existingFastener);
        
        // 清除相关缓存
        clearFastenerCache(id);
        clearFastenersCache();
        
        log.info("更新紧固件成功: {}", updatedFastener.getProductCode());
        return updatedFastener;
    }
    
    @Override
    @Transactional
    public void deleteFastener(Long id) {
        FastenerWarehouse fastener = getFastenerById(id);
        
        fastenerRepository.deleteById(id);
        
        // 清除相关缓存
        clearFastenerCache(id);
        clearFastenersCache();
        
        log.info("删除紧固件成功: {}", fastener.getProductCode());
    }
    
    @Override
    public Page<FastenerWarehouse> searchFasteners(String keyword, Pageable pageable) {
        String cacheKey = String.format("fastener_warehouse:search:%s:%d:%d", 
            keyword, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<FastenerWarehouse> cachedResult = (Page<FastenerWarehouse>) cacheService.get(cacheKey, Page.class);
        if (cachedResult != null) {
            return cachedResult;
        }
        
        Page<FastenerWarehouse> fasteners = fastenerRepository.findByKeywordContaining(keyword, pageable);
        
        // 缓存3分钟
        cacheService.set(cacheKey, fasteners, 3, TimeUnit.MINUTES);
        
        return fasteners;
    }
    
    @Override
    public FastenerWarehouse getFastenerByProductCode(String productCode) {
        String cacheKey = "fastener_warehouse:product_code:" + productCode;
        
        FastenerWarehouse cachedFastener = cacheService.get(cacheKey, FastenerWarehouse.class);
        if (cachedFastener != null) {
            return cachedFastener;
        }
        
        FastenerWarehouse fastener = fastenerRepository.findByProductCode(productCode)
            .orElseThrow(() -> new RuntimeException("紧固件不存在"));
        
        // 缓存10分钟
        cacheService.set(cacheKey, fastener, 10, TimeUnit.MINUTES);
        
        return fastener;
    }
    
    @Override
    public FastenerWarehouse getFastenerByErpCode(String erpCode) {
        String cacheKey = "fastener_warehouse:erp_code:" + erpCode;
        
        FastenerWarehouse cachedFastener = cacheService.get(cacheKey, FastenerWarehouse.class);
        if (cachedFastener != null) {
            return cachedFastener;
        }
        
        FastenerWarehouse fastener = fastenerRepository.findByErpCode(erpCode)
            .orElseThrow(() -> new RuntimeException("紧固件不存在"));
        
        // 缓存10分钟
        cacheService.set(cacheKey, fastener, 10, TimeUnit.MINUTES);
        
        return fastener;
    }
    
    @Override
    public List<FastenerWarehouse> getDefaultFasteners() {
        String cacheKey = "fastener_warehouse:default";
        
        List<FastenerWarehouse> cachedFasteners = (List<FastenerWarehouse>) cacheService.get(cacheKey, List.class);
        if (cachedFasteners != null) {
            return cachedFasteners;
        }
        
        List<FastenerWarehouse> fasteners = fastenerRepository.findByDefaultFlag(true);
        
        // 缓存15分钟
        cacheService.set(cacheKey, fasteners, 15, TimeUnit.MINUTES);
        
        return fasteners;
    }
    
    @Override
    @Transactional
    public FastenerWarehouse setDefaultFastener(Long id) {
        FastenerWarehouse fastener = getFastenerById(id);
        
        // 如果设置为默认，先清除其他默认紧固件
        if (fastener.getDefaultFlag() == null || !fastener.getDefaultFlag()) {
            List<FastenerWarehouse> existingDefaults = fastenerRepository.findByDefaultFlag(true);
            
            for (FastenerWarehouse existingDefault : existingDefaults) {
                existingDefault.setDefaultFlag(false);
                fastenerRepository.save(existingDefault);
            }
            
            fastener.setDefaultFlag(true);
            fastener = fastenerRepository.save(fastener);
        }
        
        // 清除相关缓存
        clearFastenerCache(id);
        clearFastenersCache();
        
        log.info("设置默认紧固件成功: {}", fastener.getProductCode());
        return fastener;
    }
    
    @Override
    public List<String> getMaterials() {
        String cacheKey = "fastener_warehouse:materials";
        
        List<String> cachedMaterials = (List<String>) cacheService.get(cacheKey, List.class);
        if (cachedMaterials != null) {
            return cachedMaterials;
        }
        
        List<String> materials = fastenerRepository.findDistinctMaterials();
        
        // 缓存30分钟
        cacheService.set(cacheKey, materials, 30, TimeUnit.MINUTES);
        
        return materials;
    }
    
    @Override
    public List<String> getSurfaceTreatments() {
        String cacheKey = "fastener_warehouse:surface_treatments";
        
        List<String> cachedTreatments = (List<String>) cacheService.get(cacheKey, List.class);
        if (cachedTreatments != null) {
            return cachedTreatments;
        }
        
        List<String> treatments = fastenerRepository.findDistinctSurfaceTreatments();
        
        // 缓存30分钟
        cacheService.set(cacheKey, treatments, 30, TimeUnit.MINUTES);
        
        return treatments;
    }
    
    @Override
    public List<String> getLevels() {
        String cacheKey = "fastener_warehouse:levels";
        
        List<String> cachedLevels = (List<String>) cacheService.get(cacheKey, List.class);
        if (cachedLevels != null) {
            return cachedLevels;
        }
        
        List<String> levels = fastenerRepository.findDistinctLevels();
        
        // 缓存30分钟
        cacheService.set(cacheKey, levels, 30, TimeUnit.MINUTES);
        
        return levels;
    }
    
    @Override
    public List<FastenerWarehouse> getAllFasteners() {
        // 直接从数据库获取，不使用缓存以避免类型转换问题
        return fastenerRepository.findAll();
    }
    
    private void clearFastenerCache(Long fastenerId) {
        cacheService.delete("fastener_warehouse:" + fastenerId);
    }
    
    private void clearFastenersCache() {
        // 清除所有紧固件列表相关的缓存
        try {
            // 清除所有以 "fastener_warehouse:" 开头的缓存键
            cacheService.deletePattern("fastener_warehouse:*");
            log.info("已清除紧固件库相关缓存");
        } catch (Exception e) {
            log.warn("清除紧固件库缓存失败: {}", e.getMessage());
        }
    }
}
