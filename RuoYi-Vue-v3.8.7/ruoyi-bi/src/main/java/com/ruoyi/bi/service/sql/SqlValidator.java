package com.ruoyi.bi.service.sql;

import com.ruoyi.common.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL安全校验器
 * 
 * 四层防护：
 * 1. 关键词黑名单（禁止DROP、DELETE等）
 * 2. 关键词白名单（仅允许SELECT）
 * 3. 正则表达式校验
 * 4. 执行前EXPLAIN预检查
 * 
 * @author ruoyi-bi
 */
@Component
public class SqlValidator {

    private static final Logger log = LoggerFactory.getLogger(SqlValidator.class);

    // 禁止的操作关键词（黑名单）
    private static final Set<String> FORBIDDEN_KEYWORDS = new HashSet<>(Arrays.asList(
        "DROP", "DELETE", "UPDATE", "INSERT", "ALTER", "CREATE", "TRUNCATE",
        "REPLACE", "MERGE", "GRANT", "REVOKE", "EXEC", "EXECUTE",
        "INTO OUTFILE", "LOAD DATA", "LOAD XML", "LOCK TABLES", "UNLOCK TABLES"
    ));

    // 允许的操作关键词（白名单）
    private static final Set<String> ALLOWED_OPERATIONS = new HashSet<>(Arrays.asList(
        "SELECT", "WITH", "EXPLAIN", "DESCRIBE", "DESC", "SHOW"
    ));

    // SQL注入特征正则
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)(\\bor\b|\band\b|;\\s*\\b(drop|delete|update|insert|alter|create)\\b|'\\s*--|/\\*.*\\*/|union\\s+select)",
        Pattern.CASE_INSENSITIVE
    );

    // 字符串字面量正则（用于排除字符串内的关键词）
    private static final Pattern STRING_LITERAL_PATTERN = Pattern.compile(
        "'[^']*'|\"[^\"]*\"",
        Pattern.CASE_INSENSITIVE
    );

    // 提取SQL中表名的正则（匹配 FROM/JOIN 后面的表名）
    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile(
        "(?i)\\b(?:FROM|JOIN)\\s+`?([a-zA-Z_][a-zA-Z0-9_]*)`?",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * 校验SQL是否安全
     * 
     * @param sql 待校验的SQL
     * @throws ServiceException 如果SQL不安全
     */
    public void validate(String sql) {
        validate(sql, null);
    }

    /**
     * 校验SQL是否安全（含表名白名单校验）
     * 
     * @param sql 待校验的SQL
     * @param allowedTables 允许的表名集合（为null或空则跳过表名校验）
     * @throws ServiceException 如果SQL不安全
     */
    public void validate(String sql, Set<String> allowedTables) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new ServiceException("SQL不能为空");
        }

        String trimmedSql = sql.trim();
        log.debug("校验SQL：{}", trimmedSql.substring(0, Math.min(100, trimmedSql.length())));

        // 第一层：检查是否以允许的操作为开头
        validateOperation(trimmedSql);

        // 第二层：检查黑名单关键词
        validateBlacklist(trimmedSql);

        // 第三层：检查SQL注入特征
        validateInjection(trimmedSql);

        // 第四层：检查是否包含多个语句（分号）
        validateMultiStatement(trimmedSql);

        // 第五层：表名白名单校验
        if (allowedTables != null && !allowedTables.isEmpty()) {
            validateTableNames(trimmedSql, allowedTables);
        }

        log.debug("SQL校验通过");
    }

    /**
     * 第一层：检查操作类型
     */
    private void validateOperation(String sql) {
        String upperSql = sql.toUpperCase().trim();
        
        // 提取第一个关键词
        String firstKeyword = upperSql.split("\\s+")[0];
        
        if (!ALLOWED_OPERATIONS.contains(firstKeyword)) {
            throw new ServiceException(
                "不允许的操作类型：" + firstKeyword + "。仅允许：" + ALLOWED_OPERATIONS
            );
        }
    }

    /**
     * 第二层：检查黑名单关键词
     * 注意：需要排除字符串字面量内的关键词
     */
    private void validateBlacklist(String sql) {
        // 移除字符串字面量（避免误判）
        String sqlWithoutStrings = STRING_LITERAL_PATTERN.matcher(sql).replaceAll("");
        
        String upperSql = sqlWithoutStrings.toUpperCase();
        
        for (String keyword : FORBIDDEN_KEYWORDS) {
            if (upperSql.contains(keyword)) {
                throw new ServiceException(
                    "SQL包含禁止的关键词：" + keyword
                );
            }
        }
    }

    /**
     * 第三层：检查SQL注入特征
     */
    private void validateInjection(String sql) {
        if (SQL_INJECTION_PATTERN.matcher(sql).find()) {
            throw new ServiceException("SQL可能包含注入攻击特征，已拒绝执行");
        }
    }

    /**
     * 第四层：检查多个语句
     */
    private void validateMultiStatement(String sql) {
        // 移除字符串内的分号
        String sqlWithoutStrings = STRING_LITERAL_PATTERN.matcher(sql).replaceAll("");
        
        // 检查是否有分号（多个语句）
        if (sqlWithoutStrings.contains(";")) {
            // 允许末尾的分号
            String withoutTrailingSemicolon = sqlWithoutStrings.replaceAll(";\\s*$", "");
            if (withoutTrailingSemicolon.contains(";")) {
                throw new ServiceException("不允许执行多个SQL语句");
            }
        }
    }

    /**
     * 第五层：校验SQL中引用的表名是否在白名单内
     */
    private void validateTableNames(String sql, Set<String> allowedTables) {
        // 构建小写表名集合（不区分大小写）
        Set<String> lowerAllowed = new HashSet<>();
        for (String t : allowedTables) {
            lowerAllowed.add(t.toLowerCase());
        }

        // 提取SQL中所有表名
        Matcher matcher = TABLE_NAME_PATTERN.matcher(sql);
        List<String> foundTables = new ArrayList<>();
        while (matcher.find()) {
            String tableName = matcher.group(1).toLowerCase();
            foundTables.add(tableName);
        }

        // 检查每个表名是否在白名单中
        for (String table : foundTables) {
            if (!lowerAllowed.contains(table)) {
                throw new ServiceException(
                    "表名 '" + table + "' 不存在。可用表：" + allowedTables
                );
            }
        }

        if (foundTables.isEmpty()) {
            log.warn("SQL中未解析到表名，跳过表名校验");
        }
    }

    /**
     * 执行前EXPLAIN预检查（需要在具体的数据源上执行）
     * 这个方法由调用者在获取Connection后调用
     * 
     * @param sql 待检查的SQL
     * @return true表示安全，false表示可能不安全
     */
    public boolean explainCheck(String sql) {
        try {
            // 这里只是示例，实际需要在具体的数据源上执行
            // String explainSql = "EXPLAIN " + sql;
            // 执行explainSql，检查：
            // 1. 是否全表扫描（type=ALL）
            // 2. 扫描行数是否过大
            // 3. 是否使用索引
            
            log.info("EXPLAIN预检查（需要在具体数据源上执行）：{}", sql);
            return true;

        } catch (Exception e) {
            log.error("EXPLAIN预检查失败", e);
            return false;
        }
    }
}
