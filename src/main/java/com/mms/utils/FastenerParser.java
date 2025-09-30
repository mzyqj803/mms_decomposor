package com.mms.utils;

import com.mms.dto.FastenerParseResult;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 紧固件解析器
 * 用于解析component_code和name，提取product_code、specs、level、surface_treatment等信息
 */
@Component
public class FastenerParser {
    
    // GB或GB/T开头的产品代码模式（支持空格）
    private static final Pattern GB_PATTERN = Pattern.compile("GB(?:/T)?\\s*(\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE);
    
    // M开头的规格模式：M数字、M数字*数字、M数字×数字
    private static final Pattern M_SPECS_PATTERN = Pattern.compile("M(\\d+(?:\\*|×)\\d+|\\d+)", Pattern.CASE_INSENSITIVE);
    
    // 等级模式：数字.数字、纯数字、数字HV
    private static final Pattern LEVEL_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?(?:HV)?|\\d+)");
    
    // Z结尾的表面处理模式
    private static final Pattern Z_SURFACE_PATTERN = Pattern.compile(".*Z$", Pattern.CASE_INSENSITIVE);
    
    /**
     * 解析紧固件component_code和name
     * 
     * @param componentCode 组件代码
     * @param name 名称
     * @return 解析结果
     */
    public FastenerParseResult parse(String componentCode, String name) {
        if (componentCode == null || componentCode.trim().isEmpty()) {
            return FastenerParseResult.failure(componentCode, name, "component_code不能为空");
        }
        
        try {
            // 合并component_code和name进行解析
            String combinedText = (componentCode + " " + (name != null ? name : "")).trim();
            
            // 1. 提取GB产品代码
            String productCode = extractProductCode(combinedText);
            if (productCode == null) {
                return FastenerParseResult.failure(componentCode, name, "未找到GB或GB/T开头的产品代码");
            }
            
            // 2. 提取M规格
            String specs = extractSpecs(combinedText);
            
            // 3. 提取等级
            String level = extractLevel(combinedText);
            
            // 4. 提取表面处理
            String surfaceTreatment = extractSurfaceTreatment(componentCode);
            
            return FastenerParseResult.success(componentCode, name, productCode, specs, level, surfaceTreatment);
            
        } catch (Exception e) {
            return FastenerParseResult.failure(componentCode, name, "解析过程中发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 提取产品代码（GB后面的数字）
     */
    private String extractProductCode(String text) {
        Matcher matcher = GB_PATTERN.matcher(text);
        if (matcher.find()) {
            return "GB" + matcher.group(1);
        }
        return null;
    }
    
    /**
     * 提取规格（M开头的部分）
     */
    private String extractSpecs(String text) {
        Matcher matcher = M_SPECS_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(0);
        }
        
        // 如果没有找到M开头的规格，尝试从product_code之后提取
        Matcher gbMatcher = GB_PATTERN.matcher(text);
        if (gbMatcher.find()) {
            String afterGB = text.substring(gbMatcher.end()).trim();
            // 移除分隔符（/、-、空格）
            String[] parts = afterGB.split("[/\\-\\s]+");
            for (String part : parts) {
                if (!part.isEmpty() && !isLevelPattern(part) && !part.equalsIgnoreCase("Z")) {
                    return part;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 提取等级
     */
    private String extractLevel(String text) {
        // 先尝试从末尾提取等级（通常是最后一个数字部分）
        String[] parts = text.split("[/\\-\\s]+");
        
        // 找到GB产品代码的位置，排除产品代码部分
        // 对于 "GB/T 5783 M12*45"，分割后是 ["GB", "T", "5783", "M12*45"]
        // 需要跳过 "GB", "T", "5783" 这些产品代码部分
        int skipCount = 0;
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (part.matches("GB")) {
                // 检查下一个是否是 "T"
                if (i + 1 < parts.length && parts[i + 1].trim().matches("T")) {
                    // GB/T 的情况，跳过 GB, T, 和下一个数字
                    skipCount = i + 3;
                } else {
                    // 普通 GB 的情况，跳过 GB 和下一个数字
                    skipCount = i + 2;
                }
                break;
            }
        }
        // 从后往前查找等级模式，但跳过GB产品代码部分
        for (int i = parts.length - 1; i >= skipCount; i--) {
            String part = parts[i].trim();
            // 移除Z后缀后再检查
            String cleanPart = part.replaceAll("Z$", "").trim();
            if (isLevelPattern(cleanPart) && !cleanPart.isEmpty()) {
                return cleanPart;
            }
        }
        
        return null;
    }
    
    /**
     * 判断是否为等级模式
     */
    private boolean isLevelPattern(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        // 移除Z后缀
        String cleanText = text.replaceAll("Z$", "").trim();
        
        // 检查是否为等级格式
        return LEVEL_PATTERN.matcher(cleanText).matches();
    }
    
    /**
     * 提取表面处理
     */
    private String extractSurfaceTreatment(String text) {
        // 检查整个文本是否以Z结尾
        if (Z_SURFACE_PATTERN.matcher(text).find()) {
            return "镀锌等";
        }
        return null;
    }
    
    /**
     * 测试解析功能
     */
    public static void main(String[] args) {
        FastenerParser parser = new FastenerParser();
        
        // 测试用例
        String[][] testCases = {
            {"GB5783-M6*20-8.8Z", "螺栓"},
            {"GB6170-M6-8Z", "螺母"},
            {"GB97.1-M6-140HV-Z", "平垫圈"},
            {"GB93-M6-430HV-Z", "弹簧垫圈"},
            {"GB5783-M10*35-8.8Z", "螺栓"},
            {"GB/T5783-8.8", "螺栓M12*45"},
            {"GB/T6170-8", "螺母M12"},
            {"GB/T5783/M12*45/8.8", "螺栓M12*45"},
            {"GB/T 5783 M12*45", "六角螺栓"},
            {"GB/T15389-M24×170-8.8Z", "螺杆"},
            {"GEL4Z035001", "M6拉铆螺母"}
        };
        
        System.out.println("紧固件解析测试结果：");
        System.out.println("=====================================");
        
        for (String[] testCase : testCases) {
            FastenerParseResult result = parser.parse(testCase[0], testCase[1]);
            System.out.println("输入: " + testCase[0] + " | " + testCase[1]);
            System.out.println("结果: " + (result.isSuccess() ? "成功" : "失败"));
            if (result.isSuccess()) {
                System.out.println("  ProductCode: " + result.getProductCode());
                System.out.println("  Specs: " + result.getSpecs());
                System.out.println("  Level: " + result.getLevel());
                System.out.println("  SurfaceTreatment: " + result.getSurfaceTreatment());
            } else {
                System.out.println("  错误: " + result.getErrorMessage());
            }
            System.out.println("-------------------------------------");
        }
    }
}
