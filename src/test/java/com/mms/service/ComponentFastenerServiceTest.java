package com.mms.service;

import com.mms.repository.ComponentFastenerRepository;
import com.mms.service.impl.ComponentFastenerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 工件装配类型服务测试
 */
@ExtendWith(MockitoExtension.class)
class ComponentFastenerServiceTest {
    
    @Mock
    private ComponentFastenerRepository componentFastenerRepository;
    
    @Mock
    private CacheService cacheService;
    
    @InjectMocks
    private ComponentFastenerServiceImpl componentFastenerService;
    
    @Test
    void testGetAssembledFasteners() {
        // 准备测试数据
        List<Long> expectedIds = Arrays.asList(1L, 2L, 3L);
        when(componentFastenerRepository.findAssembledFasteners()).thenReturn(expectedIds);
        when(cacheService.get(anyString(), eq(List.class))).thenReturn(null);
        
        // 执行测试
        List<Long> result = componentFastenerService.getAssembledFasteners();
        
        // 验证结果
        assertEquals(expectedIds, result);
        verify(componentFastenerRepository).findAssembledFasteners();
        verify(cacheService).set(anyString(), eq(expectedIds), eq(10L), eq(java.util.concurrent.TimeUnit.MINUTES));
    }
    
    @Test
    void testGetUnassembledFasteners() {
        // 准备测试数据
        List<Long> expectedIds = Arrays.asList(4L, 5L, 6L);
        when(componentFastenerRepository.findUnassembledFasteners()).thenReturn(expectedIds);
        when(cacheService.get(anyString(), eq(List.class))).thenReturn(null);
        
        // 执行测试
        List<Long> result = componentFastenerService.getUnassembledFasteners();
        
        // 验证结果
        assertEquals(expectedIds, result);
        verify(componentFastenerRepository).findUnassembledFasteners();
        verify(cacheService).set(anyString(), eq(expectedIds), eq(10L), eq(java.util.concurrent.TimeUnit.MINUTES));
    }
    
    @Test
    void testIsAssembledFastener() {
        // 准备测试数据
        Long componentId = 1L;
        when(componentFastenerRepository.isAssembledFastener(componentId)).thenReturn(true);
        when(cacheService.get(anyString(), eq(Boolean.class))).thenReturn(null);
        
        // 执行测试
        boolean result = componentFastenerService.isAssembledFastener(componentId);
        
        // 验证结果
        assertTrue(result);
        verify(componentFastenerRepository).isAssembledFastener(componentId);
    }
    
    @Test
    void testIsUnassembledFastener() {
        // 准备测试数据
        Long componentId = 4L;
        when(componentFastenerRepository.isUnassembledFastener(componentId)).thenReturn(true);
        when(cacheService.get(anyString(), eq(Boolean.class))).thenReturn(null);
        
        // 执行测试
        boolean result = componentFastenerService.isUnassembledFastener(componentId);
        
        // 验证结果
        assertTrue(result);
        verify(componentFastenerRepository).isUnassembledFastener(componentId);
    }
    
    @Test
    void testGetAssemblyType() {
        // 准备测试数据
        Long componentId = 1L;
        String expectedType = "产线装配";
        when(componentFastenerRepository.getAssemblyType(componentId)).thenReturn(expectedType);
        when(cacheService.get(anyString(), eq(String.class))).thenReturn(null);
        
        // 执行测试
        String result = componentFastenerService.getAssemblyType(componentId);
        
        // 验证结果
        assertEquals(expectedType, result);
        verify(componentFastenerRepository).getAssemblyType(componentId);
    }
    
    @Test
    void testGetAssemblyTypeWithCache() {
        // 准备测试数据
        Long componentId = 1L;
        String expectedType = "产线装配";
        when(cacheService.get(anyString(), eq(String.class))).thenReturn(expectedType);
        
        // 执行测试
        String result = componentFastenerService.getAssemblyType(componentId);
        
        // 验证结果
        assertEquals(expectedType, result);
        // 验证没有调用repository，因为使用了缓存
        verify(componentFastenerRepository, never()).getAssemblyType(componentId);
    }
}
