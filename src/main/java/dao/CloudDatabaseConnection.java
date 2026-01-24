package dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class CloudDatabaseConnection {

    private static final HikariDataSource dataSource;
    private static String secretKey;
    static {
        try {
            // 1. Učitavamo properties fajl direktno iz JAR/EXE arhive
            Properties props = new Properties();
            InputStream inputStream = CloudDatabaseConnection.class.getResourceAsStream("/database.properties");

            if (inputStream == null) {
                // Ovo će ti odmah reći ako fajl fali, umjesto čudnih grešaka kasnije
                throw new RuntimeException("CRITICAL ERROR: database.properties nije pronađen u resources!");
            }

            props.load(inputStream);

            // 2. Čitamo SECRET KEY i spremamo ga u statičku varijablu
            secretKey = props.getProperty("app.secret_key");

            // 3. Konfiguriramo HikariCP koristeći podatke iz properties fajla
            HikariConfig config = new HikariConfig();

            // Pazi: ovdje koristimo ključeve koje smo definirali u database.properties
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.user"));
            config.setPassword(props.getProperty("db.password"));

            // --- Tvoje postojeće optimizacije (zadržao sam ih jer su dobre) ---
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(300_000);
            config.setConnectionTimeout(10_000);
            config.setMaxLifetime(1_800_000);
            config.setAutoCommit(true);
            config.setPoolName("CloudDB-HikariPool");
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Greška pri inicijalizaciji baze: " + e.getMessage());
        }
    }

    private CloudDatabaseConnection() {}

    // Tvoja metoda za dohvat konekcije
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    // Stara metoda (ostavio sam je ako je negdje zoveš po starom imenu da ne puca kod)
    public static Connection Konekcija() throws SQLException {
        return dataSource.getConnection();
    }

    // Nova metoda za dohvat tajnog ključa
    public static String getSecretKey() {
        return secretKey;
    }
}
