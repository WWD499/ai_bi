package com.ruoyi.bi.util;

import com.ruoyi.bi.domain.BiDatasource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JdbcUrlBuilder 单元测试（纯函数，无 Spring 容器）
 */
class JdbcUrlBuilderTest {

    private BiDatasource ds(String type, String host, Integer port, String db, String jdbcUrl) {
        BiDatasource d = new BiDatasource();
        d.setType(type);
        d.setHost(host);
        d.setPort(port);
        d.setDatabaseName(db);
        d.setJdbcUrl(jdbcUrl);
        return d;
    }

    @Test
    void build_customMysqlUrl_shouldAppendPublicKeyRetrieval() {
        BiDatasource d = ds("mysql", null, null, null, "jdbc:mysql://127.0.0.1:3306/mydb");
        String url = JdbcUrlBuilder.build(d);
        assertTrue(url.contains("allowPublicKeyRetrieval=true"), url);
        assertTrue(url.contains("useSSL=false"), url);
    }

    @Test
    void build_customMysqlUrl_alreadyHasFlag_shouldNotDuplicate() {
        String raw = "jdbc:mysql://127.0.0.1:3306/mydb?allowPublicKeyRetrieval=true&useSSL=false";
        BiDatasource d = ds("mysql", null, null, null, raw);
        assertEquals(raw, JdbcUrlBuilder.build(d));
    }

    @Test
    void build_customPostgresUrl_shouldNotAppendMysqlFlag() {
        String raw = "jdbc:postgresql://127.0.0.1:5432/mydb";
        BiDatasource d = ds("postgresql", null, null, null, raw);
        assertEquals(raw, JdbcUrlBuilder.build(d));
    }

    @Test
    void build_defaultMysql_shouldUseDefaults() {
        BiDatasource d = ds("mysql", null, null, "mydb", null);
        String url = JdbcUrlBuilder.build(d);
        assertEquals("jdbc:mysql://localhost:3306/mydb?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true", url);
    }

    @Test
    void build_defaultPostgres_shouldUse5432() {
        BiDatasource d = ds("postgresql", "pg.host", 5433, "pgdb", null);
        String url = JdbcUrlBuilder.build(d);
        assertEquals("jdbc:postgresql://pg.host:5433/pgdb?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true", url);
    }

    @Test
    void catalog_postgres_mustBeEmptyString() {
        BiDatasource d = ds("postgresql", null, null, "pgdb", null);
        assertEquals("", JdbcUrlBuilder.catalog(d));
    }

    @Test
    void catalog_mysql_returnsDatabaseName() {
        BiDatasource d = ds("mysql", null, null, "mydb", null);
        assertEquals("mydb", JdbcUrlBuilder.catalog(d));
    }

    @Test
    void isPostgres_variants() {
        assertTrue(JdbcUrlBuilder.isPostgres(ds("postgresql", null, null, null, null)));
        assertTrue(JdbcUrlBuilder.isPostgres(ds(" PostgreSQL ", null, null, null, null)));
        assertFalse(JdbcUrlBuilder.isPostgres(ds("mysql", null, null, null, null)));
        assertFalse(JdbcUrlBuilder.isPostgres(ds(null, null, null, null, null)));
    }
}
