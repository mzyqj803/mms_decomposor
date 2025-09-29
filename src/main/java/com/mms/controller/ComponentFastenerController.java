package com.mms.controller;

import com.mms.dto.ComponentFastenerTypeDto;
import com.mms.dto.FastenerListResponseDto;
import com.mms.entity.Components;
import com.mms.service.ComponentFastenerService;
import com.mms.service.ComponentsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 工件装配类型控制器
 * 提供工件的装配类型查询API
 */
@RestController
@RequestMapping("/components/fastener")
@RequiredArgsConstructor
@Slf4j
public class ComponentFastenerController {
    
    private final ComponentFastenerService componentFastenerService;
    private final ComponentsService componentsService;
    
    /**
     * 获取指定工件的装配类型
     * @param componentId 工件ID
     * @return 工件装配类型信息
     */
    @GetMapping("/type/{componentId}")
    public ResponseEntity<ComponentFastenerTypeDto> getComponentAssemblyType(@PathVariable Long componentId) {
        try {
            String assemblyType = componentFastenerService.getAssemblyType(componentId);
            
            Components component = componentsService.getComponentById(componentId);
            
            ComponentFastenerTypeDto.ComponentInfo componentInfo = 
                new ComponentFastenerTypeDto.ComponentInfo(
                    component.getComponentCode(),
                    component.getName(),
                    component.getCategoryCode()
                );
            
            ComponentFastenerTypeDto result = new ComponentFastenerTypeDto(componentId, assemblyType, componentInfo);
            
            log.info("查询工件{}装配类型成功: {}", componentId, assemblyType);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("查询工件{}装配类型失败: {}", componentId, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 获取所有紧固件的装配类型分类
     * @return 产线装配和仓库装箱紧固件列表
     */
    @GetMapping("/types")
    public ResponseEntity<FastenerListResponseDto> getAllFastenerTypes() {
        try {
            // 获取产线装配紧固件列表
            List<Long> assembledIds = componentFastenerService.getAssembledFasteners();
            List<FastenerListResponseDto.ComponentFastenerInfo> assembledFasteners = 
                buildComponentFastenerInfoList(assembledIds, "产线装配");
            
            // 获取仓库装箱紧固件列表
            List<Long> unassembledIds = componentFastenerService.getUnassembledFasteners();
            List<FastenerListResponseDto.ComponentFastenerInfo> unassembledFasteners = 
                buildComponentFastenerInfoList(unassembledIds, "仓库装箱");
            
            FastenerListResponseDto result = new FastenerListResponseDto(
                assembledFasteners,
                unassembledFasteners,
                assembledFasteners.size(),
                unassembledFasteners.size()
            );
            
            log.info("查询所有紧固件装配类型成功 - 产线装配:{}个, 仓库装箱:{}个", 
                assembledFasteners.size(), unassembledFasteners.size());
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("查询所有紧固件装配类型失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 检查工件是否为产线装配紧固件
     * @param componentId 工件ID
     * @return true-是产线装配，false-不是
     */
    @GetMapping("/assembled/{componentId}")
    public ResponseEntity<Boolean> isAssembled(@PathVariable Long componentId) {
        try {
            boolean isAssembled = componentFastenerService.isAssembledFastener(componentId);
            log.info("检查工件{}是否产线装配紧固件: {}", componentId, isAssembled);
            return ResponseEntity.ok(isAssembled);
        } catch (Exception e) {
            log.error("检查工件{}是否产线装配紧固件失败: {}", componentId, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 检查工件是否为仓库装箱紧固件
     * @param componentId 工件ID
     * @return true-是仓库装箱，false-不是
     */
    @GetMapping("/unassembled/{componentId}")
    public ResponseEntity<Boolean> isUnassembled(@PathVariable Long componentId) {
        try {
            boolean isUnassembled = componentFastenerService.isUnassembledFastener(componentId);
            log.info("检查工件{}是否仓库装箱紧固件: {}", componentId, isUnassembled);
            return ResponseEntity.ok(isUnassembled);
        } catch (Exception e) {
            log.error("检查工件{}是否仓库装箱紧固件失败: {}", componentId, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 构建紧固件信息列表
     */
    private List<FastenerListResponseDto.ComponentFastenerInfo> buildComponentFastenerInfoList(
            List<Long> componentIds, String assemblyType) {
        
        List<FastenerListResponseDto.ComponentFastenerInfo> result = new ArrayList<>();
        
        for (Long componentId : componentIds) {
            try {
                Components component = componentsService.getComponentById(componentId);
                FastenerListResponseDto.ComponentFastenerInfo info = 
                    new FastenerListResponseDto.ComponentFastenerInfo(
                        component.getId(),
                        component.getComponentCode(),
                        component.getName(),
                        assemblyType
                    );
                result.add(info);
            } catch (Exception e) {
                log.warn("构建工件{}信息失败: {}", componentId, e.getMessage());
            }
        }
        
        return result;
    }
}
