package at.fhtw.MTCG.dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public enum DatabaseManager {
    INSTANCE;

    public Connection getConnection()
    {
        try {
            return DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/mtcg_db",
                    "postgres",
                    "123");
        } catch (SQLException e) {
            throw new DataAccessException("Datenbankverbindungsaufbau nicht erfolgreich", e);
        }
    }
}
