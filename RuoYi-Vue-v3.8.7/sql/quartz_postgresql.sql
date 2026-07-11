-- ============================================
-- Quartz 定时任务表 PostgreSQL 版本
-- 基于官方 quartz-2.x.x/docs/dbTables/tables_postgresql.sql
-- ============================================

DROP TABLE IF EXISTS qrtz_fired_triggers;
DROP TABLE IF EXISTS qrtz_paused_trigger_grps;
DROP TABLE IF EXISTS qrtz_scheduler_state;
DROP TABLE IF EXISTS qrtz_locks;
DROP TABLE IF EXISTS qrtz_simple_triggers;
DROP TABLE IF EXISTS qrtz_simprop_triggers;
DROP TABLE IF EXISTS qrtz_cron_triggers;
DROP TABLE IF EXISTS qrtz_blob_triggers;
DROP TABLE IF EXISTS qrtz_triggers;
DROP TABLE IF EXISTS qrtz_job_details;
DROP TABLE IF EXISTS qrtz_calendars;

-- 1、存储每一个已配置的 jobDetail 的详细信息
CREATE TABLE qrtz_job_details (
    sched_name           VARCHAR(120)    NOT NULL,
    job_name             VARCHAR(200)    NOT NULL,
    job_group            VARCHAR(200)    NOT NULL,
    description          VARCHAR(250)    NULL,
    job_class_name       VARCHAR(250)    NOT NULL,
    is_durable          VARCHAR(1)      NOT NULL,
    is_nonconcurrent     VARCHAR(1)      NOT NULL,
    is_update_data       VARCHAR(1)      NOT NULL,
    requests_recovery    VARCHAR(1)      NOT NULL,
    job_data             BYTEA            NULL,
    PRIMARY KEY (sched_name, job_name, job_group)
);

COMMENT ON TABLE qrtz_job_details IS '任务详细信息表';

-- 2、存储已配置的 Trigger 的信息
CREATE TABLE qrtz_triggers (
    sched_name           VARCHAR(120)    NOT NULL,
    trigger_name         VARCHAR(200)    NOT NULL,
    trigger_group        VARCHAR(200)    NOT NULL,
    job_name             VARCHAR(200)    NULL,
    job_group            VARCHAR(200)    NULL,
    description          VARCHAR(250)    NULL,
    next_fire_time       BIGINT          NULL,
    prev_fire_time       BIGINT          NULL,
    priority             INTEGER         NULL,
    trigger_state        VARCHAR(16)     NOT NULL,
    trigger_type         VARCHAR(8)      NOT NULL,
    start_time           BIGINT          NOT NULL,
    end_time             BIGINT          NULL,
    calendar_name        VARCHAR(200)    NULL,
    misfire_instr        SMALLINT        NULL,
    job_data             BYTEA          NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group),
    FOREIGN KEY (sched_name, job_name, job_group) REFERENCES qrtz_job_details(sched_name, job_name, job_group)
);

COMMENT ON TABLE qrtz_triggers IS '触发器详细信息表';

-- 3、存储简单的 Trigger，包括重复次数，间隔，以及已触发的次数
CREATE TABLE qrtz_simple_triggers (
    sched_name           VARCHAR(120)    NOT NULL,
    trigger_name         VARCHAR(200)    NOT NULL,
    trigger_group        VARCHAR(200)    NOT NULL,
    repeat_count         BIGINT          NOT NULL,
    repeat_interval      BIGINT          NOT NULL,
    times_triggered      BIGINT          NOT NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group),
    FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES qrtz_triggers(sched_name, trigger_name, trigger_group)
);

-- 4、存储 Cron Trigger，包括 Cron 表达式和时区信息
CREATE TABLE qrtz_cron_triggers (
    sched_name           VARCHAR(120)    NOT NULL,
    trigger_name         VARCHAR(200)    NOT NULL,
    trigger_group        VARCHAR(200)    NOT NULL,
    cron_expression      VARCHAR(200)    NOT NULL,
    time_zone_id         VARCHAR(80)     NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group),
    FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES qrtz_triggers(sched_name, trigger_name, trigger_group)
);

