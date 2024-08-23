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

public class ConversationDAO implements GeneralDAO {

    @Override
    public Conversation execute(Object... params) throws DAOException {
        String descrizione = (String) params[0];
        String nomeProgetto = (String) params[1];

        Connection connection = null;
        CallableStatement callableStatement = null;
        Long id = null;

        try {

            connection = ConnectionSingleton.getConnection();

            callableStatement = connection.prepareCall("{call CreateConversation(?, ?, ?)}");
            callableStatement.setString(1, descrizione);
            callableStatement.setString(2, nomeProgetto);

            callableStatement.execute();

            //Recupero ID
            ResultSet generatedKeys = callableStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                id = generatedKeys.getLong(1);
            }

        } catch (SQLException e) {
            throw new DAOException("Errore nella creazione della conversazione: " + e.getMessage(), e);

        }

        return new Conversation(id, descrizione, nomeProgetto, ConversationState.APERTA, ConversationType.PUBBLICA);
    }

    // Metodo che aggiunge un dipendente ad una conversazione
    public void addEmployeeToConversation(Long conversazioneID, String usernameDipendente) throws DAOException {
        Connection connection = null;
        CallableStatement callableStatement = null;

        try {
            connection = ConnectionSingleton.getConnection();
            callableStatement = connection.prepareCall("{call AddEmployeeToConversation(?, ?)}");

            callableStatement.setLong(1, conversazioneID);
            callableStatement.setString(2, usernameDipendente);

            callableStatement.execute();

        } catch (SQLException e) {
            throw new DAOException("Errore nell'aggiunta del dipendente alla conversazione: " + e.getMessage(), e);
        }
    }

    // Metodo che rimuove un dipendente da una conversazione
    public void removeEmployeeFromConversation(Long conversazioneID, String usernameDipendente) throws DAOException {


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
    public void closeConversation(Long conversazioneID) throws DAOException {
        Connection connection = null;
        CallableStatement callableStatement = null;

        try {
            connection = ConnectionSingleton.getConnection();
            callableStatement = connection.prepareCall("{call CloseConversation(?)}");

            callableStatement.setLong(1, conversazioneID);

            callableStatement.execute();

        } catch (SQLException e) {
            throw new DAOException("Errore nella chiusura della conversazione: " + e.getMessage(), e);
        }
    }

    public Conversation openPrivateConversation(String descrizione, String nomeProgetto) throws DAOException {

        try {

            Connection connection = ConnectionSingleton.getConnection();

            CallableStatement callableStatement = connection.prepareCall("{call CreateConversation(?, ?, ?)}");
            callableStatement.setString(1, descrizione);
            callableStatement.setString(2, nomeProgetto);
            callableStatement.setString(3, ConversationType.PRIVATA.name());

            //Recupero ID
            ResultSet resultSet = callableStatement.executeQuery();
            if (resultSet.next()) {
                Long ID = resultSet.getLong("id");
            }

        } catch (SQLException e) {
            throw new DAOException("Errore nella creazione della conversazione privata: " + e.getMessage(), e);
        }
        return new Conversation(descrizione, nomeProgetto, ConversationState.APERTA, ConversationType.PRIVATA);
    }

    public List<Conversation> getConversationsByEmployee(String usernameDipendente) throws DAOException {
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

                // Creare l'oggetto Conversation e aggiungerlo alla lista
                Conversation conversation = new Conversation(id, descrizione, nomeProgetto, stato, tipo);
                conversations.add(conversation);
            }

        } catch (SQLException e) {
            throw new DAOException("Errore nel recupero delle conversazioni: " + e.getMessage(), e);
        }

        return conversations;
    }
}
