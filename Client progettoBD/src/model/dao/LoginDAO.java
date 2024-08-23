package model.dao;

import model.domain.Credentials;
import model.domain.enums.Role;
import exception.DAOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;


public class LoginDAO implements GeneralDAO{
    @Override
    public Credentials execute(Object... params) throws DAOException {
        String username = (String) params[0];
        String password = (String) params[1];
        int role;

        try {
            Connection connection = ConnectionSingleton.getConnection();
            CallableStatement callableStatement = connection.prepareCall("{call Login(?,?,?)}");
            callableStatement.setString(1, username);
            callableStatement.setString(2, password);
            callableStatement.registerOutParameter(3, Types.NUMERIC);
            callableStatement.executeQuery();
            role = callableStatement.getInt(3);
        } catch(SQLException e) {
            throw new DAOException("Login error: " + e.getMessage());
        }

        return new Credentials(username, password, Role.fromInt(role));
    }
}

//Miglioramenti possibili: hashing della password in SHA-256 e creazione di un token di sessione