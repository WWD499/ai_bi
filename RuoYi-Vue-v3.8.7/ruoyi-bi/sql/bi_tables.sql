-- AI智能BI数据分析平台 - 数据库表设计（PostgreSQL 15+）
-- 扩展：pgvector（向量存储）

-- 启用pgvector扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- =====================================================
-- 1. 数据源管理表
-- =====================================================
DROP TABLE IF EXISTS bi_datasource CASCADE;
CREATE TABLE bi_datasource (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    host VARCHAR(200) NOT NULL,
    port INT NOT NULL DEFAULT 3306,
    database_name VARCHAR(100) NOT NULL,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(500),
    jdbc_url VARCHAR(500),
    status INT DEFAULT 0,
    description VARCHAR(500),
    create_by VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500)
);

CREATE INDEX idx_bi_datasource_name ON bi_datasource(name);
CREATE INDEX idx_bi_datasource_type ON bi_datasource(type);

COMMENT ON TABLE bi_datasource IS 'BI数据源配置表';
COMMENT ON COLUMN bi_datasource.id IS '主键ID';
COMMENT ON COLUMN bi_datasource.name IS '数据源名称';
COMMENT ON COLUMN bi_datasource.type IS '类型：mysql、postgresql、oracle、sqlserver';
COMMENT ON COLUMN bi_datasource.host IS '主机地址';
COMMENT ON COLUMN bi_datasource.port IS '端口';
COMMENT ON COLUMN bi_datasource.database_name IS '数据库名';
COMMENT ON COLUMN bi_datasource.username IS '用户名';
COMMENT ON COLUMN bi_datasource.password IS '密码（AES加密存储）';
COMMENT ON COLUMN bi_datasource.jdbc_url IS 'JDBC连接串（自动生成）';
COMMENT ON COLUMN bi_datasource.status IS '状态：0-停用，1-启用';
COMMENT ON COLUMN bi_datasource.description IS '描述';
COMMENT ON COLUMN bi_datasource.create_by IS '创建者';
COMMENT ON COLUMN bi_datasource.create_time IS '创建时间';
COMMENT ON COLUMN bi_datasource.update_by IS '更新者';
COMMENT ON COLUMN bi_datasource.update_time IS '更新时间';
COMMENT ON COLUMN bi_datasource.remark IS '备注';

-- =====================================================
-- 2. 数据表结构缓存表
-- =====================================================
DROP TABLE IF EXISTS bi_table_metadata CASCADE;
CREATE TABLE bi_table_metadata (
    id BIGSERIAL PRIMARY KEY,
    datasource_id BIGINT NOT NULL,
    table_name VARCHAR(100) NOT NULL,
    table_comment VARCHAR(500),
    column_name VARCHAR(100) NOT NULL,
    column_type VARCHAR(50) NOT NULL,
    column_comment VARCHAR(500),
    is_primary INT DEFAULT 0,
    is_nullable INT DEFAULT 1,
    sample_data TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (datasource_id) REFERENCES bi_datasource(id) ON DELETE CASCADE
);

CREATE INDEX idx_bi_table_metadata_ds ON bi_table_metadata(datasource_id);
CREATE INDEX idx_bi_table_metadata_table ON bi_table_metadata(table_name);

COMMENT ON TABLE bi_table_metadata IS 'BI数据表结构缓存表';
COMMENT ON COLUMN bi_table_metadata.id IS '主键ID';
COMMENT ON COLUMN bi_table_metadata.datasource_id IS '数据源ID';
COMMENT ON COLUMN bi_table_metadata.table_name IS '表名';
COMMENT ON COLUMN bi_table_metadata.table_comment IS '表注释';
COMMENT ON COLUMN bi_table_metadata.column_name IS '列名';
COMMENT ON COLUMN bi_table_metadata.column_type IS '列类型';
COMMENT ON COLUMN bi_table_metadata.column_comment IS '列注释';
COMMENT ON COLUMN bi_table_metadata.is_primary IS '是否主键：0-否，1-是';
COMMENT ON COLUMN bi_table_metadata.is_nullable IS '是否可为空：0-否，1-是';
COMMENT ON COLUMN bi_table_metadata.sample_data IS '样例数据（JSON数组）';
COMMENT ON COLUMN bi_table_metadata.create_time IS '创建时间';
COMMENT ON COLUMN bi_table_metadata.update_time IS '更新时间';

-- =====================================================
-- 3. RAG知识库表（向量存储）
-- =====================================================
DROP TABLE IF EXISTS bi_knowledge CASCADE;
CREATE TABLE bi_knowledge (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    content_vector vector(1024),
    source_type VARCHAR(20),
    source_url VARCHAR(500),
    business_domain VARCHAR(100),
    tags VARCHAR(200),
    chunk_index INT DEFAULT 0,
    total_chunks INT DEFAULT 1,
    status INT DEFAULT 1,
    create_by VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500)
);

