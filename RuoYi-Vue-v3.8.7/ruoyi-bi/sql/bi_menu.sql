-- =============================================
-- BI模块菜单与权限配置 SQL（PostgreSQL）
-- 在 RuoYi 系统库 ry 中执行
-- =============================================

-- 一级目录：BI分析
insert into sys_menu values('2000', 'BI数据分析', '0', '5', 'bi', null, '', 1, 0, 'M', '0', '0', '', 'chart', 'admin', CURRENT_TIMESTAMP, '', null, 'BI数据分析平台');

-- 二级菜单：数据源管理
insert into sys_menu values('2001', '数据源管理', '2000', '1', 'datasource', 'bi/datasource/index', '', 1, 0, 'C', '0', '0', 'bi:datasource:list', 'server', 'admin', CURRENT_TIMESTAMP, '', null, '数据源管理菜单');
-- 数据源按钮权限
insert into sys_menu values('2002', '数据源查询', '2001', '1', '', '', '', 1, 0, 'F', '0', '0', 'bi:datasource:query',  '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
insert into sys_menu values('2003', '数据源新增', '2001', '2', '', '', '', 1, 0, 'F', '0', '0', 'bi:datasource:add',    '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
insert into sys_menu values('2004', '数据源修改', '2001', '3', '', '', '', 1, 0, 'F', '0', '0', 'bi:datasource:edit',   '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
insert into sys_menu values('2005', '数据源删除', '2001', '4', '', '', '', 1, 0, 'F', '0', '0', 'bi:datasource:remove', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');

-- 二级菜单：自然语言查询
insert into sys_menu values('2006', '自然语言查询', '2000', '2', 'query', 'bi/query/index', '', 1, 0, 'C', '0', '0', 'bi:query:list', 'search', 'admin', CURRENT_TIMESTAMP, '', null, '自然语言查询菜单');

-- 二级菜单：知识库管理
insert into sys_menu values('2007', '知识库管理', '2000', '3', 'knowledge', 'bi/knowledge/index', '', 1, 0, 'C', '0', '0', 'bi:knowledge:list', 'education', 'admin', CURRENT_TIMESTAMP, '', null, '知识库管理菜单');

-- 为普通角色(role_id=2)分配BI菜单权限
insert into sys_role_menu values ('2', '2000');
insert into sys_role_menu values ('2', '2001');
insert into sys_role_menu values ('2', '2002');
insert into sys_role_menu values ('2', '2003');
insert into sys_role_menu values ('2', '2004');
insert into sys_role_menu values ('2', '2005');
insert into sys_role_menu values ('2', '2006');
insert into sys_role_menu values ('2', '2007');
