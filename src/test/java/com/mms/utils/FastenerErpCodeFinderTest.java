package com.mms.utils;

import com.mms.dto.FastenerParseResult;
import com.mms.entity.FastenerWarehouse;
import com.mms.repository.ComponentFastenerRepository;
import com.mms.service.FastenerWarehouseCacheService;
import com.mms.utils.FastenerErpCodeFinder.ErpCodeResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 紧固件ERP代码查找工具类测试
 */
@ExtendWith(MockitoExtension.class)
public class FastenerErpCodeFinderTest {
    
    @Mock
    private ComponentFastenerRepository componentFastenerRepository;
    
    @Mock
    private FastenerWarehouseCacheService fastenerWarehouseCacheService;
    
    @Mock
    private FastenerParser fastenerParser;
    
    @InjectMocks
    private FastenerErpCodeFinder finder;
    
    private FastenerWarehouse sampleFastener;
    
    @BeforeEach
    void setUp() {
        // 创建测试用的紧固件数据
        sampleFastener = new FastenerWarehouse();
        sampleFastener.setId(1L);
        sampleFastener.setProductCode("GB5783");
        sampleFastener.setSpecs("M6*20");
        sampleFastener.setLevel("8.8");
        sampleFastener.setSurfaceTreatment("镀锌等");
        sampleFastener.setErpCode("07.0100.00001");
    }
    
    @Test
    void testFindErpCode_NotFastener() {
        // 测试非紧固件的情况
        Long componentId = 1L;
        String componentCode = "FT001";
        String name = "非紧固件";
        
        when(componentFastenerRepository.isAssembledFastener(componentId)).thenReturn(false);
        when(componentFastenerRepository.isUnassembledFastener(componentId)).thenReturn(false);
        
        ErpCodeResult result = finder.findErpCode(componentId, componentCode, name);
        
        assertFalse(result.isSuccess());
        assertEquals("不是紧固件，跳过", result.getErrorMessage());
        assertEquals(componentId, result.getComponentId());
        assertEquals(componentCode, result.getComponentCode());
        assertFalse(result.isFastenerComponent()); // 验证不是紧固件组件
        assertEquals(name, result.getName());
    }
    
    @Test
    void testFindErpCode_ParseError() {
        // 测试解析错误的情况
        Long componentId = 1L;
        String componentCode = "INVALID_CODE";
        String name = "无效代码";
        
        when(componentFastenerRepository.isAssembledFastener(componentId)).thenReturn(true);
        
        FastenerParseResult parseResult = FastenerParseResult.failure(componentCode, name, "解析失败");
        when(fastenerParser.parse(componentCode, name)).thenReturn(parseResult);
        
        ErpCodeResult result = finder.findErpCode(componentId, componentCode, name);
        
        assertFalse(result.isSuccess());
        assertEquals("解析错误: 解析失败", result.getErrorMessage());
    }
    
    @Test
    void testFindErpCode_UnknownMaterial() {
        // 测试未知物料的情况
        Long componentId = 1L;
        String componentCode = "GB9999-M6*20-8.8Z";
        String name = "螺栓";
        
        when(componentFastenerRepository.isAssembledFastener(componentId)).thenReturn(true);
        
        FastenerParseResult parseResult = FastenerParseResult.success(componentCode, name, "GB9999", "M6*20", "8.8", "镀锌等");
        when(fastenerParser.parse(componentCode, name)).thenReturn(parseResult);
        when(fastenerWarehouseCacheService.getFastenersByProductCodeContaining("GB9999")).thenReturn(Collections.emptyList());
        
        ErpCodeResult result = finder.findErpCode(componentId, componentCode, name);
        
        assertFalse(result.isSuccess());
        assertEquals("未知物料: GB9999", result.getErrorMessage());
    }
    
