# 智能 BI 数据分析平台（后端）

> 基于 RuoYi 快速开发框架自研的 AI 驱动数据分析平台。
> 本文档为仓库 README，**请重命名为 `README.md` 后使用**（原 RuoYi 自带 README 可保留或替换）。

## 项目简介
让非技术用户用自然语言完成「提问 → 取数 → 可视化 → 异常预警」的完整数据闭环。

## 技术栈
- **后端**：Spring Boot 2.6 / MyBatis / Redis / Druid 连接池
- **AI**：火山方舟 Ark API（DeepSeek-V4-Flash 对话/生成）、BGE-M3 嵌入模型
- **数据**：PostgreSQL 17 + pgvector（向量知识库）、MySQL（业务数据源动态接入）
- **OCR**：本地部署 PaddleOCR（Python FastAPI 服务）

## 核心模块（`ruoyi-bi`）
| 模块 | 说明 |
|------|------|
| **NL2SQL** | 中文提问 → 大模型生成 SQL → 多数据源（MySQL/PG）执行 → ECharts 可视化 |
| **RAG 知识库** | pgvector 向量检索 + BGE-M3 嵌入，让 NL2SQL 理解企业表结构与业务口径，降低幻觉 |
| **异常预警** | 定时扫描业务指标，阈值/趋势异常自动分级（info/warning/critical），站内信+邮件双通道通知，待处理去重 |
| **OCR 识别** | 图片/PDF → 结构化文本，接入分析流程 |
| **BI 大屏** | 拖拽式组件编排、多图表联动 |

## 快速开始
1. 准备环境：JDK 8、Maven 3.9+、PostgreSQL 17（含 pgvector 扩展）、Redis 5
2. 配置：`cp ruoyi-admin/src/main/resources/application-example.yml application.yml` 并填入真实密钥（见下方环境变量表）
3. 同上处理 `application-druid-example.yml`
4. 建库：执行项目 SQL 初始化脚本（若依基础表 + BI 业务表）
5. 启动：`mvn clean package -pl ruoyi-admin -am` 后运行 `ruoyi-admin`

> 🔒 安全说明：`application.yml` / `application-druid.yml` 已被 `.gitignore` 忽略，**真实密钥永远不会进仓库**；仓库仅保留 `-example` 占位符模板。

## 环境变量（务必通过环境变量注入，勿写死）
| 变量 | 说明 |
|------|------|
| `ARK_API_KEY` | 火山方舟 API Key（留空则 AI 功能禁用） |
| `DB_PASSWORD` / `DB_USER` / `DB_HOST` / `DB_PORT` / `DB_NAME` | 主库 PostgreSQL 连接 |
| `PGVECTOR_PASSWORD` / `PGVECTOR_USER` / `PGVECTOR_DB` | 向量库连接 |
| `TOKEN_SECRET` | JWT 签名密钥（改随机长串） |
| `REDIS_PASSWORD` | Redis 密码（可空） |
| `DRUID_CONSOLE_PASSWORD` | Druid 监控台密码 |
| `OCR_PYTHON_PATH` / `OCR_SCRIPT_PATH` | PaddleOCR 执行环境 |

## 测试
- **后端单测**：JUnit 5 + Mockito，**29/29 全绿**
  ```bash
  mvn -pl ruoyi-bi -am test
  ```
- 覆盖：NL2SQL 引擎编排（无数据源跳过 / 间隔未到跳过 / pending 去重 / 触发入库+通知）、预警分级 `determineAlertLevel`、间隔判断 `shouldCheck`、JDBC URL 构建 `JdbcUrlBuilder`、枚举往返等。

## 代码质量
- 10 条质量红线 + Code Review 清单（团队可复用资产）
- 密钥全部环境变量化、时间字段统一 `LocalDateTime`、HikariCP 连接池替代 `DriverManager`、枚举消灭魔法字符串
