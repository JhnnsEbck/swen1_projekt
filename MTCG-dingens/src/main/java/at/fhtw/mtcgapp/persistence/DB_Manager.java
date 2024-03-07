package at.fhtw.mtcgapp.persistence;

import at.fhtw.mtcgapp.exception.DataAccessException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public enum DB_Manager {
    INSTANCE;

    public Connection getConnection()
    {
        try {
            String url = "jdbc:postgresql://localhost:5432/DB_MTCG";
            String user = "postgres";
            String password = "postgres";

            Connection connection = DriverManager.getConnection(url, user, password);

            return connection;

        } catch (SQLException e) {
            throw new DataAccessException("Datenbankverbindungsaufbau nicht erfolgreich", e);
        }
    }
}