    @Test
    void testFindErpCode_Success_ExactMatch() {
        // 测试完全匹配的情况
        Long componentId = 1L;
        String componentCode = "GB5783-M6*20-8.8Z";
        String name = "螺栓";
        
        when(componentFastenerRepository.isAssembledFastener(componentId)).thenReturn(true);
        
        FastenerParseResult parseResult = FastenerParseResult.success(componentCode, name, "GB5783", "M6*20", "8.8", "镀锌等");
        when(fastenerParser.parse(componentCode, name)).thenReturn(parseResult);
        
        // 模拟缓存查询结果
        List<FastenerWarehouse> productCodeMatches = Arrays.asList(sampleFastener);
        when(fastenerWarehouseCacheService.getFastenersByProductCodeContaining("GB5783")).thenReturn(productCodeMatches);
        
        ErpCodeResult result = finder.findErpCode(componentId, componentCode, name);
        
        assertTrue(result.isSuccess());
        assertEquals("07.0100.00001", result.getErpCode());
        assertEquals("GB5783", result.getMatchedProductCode());
        assertEquals("M6*20", result.getMatchedSpecs());
        assertEquals("8.8", result.getMatchedLevel());
        assertEquals("镀锌等", result.getMatchedSurfaceTreatment());
        assertTrue(result.isFastenerComponent()); // 验证是紧固件组件
    }
    
    @Test
    void testFindErpCode_Success_PartialMatch() {
        // 测试部分匹配的情况（只有productCode匹配）
        Long componentId = 1L;
        String componentCode = "GB5783-M8*25-10.9";
        String name = "螺栓";
        
        when(componentFastenerRepository.isAssembledFastener(componentId)).thenReturn(true);
        
        FastenerParseResult parseResult = FastenerParseResult.success(componentCode, name, "GB5783", "M8*25", "10.9", null);
        when(fastenerParser.parse(componentCode, name)).thenReturn(parseResult);
        
        // 模拟缓存查询结果 - 只有productCode匹配
        List<FastenerWarehouse> productCodeMatches = Arrays.asList(sampleFastener);
        when(fastenerWarehouseCacheService.getFastenersByProductCodeContaining("GB5783")).thenReturn(productCodeMatches);
        
        ErpCodeResult result = finder.findErpCode(componentId, componentCode, name);
        
        assertTrue(result.isSuccess());
        assertEquals("07.0100.00001", result.getErpCode());
        assertEquals("GB5783", result.getMatchedProductCode());
        assertEquals("M8*25", result.getMatchedSpecs());
        assertEquals("10.9", result.getMatchedLevel());
        assertNull(result.getMatchedSurfaceTreatment());
        assertTrue(result.isFastenerComponent()); // 验证是紧固件组件
    }
    
    @Test
    void testFindErpCode_Success_MultipleMatches() {
        // 测试多个匹配的情况
        Long componentId = 1L;
        String componentCode = "GB5783-M6*20-8.8Z";
        String name = "螺栓";
        
        when(componentFastenerRepository.isAssembledFastener(componentId)).thenReturn(true);
        
        FastenerParseResult parseResult = FastenerParseResult.success(componentCode, name, "GB5783", "M6*20", "8.8", "镀锌等");
        when(fastenerParser.parse(componentCode, name)).thenReturn(parseResult);
        
        // 创建多个匹配的紧固件
        FastenerWarehouse fastener1 = new FastenerWarehouse();
        fastener1.setId(1L);
        fastener1.setProductCode("GB5783");
        fastener1.setSpecs("M6*20");
        fastener1.setLevel("8.8");
        fastener1.setSurfaceTreatment("镀锌等");
        fastener1.setErpCode("07.0100.00001");
        
        FastenerWarehouse fastener2 = new FastenerWarehouse();
        fastener2.setId(2L);
        fastener2.setProductCode("GB5783");
        fastener2.setSpecs("M6*20");
        fastener2.setLevel("8.8");
        fastener2.setSurfaceTreatment("镀锌等");
        fastener2.setErpCode("07.0100.00002");
        
        List<FastenerWarehouse> productCodeMatches = Arrays.asList(fastener1, fastener2);
        when(fastenerWarehouseCacheService.getFastenersByProductCodeContaining("GB5783")).thenReturn(productCodeMatches);
        
        ErpCodeResult result = finder.findErpCode(componentId, componentCode, name);
        
        assertTrue(result.isSuccess());
        assertEquals("07.0100.00001", result.getErpCode()); // 应该返回第一个匹配的
        assertTrue(result.isFastenerComponent()); // 验证是紧固件组件
    }
}
