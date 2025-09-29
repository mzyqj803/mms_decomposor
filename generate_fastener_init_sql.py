#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成 fastener_warehouse 表的数据库初始化SQL脚本
从 fastener.csv 文件读取数据并生成INSERT语句，用于数据库初始化
"""

import csv
import os

def generate_init_sql():
    """生成数据库初始化用的SQL脚本"""
    
    csv_file = 'src/main/resources/sql/data_init/fastener.csv'
    sql_file = 'src/main/resources/sql/data_init/fastener_warehouse_data.sql'
    
    if not os.path.exists(csv_file):
        print(f"错误：找不到文件 {csv_file}")
        return
    
    sql_statements = []
    sql_statements.append("-- Fastener Warehouse Complete Data")
    sql_statements.append("-- 紧固件库表完整数据")
    sql_statements.append("-- 从 fastener.csv 导入的完整数据")
    sql_statements.append("")
    sql_statements.append("-- 清空现有数据")
    sql_statements.append("DELETE FROM fastener_warehouse;")
    sql_statements.append("")
    sql_statements.append("-- 插入完整的紧固件数据")
    sql_statements.append("INSERT INTO fastener_warehouse (")
    sql_statements.append("    erp_code, specs, product_code, name, level, material, surface_treatment, remark,")
    sql_statements.append("    default_flag, entry_user, entry_ts, last_update_user, last_update_ts")
    sql_statements.append(") VALUES")
    
    try:
        with open(csv_file, 'r', encoding='utf-8-sig') as file:
            csv_reader = csv.DictReader(file)
            
            values = []
            for row in csv_reader:
                # 处理空值和特殊字符，使用Unicode转义来避免编码问题
                def escape_string(s):
                    if not s:
                        return "''"
                    # 首先替换单引号
                    s = s.replace("'", "''")
                    # 对于包含中文的字符串，我们转换为UTF-8 HEX格式
                    if any(ord(char) > 127 for char in s):
                        # 转换为UTF-8字节，然后转换为HEX
                        hex_str = s.encode('utf-8').hex()
                        return f"UNHEX('{hex_str.upper()}')"
                    else:
                        return f"'{s}'"
                
                erp_code = escape_string(row['ERP_Code'])
                specs = escape_string(row['Specs'])
                product_code = escape_string(row['Product_Code'])
                name = escape_string(row['Name'])
                level = escape_string(row['Level'])
                material = escape_string(row['Material'])
                surface_treatment = escape_string(row['Surface_Treatment'])
                remark = escape_string(row['Remark'])
                
                value = f"({erp_code}, {specs}, {product_code}, {name}, {level}, {material}, {surface_treatment}, {remark}, 0, 'SYS_USER', CURRENT_TIMESTAMP(), 'SYS_USER', CURRENT_TIMESTAMP())"
                values.append(value)
            
            # 将所有值用逗号连接，每行一个值以便阅读
            for i, value in enumerate(values):
                if i == len(values) - 1:
                    sql_statements.append(value + ";")  # 最后一行用分号结束
                else:
                    sql_statements.append(value + ",")  # 其他行用逗号结束
            
            sql_statements.append("")
            sql_statements.append("-- 设置一些默认的常用紧固件")
            sql_statements.append("UPDATE fastener_warehouse SET default_flag = 1 WHERE specs IN ('M8x20', 'M10x25', 'M12x30', 'M16x40');")
            sql_statements.append("")
            sql_statements.append("-- 数据导入完成")
            sql_statements.append("-- Total records imported: " + str(len(values)))
            
        # 写入SQL文件
        with open(sql_file, 'w', encoding='utf-8', newline='\n') as file:
            file.write('\n'.join(sql_statements))
        
        print(f"成功生成数据库初始化SQL文件：{sql_file}")
        print(f"共处理 {len(values)} 条记录")
        
    except Exception as e:
        print(f"处理文件时出错：{e}")

if __name__ == "__main__":
    generate_init_sql()
