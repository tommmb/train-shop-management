package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnectionHandler {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/com2008"; // Local PostgreSQL URL
    private static final String DB_USER = "postgres"; // Replace with your local PostgreSQL user
    private static final String DB_PASSWORD = "pass"; // Replace with your local PostgreSQL password

    private Connection connection = null;

    public void openConnection() throws SQLException {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Connection getConnection() {
        return this.connection;
    }
}
