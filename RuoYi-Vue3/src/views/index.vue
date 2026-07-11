<template>
  <div class="app-container home">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <h1>AI智能BI数据分析平台</h1>
        <p>基于大模型 + RAG的智能商业智能分析系统，让数据驱动决策更简单</p>
      </div>
      <div class="welcome-icon">
        <el-icon :size="80" color="#409EFF"><DataAnalysis /></el-icon>
      </div>
    </div>

    <!-- 快捷统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :xs="12" :sm="6">
        <div class="stat-card" @click="router.push('/bi/datasource')">
          <div class="stat-icon" style="background: #e8f4ff;">
            <el-icon :size="28" color="#409EFF"><Coin /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">数据源管理</div>
            <div class="stat-desc">配置和管理业务数据库</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card" @click="router.push('/bi/query')">
          <div class="stat-icon" style="background: #f0f9eb;">
            <el-icon :size="28" color="#67C23A"><ChatDotRound /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">智能查询</div>
            <div class="stat-desc">自然语言转SQL即时查询</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card" @click="router.push('/bi/alert')">
          <div class="stat-icon" style="background: #fdf6ec;">
            <el-icon :size="28" color="#E6A23C"><BellFilled /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">数据预警</div>
            <div class="stat-desc">智能监控与异常告警</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card" @click="router.push('/bi/dashboard')">
          <div class="stat-icon" style="background: #fef0f0;">
            <el-icon :size="28" color="#F56C6C"><Monitor /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">BI大屏</div>
            <div class="stat-desc">可视化拖拽数据看板</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 平台功能介绍 -->
    <el-row :gutter="20" class="feature-row">
      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="hover" class="feature-card">
          <template #header>
            <div class="card-header">
              <el-icon :size="22" color="#409EFF"><ChatDotRound /></el-icon>
              <span>自然语言转SQL</span>
            </div>
          </template>
          <p>用户输入自然语言问题，大模型自动理解意图并生成对应SQL查询语句，无需编写代码即可获取数据。</p>
          <div class="tech-tags">
            <el-tag size="small" type="primary">DeepSeek大模型</el-tag>
            <el-tag size="small" type="success">RestTemplate</el-tag>
            <el-tag size="small" type="info">Ark API</el-tag>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="hover" class="feature-card">
          <template #header>
            <div class="card-header">
              <el-icon :size="22" color="#67C23A"><DataAnalysis /></el-icon>
              <span>智能图表生成</span>
            </div>
          </template>
          <p>根据查询结果自动选择最佳图表类型（柱状图、折线图、饼图等），支持用户意图识别，一键生成可视化报表。</p>
          <div class="tech-tags">
            <el-tag size="small" type="primary">ECharts 5.6</el-tag>
            <el-tag size="small" type="success">意图识别</el-tag>
            <el-tag size="small" type="info">6种图表</el-tag>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="hover" class="feature-card">
          <template #header>
            <div class="card-header">
              <el-icon :size="22" color="#E6A23C"><BellFilled /></el-icon>
              <span>数据异常预警</span>
            </div>
          </template>
          <p>配置监控规则，定时扫描业务数据，当指标超出阈值时自动触发预警，并调用AI进行根因分析。</p>
          <div class="tech-tags">
            <el-tag size="small" type="primary">Quartz定时任务</el-tag>
            <el-tag size="small" type="success">AI根因分析</el-tag>
            <el-tag size="small" type="info">多级告警</el-tag>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="hover" class="feature-card">
          <template #header>
            <div class="card-header">
              <el-icon :size="22" color="#F56C6C"><Monitor /></el-icon>
              <span>BI可视化大屏</span>
            </div>
          </template>
          <p>拖拽式布局，自由组合图表、图片等组件，打造专业数据大屏。支持预览模式和实时数据刷新。</p>
          <div class="tech-tags">
            <el-tag size="small" type="primary">vue-grid-layout</el-tag>
            <el-tag size="small" type="success">拖拽布局</el-tag>
            <el-tag size="small" type="info">图片Widget</el-tag>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="hover" class="feature-card">
          <template #header>
            <div class="card-header">
              <el-icon :size="22" color="#909399"><Document /></el-icon>
              <span>RAG业务知识库</span>
            </div>
          </template>
          <p>上传业务文档，系统自动解析并构建知识库，辅助大模型理解业务术语，提升SQL生成准确率。</p>
          <div class="tech-tags">
            <el-tag size="small" type="primary">方案B注入</el-tag>
            <el-tag size="small" type="success">OCR识别</el-tag>
            <el-tag size="small" type="info">PaddleOCR</el-tag>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="hover" class="feature-card">
          <template #header>
            <div class="card-header">
              <el-icon :size="22" color="#9C27B0"><Coin /></el-icon>
              <span>多数据源接入</span>
            </div>
          </template>
          <p>支持MySQL、PostgreSQL等多种数据库动态接入，通过统一元数据管理实现跨数据源查询能力。</p>
          <div class="tech-tags">
            <el-tag size="small" type="primary">动态数据源</el-tag>
            <el-tag size="small" type="success">JDBC</el-tag>
            <el-tag size="small" type="info">元数据管理</el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 技术架构 -->
    <el-card shadow="never" class="arch-card">
      <template #header>
        <div class="card-header">
          <el-icon :size="20" color="#409EFF"><Setting /></el-icon>
          <span>技术架构</span>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6">
          <div class="arch-item">
            <div class="arch-title">前端技术</div>
            <ul>
              <li>Vue 3.5 + Element Plus 2.13</li>
              <li>Vite 6.4 + Pinia 3.0</li>
              <li>ECharts 5.6 + vue-grid-layout</li>
              <li>RuoYi-Vue3 框架</li>
            </ul>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="arch-item">
            <div class="arch-title">后端技术</div>
            <ul>
              <li>SpringBoot + RuoYi-Vue v3.8.7</li>
              <li>MyBatis + Druid 连接池</li>
              <li>Quartz 定时任务调度</li>
              <li>RestTemplate 大模型调用</li>
            </ul>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="arch-item">
            <div class="arch-title">数据存储</div>
            <ul>
              <li>PostgreSQL 17 + pgvector</li>
              <li>MySQL 8（业务数据源）</li>
              <li>Redis 5（缓存/会话）</li>
              <li>一库双用架构</li>
            </ul>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="arch-item">
            <div class="arch-title">AI能力</div>
            <ul>
              <li>火山方舟 Ark API</li>
              <li>DeepSeek 大模型</li>
              <li>PaddleOCR 本地部署</li>
              <li>BGE-M3 Embedding</li>
            </ul>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup name="Index">
