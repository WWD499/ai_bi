package com.ruoyi.bi.util;

import com.ruoyi.bi.domain.BiDatasource;

/**
 * JDBC URL 构建工具（消除 BI 模块内 4 处重复的拼接逻辑）
 * <p>
 * 关键约定：
 * 1) MySQL 8.x 的 caching_sha2_password 在 useSSL=false 时必须带 allowPublicKeyRetrieval=true；
 * 2) PostgreSQL 的 catalog 必须为 ""（由连接绑定当前库），传 databaseName 会拿不到表结构。
 *
 * @author ruoyi-bi
 */
public final class JdbcUrlBuilder {

    private JdbcUrlBuilder() {
    }

    /** 根据数据源构建完整 JDBC URL（自动为 MySQL 补齐 allowPublicKeyRetrieval） */
    public static String build(BiDatasource ds) {
        if (ds.getJdbcUrl() != null && !ds.getJdbcUrl().trim().isEmpty()) {
            String url = ds.getJdbcUrl().trim();
            if (url.contains("mysql") && !url.contains("allowPublicKeyRetrieval")) {
                url += (url.contains("?") ? "&" : "?") + "allowPublicKeyRetrieval=true&useSSL=false";
            }
            return url;
        }
        String type = isPostgres(ds) ? "postgresql" : "mysql";
        String host = ds.getHost() != null ? ds.getHost() : "localhost";
        int port = ds.getPort() != null ? ds.getPort() : defaultPort(type);
        String db = ds.getDatabaseName() != null ? ds.getDatabaseName() : "";
        return String.format(
                "jdbc:%s://%s:%d/%s?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true",
                type, host, port, db);
    }

    /** 获取 DatabaseMetaData.getTables / getColumns 的 catalog 参数（PG 必须是 ""） */
    public static String catalog(BiDatasource ds) {
        return isPostgres(ds) ? "" : ds.getDatabaseName();
    }

    public static boolean isPostgres(BiDatasource ds) {
        return ds.getType() != null && "postgresql".equalsIgnoreCase(ds.getType().trim());
    }

    private static int defaultPort(String type) {
        return "postgresql".equals(type) ? 5432 : 3306;
    }
}
