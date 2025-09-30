package com.mms.utils;

import com.mms.entity.FastenerWarehouse;
import com.mms.service.FastenerWarehouseCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 紧固件ERP代码查找演示程序 - 展示fastenerComponent字段的使用
 */
@Component
public class FastenerErpCodeDemoWithFastenerComponent {
    
    @Autowired
    private FastenerErpCodeFinder fastenerErpCodeFinder;
    
    /**
     * 演示紧固件组件的ERP代码查找
     */
    public void demonstrateFastenerComponent() {
        System.out.println("=== 紧固件ERP代码查找演示（包含fastenerComponent字段）===");
        
        // 测试用例1: 紧固件组件
        System.out.println("\n1. 测试紧固件组件:");
        FastenerErpCodeFinder.ErpCodeResult result1 = fastenerErpCodeFinder.findErpCode(
                1L, "GB5783-M6*20-8.8Z", "螺栓");
        
        System.out.println("   组件ID: " + result1.getComponentId());
        System.out.println("   组件代码: " + result1.getComponentCode());
        System.out.println("   组件名称: " + result1.getName());
        System.out.println("   是否紧固件组件: " + (result1.isFastenerComponent() ? "是" : "否"));
        System.out.println("   查找成功: " + result1.isSuccess());
        if (result1.isSuccess()) {
            System.out.println("   ERP代码: " + result1.getErpCode());
            System.out.println("   匹配的产品代码: " + result1.getMatchedProductCode());
            System.out.println("   匹配的规格: " + result1.getMatchedSpecs());
            System.out.println("   匹配的等级: " + result1.getMatchedLevel());
            System.out.println("   匹配的表面处理: " + result1.getMatchedSurfaceTreatment());
        } else {
            System.out.println("   错误信息: " + result1.getErrorMessage());
        }
        
        // 测试用例2: 非紧固件组件
        System.out.println("\n2. 测试非紧固件组件:");
        FastenerErpCodeFinder.ErpCodeResult result2 = fastenerErpCodeFinder.findErpCode(
                999L, "FT001", "非紧固件");
        
        System.out.println("   组件ID: " + result2.getComponentId());
        System.out.println("   组件代码: " + result2.getComponentCode());
        System.out.println("   组件名称: " + result2.getName());
        System.out.println("   是否紧固件组件: " + (result2.isFastenerComponent() ? "是" : "否"));
        System.out.println("   查找成功: " + result2.isSuccess());
        if (result2.isSuccess()) {
            System.out.println("   ERP代码: " + result2.getErpCode());
        } else {
            System.out.println("   错误信息: " + result2.getErrorMessage());
        }
        
        // 测试用例3: 未知物料
        System.out.println("\n3. 测试未知物料:");
        FastenerErpCodeFinder.ErpCodeResult result3 = fastenerErpCodeFinder.findErpCode(
                2L, "GB9999-M6*20-8.8Z", "未知螺栓");
        
        System.out.println("   组件ID: " + result3.getComponentId());
        System.out.println("   组件代码: " + result3.getComponentCode());
        System.out.println("   组件名称: " + result3.getName());
        System.out.println("   是否紧固件组件: " + (result3.isFastenerComponent() ? "是" : "否"));
        System.out.println("   查找成功: " + result3.isSuccess());
        if (result3.isSuccess()) {
            System.out.println("   ERP代码: " + result3.getErpCode());
        } else {
            System.out.println("   错误信息: " + result3.getErrorMessage());
        }
        
        System.out.println("\n=== 演示完成 ===");
    }
    
    /**
     * 演示在业务逻辑中如何使用fastenerComponent字段
     */
    public void demonstrateBusinessLogic() {
        System.out.println("\n=== 业务逻辑演示（使用fastenerComponent字段）===");
        
        // 模拟处理多个组件
        List<Long> componentIds = Arrays.asList(1L, 2L, 999L);
        List<String> componentCodes = Arrays.asList("GB5783-M6*20-8.8Z", "GB9999-M6*20-8.8Z", "FT001");
        List<String> componentNames = Arrays.asList("螺栓", "未知螺栓", "非紧固件");
        
        int fastenerCount = 0;
        int nonFastenerCount = 0;
        int successCount = 0;
        int failureCount = 0;
        
        for (int i = 0; i < componentIds.size(); i++) {
            FastenerErpCodeFinder.ErpCodeResult result = fastenerErpCodeFinder.findErpCode(
                    componentIds.get(i), componentCodes.get(i), componentNames.get(i));
            
            // 根据fastenerComponent字段进行分类统计
            if (result.isFastenerComponent()) {
                fastenerCount++;
                System.out.println("紧固件组件: " + componentCodes.get(i) + " - " + 
                        (result.isSuccess() ? "成功" : "失败"));
            } else {
                nonFastenerCount++;
                System.out.println("非紧固件组件: " + componentCodes.get(i) + " - 跳过处理");
            }
            
            // 根据success字段进行分类统计
            if (result.isSuccess()) {
                successCount++;
            } else {
                failureCount++;
            }
        }
        
        System.out.println("\n统计结果:");
        System.out.println("紧固件组件数量: " + fastenerCount);
        System.out.println("非紧固件组件数量: " + nonFastenerCount);
        System.out.println("成功查找数量: " + successCount);
        System.out.println("失败查找数量: " + failureCount);
        
        System.out.println("\n=== 业务逻辑演示完成 ===");
    }
}
