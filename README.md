<<<<<<< HEAD
# 🤖 AI 智能 BI 数据分析平台

> 基于 **RuoYi 框架 + RAG 大模型**的智能化 BI 数据分析平台。让业务人员用**自然语言**完成「问数 → 取数 → 看图 → 预警」的全链路，无需写 SQL。

---

## ✨ 核心特性

| 特性 | 说明 |
|------|------|
| **自然语言转 SQL（NL2SQL）** | 输入"上月销售额 Top10 商品"，自动生成、校验并执行 SQL |
| **RAG 业务知识库** | 注入表结构 + 业务术语 + 历史问答，提升大模型对业务语义的理解 |
| **智能图表生成** | 根据查询结果自动推荐 ECharts 图表类型并渲染可视化 |
| **数据异常预警** | 定时扫描指标，阈值越界自动生成预警记录 + 站内信 / 邮件通知 |
| **OCR 文档识别** | 上传图片 / PDF，PaddleOCR 识别后自动清洗入库 |
| **BI 可视化大屏** | 拖拽式仪表盘，支持多数据源接入 |

---

## 🏗️ 技术架构

| 层 | 技术栈 |
|----|--------|
| 前端 | Vue 3.5 · Element Plus · ECharts 5.6 · Vite 6 · Vitest |
| 后端 | Spring Boot 2.6（Java 8）· RuoYi-Vue · MyBatis-Plus · PageHelper |
| 大模型 | 火山方舟 Ark API（DeepSeek V4 Flash，用于 NL2SQL 与数据解读） |
| 向量库 | PostgreSQL 17 + pgvector（BGE-M3，1024 维） |
| 缓存 | Redis 5 |
| OCR | PaddleOCR（Python FastAPI 服务） |

---

## 📁 目录结构（Monorepo）

```
ai_bi/
├── RuoYi-Vue-v3.8.7/          # 后端（Spring Boot + RuoYi）
│   ├── ruoyi-bi/               # ★ BI 核心业务模块（NL2SQL / 预警 / OCR / 向量检索）
│   ├── ruoyi-admin/            # 管理端启动模块 + 全局配置
│   ├── ruoyi-common/           # 公共模块（OcrService 等）
│   └── sql/                    # 数据库初始化脚本
│
└── RuoYi-Vue3/                # 前端（Vue3 + Element Plus + ECharts）
    └── src/views/bi/           # BI 功能页面（对话式查询 / 预警 / OCR / 大屏）
```

---

## 🚀 快速开始

### 后端

```bash
# 1. 准备环境：JDK 8 / PostgreSQL 17（启用 pgvector 扩展）/ Redis 5
# 2. 复制脱敏配置模板，填入真实密码（application.yml 已被 .gitignore 忽略，不会入库）
cp RuoYi-Vue-v3.8.7/ruoyi-admin/src/main/resources/application-example.yml \
   RuoYi-Vue-v3.8.7/ruoyi-admin/src/main/resources/application.yml
cp RuoYi-Vue-v3.8.7/ruoyi-admin/src/main/resources/application-druid-example.yml \
   RuoYi-Vue-v3.8.7/ruoyi-admin/src/main/resources/application-druid.yml

# 3. 启动 ruoyi-admin 的 RuoYiApplication
```

### 前端

```bash
cd RuoYi-Vue3
npm install
npm run dev        # 开发模式
# npm run build:prod  # 生产构建
```

---

## 🔐 环境变量（密钥不入库）

真实配置通过 `${ENV:默认值}` 占位符注入，详见 `application-example.yml` / `application-druid-example.yml`：

| 变量 | 用途 |
|------|------|
| `ARK_API_KEY` | 火山方舟大模型 API Key（留空则 AI 功能禁用） |
| `DB_PASSWORD` / `PGVECTOR_PASSWORD` | 主库 / 向量库密码 |
| `TOKEN_SECRET` | JWT 密钥（**务必改成随机长串**） |
| `REDIS_PASSWORD` | Redis 密码（可空） |

---

## 🧪 测试

| 层 | 框架 | 用例 | 状态 |
|----|------|------|------|
| 后端 | JUnit 5 + Mockito | 29（纯函数 / 枚举 / JDBC 构建 / 引擎编排 Mock） | ✅ 全绿 |
| 前端 | Vitest + Vue Test Utils | 5（OCR 页面重复渲染修复等） | ✅ 全绿 |

```bash
# 后端测试
cd RuoYi-Vue-v3.8.7 && mvn -pl ruoyi-bi -am test

# 前端测试
cd RuoYi-Vue3 && npm test
```

---

## 📄 许可证

个人学习项目。后端基于 RuoYi（遵循其原许可证）；BI 扩展模块为自研。
=======
# ai_bi
>>>>>>> 32ceae4c2adef587163ff056cd0df95c4ea67e0f
