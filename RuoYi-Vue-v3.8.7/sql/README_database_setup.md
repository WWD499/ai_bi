# AI智能BI平台 — 数据库初始化指南

## 前提条件
- PostgreSQL 17 已安装并运行
- pgvector 扩展已安装（`vector.dll` 和扩展文件已就位）
- 知道 PostgreSQL `postgres` 用户的密码

---

## 第一步：创建数据库并启用 pgvector

打开 **psql** 或 **pgAdmin**，连接至 PostgreSQL，执行：

```sql
-- 创建 ruoyi 系统库（如果还没有）
CREATE DATABASE ry
  OWNER postgres
  ENCODING 'UTF8';

-- 连接到 ry 数据库
\c ry

-- 启用 pgvector 扩展（BI 向量检索需要）
CREATE EXTENSION IF NOT EXISTS vector;
```

> 或者直接在命令行执行：
> ```bash
> "/d/Program Files/PostgreSQL/17/bin/createdb.exe" -U postgres ry
> ```

---

## 第二步：执行初始化 SQL

在 `ry` 数据库中，按顺序执行以下 SQL 文件：

### 方式A：命令行（推荐）

```bash
# 设置环境变量（避免每次输入密码）
set PGPASSWORD=你的密码

# 执行若依系统表
"/d/Program Files/PostgreSQL/17/bin/psql.exe" -U postgres -d ry -f "D:/工程组/RuoYi-Vue-v3.8.7/sql/ry_postgresql.sql"

# 执行 Quartz 定时任务表
"/d/Program Files/PostgreSQL/17/bin/psql.exe" -U postgres -d ry -f "D:/工程组/RuoYi-Vue-v3.8.7/sql/quartz_postgresql.sql"

# 执行 BI 平台表（含 pgvector 向量表）
"/d/Program Files/PostgreSQL/17/bin/psql.exe" -U postgres -d ry -f "D:/工程组/RuoYi-Vue-v3.8.7/ruoyi-bi/sql/bi_tables.sql"
```

### 方式B：pgAdmin 图形界面

1. 打开 pgAdmin，连接至 PostgreSQL
2. 右键 `ry` 数据库 → **Query Tool**
3. 依次打开并执行上述三个 SQL 文件

---

## 第三步：验证数据库

执行以下查询，确认表已创建：

```sql
-- 查看所有表
\dt

-- 应看到以下表（若依系统表）：
-- sys_dept, sys_user, sys_role, sys_menu, sys_post, ...
-- （Quartz 表）：
-- qrtz_job_details, qrtz_triggers, ...
-- （BI 平台表）：
-- bi_datasource, bi_table_metadata, bi_knowledge, bi_query_history,
-- bi_alert_config, bi_dashboard

-- 验证 pgvector 扩展已启用
SELECT * FROM pg_extension WHERE extname = 'vector';

-- 验证向量表可使用向量类型
\d bi_knowledge
-- 应看到 content_vector 列，类型为 vector(1024)
```

---

## 第四步：更新 application-druid.yml 数据库密码

打开 `ruoyi-admin/src/main/resources/application-druid.yml`，找到：

```yaml
master:
    url: jdbc:postgresql://localhost:5432/ry?...
    username: postgres
    password: Admin@123   ← 改为你的 PostgreSQL 密码
```

---

## 第五步：启动应用

1. 在 IDEA 中重新加载 Maven 项目（右键项目 → Maven → Reload Project）
2. 运行 `RuoYiApplication.java`
3. 访问 `http://localhost:8080` 验证系统启动成功

---

## 常见问题

### Q: psql 连接时提示密码错误
A: 确认 PostgreSQL 的 `pg_hba.conf` 配置。若是本地开发，建议改为 `trust` 或 `md5` 认证方式。

### Q: CREATE EXTENSION vector 报错
A: 确认 `vector.dll` 已复制到 `D:\Program Files\PostgreSQL\17\lib\`，且扩展 SQL 文件在 `share\extension\` 目录。

### Q: 若依启动时报 SQL 语法错误
A: 检查 `ry_postgresql.sql` 转换是否完整，重点关注 `BIGSERIAL` 和 `CURRENT_TIMESTAMP` 是否正确。

### Q: 中文乱码
A: 确认数据库编码为 UTF8：`SHOW server_encoding;`
