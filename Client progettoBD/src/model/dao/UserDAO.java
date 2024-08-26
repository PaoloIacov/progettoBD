package model.dao;

import exception.DAOException;
import model.domain.User;
import model.domain.enums.Role;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO implements GeneralDAO<User>{

    @Override
    public User execute(Object... params) throws DAOException {
        String operation = (String) params[0];

        switch (operation) {
            case "addEmployee":
                return addEmployee((User) params[1]);
            case "findUserByUsername":
                return findUserByUsername((String) params[1]);
            default:
                throw new DAOException("Operazione non supportata: " + operation);
        }
    }

    // Metodo per aggiungere un dipendente al database
    private User addEmployee(User user) throws DAOException {
        try {
            Connection connection = ConnectionSingleton.getConnection();

            CallableStatement callableStatement = connection.prepareCall("{call AddEmployee(?, ?, ?, ?, ?)}");
            callableStatement.setString(1, user.getUsername());
            callableStatement.setString(2, user.getPassword());
            callableStatement.setString(3, user.getName());
            callableStatement.setString(4, user.getSurname());
            callableStatement.setInt(5, user.getRole().getId());

            // Eseguire la stored procedure
            callableStatement.execute();

            return user;

        } catch (SQLException e) {
            throw new DAOException("Errore durante l'aggiunta del dipendente: " + e.getMessage(), e);
        }
    }

    // Metodo per trovare un dipendente nel database per username utilizzando CallableStatement
    private User findUserByUsername(String username) throws DAOException {
        User user = null;
        try {
            Connection connection = ConnectionSingleton.getConnection();

            CallableStatement callableStatement = connection.prepareCall("{call GetUserByUsername(?)}");
            callableStatement.setString(1, username);

            ResultSet resultSet = callableStatement.executeQuery();
            if (resultSet.next()) {
                String foundUsername = resultSet.getString("username");
                String password = resultSet.getString("password");
                String nome = resultSet.getString("nome");
                String cognome = resultSet.getString("cognome");
                int ruoloId = resultSet.getInt("ruolo");

                Role ruolo = Role.fromInt(ruoloId); // Converti l'INT in Role usando l'enum

                user = new User(foundUsername, password, nome, cognome, ruolo);
            }

        } catch (SQLException e) {
            throw new DAOException("Errore nel recupero dell'utente: " + e.getMessage(), e);
        }
        return user;
    }
}
