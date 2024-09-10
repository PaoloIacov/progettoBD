package model.dao;

import exception.DAOException;
import model.domain.User;
import model.domain.enums.Role;

import java.sql.*;

public class UserDAO implements GeneralDAO<User> {

    Connection connection;

    public UserDAO() {
        try {
            // Inizializza la connessione qui
            this.connection = ConnectionFactory.getConnection();
            System.out.println("Connessione ottenuta con successo nel costruttore UserDAO.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Errore durante la connessione al database nel costruttore UserDAO.");
            this.connection = null;
        }
    }



    @Override
    public User execute(Object... params) throws DAOException {
        String operation = (String) params[0];

        switch (operation) {
            case "addEmployee":
                return addEmployee((String) params[1], (String) params[2], (String) params[3], (String) params[4], (int) params[5]);
            case "getEmployeeByUsername":
                return getEmployeeByUsername((String) params[1]);
            case "changeUserRole":
                changeUserRole((String) params[1], (int) params[2]);
                return null;
            case "deleteEmployee":
                deleteEmployee((String) params[1]);
                return null;
            default:
                throw new DAOException("Operazione non supportata: " + operation);
        }
    }

    // Metodo per aggiungere un dipendente al database
    public User addEmployee(String username, String password, String name, String surname, int roleId) throws DAOException {
        User newUser = new User(username, password, name, surname, roleId);

        try (Connection connection = ConnectionFactory.getConnection();
             CallableStatement callableStatement = connection.prepareCall("{call AddEmployee(?, ?, ?, ?, ?)}")) {

            // Impostazione dei parametri per la stored procedure
            callableStatement.setString(1, username);
            callableStatement.setString(2, password);
            callableStatement.setString(3, name);
            callableStatement.setString(4, surname);
            callableStatement.setInt(5, roleId);

            // Eseguire la stored procedure
            callableStatement.execute();

            System.out.println("Dipendente aggiunto con successo: " + username);

        } catch (SQLException e) {
            throw new DAOException("Errore durante l'aggiunta del dipendente: " + e.getMessage(), e);
        }
        return newUser;
    }


    public User getEmployeeByUsername(String username) throws DAOException {

        try {

            Connection connection = ConnectionFactory.getConnection();
            if (connection == null) {
                throw new DAOException("La connessione al database non è stata inizializzata.");
            }

            CallableStatement callableStatement = connection.prepareCall("{call GetEmployeeByUsername(?)}");
            callableStatement.setString(1, username);

            // Eseguire la stored procedure e ottenere i risultati
            ResultSet resultSet = callableStatement.executeQuery();

            // Se il risultato contiene dati, crea un oggetto User e restituiscilo
            if (resultSet.next()) {
                String name = resultSet.getString("nome");
                String surname = resultSet.getString("cognome");
                int roleId = resultSet.getInt("ruolo");

                return new User(username, null, name, surname, roleId);
            } else {
                throw new DAOException("Dipendente non trovato con username: " + username);
            }

        } catch (SQLException e) {
            throw new DAOException("Errore durante il recupero del dipendente: " + e.getMessage(), e);
        }
    }

    // Metodo per cambiare il ruolo di un utente
    public void changeUserRole(String username, int newRoleId) throws DAOException {
        try {
            if (connection == null) {
                throw new DAOException("La connessione al database non è stata inizializzata.");
            }

            CallableStatement callableStatement = connection.prepareCall("{call ChangeUserRole(?, ?)}");
            callableStatement.setString(1, username);
            callableStatement.setInt(2, newRoleId);

            callableStatement.execute();

            System.out.println("Ruolo aggiornato con successo per l'utente: " + username);

        } catch (SQLException e) {
            throw new DAOException("Errore durante il cambio del ruolo dell'utente: " + e.getMessage(), e);
        }
    }

    public void deleteEmployee(String username) throws DAOException {
        try {
            if (connection == null) {
                throw new DAOException("La connessione al database non è stata inizializzata.");
            }

            // Verifica se lo username esiste nel database
            String checkQuery = "SELECT COUNT(*) FROM Dipendente WHERE username = ?";
            try (PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {
                checkStatement.setString(1, username);
                ResultSet resultSet = checkStatement.executeQuery();

                if (resultSet.next() && resultSet.getInt(1) == 0) {
                    throw new DAOException("Errore: il dipendente con username " + username + " non esiste.");
                }
            }

            // Procedi con l'eliminazione del dipendente se esiste
            CallableStatement callableStatement = connection.prepareCall("{call DeleteEmployee(?)}");
            callableStatement.setString(1, username);

            // Esegui la stored procedure
            callableStatement.execute();

            System.out.println("Dipendente eliminato con successo: " + username);

        } catch (SQLException e) {
            throw new DAOException("Errore durante l'eliminazione del dipendente: " + e.getMessage(), e);
        }
    }

    public boolean isUserInConversation(Long conversationId, String username) throws DAOException {
        try {
            if (connection == null) {
                throw new DAOException("La connessione al database non è stata inizializzata.");
            }

            // Preparazione della chiamata alla stored procedure o della query SQL
            CallableStatement callableStatement = connection.prepareCall("{call IsUserInConversation(?, ?)}");
            callableStatement.setLong(1, conversationId);  // Imposta l'ID della conversazione come parametro
            callableStatement.setString(2, username);  // Imposta l'username del dipendente come parametro

            // Esecuzione della stored procedure
            ResultSet resultSet = callableStatement.executeQuery();

            // Controllo del risultato
            if (resultSet.next()) {
                int count = resultSet.getInt(1);  // Ottieni il risultato come un numero
                return count > 0;  // Se il risultato è maggiore di 0, l'utente è nella conversazione
            }

        } catch (SQLException e) {
            throw new DAOException("Errore nel controllo della presenza dell'utente nella conversazione: " + e.getMessage(), e);
        }

        return false;  // Restituisce false se l'utente non è trovato nella conversazione
    }

    public User getEmployeeInfo(String username) throws DAOException {
        if (connection == null) {
            throw new DAOException("La connessione al database non è stata inizializzata.");
        }

        try (CallableStatement callableStatement = connection.prepareCall("{call showEmployeeInfo(?)}")) {
            callableStatement.setString(1, username);

            try (ResultSet resultSet = callableStatement.executeQuery()) {
                if (resultSet.next()) {

                    String name = resultSet.getString("nome");
                    String surname = resultSet.getString("cognome");
                    int roleId = resultSet.getInt("ruolo");

                    // Crea un oggetto User per contenere i dettagli
                    return new User(username, null, name, surname, roleId);
                } else {
                    return null; // Dipendente non trovato
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Errore durante il recupero delle informazioni del dipendente: " + e.getMessage(), e);
        }
    }
}

