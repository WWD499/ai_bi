# 智能 BI 数据分析平台（前端）

> 基于 RuoYi-Vue3 开发的 BI 数据分析平台前端。
> 本文档为仓库 README，**请重命名为 `README.md` 使用**。

## 技术栈
Vue 3 + Vite 6 + Element Plus + Pinia + ECharts 5 + vuedraggable

## BI 功能页面（`src/views/bi/`）
| 页面 | 说明 |
|------|------|
| `query/` | 自然语言取数（NL2SQL 对话式提问 → SQL → 图表） |
| `alert/` | 异常预警规则配置与告警列表 |
| `ocr/` | OCR 文档识别（含服务健康检测） |
| `dashboard/` | 拖拽式 BI 大屏编排 |

## 目录约定
- 前端菜单由后端 `sys_menu` 表驱动，`loadView()` 自动匹配 `src/views/**/*.vue`
- 新增页面：建 `.vue` → 后端 `sys_menu` 插记录（`component` 填 `bi/xxx/index`）
- API 文件放 `src/api/bi/`，用 `export function` + `request.js` 封装
- 前端代理：`/dev-api` → `http://localhost:8080`

## 开发
```bash
npm install
npm run dev        # 开发模式
npm run build:prod  # 生产构建
npm test            # Vitest 单测（OCR 页面 5/5 全绿）
```

## 测试
- **前端单测**：Vitest + @vue/test-utils，**5/5 全绿**
- 覆盖：`el-alert` 文本重复渲染修复、健康检查成功/失败状态、时间格式化 `fmtTime` 边界