const router = useRouter()
</script>

<style scoped lang="scss">
.home {
  padding: 16px;
}

.welcome-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 32px 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  color: #fff;

  .welcome-content {
    h1 {
      font-size: 28px;
      margin: 0 0 8px 0;
      font-weight: 700;
    }
    p {
      font-size: 14px;
      margin: 0;
      opacity: 0.9;
    }
  }
  .welcome-icon {
    flex-shrink: 0;
  }
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 16px;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
  }

  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .stat-info {
    .stat-label {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 4px;
    }
    .stat-desc {
      font-size: 12px;
      color: #909399;
    }
  }
}

.feature-row {
  margin-bottom: 20px;
}

.feature-card {
  margin-bottom: 16px;
  min-height: 200px;

  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
  }

  p {
    font-size: 13px;
    color: #606266;
    line-height: 1.8;
    margin: 0 0 12px 0;
  }

  .tech-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
  }
}

.arch-card {
  margin-bottom: 20px;

  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
  }
}

.arch-item {
  .arch-title {
    font-size: 14px;
    font-weight: 600;
    color: #409EFF;
    margin-bottom: 10px;
    padding-bottom: 6px;
    border-bottom: 2px solid #ecf0f1;
  }
  ul {
    list-style: none;
    padding: 0;
    margin: 0;
    li {
      font-size: 13px;
      color: #606266;
      padding: 4px 0;
      &::before {
        content: "▪";
        color: #409EFF;
        margin-right: 6px;
      }
    }
  }
}
</style>
