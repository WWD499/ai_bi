-- =====================================================
-- BI模块测试数据（MySQL 8.0+）
-- 用途：为BI智能分析模块提供可查询的业务数据
-- 执行前请确认当前连接的是目标数据库（如 test）
-- =====================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. 部门表
-- =====================================================
DROP TABLE IF EXISTS demo_department;
CREATE TABLE demo_department (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50) NOT NULL COMMENT '部门名称',
    manager     VARCHAR(50) COMMENT '部门负责人',
    location    VARCHAR(100) COMMENT '办公地点'
) ENGINE=InnoDB COMMENT='测试-部门表';

INSERT INTO demo_department (name, manager, location) VALUES
('技术部',   '张伟',   '北京总部A座3层'),
('销售部',   '李娜',   '北京总部A座5层'),
('市场部',   '王芳',   '上海分部2层'),
('财务部',   '赵敏',   '北京总部A座4层'),
('人力资源', '刘洋',   '北京总部A座2层'),
('运营部',   '陈静',   '上海分部3层'),
('产品部',   '杨磊',   '北京总部A座3层'),
('客服部',   '周丽',   '广州分部1层');

-- =====================================================
-- 2. 员工表
-- =====================================================
DROP TABLE IF EXISTS demo_employee;
CREATE TABLE demo_employee (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(50) NOT NULL COMMENT '姓名',
    gender        VARCHAR(5) COMMENT '性别',
    age           INT COMMENT '年龄',
    department_id INT COMMENT '部门ID',
    position      VARCHAR(50) COMMENT '职位',
    salary        DECIMAL(12, 2) COMMENT '月薪（元）',
    hire_date     DATE COMMENT '入职日期',
    phone         VARCHAR(20) COMMENT '电话',
    email         VARCHAR(100) COMMENT '邮箱'
) ENGINE=InnoDB COMMENT='测试-员工表';

INSERT INTO demo_employee (name, gender, age, department_id, position, salary, hire_date, phone, email) VALUES
('张三',   '男', 28, 1, '高级工程师',   25000.00, '2020-03-15', '13800001001', 'zhangsan@test.com'),
('李四',   '男', 32, 1, '技术经理',     35000.00, '2018-07-01', '13800001002', 'lisi@test.com'),
('王五',   '女', 26, 1, '初级工程师',   15000.00, '2022-01-10', '13800001003', 'wangwu@test.com'),
('赵六',   '男', 30, 2, '销售主管',     20000.00, '2019-05-20', '13800001004', 'zhaoliu@test.com'),
('钱七',   '女', 27, 2, '销售代表',     12000.00, '2021-09-01', '13800001005', 'qianqi@test.com'),
('孙八',   '男', 35, 2, '大客户经理',   30000.00, '2017-11-15', '13800001006', 'sunba@test.com'),
('周九',   '女', 29, 3, '市场专员',     14000.00, '2020-08-01', '13800001007', 'zhoujiu@test.com'),
('吴十',   '男', 33, 3, '市场经理',     28000.00, '2018-04-10', '13800001008', 'wushi@test.com'),
('郑十一', '女', 25, 4, '会计',         13000.00, '2022-06-15', '13800001009', 'zheng11@test.com'),
('冯十二', '男', 38, 4, '财务总监',     40000.00, '2016-02-01', '13800001010', 'feng12@test.com'),
('陈十三', '女', 31, 5, 'HR专员',       14000.00, '2019-10-01', '13800001011', 'chen13@test.com'),
('卫十四', '男', 29, 6, '运营专员',     16000.00, '2021-03-20', '13800001012', 'wei14@test.com'),
('蒋十五', '女', 34, 6, '运营经理',     26000.00, '2018-12-01', '13800001013', 'jiang15@test.com'),
('沈十六', '男', 27, 7, '产品经理',     22000.00, '2020-05-10', '13800001014', 'shen16@test.com'),
('韩十七', '女', 24, 7, '产品助理',     12000.00, '2023-02-01', '13800001015', 'han17@test.com'),
('杨十八', '男', 36, 1, '架构师',       45000.00, '2017-06-01', '13800001016', 'yang18@test.com'),
('朱十九', '女', 28, 8, '客服组长',     13000.00, '2021-07-15', '13800001017', 'zhu19@test.com'),
('秦二十', '男', 31, 2, '销售代表',     11000.00, '2022-04-01', '13800001018', 'qin20@test.com'),
('许二一', '女', 26, 3, '市场助理',     10000.00, '2023-01-10', '13800001019', 'xu21@test.com'),
('何二二', '男', 33, 1, '中级工程师',   20000.00, '2019-08-15', '13800001020', 'he22@test.com');

