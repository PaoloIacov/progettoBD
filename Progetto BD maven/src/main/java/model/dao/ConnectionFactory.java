package model.dao;

import model.domain.enums.Role;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionFactory {

    private static Properties properties;

    static {
        try (InputStream input = ConnectionFactory.class.getClassLoader().getResourceAsStream("database.properties")) {
            properties = new Properties();
            if (input == null) {
                throw new IOException("File properties non trovato.");
            }
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        String connectionUrl = properties.getProperty("CONNECTION_URL");
        String user = properties.getProperty("LOGIN_USER");
        String pass = properties.getProperty("LOGIN_PASS");

        return DriverManager.getConnection(connectionUrl, user, pass);
    }

    public static Connection getConnectionByRole(Role role) throws SQLException {
        String connectionUrl = properties.getProperty("CONNECTION_URL");
        String userKey = role.name() + "_USER";
        String passKey = role.name() + "_PASS";

        String user = properties.getProperty(userKey);
        String pass = properties.getProperty(passKey);

        if (user == null || pass == null) {
            throw new SQLException("Credenziali non trovate per il ruolo: " + role);
        }

        return DriverManager.getConnection(connectionUrl, user, pass);
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("Connessione chiusa con successo.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Errore durante la chiusura della connessione.");
            }
        }
    }
}
