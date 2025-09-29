#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成 fastener_warehouse 表的完整导入SQL脚本
从 docs/fastener.csv 文件读取数据并生成INSERT语句
"""

import csv
import os

def generate_sql_insert():
    """生成完整的SQL插入语句"""
    
    csv_file = 'fastener.csv'
    sql_file = 'import_fastener_complete.sql'
    
    if not os.path.exists(csv_file):
        print(f"错误：找不到文件 {csv_file}")
        return
    
    sql_statements = []
    sql_statements.append("-- 完整的 fastener_warehouse 数据导入脚本")
    sql_statements.append("-- 从 docs/fastener.csv 导入所有数据")
    sql_statements.append("")
    sql_statements.append("-- 清空现有数据（可选）")
    sql_statements.append("-- TRUNCATE TABLE fastener_warehouse;")
    sql_statements.append("")
    sql_statements.append("-- 开始插入数据")
    sql_statements.append("INSERT INTO fastener_warehouse (")
    sql_statements.append("    erp_code, specs, product_code, name, level, material, surface_treatment, remark,")
    sql_statements.append("    default_flag, entry_user, entry_ts, last_update_user, last_update_ts")
    sql_statements.append(") VALUES")
    
    try:
        with open(csv_file, 'r', encoding='utf-8-sig') as file:
            csv_reader = csv.DictReader(file)
            
            values = []
            for row in csv_reader:
                # 处理空值和特殊字符
                erp_code = row['ERP_Code'].replace("'", "''") if row['ERP_Code'] else ''
                specs = row['Specs'].replace("'", "''") if row['Specs'] else ''
                product_code = row['Product_Code'].replace("'", "''") if row['Product_Code'] else ''
                name = row['Name'].replace("'", "''") if row['Name'] else ''
                level = row['Level'].replace("'", "''") if row['Level'] else ''
                material = row['Material'].replace("'", "''") if row['Material'] else ''
                surface_treatment = row['Surface_Treatment'].replace("'", "''") if row['Surface_Treatment'] else ''
                remark = row['Remark'].replace("'", "''") if row['Remark'] else ''
                
                value = f"('{erp_code}', '{specs}', '{product_code}', '{name}', '{level}', '{material}', '{surface_treatment}', '{remark}', 0, 'SYS_USER', CURRENT_TIMESTAMP(), 'SYS_USER', CURRENT_TIMESTAMP())"
                values.append(value)
            
            # 将所有值用逗号连接
            sql_statements.append(",\n".join(values))
            sql_statements.append(";")
            sql_statements.append("")
            sql_statements.append("-- 验证导入结果")
            sql_statements.append("SELECT COUNT(*) as total_records FROM fastener_warehouse;")
            sql_statements.append("SELECT * FROM fastener_warehouse LIMIT 10;")
            
        # 写入SQL文件
        with open(sql_file, 'w', encoding='utf-8', newline='\n') as file:
            file.write('\n'.join(sql_statements))
        
        print(f"成功生成SQL文件：{sql_file}")
        print(f"共处理 {len(values)} 条记录")
        
    except Exception as e:
        print(f"处理文件时出错：{e}")

if __name__ == "__main__":
    generate_sql_insert()
