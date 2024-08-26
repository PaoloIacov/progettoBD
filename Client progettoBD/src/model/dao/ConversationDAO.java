package model.dao;

import model.domain.Conversation;
import exception.DAOException;
import model.domain.enums.ConversationState;
import model.domain.enums.ConversationType;

import java.util.ArrayList;
import java.util.List;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConversationDAO implements GeneralDAO<Object> {

    @Override
    public Object execute(Object... params) throws DAOException {
        String operation = (String) params[0];

        switch (operation) {
            case "createConversation":
                return createConversation((String) params[1], (String) params[2], (ConversationType) params[3]);
            case "addEmployeeToConversation":
                addEmployeeToConversation((Long) params[1], (String) params[2]);
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
                return openPrivateConversation((String) params[1], (String) params[2]);
            default:
                throw new DAOException("Operazione non supportata: " + operation);
        }
        return null;
    }

    // Metodo per creare una conversazione (pubblica o privata)
    private Conversation createConversation(String descrizione, String nomeProgetto, ConversationType tipo) throws DAOException {
        Connection connection = null;
        CallableStatement callableStatement = null;
        Long id = null;

        try {
            connection = ConnectionSingleton.getConnection();
            callableStatement = connection.prepareCall("{call CreateConversation(?, ?, ?)}");
            callableStatement.setString(1, descrizione);
            callableStatement.setString(2, nomeProgetto);
            callableStatement.setString(3, tipo.name());

            // Recupero ID
            ResultSet generatedKeys = callableStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                id = generatedKeys.getLong(1);
            }

        } catch (SQLException e) {
            throw new DAOException("Errore nella creazione della conversazione: " + e.getMessage(), e);
        }

        return new Conversation(id, descrizione, nomeProgetto, ConversationState.APERTA, tipo);
    }

    // Metodo che aggiunge un dipendente ad una conversazione
    private void addEmployeeToConversation(Long conversazioneID, String usernameDipendente) throws DAOException {
        try {
            Connection connection = ConnectionSingleton.getConnection();
            CallableStatement callableStatement = connection.prepareCall("{call AddEmployeeToConversation(?, ?)}");

            callableStatement.setLong(1, conversazioneID);
            callableStatement.setString(2, usernameDipendente);

            callableStatement.execute();

        } catch (SQLException e) {
            throw new DAOException("Errore nell'aggiunta del dipendente alla conversazione: " + e.getMessage(), e);
        }
    }

    // Metodo che rimuove un dipendente da una conversazione
    private void removeEmployeeFromConversation(Long conversazioneID, String usernameDipendente) throws DAOException {
        try {
            Connection connection = ConnectionSingleton.getConnection();
            CallableStatement callableStatement = connection.prepareCall("{call RemoveEmployeeFromConversation(?, ?)}");

            callableStatement.setLong(1, conversazioneID);
            callableStatement.setString(2, usernameDipendente);

            callableStatement.execute();

        } catch (SQLException e) {
            throw new DAOException("Errore nella rimozione del dipendente dalla conversazione: " + e.getMessage(), e);
        }
    }

    // Metodo che chiude una conversazione
    private void closeConversation(Long conversazioneID) throws DAOException {
        try {
            Connection connection = ConnectionSingleton.getConnection();
            CallableStatement callableStatement = connection.prepareCall("{call CloseConversation(?)}");

            callableStatement.setLong(1, conversazioneID);

            callableStatement.execute();

        } catch (SQLException e) {
            throw new DAOException("Errore nella chiusura della conversazione: " + e.getMessage(), e);
        }
    }

    public Conversation openPrivateConversation(String descrizione, String nomeProgetto) throws DAOException {
        Long id = null;

        try {
            Connection connection = ConnectionSingleton.getConnection();

            CallableStatement callableStatement = connection.prepareCall("{call CreateConversation(?, ?, ?)}");
            callableStatement.setString(1, descrizione);
            callableStatement.setString(2, nomeProgetto);
            callableStatement.setString(3, ConversationType.PRIVATA.name());


            ResultSet resultSet = callableStatement.executeQuery();
            if (resultSet.next()) {
                id = resultSet.getLong(1);
            }

        } catch (SQLException e) {
            throw new DAOException("Errore nella creazione della conversazione privata: " + e.getMessage(), e);
        }

        // Verificare che l'ID sia stato recuperato correttamente
        if (id == null) {
            throw new DAOException("Errore nel recupero dell'ID della conversazione.");
        }

        // Restituisce l'oggetto Conversation con l'ID corretto
        return new Conversation(id, descrizione, nomeProgetto, ConversationState.APERTA, ConversationType.PRIVATA);
    }



    // Metodo per ottenere tutte le conversazioni di un dipendente
    private List<Conversation> getConversationsByEmployee(String usernameDipendente) throws DAOException {
        List<Conversation> conversations = new ArrayList<>();

        try {
            Connection connection = ConnectionSingleton.getConnection();
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
}
