package com.mms.controller;

import com.mms.entity.FastenerWarehouse;
import com.mms.service.FastenerWarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/fastener-warehouse")
@RequiredArgsConstructor
public class FastenerWarehouseController {
    
    private final FastenerWarehouseService fastenerService;
    
    /**
     * 获取紧固件列表（支持分页和多条件搜索）
     */
    @GetMapping
    public ResponseEntity<Page<FastenerWarehouse>> getFasteners(
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) String erpCode,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String specs,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String surfaceTreatment,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) Boolean defaultFlag,
            Pageable pageable) {
        
        Page<FastenerWarehouse> fasteners = fastenerService.getFasteners(
            productCode, erpCode, name, specs, material, surfaceTreatment, level, defaultFlag, pageable);
        return ResponseEntity.ok(fasteners);
    }
    
    /**
     * 获取紧固件详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<FastenerWarehouse> getFastener(@PathVariable Long id) {
        FastenerWarehouse fastener = fastenerService.getFastenerById(id);
        return ResponseEntity.ok(fastener);
    }
    
    /**
     * 创建紧固件
     */
    @PostMapping
    public ResponseEntity<FastenerWarehouse> createFastener(@Valid @RequestBody FastenerWarehouse fastener) {
        FastenerWarehouse createdFastener = fastenerService.createFastener(fastener);
        return ResponseEntity.ok(createdFastener);
    }
    
    /**
     * 更新紧固件
     */
    @PutMapping("/{id}")
    public ResponseEntity<FastenerWarehouse> updateFastener(@PathVariable Long id, @Valid @RequestBody FastenerWarehouse fastener) {
        FastenerWarehouse updatedFastener = fastenerService.updateFastener(id, fastener);
        return ResponseEntity.ok(updatedFastener);
    }
    
    /**
     * 删除紧固件
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFastener(@PathVariable Long id) {
        fastenerService.deleteFastener(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 搜索紧固件
     */
    @GetMapping("/search")
    public ResponseEntity<Page<FastenerWarehouse>> searchFasteners(
            @RequestParam String keyword,
            Pageable pageable) {
        
        Page<FastenerWarehouse> fasteners = fastenerService.searchFasteners(keyword, pageable);
        return ResponseEntity.ok(fasteners);
    }
    
    /**
     * 根据产品代码获取紧固件
     */
    @GetMapping("/product-code/{productCode}")
    public ResponseEntity<FastenerWarehouse> getFastenerByProductCode(@PathVariable String productCode) {
        FastenerWarehouse fastener = fastenerService.getFastenerByProductCode(productCode);
        return ResponseEntity.ok(fastener);
    }
    
    /**
     * 根据ERP代码获取紧固件
     */
    @GetMapping("/erp-code/{erpCode}")
    public ResponseEntity<FastenerWarehouse> getFastenerByErpCode(@PathVariable String erpCode) {
        FastenerWarehouse fastener = fastenerService.getFastenerByErpCode(erpCode);
        return ResponseEntity.ok(fastener);
    }
    
    /**
     * 获取默认紧固件列表
     */
    @GetMapping("/default")
    public ResponseEntity<List<FastenerWarehouse>> getDefaultFasteners() {
        List<FastenerWarehouse> fasteners = fastenerService.getDefaultFasteners();
        return ResponseEntity.ok(fasteners);
    }
    
    /**
     * 设置默认紧固件
     */
    @PutMapping("/{id}/default")
    public ResponseEntity<FastenerWarehouse> setDefaultFastener(@PathVariable Long id) {
        FastenerWarehouse fastener = fastenerService.setDefaultFastener(id);
        return ResponseEntity.ok(fastener);
    }
    
    /**
     * 获取所有材料列表
     */
    @GetMapping("/materials")
    public ResponseEntity<List<String>> getMaterials() {
        List<String> materials = fastenerService.getMaterials();
        return ResponseEntity.ok(materials);
    }
    
    /**
     * 获取所有表面处理列表
     */
    @GetMapping("/surface-treatments")
    public ResponseEntity<List<String>> getSurfaceTreatments() {
        List<String> treatments = fastenerService.getSurfaceTreatments();
        return ResponseEntity.ok(treatments);
    }
    
    /**
     * 获取所有等级列表
     */
    @GetMapping("/levels")
    public ResponseEntity<List<String>> getLevels() {
        List<String> levels = fastenerService.getLevels();
        return ResponseEntity.ok(levels);
    }
}
