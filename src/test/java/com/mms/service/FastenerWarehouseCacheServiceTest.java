package com.mms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mms.entity.FastenerWarehouse;
import com.mms.repository.FastenerWarehouseRepository;
import com.mms.service.impl.FastenerWarehouseCacheServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 紧固件仓库缓存服务测试
 */
@ExtendWith(MockitoExtension.class)
public class FastenerWarehouseCacheServiceTest {
    
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    
    @Mock
    private FastenerWarehouseRepository fastenerWarehouseRepository;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @Mock
    private ValueOperations<String, Object> valueOperations;
    
    @InjectMocks
    private FastenerWarehouseCacheServiceImpl cacheService;
    
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
    void testRedisUnavailable_FallbackToDatabase() {
        // 测试Redis不可用时的降级处理
        String productCode = "GB5783";
        
        // 设置Redis模板的模拟行为
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        // 模拟Redis不可用
        when(valueOperations.get("test:connection")).thenThrow(new RuntimeException("Redis connection failed"));
        
        // 模拟数据库查询
        when(fastenerWarehouseRepository.findByProductCodeContaining(productCode))
            .thenReturn(Arrays.asList(sampleFastener));
        
        List<FastenerWarehouse> result = cacheService.getFastenersByProductCode(productCode);
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("GB5783", result.get(0).getProductCode());
    }
    
    @Test
    void testIsRedisAvailable() {
        // 测试Redis可用性检查
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("test:connection")).thenReturn(null);
        
        boolean isAvailable = cacheService.isRedisAvailable();
        
        assertTrue(isAvailable);
    }
    
    @Test
    void testIsRedisUnavailable() {
        // 测试Redis不可用检查
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("test:connection")).thenThrow(new RuntimeException("Connection failed"));
        
        boolean isAvailable = cacheService.isRedisAvailable();
        
        assertFalse(isAvailable);
    }
    
    @Test
    void testEmptyProductCode() {
        // 测试空产品代码
        List<FastenerWarehouse> result1 = cacheService.getFastenersByProductCode("");
        List<FastenerWarehouse> result2 = cacheService.getFastenersByProductCode(null);
        List<FastenerWarehouse> result3 = cacheService.getFastenersByProductCodeContaining("");
        List<FastenerWarehouse> result4 = cacheService.getFastenersByProductCodeContaining(null);
        
        assertTrue(result1.isEmpty());
        assertTrue(result2.isEmpty());
        assertTrue(result3.isEmpty());
        assertTrue(result4.isEmpty());
    }
    
    @Test
    void testGetCacheSize_RedisUnavailable() {
        // 测试Redis不可用时获取缓存大小
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("test:connection")).thenThrow(new RuntimeException("Connection failed"));
        
        long cacheSize = cacheService.getCacheSize();
        
        assertEquals(0, cacheSize);
    }
}