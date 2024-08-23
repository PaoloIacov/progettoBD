package model.dao;

import exception.DAOException;
import java.sql.SQLException;

public interface GeneralDAO<P> {
    P execute(Object... params) throws DAOException, SQLException;
}
