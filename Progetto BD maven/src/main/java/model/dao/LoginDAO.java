package model.dao;

import exception.DAOException;
import model.domain.Credentials;
import model.domain.enums.Role;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

public class LoginDAO {

    public Credentials login(String username, String password) throws DAOException {
        Role role = null;

        try (Connection connection = ConnectionFactory.getConnection()) {
            CallableStatement callableStatement = connection.prepareCall("{call Login(?, ?, ?)}");
            callableStatement.setString(1, username);
            callableStatement.setString(2, password);
            callableStatement.registerOutParameter(3, Types.INTEGER);

            callableStatement.execute();
            int roleId = callableStatement.getInt(3);
            role = Role.fromInt(roleId);

        } catch (SQLException e) {
            throw new DAOException("Login error: " + e.getMessage());
        }

        return new Credentials(username, password, role);
    }
}
//Miglioramenti possibili: hashing della password in SHA-256 e creazione di un token di sessione