package io.github.akumetan.playerbridge.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.akumetan.playerbridge.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private final DatabaseConfig databaseConfig;
    private HikariDataSource dataSource;

    public DatabaseManager(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public void connect() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("jdbc:mariadb://%s:%d/%s", databaseConfig.host(), databaseConfig.port(), databaseConfig.database()));
        config.setDriverClassName("org.mariadb.jdbc.Driver");
        config.setUsername(databaseConfig.username());
        config.setPassword(databaseConfig.password());
        config.setMaximumPoolSize(databaseConfig.maxPoolSize());
        config.setMinimumIdle(databaseConfig.minPoolSize());
        config.setConnectionTimeout(databaseConfig.connectionTimeoutMs());

        this.dataSource = new HikariDataSource(config);
        this.initSchema();
    }

    private void initSchema() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS player_profiles (
                    uuid CHAR(36) NOT NULL PRIMARY KEY,
                    player_data LONGTEXT NULL,
                    owner_server_id VARCHAR(64) NULL,
                    owner_token CHAR(36) NULL,
                    owner_until TIMESTAMP(6) NULL,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
                """;

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    public Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    public void close() {
        this.dataSource.close();
    }
}