-- =====================================================
-- 3. 产品表
-- =====================================================
DROP TABLE IF EXISTS demo_product;
CREATE TABLE demo_product (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL COMMENT '产品名称',
    category      VARCHAR(50) COMMENT '产品分类',
    price         DECIMAL(10, 2) COMMENT '售价',
    cost          DECIMAL(10, 2) COMMENT '成本价',
    stock         INT DEFAULT 0 COMMENT '库存数量',
    unit          VARCHAR(20) COMMENT '单位',
    create_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB COMMENT='测试-产品表';

INSERT INTO demo_product (name, category, price, cost, stock, unit) VALUES
('笔记本电脑 Pro',   '电子产品', 8999.00,  5500.00,  150, '台'),
('无线蓝牙耳机',     '电子产品', 599.00,   220.00,   500, '副'),
('机械键盘',         '电子产品', 399.00,   150.00,   300, '个'),
('智能手表',         '电子产品', 1299.00,  680.00,   200, '块'),
('办公椅 人体工学',  '办公家具', 1599.00,  800.00,   80,  '把'),
('升降办公桌',       '办公家具', 2999.00,  1500.00,  50,  '张'),
('A4打印纸（5箱）',  '办公耗材', 120.00,   65.00,    1000,'箱'),
('墨盒套装',         '办公耗材', 180.00,   90.00,    400, '套'),
('投影仪 4K',        '电子产品', 5999.00,  3200.00,  30,  '台'),
('白板 120x90cm',   '办公家具', 299.00,   120.00,   100, '块'),
('USB-C扩展坞',      '电子产品', 259.00,   100.00,   350, '个'),
('防蓝光眼镜',       '电子产品', 199.00,   60.00,    600, '副');

-- =====================================================
-- 4. 客户表
-- =====================================================
DROP TABLE IF EXISTS demo_customer;
CREATE TABLE demo_customer (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL COMMENT '客户姓名',
    company       VARCHAR(200) COMMENT '公司',
    industry      VARCHAR(50) COMMENT '行业',
    region        VARCHAR(50) COMMENT '区域',
    level         VARCHAR(10) COMMENT '客户等级：A/B/C',
    phone         VARCHAR(20) COMMENT '电话',
    email         VARCHAR(100) COMMENT '邮箱',
    create_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB COMMENT='测试-客户表';

INSERT INTO demo_customer (name, company, industry, region, level, phone, email) VALUES
('刘总',   '华创科技有限公司',     '互联网',   '华北', 'A', '13900001001', 'liu@huachuang.com'),
('陈总',   '盛达集团',             '制造业',   '华东', 'A', '13900001002', 'chen@shengda.com'),
('王经理', '中联物流有限公司',     '物流',     '华南', 'B', '13900001003', 'wang@zhonglian.com'),
('李总',   '天宇教育集团',         '教育',     '华北', 'B', '13900001004', 'li@tianyu.com'),
('张经理', '博远咨询有限公司',     '咨询',     '华东', 'C', '13900001005', 'zhang@boyuan.com'),
('赵总',   '鑫源贸易有限公司',     '贸易',     '华南', 'A', '13900001006', 'zhao@xinyuan.com'),
('孙经理', '恒信地产',             '房地产',   '华北', 'B', '13900001007', 'sun@hengxin.com'),
('周总',   '蓝海生物科技',         '医疗',     '华东', 'A', '13900001008', 'zhou@lanhai.com'),
('吴经理', '金桥零售连锁',         '零售',     '西南', 'C', '13900001009', 'wu@jinqiao.com'),
('郑总',   '明远文化传媒',         '传媒',     '华东', 'C', '13900001010', 'zheng@mingyuan.com'),
('钱经理', '宏达建设工程',         '建筑',     '华北', 'B', '13900001011', 'qian@hongda.com'),
('冯总',   '绿源农业开发',         '农业',     '西南', 'C', '13900001012', 'feng@lvyuan.com');

-- =====================================================
-- 5. 订单表（核心分析表）
-- =====================================================
DROP TABLE IF EXISTS demo_order;
CREATE TABLE demo_order (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    order_no      VARCHAR(30) NOT NULL COMMENT '订单编号',
    customer_id   INT COMMENT '客户ID',
    product_id    INT COMMENT '产品ID',
    quantity      INT NOT NULL COMMENT '数量',
    unit_price    DECIMAL(10, 2) NOT NULL COMMENT '单价',
    total_amount  DECIMAL(12, 2) NOT NULL COMMENT '总金额',
    order_date    DATE NOT NULL COMMENT '订单日期',
    status        VARCHAR(10) DEFAULT '已完成' COMMENT '状态：已完成、已退货、进行中',
    salesperson   VARCHAR(50) COMMENT '销售人员',
    region        VARCHAR(50) COMMENT '区域',
    create_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB COMMENT='测试-订单表';

-- 使用存储过程批量生成2024年全年订单数据（约400条）
DELIMITER //
CREATE PROCEDURE generate_orders()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE v_customer_id INT;
    DECLARE v_product_id INT;
    DECLARE v_quantity INT;
    DECLARE v_price DECIMAL(10,2);
    DECLARE v_order_date DATE;
    DECLARE v_status VARCHAR(10);
    DECLARE v_salesperson VARCHAR(50);
    DECLARE v_region VARCHAR(50);
    DECLARE v_order_no VARCHAR(30);
    DECLARE v_rand INT;

    WHILE i < 400 DO
        SET v_customer_id = 1 + FLOOR(RAND() * 12);
        SET v_product_id = 1 + FLOOR(RAND() * 12);
        SET v_quantity = 1 + FLOOR(RAND() * 20);
        SET v_order_date = DATE_ADD('2024-01-05', INTERVAL FLOOR(RAND() * 358) DAY);

        -- 根据product_id确定单价
        SET v_price = CASE v_product_id
            WHEN 1  THEN 8999.00  WHEN 2  THEN 599.00   WHEN 3  THEN 399.00
            WHEN 4  THEN 1299.00  WHEN 5  THEN 1599.00  WHEN 6  THEN 2999.00
            WHEN 7  THEN 120.00   WHEN 8  THEN 180.00   WHEN 9  THEN 5999.00
            WHEN 10 THEN 299.00   WHEN 11 THEN 259.00   WHEN 12 THEN 199.00
            ELSE 999.00
        END;

        -- 状态：80%已完成，10%已退货，10%进行中
        SET v_rand = FLOOR(RAND() * 10);
        SET v_status = CASE
            WHEN v_rand < 8 THEN '已完成'
            WHEN v_rand = 8 THEN '已退货'
            ELSE '进行中'
        END;

        -- 销售员
        SET v_salesperson = ELT(1 + FLOOR(RAND() * 4), '赵六', '钱七', '孙八', '秦二十');

        -- 区域
        SET v_region = ELT(1 + FLOOR(RAND() * 4), '华北', '华东', '华南', '西南');

        -- 订单编号
        SET v_order_no = CONCAT('ORD-', DATE_FORMAT(v_order_date, '%Y%m%d'), '-', LPAD(i + 1, 4, '0'));

        INSERT INTO demo_order (order_no, customer_id, product_id, quantity, unit_price, total_amount, order_date, status, salesperson, region)
        VALUES (v_order_no, v_customer_id, v_product_id, v_quantity, v_price, v_quantity * v_price, v_order_date, v_status, v_salesperson, v_region);

        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

CALL generate_orders();
DROP PROCEDURE IF EXISTS generate_orders;

-- =====================================================
-- 6. 月度营收汇总表
-- =====================================================
DROP TABLE IF EXISTS demo_monthly_revenue;
CREATE TABLE demo_monthly_revenue (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    `year_month`  VARCHAR(7) NOT NULL COMMENT '年月（YYYY-MM）',
    `category`    VARCHAR(50) NOT NULL COMMENT '产品分类',
    `revenue`     DECIMAL(14, 2) DEFAULT 0 COMMENT '营收',
    `cost`        DECIMAL(14, 2) DEFAULT 0 COMMENT '成本',
    `profit`      DECIMAL(14, 2) DEFAULT 0 COMMENT '利润',
    `order_count` INT DEFAULT 0 COMMENT '订单数'
) ENGINE=InnoDB COMMENT='测试-月度营收汇总';

INSERT INTO demo_monthly_revenue (`year_month`, `category`, `revenue`, `cost`, `profit`, `order_count`) VALUES
('2024-01', '电子产品', 285000.00, 152000.00, 133000.00, 42),
('2024-01', '办公家具', 98000.00,  48000.00,  50000.00,  15),
('2024-01', '办公耗材', 32000.00,  16000.00,  16000.00,  28),
('2024-02', '电子产品', 310000.00, 168000.00, 142000.00, 48),
('2024-02', '办公家具', 105000.00, 52000.00,  53000.00,  18),
('2024-02', '办公耗材', 28000.00,  14000.00,  14000.00,  25),
('2024-03', '电子产品', 420000.00, 225000.00, 195000.00, 65),
('2024-03', '办公家具', 135000.00, 68000.00,  67000.00,  22),
('2024-03', '办公耗材', 41000.00,  20000.00,  21000.00,  35),
('2024-04', '电子产品', 365000.00, 195000.00, 170000.00, 55),
('2024-04', '办公家具', 112000.00, 56000.00,  56000.00,  19),
('2024-04', '办公耗材', 38000.00,  19000.00,  19000.00,  32),
('2024-05', '电子产品', 398000.00, 210000.00, 188000.00, 60),
('2024-05', '办公家具', 128000.00, 64000.00,  64000.00,  21),
('2024-05', '办公耗材', 45000.00,  22000.00,  23000.00,  38),
('2024-06', '电子产品', 450000.00, 240000.00, 210000.00, 70),
('2024-06', '办公家具', 142000.00, 72000.00,  70000.00,  24),
('2024-06', '办公耗材', 52000.00,  26000.00,  26000.00,  42),
('2024-07', '电子产品', 480000.00, 255000.00, 225000.00, 75),
('2024-07', '办公家具', 155000.00, 78000.00,  77000.00,  26),
('2024-07', '办公耗材', 48000.00,  24000.00,  24000.00,  40),
('2024-08', '电子产品', 435000.00, 230000.00, 205000.00, 68),
('2024-08', '办公家具', 138000.00, 70000.00,  68000.00,  23),
('2024-08', '办公耗材', 42000.00,  21000.00,  21000.00,  36),
('2024-09', '电子产品', 510000.00, 270000.00, 240000.00, 80),
('2024-09', '办公家具', 168000.00, 85000.00,  83000.00,  28),
('2024-09', '办公耗材', 55000.00,  27000.00,  28000.00,  45),
('2024-10', '电子产品', 520000.00, 278000.00, 242000.00, 82),
('2024-10', '办公家具', 175000.00, 88000.00,  87000.00,  30),
('2024-10', '办公耗材', 58000.00,  29000.00,  29000.00,  48),
('2024-11', '电子产品', 580000.00, 310000.00, 270000.00, 92),
('2024-11', '办公家具', 195000.00, 98000.00,  97000.00,  33),
('2024-11', '办公耗材', 62000.00,  31000.00,  31000.00,  52),
('2024-12', '电子产品', 620000.00, 330000.00, 290000.00, 98),
('2024-12', '办公家具', 210000.00, 105000.00, 105000.00, 35),
('2024-12', '办公耗材', 68000.00,  34000.00,  34000.00,  55);

-- =====================================================
-- 完成：验证数据量
-- =====================================================
SELECT 'demo_department' AS table_name, COUNT(*) AS row_count FROM demo_department
UNION ALL
SELECT 'demo_employee', COUNT(*) FROM demo_employee
UNION ALL
SELECT 'demo_product', COUNT(*) FROM demo_product
UNION ALL
SELECT 'demo_customer', COUNT(*) FROM demo_customer
UNION ALL
SELECT 'demo_order', COUNT(*) FROM demo_order
UNION ALL
SELECT 'demo_monthly_revenue', COUNT(*) FROM demo_monthly_revenue
ORDER BY table_name;

SET FOREIGN_KEY_CHECKS = 1;