-- 向量索引（IVFFlat，适合中等规模向量）
CREATE INDEX idx_bi_knowledge_vector ON bi_knowledge USING ivfflat (content_vector vector_cosine_ops) WITH (lists = 100);

COMMENT ON TABLE bi_knowledge IS 'BI RAG知识库表（向量存储）';
COMMENT ON COLUMN bi_knowledge.id IS '主键ID';
COMMENT ON COLUMN bi_knowledge.title IS '文档标题';
COMMENT ON COLUMN bi_knowledge.content IS '文档内容（切片后）';
COMMENT ON COLUMN bi_knowledge.content_vector IS '内容向量（BGE-M3：1024维）';
COMMENT ON COLUMN bi_knowledge.source_type IS '来源类型：manual-手动录入、ocr-OCR识别、file-文件上传';
COMMENT ON COLUMN bi_knowledge.source_url IS '来源URL或文件路径';
COMMENT ON COLUMN bi_knowledge.business_domain IS '业务领域（如：财务、销售、库存等）';
COMMENT ON COLUMN bi_knowledge.tags IS '标签（逗号分隔）';
COMMENT ON COLUMN bi_knowledge.chunk_index IS '切片序号（同一文档的多个切片）';
COMMENT ON COLUMN bi_knowledge.total_chunks IS '总切片数';
COMMENT ON COLUMN bi_knowledge.status IS '状态：0-停用，1-启用';
COMMENT ON COLUMN bi_knowledge.create_by IS '创建者';
COMMENT ON COLUMN bi_knowledge.create_time IS '创建时间';
COMMENT ON COLUMN bi_knowledge.update_by IS '更新者';
COMMENT ON COLUMN bi_knowledge.update_time IS '更新时间';
COMMENT ON COLUMN bi_knowledge.remark IS '备注';

-- =====================================================
-- 4. 查询历史表
-- =====================================================
DROP TABLE IF EXISTS bi_query_history CASCADE;
CREATE TABLE bi_query_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    datasource_id BIGINT,
    natural_query TEXT NOT NULL,
    generated_sql TEXT,
    sql_valid INT DEFAULT 0,
    execution_time INT,
    result_count INT,
    chart_type VARCHAR(20),
    chart_config TEXT,
    status INT DEFAULT 1,
    error_message TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_bi_query_history_user ON bi_query_history(user_id);
CREATE INDEX idx_bi_query_history_ds ON bi_query_history(datasource_id);
CREATE INDEX idx_bi_query_history_time ON bi_query_history(create_time);

COMMENT ON TABLE bi_query_history IS 'BI查询历史表';
COMMENT ON COLUMN bi_query_history.id IS '主键ID';
COMMENT ON COLUMN bi_query_history.user_id IS '用户ID';
COMMENT ON COLUMN bi_query_history.datasource_id IS '数据源ID';
COMMENT ON COLUMN bi_query_history.natural_query IS '自然语言查询';
COMMENT ON COLUMN bi_query_history.generated_sql IS '生成的SQL';
COMMENT ON COLUMN bi_query_history.sql_valid IS 'SQL是否合法：0-否，1-是';
COMMENT ON COLUMN bi_query_history.execution_time IS '执行耗时（毫秒）';
COMMENT ON COLUMN bi_query_history.result_count IS '结果行数';
COMMENT ON COLUMN bi_query_history.chart_type IS '图表类型：bar、line、pie、scatter等';
COMMENT ON COLUMN bi_query_history.chart_config IS '图表配置（JSON格式）';
COMMENT ON COLUMN bi_query_history.status IS '状态：0-失败，1-成功';
COMMENT ON COLUMN bi_query_history.error_message IS '错误信息';
COMMENT ON COLUMN bi_query_history.create_time IS '创建时间';

-- =====================================================
-- 5. 异常预警配置表
-- =====================================================
DROP TABLE IF EXISTS bi_alert_config CASCADE;
CREATE TABLE bi_alert_config (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    datasource_id BIGINT NOT NULL,
    table_name VARCHAR(100) NOT NULL,
    condition_sql TEXT NOT NULL,
    threshold_value DECIMAL(20, 4),
    comparison_operator VARCHAR(10),
    check_interval INT DEFAULT 60,
    notify_type VARCHAR(50),
    notify_target VARCHAR(500),
    status INT DEFAULT 1,
    last_check_time TIMESTAMP,
    last_alert_time TIMESTAMP,
    create_by VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500),
    FOREIGN KEY (datasource_id) REFERENCES bi_datasource(id) ON DELETE CASCADE
);

