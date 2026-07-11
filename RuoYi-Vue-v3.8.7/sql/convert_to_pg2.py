#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
将若依 MySQL 初始化 SQL 转换为 PostgreSQL 兼容格式
用法：python convert_to_pg2.py
"""
import re

INPUT  = "ry_20231130.sql"
OUTPUT = "ry_postgresql.sql"

with open(INPUT, "r", encoding="utf-8") as f:
    lines = f.readlines()

out = []
current_table = None
table_comment = {}
col_comment = []   # [(table, col, comment)]

for raw in lines:
    line = raw.rstrip("\n")

    # 去掉 ` 反引号
    line = line.replace("`", "")

    # 检测当前表
    m = re.match(r"create table (\w+)\s*\(", line, re.I)
    if m:
        current_table = m.group(1).lower()

    # 提取表注释（在 ) 行里）
    m2 = re.search(r"\)\s*comment\s*=\s*'([^']+)'", line, re.I)
    if m2 and current_table:
        table_comment[current_table] = m2.group(1)
        line = re.sub(r"\s+comment\s*=\s*'[^']+'", "", line, flags=re.I)

    # 提取列注释（列定义行末尾的 comment '...'）
    # 匹配模式：列定义 ... comment '注释' ,?  或 ... comment '注释' )
    m3 = re.search(r"comment\s+'([^']+)'\s*([,)])", line, re.I)
    if m3 and current_table:
        # 取列名：行首到第一个空白
        col = line.strip().split()[0].lower()
        col_comment.append((current_table, col, m3.group(1)))
        line = re.sub(r"\s+comment\s+'[^']+'\s*([,)])", r"\1", line, flags=re.I)

    # BIGINT(20) NOT NULL AUTO_INCREMENT  → BIGSERIAL
    line = re.sub(r"\bbigint\(\d+\)\s+not\s+null\s+auto_increment",
                    "BIGSERIAL", line, flags=re.I)

    # INT(11) NOT NULL AUTO_INCREMENT  → SERIAL（若依无此用法，保留）
    line = re.sub(r"\bint\(\d+\)\s+not\s+null\s+auto_increment",
                    "SERIAL", line, flags=re.I)

    # 去掉列长度 (INT(4) → INT, 但保留 VARCHAR(N))
    # 只处理整数类型
    line = re.sub(r"\b(tinyint|smallint|mediumint|bigint)\(\d+\)",
                    lambda m: m.group(1), line, flags=re.I)
    line = re.sub(r"\bint\(\d+\)",
                    "INTEGER", line, flags=re.I)

    # DATETIME → TIMESTAMP
    line = re.sub(r"\bdatetime\b", "TIMESTAMP", line, flags=re.I)

    # SYSDATE() → CURRENT_TIMESTAMP
    line = re.sub(r"\bsysdate\(\)", "CURRENT_TIMESTAMP", line, flags=re.I)

    # 去掉 ENGINE=InnoDB ...
    line = re.sub(r"\)\s*engine\s*=\s*innodb[^;]*", ")", line, flags=re.I)

    # 去掉 CHARACTER SET ... COLLATE ...
    line = re.sub(r"\s+character\s+set\s+\w+(\s+collate\s+\w+)?", "", line, flags=re.I)

    # 去掉行尾多余的逗号（如果出现 ) 前有逗号）
    # 由 SQL 语法保证，暂不处理

    out.append(line)

# 末尾追加 COMMENT ON 语句
out.append("\n\n-- =========  表注释 =========")
for t, c in table_comment.items():
    out.append(f"COMMENT ON TABLE {t} IS '{c}';")

out.append("\n-- =========  列注释 =========")
for (t, col, c) in col_comment:
    out.append(f"COMMENT ON COLUMN {t}.{col} IS '{c}';")

content = "\n".join(out)

# 文件头
header = """-- ============================================
-- 若依系统表 PostgreSQL 版本
-- 由 convert_to_pg2.py 自动转换
-- 执行前请先创建数据库 ry：
--   CREATE DATABASE ry OWNER postgres;
-- ============================================

"""
content = header + content

with open(OUTPUT, "w", encoding="utf-8") as f:
    f.write(content)

print(f"✅ 转换完成：{INPUT} → {OUTPUT}")
print(f"⚠️  请务必检查 {OUTPUT} 并手动修正残留的 MySQL 语法")
print(f"   重点检查：AUTO_INCREMENT 残留、COMMENT 格式、INSERT 语句中的 SYSDATE()")
