package com.mms.utils.rules;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
@Data
public class FastenerNormalizer {

    List<Function<String, String>> normalizingRules = new ArrayList<>();

    private String rawStr;

    public FastenerNormalizer applyRules(Function<String, String> rule) {
        normalizingRules.add(rule);
        return this;
    }

    public String normalize() {
        String normalizedStr = this.rawStr;

        if (normalizedStr == null) {
            return "";
        }
        for (Function<String, String> rule : normalizingRules) {
            normalizedStr = rule.apply(normalizedStr);
        }
        return normalizedStr;
    }

     /**
      * 初始化归一化器，添加默认的归一化规则
      * VBA 代码中的归一化规则
      *  .Range("B6:C" & x).Copy .Range("L6")
      *                     .Range("J6:M" & x).Replace what:="~*", replacement:="x"
      *                     .Range("J6:M" & x).Replace what:="¡Á", replacement:="x"
      *                     .Range("J6:M" & x).Replace what:="X", replacement:="x"
      *                     .Range("J6:M" & x).Replace what:="GB/T", replacement:="GB"
      *                     .Range("J6:M" & x).Replace what:="GB/", replacement:="GB"
      *                     .Range("J6:M" & x).Replace what:="GB ", replacement:="GB"
      *                     .Range("J6:M" & x).Replace what:="m", replacement:="M"
      *                     .Range("J6:M" & x).Replace what:="¦Õ", replacement:=""
      *                     .Range("J6:M" & x).Replace what:="¦Ä", replacement:=""
      *                     .Range("J6:J" & x).Replace what:="¦Õ", replacement:=""
      *                     .Range("J6:J" & x).Replace what:="¦Ä", replacement:=""
      *                     .Range("J6:L" & x).Replace what:="   ", replacement:=" "
      *                     .Range("J6:L" & x).Replace what:="  ", replacement:=" "
      *                     .Range("M6:M" & x).Replace what:=" ", replacement:=""
      *                     .Range("J6:L" & x).Replace what:="GBB", replacement:="GB"
      *                     .Range("J6:L" & x).Replace what:="97-1", replacement:="97.1"
      *                     .Range("M6:M" & x).Replace what:="µ¯È¦", replacement:="µ¯µæ"
      * @return 归一化器
      */
     public static FastenerNormalizer intialize() {
         FastenerNormalizer normalizer = new FastenerNormalizer();
         normalizer.applyRules(String::trim)
                 .applyRules(str -> str.replace("*", "x"))
                 .applyRules(str -> str.replace("×", "x"))
                 .applyRules(str -> str.replace("X", "x"))
                 .applyRules(str -> str.replace("GB/T", "GB"))
                 .applyRules(str -> str.replace("GB/", "GB"))
                 .applyRules(str -> str.replace("GB ", "GB"))
                 .applyRules(str -> str.replace("m", "M"))
                 .applyRules(str -> str.replace("φ", ""))
                 .applyRules(str -> str.replace("δ", ""))
                 .applyRules(str -> str.replace("   ", " "))
                 .applyRules(str -> str.replace("  ", " "))
                 .applyRules(str -> str.replace(" ", ""))
                 .applyRules(str -> str.replace("GBB", "GB"))
                 .applyRules(str -> str.replace("97-1", "97.1"))
                 .applyRules(str -> str.replace("弹圈", "弹垫"));

         return normalizer;
    }

}