CREATE INDEX idx_bi_alert_config_status ON bi_alert_config(status);
CREATE INDEX idx_bi_alert_config_next_check ON bi_alert_config(last_check_time);

COMMENT ON TABLE bi_alert_config IS 'BI异常预警配置表';
COMMENT ON COLUMN bi_alert_config.id IS '主键ID';
COMMENT ON COLUMN bi_alert_config.name IS '预警名称';
COMMENT ON COLUMN bi_alert_config.datasource_id IS '数据源ID';
COMMENT ON COLUMN bi_alert_config.table_name IS '监控的表名';
COMMENT ON COLUMN bi_alert_config.condition_sql IS '预警条件SQL';
COMMENT ON COLUMN bi_alert_config.threshold_value IS '阈值';
COMMENT ON COLUMN bi_alert_config.comparison_operator IS '比较运算符：>、<、>=、<=、=、!=';
COMMENT ON COLUMN bi_alert_config.check_interval IS '检查间隔（分钟）';
COMMENT ON COLUMN bi_alert_config.notify_type IS '通知方式：email、sms、wechat';
COMMENT ON COLUMN bi_alert_config.notify_target IS '通知目标（邮箱、手机号等）';
COMMENT ON COLUMN bi_alert_config.status IS '状态：0-停用，1-启用';
COMMENT ON COLUMN bi_alert_config.last_check_time IS '上次检查时间';
COMMENT ON COLUMN bi_alert_config.last_alert_time IS '上次预警时间';
COMMENT ON COLUMN bi_alert_config.create_by IS '创建者';
COMMENT ON COLUMN bi_alert_config.create_time IS '创建时间';
COMMENT ON COLUMN bi_alert_config.update_by IS '更新者';
COMMENT ON COLUMN bi_alert_config.update_time IS '更新时间';
COMMENT ON COLUMN bi_alert_config.remark IS '备注';

-- =====================================================
-- 6. BI大屏配置表
-- =====================================================
DROP TABLE IF EXISTS bi_dashboard CASCADE;
CREATE TABLE bi_dashboard (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    config_json TEXT NOT NULL,
    thumbnail VARCHAR(500),
    status INT DEFAULT 1,
    is_public INT DEFAULT 0,
    access_token VARCHAR(100),
    create_by VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500)
);

CREATE INDEX idx_bi_dashboard_name ON bi_dashboard(name);
CREATE INDEX idx_bi_dashboard_public ON bi_dashboard(is_public);

COMMENT ON TABLE bi_dashboard IS 'BI大屏配置表';
COMMENT ON COLUMN bi_dashboard.id IS '主键ID';
COMMENT ON COLUMN bi_dashboard.name IS '大屏名称';
COMMENT ON COLUMN bi_dashboard.description IS '描述';
COMMENT ON COLUMN bi_dashboard.config_json IS '大屏配置JSON，包含：布局（GridStack）、图表列表、数据源绑定等';
COMMENT ON COLUMN bi_dashboard.thumbnail IS '缩略图URL';
COMMENT ON COLUMN bi_dashboard.status IS '状态：0-停用，1-启用';
COMMENT ON COLUMN bi_dashboard.is_public IS '是否公开：0-否，1-是';
COMMENT ON COLUMN bi_dashboard.access_token IS '访问令牌（公开分享用）';
COMMENT ON COLUMN bi_dashboard.create_by IS '创建者';
COMMENT ON COLUMN bi_dashboard.create_time IS '创建时间';
COMMENT ON COLUMN bi_dashboard.update_by IS '更新者';
COMMENT ON COLUMN bi_dashboard.update_time IS '更新时间';
COMMENT ON COLUMN bi_dashboard.remark IS '备注';

-- =====================================================
-- 初始化数据
-- =====================================================

-- 插入示例数据源（MySQL本地）
INSERT INTO bi_datasource (name, type, host, port, database_name, username, password, status, description) 
VALUES ('本地MySQL', 'mysql', 'localhost', 3306, 'test', 'root', '123456', 1, '本地测试数据库');

-- 插入示例知识库条目
INSERT INTO bi_knowledge (title, content, source_type, business_domain, tags, chunk_index, total_chunks, status)
VALUES 
('销售数据统计规则', '销售数据统计规则：按月份统计销售额时，使用orders表的order_date字段，按MONTH()函数分组。销售额=SUM(amount)，其中amount字段为订单金额。', 'manual', '销售', '销售统计,月份统计', 0, 1, 1),
('库存预警规则', '库存预警规则：当inventory表的quantity字段小于min_threshold字段时，触发库存预警。建议定期检查库存，避免缺货。', 'manual', '库存', '库存预警,缺货', 0, 1, 1);

COMMIT;