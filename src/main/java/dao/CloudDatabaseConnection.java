package dao;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class CloudDatabaseConnection {

    private static final Dotenv dotenv = Dotenv.configure()
            .directory("src/main/resources")
            .load();

    private static final String url =  dotenv.get("DB_URL");
    private static final String username = dotenv.get("DB_USERNAME");
    private static final String password = dotenv.get("DB_PASSWORD");


    public static Connection Konekcija() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
