package com.animelog.animelogserver.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSchemaInitializer implements ApplicationRunner {
    private static final String TABLE_NAME = "anime";
    private static final String INDEX_NAME = "uk_anime_source_subject";

    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            ensureAnimeColumn(connection, "source_provider",
                    "ALTER TABLE anime ADD COLUMN source_provider VARCHAR(50) DEFAULT NULL COMMENT '外部来源提供方: bangumi' AFTER synopsis");
            ensureAnimeColumn(connection, "source_subject_id",
                    "ALTER TABLE anime ADD COLUMN source_subject_id BIGINT DEFAULT NULL COMMENT '外部来源条目ID' AFTER source_provider");
            ensureAnimeIndex(connection, INDEX_NAME,
                    "ALTER TABLE anime ADD UNIQUE KEY uk_anime_source_subject (source_provider, source_subject_id)");
        }
    }

    private void ensureAnimeColumn(Connection connection, String columnName, String ddl) throws SQLException {
        if (hasColumn(connection, TABLE_NAME, columnName)) {
            return;
        }
        executeDdl(connection, ddl, "补齐字段 " + columnName);
    }

    private void ensureAnimeIndex(Connection connection, String indexName, String ddl) throws SQLException {
        if (hasIndex(connection, TABLE_NAME, indexName)) {
            return;
        }
        executeDdl(connection, ddl, "补齐索引 " + indexName);
    }

    private boolean hasColumn(Connection connection, String tableName, String columnName) throws SQLException {
        String sql = """
                SELECT 1
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = ?
                LIMIT 1
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            statement.setString(2, columnName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private boolean hasIndex(Connection connection, String tableName, String indexName) throws SQLException {
        String sql = """
                SELECT 1
                FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND INDEX_NAME = ?
                LIMIT 1
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            statement.setString(2, indexName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void executeDdl(Connection connection, String ddl, String description) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(ddl);
            log.info("数据库结构自动升级成功: {}", description);
        }
    }
}
