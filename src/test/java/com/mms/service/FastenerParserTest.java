package com.mms.service;

import com.mms.dto.FastenerParseResult;
import com.mms.utils.FastenerParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 紧固件解析器单元测试
 */
@SpringBootTest
public class FastenerParserTest {
    
    private FastenerParser parser;
    
    @BeforeEach
    void setUp() {
        parser = new FastenerParser();
    }
    
    @Test
    void testParseStandardFormat() {
        // 测试标准格式：GB5783-M6*20-8.8Z
        FastenerParseResult result = parser.parse("GB5783-M6*20-8.8Z", "螺栓");
        
        assertTrue(result.isSuccess());
        assertEquals("GB5783", result.getProductCode());
        assertEquals("M6*20", result.getSpecs());
        assertEquals("8.8", result.getLevel());
        assertEquals("镀锌等", result.getSurfaceTreatment());
    }
    
    @Test
    void testParseGBTSlashFormat() {
        // 测试GB/T斜杠格式：GB/T5783/M12*45/8.8
        FastenerParseResult result = parser.parse("GB/T5783/M12*45/8.8", "螺栓M12*45");
        
        assertTrue(result.isSuccess());
        assertEquals("GB5783", result.getProductCode());
        assertEquals("M12*45", result.getSpecs());
        assertEquals("8.8", result.getLevel());
        assertNull(result.getSurfaceTreatment());
    }
    
    @Test
    void testParseGBTSpaceFormat() {
        // 测试GB/T空格格式：GB/T 5783 M12*45
        FastenerParseResult result = parser.parse("GB/T 5783 M12*45", "六角螺栓");
        
        assertTrue(result.isSuccess());
        assertEquals("GB5783", result.getProductCode());
        assertEquals("M12*45", result.getSpecs());
        assertNull(result.getLevel()); // 这个格式没有明确的等级
        assertNull(result.getSurfaceTreatment());
    }
    
    @Test
    void testParseWithHVLevel() {
        // 测试HV等级格式：GB97.1-M6-140HV-Z
        FastenerParseResult result = parser.parse("GB97.1-M6-140HV-Z", "平垫圈");
        
        assertTrue(result.isSuccess());
        assertEquals("GB97.1", result.getProductCode());
        assertEquals("M6", result.getSpecs());
        assertEquals("140HV", result.getLevel());
        assertEquals("镀锌等", result.getSurfaceTreatment());
    }
    
    @Test
    void testParseWithCrossSymbol() {
        // 测试×符号格式：GB/T15389-M24×170-8.8Z
        FastenerParseResult result = parser.parse("GB/T15389-M24×170-8.8Z", "螺杆");
        
        assertTrue(result.isSuccess());
        assertEquals("GB15389", result.getProductCode());
        assertEquals("M24×170", result.getSpecs());
        assertEquals("8.8", result.getLevel());
        assertEquals("镀锌等", result.getSurfaceTreatment());
    }
    
    @Test
    void testParseWithoutGB() {
        // 测试非GB格式：GEL4Z035001
        FastenerParseResult result = parser.parse("GEL4Z035001", "M6拉铆螺母");
        
        assertTrue(result.isSuccess());
        assertEquals("GEL4Z035001", result.getProductCode());
        assertEquals("M6", result.getSpecs());
        assertNull(result.getLevel());
        assertNull(result.getSurfaceTreatment());
    }
    
    @Test
    void testParseEmptyComponentCode() {
        // 测试空component_code
        FastenerParseResult result = parser.parse("", "螺栓");
        
        assertFalse(result.isSuccess());
        assertEquals("component_code不能为空", result.getErrorMessage());
    }
    
    @Test
    void testParseNullComponentCode() {
        // 测试null component_code
        FastenerParseResult result = parser.parse(null, "螺栓");
        
        assertFalse(result.isSuccess());
        assertEquals("component_code不能为空", result.getErrorMessage());
    }
    
    @Test
    void testParseOtherNonGBFormat() {
        // 测试其他非GB格式：ABC123
        FastenerParseResult result = parser.parse("ABC123", "测试零件");
        
        assertTrue(result.isSuccess());
        assertEquals("ABC123", result.getProductCode());
        assertNull(result.getSpecs());
        assertNull(result.getLevel());
        assertNull(result.getSurfaceTreatment());
    }
}
