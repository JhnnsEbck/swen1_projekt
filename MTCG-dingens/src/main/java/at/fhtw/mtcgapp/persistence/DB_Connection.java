package at.fhtw.mtcgapp.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB_Connection {
    private static Connection connection = null;
    private static DB_Connection dbIsntance;

    public static DB_Connection getInstance()
    {
        if(dbIsntance == null)
        {
            dbIsntance = new DB_Connection();
        }
        return dbIsntance;
    }

    public Connection getConnection()
    {
        if(connection == null)
        {
            try
            {
                String url = "jdbc:postgresql://localhost:5432/DB_MTCG";
                String user = "postgres";
                String password = "postgres";

                connection = DriverManager.getConnection(url, user, password);
            } catch (SQLException e) {
                throw new RuntimeException(e);
                //System.err.println("Cannot connect to database");
            }
        }

        return connection;
    }
}
