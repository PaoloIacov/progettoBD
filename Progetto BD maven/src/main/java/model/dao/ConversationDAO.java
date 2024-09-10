package model.dao;

import exception.DAOException;
import model.domain.Conversation;
import model.domain.enums.ConversationState;
import model.domain.enums.ConversationType;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConversationDAO implements GeneralDAO<Object> {

    @Override
    public Object execute(Object... params) throws DAOException {
        String operation = (String) params[0];

        switch (operation) {
            case "createConversation":
                return createConversation((String) params[1], (String) params[2], (ConversationType) params[3]);
            case "addEmployeeToConversation":
                addEmployeeToConversation((String) params[1], (String) params[2]);
                break;
            case "removeEmployeeFromConversation":
                removeEmployeeFromConversation((Long) params[1], (String) params[2]);
                break;
            case "closeConversation":
                closeConversation((Long) params[1]);
                break;
            case "getConversationsByEmployee":
                return getConversationsByEmployee((String) params[1]);
            case "openPrivateConversation":
                return createPrivateConversation((String) params[1], (String) params[2], (String) params[3], (String) params[4]);
            case "getEmployeesInConversation":
                return getEmployeesInConversation((Long) params[1]);
            case "getConversationsByProject":
                return getConversationsByProject((String) params[1]);
            default:
                throw new DAOException("Operazione non supportata: " + operation);
        }
        return null;
    }

    // Metodo per creare una conversazione (pubblica o privata)
    public Conversation createConversation(String descrizione, String nomeProgetto, ConversationType tipo) throws DAOException {
    Connection connection = null;
    CallableStatement callableStatement = null;
    Long id = null;
    String retrievedDescription = null;

    try {
        connection = ConnectionFactory.getConnection();
        callableStatement = connection.prepareCall("{call CreateConversation(?, ?, ?)}");
        callableStatement.setString(1, descrizione);
        callableStatement.setString(2, nomeProgetto);
        callableStatement.setString(3, tipo.name());

        // Esegui la stored procedure e ottieni i risultati
        boolean hasResult = callableStatement.execute();

        // Recupera l'ID e la descrizione generati dalla stored procedure
        if (hasResult) {
            ResultSet resultSet = callableStatement.getResultSet();
            if (resultSet.next()) {
                id = resultSet.getLong("id");
                retrievedDescription = resultSet.getString("descrizione");
            }
        }

    } catch (SQLException e) {
        throw new DAOException("Errore nella creazione della conversazione: " + e.getMessage(), e);
    }

    return new Conversation(id, retrievedDescription, nomeProgetto, ConversationState.APERTA, tipo);
}

    // Metodo che aggiunge un dipendente ad una conversazione
    private void addEmployeeToConversation(String conversationId, String employeeUsername) throws DAOException {
        try {
            Connection connection = ConnectionFactory.getConnection();  // Ottieni la connessione al database

            // Chiama la stored procedure AddEmployeeToConversation
            CallableStatement callableStatement = connection.prepareCall("{call AddEmployeeToConversation(?, ?)}");
            callableStatement.setString(1, conversationId);
            callableStatement.setString(2, employeeUsername);

            // Esegui la procedura
            callableStatement.execute();
            System.out.println("Dipendente aggiunto con successo alla conversazione: " + conversationId);

        } catch (SQLException e) {
            throw new DAOException("Errore durante l'aggiunta del dipendente alla conversazione: " + e.getMessage(), e);
        }
    }


    // Metodo che rimuove un dipendente da una conversazione
    public void removeEmployeeFromConversation(Long conversazioneID, String usernameDipendente) throws DAOException {
        try {
            Connection connection = ConnectionFactory.getConnection();
            CallableStatement callableStatement = connection.prepareCall("{call RemoveEmployeeFromConversation(?, ?)}");

            callableStatement.setLong(1, conversazioneID);
            callableStatement.setString(2, usernameDipendente);

            callableStatement.execute();

        } catch (SQLException e) {
            throw new DAOException("Errore nella rimozione del dipendente dalla conversazione: " + e.getMessage(), e);
        }
    }

    // Metodo che chiude una conversazione
    public void closeConversation(Long conversazioneID) throws DAOException {
        try {
            Connection connection = ConnectionFactory.getConnection();
            CallableStatement callableStatement = connection.prepareCall("{call CloseConversation(?)}");

            callableStatement.setLong(1, conversazioneID);

            callableStatement.execute();

        } catch (SQLException e) {
            throw new DAOException("Errore nella chiusura della conversazione: " + e.getMessage(), e);
        }
    }

    public Conversation createPrivateConversation(String descrizione, String nomeProgetto, String usernameDipendenteAvviatore, String usernameDipendenteAggiunto) throws DAOException {
        Long id = null;

        try {
            Connection connection = ConnectionFactory.getConnection();

            CallableStatement callableStatement = connection.prepareCall("{call CreatePrivateConversation(?, ?, ?, ?)}");
            callableStatement.setString(1, descrizione);
            callableStatement.setString(2, nomeProgetto);
            callableStatement.setString(3, usernameDipendenteAvviatore);
            callableStatement.setString(4, usernameDipendenteAggiunto);

            ResultSet resultSet = callableStatement.executeQuery();
            if (resultSet.next()) {
                id = resultSet.getLong("id");
            }

            if (id == null) {
                throw new DAOException("Errore nel recupero dell'ID della conversazione.");
            }

        } catch (SQLException e) {
            throw new DAOException("Errore nella creazione della conversazione privata: " + e.getMessage(), e);
        }

        // Restituisce l'oggetto Conversation con l'ID corretto
        return new Conversation(id, descrizione, nomeProgetto, ConversationState.APERTA, ConversationType.PRIVATA);
    }


    // Metodo per ottenere tutte le conversazioni di un dipendente
    public List<Conversation> getConversationsByEmployee(String usernameDipendente) throws DAOException {
        List<Conversation> conversations = new ArrayList<>();

        try {
            Connection connection = ConnectionFactory.getConnection();
            CallableStatement callableStatement = connection.prepareCall("{call GetConversationsByEmployee(?)}");
            callableStatement.setString(1, usernameDipendente);

            ResultSet resultSet = callableStatement.executeQuery();

            while (resultSet.next()) {
                Long id = resultSet.getLong("ID");
                String descrizione = resultSet.getString("descrizione");
                String nomeProgetto = resultSet.getString("nomeProgetto");
                ConversationState stato = ConversationState.valueOf(resultSet.getString("stato"));
                ConversationType tipo = ConversationType.valueOf(resultSet.getString("tipo"));

                Conversation conversation = new Conversation(id, descrizione, nomeProgetto, stato, tipo);
                conversations.add(conversation);
            }

        } catch (SQLException e) {
            throw new DAOException("Errore nel recupero delle conversazioni: " + e.getMessage(), e);
        }

        return conversations;
    }

    public List<String> getEmployeesInConversation(Long conversationId) throws DAOException {
        List<String> employees = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             CallableStatement callableStatement = connection.prepareCall("{call GetEmployeesInConversation(?)}")) {

            callableStatement.setLong(1, conversationId);

            ResultSet resultSet = callableStatement.executeQuery();

            while (resultSet.next()) {
                employees.add(resultSet.getString("usernameDipendente"));
            }

        } catch (SQLException e) {
            throw new DAOException("Errore nel recupero dei dipendenti nella conversazione: " + e.getMessage(), e);
        }

        return employees;
    }

    public List<Conversation> getConversationsByProject(String projectName) throws DAOException {
        List<Conversation> conversations = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection()) {
            CallableStatement callableStatement = connection.prepareCall("{call GetConversationsByProject(?)}");
            callableStatement.setString(1, projectName);

            ResultSet resultSet = callableStatement.executeQuery();

            while (resultSet.next()) {
                Long id = resultSet.getLong("ID");
                String descrizione = resultSet.getString("descrizione");
                String nomeProgetto = resultSet.getString("nomeProgetto");
                ConversationState stato = ConversationState.valueOf(resultSet.getString("stato"));
                ConversationType tipo = ConversationType.valueOf(resultSet.getString("tipo"));

                conversations.add(new Conversation(id, descrizione, nomeProgetto, stato, tipo));
            }

        } catch (SQLException e) {
            throw new DAOException("Errore nel recupero delle conversazioni per il progetto: " + e.getMessage(), e);
        }

        return conversations;
    }
}
