package model.dao;

import model.domain.enums.Role;
import java.io.*;
import java.sql.*;
import java.util.Properties;

public class ConnectionSingleton {
    private static ConnectionSingleton instance;
    private static Connection connection;

    private ConnectionSingleton() {
        try (InputStream input = new FileInputStream("resources/db.properties")) {
            Properties properties = new Properties();
            properties.load(input);

            String connection_url = properties.getProperty("CONNECTION_URL");
            String user = properties.getProperty("LOGIN_USER");
            String pass = properties.getProperty("LOGIN_PASS");

            connection = DriverManager.getConnection(connection_url, user, pass);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static synchronized ConnectionSingleton getInstance() {
        if (instance == null) {
            instance = new ConnectionSingleton();
        }
        return instance;
    }

    public static Connection getConnection() {
        return connection;
    }

    //Serve a cambiare l'istanza di connessione in base al ruolo
    public void changeRole(Role role) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }

            try (InputStream input = new FileInputStream("resources/database.properties")) {
                Properties properties = new Properties();
                properties.load(input);

                String connection_url = properties.getProperty("CONNECTION_URL");
                String user = properties.getProperty(role.name() + "_USER");
                String pass = properties.getProperty(role.name() + "_PASS");

                connection = DriverManager.getConnection(connection_url, user, pass);
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}