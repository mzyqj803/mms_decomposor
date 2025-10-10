package com.mms.utils.rules;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * FastenerNormalizer 测试类
 */
public class FastenerNormalizerTest {
    
    @Test
    void testNormalizeBasicRules() {
        // 测试基本的标准化规则
        String input = "GB/T 5783-M6*20-8.8Z";
        String expected = "GB5783-M6x20-8.8Z";
        String result = FastenerNormalizer.initialize()
                .setRawStr(input)
                .normalize();
        assertEquals(expected, result);
    }
    
    @Test
    void testNormalizeWithSpaces() {
        // 测试空格处理
        String input = "GB 5783-M6*20-8.8Z";
        String expected = "GB5783-M6x20-8.8Z";
        String result = FastenerNormalizer.initialize()
                .setRawStr(input)
                .normalize();
        assertEquals(expected, result);
    }
    
    @Test
    void testNormalizeWithChineseCharacters() {
        // 测试中文字符处理
        String input = "螺栓 弹圈";
        String expected = "螺栓弹垫";
        String result = FastenerNormalizer.initialize()
                .setRawStr(input)
                .normalize();
        assertEquals(expected, result);
    }
    
    @Test
    void testNormalizeNullInput() {
        // 测试null输入
        String result = FastenerNormalizer.initialize()
                .setRawStr(null)
                .normalize();
        assertEquals("", result);
    }
}
