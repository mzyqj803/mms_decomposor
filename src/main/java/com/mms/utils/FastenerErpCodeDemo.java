package com.mms.utils;

import com.mms.utils.FastenerErpCodeFinder.ErpCodeResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 紧固件ERP代码查找演示程序
 * 展示FastenerErpCodeFinder工具类的实际使用效果
 */
@Component
public class FastenerErpCodeDemo {
    
    @Autowired
    private FastenerErpCodeFinder fastenerErpCodeFinder;
    
    /**
     * 运行演示程序
     */
    public void runDemo() {
        System.out.println("=== 紧固件ERP代码查找工具类演示 ===");
        System.out.println();
        
        // 演示数据
        Object[][] demoData = {
            {1L, "GB5783-M6*20-8.8Z", "螺栓", "标准紧固件，应该能完全匹配"},
            {2L, "GB6170-M6-8Z", "螺母", "螺母类型，应该能匹配"},
            {3L, "GB97.1-M6-140HV-Z", "平垫圈", "垫圈类型，应该能匹配"},
            {4L, "GB5783-M8*25-10.9", "螺栓", "部分匹配（规格和等级不同）"},
            {5L, "FT001", "非紧固件", "非紧固件，应该跳过"},
            {6L, "INVALID_CODE", "无效代码", "无效代码，应该解析失败"},
            {7L, "GB9999-M6*20-8.8Z", "未知物料", "未知物料，应该返回未知物料错误"},
        };
        
        System.out.println("开始查找ERP代码...");
        System.out.println();
        
        for (Object[] data : demoData) {
            Long componentId = (Long) data[0];
            String componentCode = (String) data[1];
            String name = (String) data[2];
            String description = (String) data[3];
            
            System.out.println("【测试用例】" + description);
            System.out.println("工件ID: " + componentId);
            System.out.println("工件代码: " + componentCode);
            System.out.println("工件名称: " + name);
            
            try {
                ErpCodeResult result = fastenerErpCodeFinder.findErpCode(componentId, componentCode, name);
                
                if (result.isSuccess()) {
                    System.out.println("✅ 查找成功！");
                    System.out.println("   ERP代码: " + result.getErpCode());
                    System.out.println("   匹配的产品代码: " + result.getMatchedProductCode());
                    System.out.println("   匹配的规格: " + result.getMatchedSpecs());
                    System.out.println("   匹配的等级: " + result.getMatchedLevel());
                    System.out.println("   匹配的表面处理: " + result.getMatchedSurfaceTreatment());
                } else {
                    System.out.println("❌ 查找失败: " + result.getErrorMessage());
                }
                
            } catch (Exception e) {
                System.out.println("💥 发生异常: " + e.getMessage());
            }
            
            System.out.println("----------------------------------------");
            System.out.println();
        }
        
        System.out.println("=== 演示完成 ===");
    }
    
    /**
     * 测试特定场景
     */
    public void testSpecificScenario() {
        System.out.println("=== 特定场景测试 ===");
        
        // 测试完全匹配
        System.out.println("1. 完全匹配测试");
        ErpCodeResult result1 = fastenerErpCodeFinder.findErpCode(1L, "GB5783-M6*20-8.8Z", "螺栓");
        printResult("完全匹配", result1);
        
        // 测试部分匹配
        System.out.println("2. 部分匹配测试");
        ErpCodeResult result2 = fastenerErpCodeFinder.findErpCode(2L, "GB5783-M8*25-10.9", "螺栓");
        printResult("部分匹配", result2);
        
        // 测试非紧固件
        System.out.println("3. 非紧固件测试");
        ErpCodeResult result3 = fastenerErpCodeFinder.findErpCode(3L, "FT001", "非紧固件");
        printResult("非紧固件", result3);
        
        System.out.println("=== 特定场景测试完成 ===");
    }
    
    /**
     * 打印结果
     */
    private void printResult(String scenario, ErpCodeResult result) {
        System.out.println("场景: " + scenario);
        if (result.isSuccess()) {
            System.out.println("  结果: 成功");
            System.out.println("  ERP代码: " + result.getErpCode());
        } else {
            System.out.println("  结果: 失败");
            System.out.println("  错误: " + result.getErrorMessage());
        }
        System.out.println();
    }
}