COMMENT ON TABLE qrtz_cron_triggers IS 'Cron类型的触发器表';

-- 5、Trigger 作为 Bytea 类型存储
CREATE TABLE qrtz_blob_triggers (
    sched_name           VARCHAR(120)    NOT NULL,
    trigger_name         VARCHAR(200)    NOT NULL,
    trigger_group        VARCHAR(200)    NOT NULL,
    blob_data            BYTEA          NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group),
    FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES qrtz_triggers(sched_name, trigger_name, trigger_group)
);

-- 6、以 Bytea 类型存储存放日历信息
CREATE TABLE qrtz_calendars (
    sched_name           VARCHAR(120)    NOT NULL,
    calendar_name        VARCHAR(200)    NOT NULL,
    calendar             BYTEA         NOT NULL,
    PRIMARY KEY (sched_name, calendar_name)
);

-- 7、存储已暂停的 Trigger 组的信息
CREATE TABLE qrtz_paused_trigger_grps (
    sched_name           VARCHAR(120)    NOT NULL,
    trigger_group        VARCHAR(200)    NOT NULL,
    PRIMARY KEY (sched_name, trigger_group)
);

-- 8、存储与已触发的 Trigger 相关的状态信息
CREATE TABLE qrtz_fired_triggers (
    sched_name           VARCHAR(120)    NOT NULL,
    entry_id             VARCHAR(95)     NOT NULL,
    trigger_name         VARCHAR(200)    NOT NULL,
    trigger_group        VARCHAR(200)    NOT NULL,
    instance_name        VARCHAR(200)    NOT NULL,
    fired_time           BIGINT          NOT NULL,
    sched_time           BIGINT          NOT NULL,
    priority             INTEGER         NOT NULL,
    state                VARCHAR(16)     NOT NULL,
    job_name             VARCHAR(200)    NULL,
    job_group            VARCHAR(200)    NULL,
    is_nonconcurrent     VARCHAR(1)      NULL,
    requests_recovery    VARCHAR(1)      NULL,
    PRIMARY KEY (sched_name, entry_id)
);

-- 9、存储少量的有关 Scheduler 的状态信息
CREATE TABLE qrtz_scheduler_state (
    sched_name           VARCHAR(120)    NOT NULL,
    instance_name        VARCHAR(200)    NOT NULL,
    last_checkin_time    BIGINT          NOT NULL,
    checkin_interval     BIGINT          NOT NULL,
    PRIMARY KEY (sched_name, instance_name)
);

-- 10、存储程序的悲观锁的信息
CREATE TABLE qrtz_locks (
    sched_name           VARCHAR(120)    NOT NULL,
    lock_name            VARCHAR(40)     NOT NULL,
    PRIMARY KEY (sched_name, lock_name)
);

-- 11、Quartz 集群实现同步机制的行锁表（部分版本需要）
CREATE TABLE qrtz_simprop_triggers (
    sched_name           VARCHAR(120)    NOT NULL,
    trigger_name         VARCHAR(200)    NOT NULL,
    trigger_group        VARCHAR(200)    NOT NULL,
    str_prop_1           VARCHAR(512)    NULL,
    str_prop_2           VARCHAR(512)    NULL,
    str_prop_3           VARCHAR(512)    NULL,
    int_prop_1           INTEGER         NULL,
    int_prop_2           INTEGER         NULL,
    long_prop_1          BIGINT          NULL,
    long_prop_2          BIGINT          NULL,
    dec_prop_1           NUMERIC(13,4)   NULL,
    dec_prop_2           NUMERIC(13,4)   NULL,
    bool_prop_1          VARCHAR(1)      NULL,
    bool_prop_2          VARCHAR(1)      NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group),
    FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES qrtz_triggers(sched_name, trigger_name, trigger_group)
);
