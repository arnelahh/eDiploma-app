package dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.SQLException;

public class CloudDatabaseConnection {

    private static final HikariDataSource dataSource;

    static {
        Dotenv dotenv = Dotenv.configure()
                .directory("src/main/resources")
                .load();

        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(dotenv.get("DB_URL"));
        config.setUsername(dotenv.get("DB_USERNAME"));
        config.setPassword(dotenv.get("DB_PASSWORD"));

        //Pool tuning test
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(300_000);      // 5 minutes
        config.setConnectionTimeout(10_000); // 10 seconds
        config.setMaxLifetime(1_800_000);    // 30 minutes

        // Performance optimizations
        config.setAutoCommit(true);
        config.setPoolName("CloudDB-HikariPool");

        // Optional but recommended
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);
    }

    private CloudDatabaseConnection() {}

    public static Connection Konekcija() throws SQLException {
        return dataSource.getConnection();
    }
}
