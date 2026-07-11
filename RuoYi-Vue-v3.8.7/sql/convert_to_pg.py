#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
将若依 MySQL 初始化 SQL 转换为 PostgreSQL 兼容格式
用法：python convert_to_pg.py
"""
import re, sys

INPUT  = "ry_20231130.sql"
OUTPUT = "ry_postgresql.sql"

with open(INPUT, "r", encoding="utf-8") as f:
    content = f.read()

# 1. 去掉 ` 反引号
content = content.replace("`", '"')
# 但 PostgreSQL 小写标识符不需要引号，去掉所有双引号包裹的小写标识符
# 简化：先把所有 `"word"` 里的双引号去掉（若依的标识符都是小写+下划线）
content = re.sub(r'"([a-z0-9_]+)"', r'\1', content)

# 2. AUTO_INCREMENT → 改为 BIGSERIAL（需要处理 BIGINT(20) NOT NULL AUTO_INCREMENT PRIMARY KEY）
#    模式：列定义行含 AUTO_INCREMENT
def fix_auto_inc(m):
    line = m.group(0)
    # BIGINT(20) NOT NULL AUTO_INCREMENT → BIGSERIAL
    line = re.sub(r'\bbigint\(\d+\)\s+not null\s+auto_increment', 'BIGSERIAL', line, flags=re.I)
    # INT(11) NOT NULL AUTO_INCREMENT → SERIAL（但若依只用 BIGINT）
    line = re.sub(r'\bint\(\d+\)\s+not null\s+auto_increment', 'SERIAL', line, flags=re.I)
    return line

content = fix_auto_inc(content)  # 先处理再去掉多余括号

# 3. 去掉列定义里的 (N) 长度（PostgreSQL 不需要 INT(4) 这种写法）
#    但保留 VARCHAR(N) 不变
content = re.sub(r'\b(bigint|integer|int|smallint|tinyint)\(\d+\)', lambda m: m.group(1), content, flags=re.I)

# 4. DATETIME → TIMESTAMP
content = re.sub(r'\bdatetime\b', 'TIMESTAMP', content, flags=re.I)

# 5. SYSDAte() → CURRENT_TIMESTAMP
content = re.sub(r'\bsysdate\(\)', 'CURRENT_TIMESTAMP', content, flags=re.I)

# 6. 去掉 ENGINE=InnoDB ... 行尾剩余
content = re.sub(r'\)\s*engine\s*=\s*innodb[^;]*', ')', content, flags=re.I)

# 7. 去掉 CHARACTER SET ... COLLATE ...
content = re.sub(r'\s*character\s+set\s+\w+\s+collate\s+\w+', '', content, flags=re.I)
content = re.sub(r'\s*character\s+set\s+\w+', '', content, flags=re.I)

# 8. 行内 COMMENT '...' → 暂存，后面统一生成 COMMENT ON 语句
#    先把 CREATE TABLE 里的行内 comment 提取出来
table_comments = {}
col_comments  = {}  # key: (table, col)

def extract_comments(sql_text):
    # 提取表注释：) ENGINE=... COMMENT = '...'  或直接 ) COMMENT='...'
    # 简化：删除行内 COMMENT，改为末尾统一生成 COMMENT ON
    lines = sql_text.split('\n')
    out = []
    cur_table = None
    for ln in lines:
        # 检测当前表名
        m = re.match(r'create table (\w+)\s*\(', ln, re.I)
        if m:
            cur_table = m.group(1).lower()
            table_comments[cur_table] = None
        # 提取表注释（在 ) 之后）
        m2 = re.search(r"\)\s*comment\s*=\s*'([^']+)'", ln, re.I)
        if m2 and cur_table:
            table_comments[cur_table] = m2.group(1)
            ln = re.sub(r"\s*comment\s*=\s*'[^']+'", '', ln, flags=re.I)
        # 提取列注释
        m3 = re.search(r"comment\s+'([^']+)'\s*(,?)\\s*$", ln, re.I)
        if m3 and cur_table:
            col = re.split(r'\s+', ln.strip())[0].lower()
            col_comments[(cur_table, col)] = m3.group(1)
            ln = re.sub(r'\s+comment\s+\'[^\']+\'', '', ln, flags=re.I)
        out.append(ln)
    return '\n'.join(out)

content = extract_comments(content)

# 9. primary key 写在一行里的情况：PRIMARY KEY (col) → 已在列定义里用 BIGSERIAL 处理了

# 10. 字符串比较：MySQL 默认不区分大小写，PG 区分；但若依无特殊依赖，保持原样

# 11. 追加 COMMENT ON 语句
extra = '\n\n-- 表注释和列注释\n'
for t, c in table_comments.items():
    if c:
        extra += f"COMMENT ON TABLE {t} IS '{c}';\n"
for (t, col), c in col_comments.items():
    extra += f"COMMENT ON COLUMN {t}.{col} IS '{c}';\n"

content += extra

# 12. 文件头
header = """-- ============================================
-- 若依系统表 PostgreSQL 版本
-- 由 convert_to_pg.py 自动转换
-- 请先创建数据库 ry，然后执行本脚本
-- ============================================

-- 若依系统表一般使用 ry 数据库
-- CREATE DATABASE ry;

"""
content = header + content

with open(OUTPUT, "w", encoding="utf-8") as f:
    f.write(content)

print(f"✅ 转换完成：{INPUT} → {OUTPUT}")
print(f"   请检查 {OUTPUT} 并手动修正残留的 MySQL 语法")
