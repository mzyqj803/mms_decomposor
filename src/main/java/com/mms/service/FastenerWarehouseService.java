package com.mms.service;

import com.mms.entity.FastenerWarehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FastenerWarehouseService {
    
    /**
     * 获取紧固件列表（支持分页和多条件搜索）
     */
    Page<FastenerWarehouse> getFasteners(String productCode, String erpCode, String name, 
                                        String specs, String material, String surfaceTreatment, 
                                        String level, Boolean defaultFlag, Pageable pageable);
    
    /**
     * 根据ID获取紧固件
     */
    FastenerWarehouse getFastenerById(Long id);
    
    /**
     * 创建紧固件
     */
    FastenerWarehouse createFastener(FastenerWarehouse fastener);
    
    /**
     * 更新紧固件
     */
    FastenerWarehouse updateFastener(Long id, FastenerWarehouse fastener);
    
    /**
     * 删除紧固件
     */
    void deleteFastener(Long id);
    
    /**
     * 搜索紧固件
     */
    Page<FastenerWarehouse> searchFasteners(String keyword, Pageable pageable);
    
    /**
     * 根据产品代码获取紧固件
     */
    FastenerWarehouse getFastenerByProductCode(String productCode);
    
    /**
     * 根据ERP代码获取紧固件
     */
    FastenerWarehouse getFastenerByErpCode(String erpCode);
    
    /**
     * 获取默认紧固件列表
     */
    List<FastenerWarehouse> getDefaultFasteners();
    
    /**
     * 设置默认紧固件
     */
    FastenerWarehouse setDefaultFastener(Long id);
    
    /**
     * 获取所有材料列表
     */
    List<String> getMaterials();
    
    /**
     * 获取所有表面处理列表
     */
    List<String> getSurfaceTreatments();
    
    /**
     * 获取所有等级列表
     */
    List<String> getLevels();
    
    /**
     * 获取所有紧固件（用于索引构建）
     */
    List<FastenerWarehouse> getAllFasteners();
}
