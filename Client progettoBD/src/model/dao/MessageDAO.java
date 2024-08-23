package model.dao;

import model.domain.Message;
import exception.DAOException;

import java.util.List;
import java.util.ArrayList;
import java.sql.*;
import java.time.LocalDateTime;

public class MessageDAO implements GeneralDAO {

    @Override
    public Message execute(Object... params) throws DAOException {
        String contenuto = (String) params[0];
        LocalDateTime datetime = (LocalDateTime) params[1];
        String usernameDipendente = (String) params[2];

        try {
            Connection connection = ConnectionSingleton.getConnection();

            CallableStatement callableStatement = connection.prepareCall("{call SendMessage(?, ?, ?)}");
            callableStatement.setString(1, contenuto);
            callableStatement.setTimestamp(2, Timestamp.valueOf(datetime));
            callableStatement.setString(3, usernameDipendente);

            callableStatement.execute();

        } catch (SQLException e) {
            throw new DAOException("Errore nell'invio del messaggio: " + e.getMessage());
        }

        return new Message(contenuto, datetime, usernameDipendente);
    }

    public List<Message> getMessagesByConversation(Long conversazioneId) throws DAOException {
        List<Message> messaggi = new ArrayList<>();

        try {
            Connection connection = ConnectionSingleton.getConnection();

            CallableStatement callableStatement = connection.prepareCall("{call RetrieveMessages(?)}");
            callableStatement.setLong(1, conversazioneId); // Imposta l'ID della conversazione (Capire come farlo bene)

            ResultSet resultSet = callableStatement.executeQuery();

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String contenuto = resultSet.getString("contenuto");
                LocalDateTime datetime = resultSet.getTimestamp("datetime").toLocalDateTime();
                String usernameDipendente = resultSet.getString("usernameDipendente");

                // Aggiungi il messaggio alla lista
                Message message = new Message(contenuto, datetime, usernameDipendente);
                messaggi.add(message);
            }

        } catch (SQLException e) {
            throw new DAOException("Errore nel recupero dei messaggi: " + e.getMessage(), e);
        }

        return messaggi;
    }

    // Metodo per rispondere a un messaggio
    public Message replyToMessage(String contenuto, Long rispostaA) throws DAOException {

        Long id = null;
        try {
            Connection connection = ConnectionSingleton.getConnection();

            CallableStatement callableStatement = connection.prepareCall("{call ReplyToMessage(?, ?)}");
            callableStatement.setString(1, contenuto);
            callableStatement.setLong(2, rispostaA);

            // Eseguire la stored procedure
            ResultSet resultSet = callableStatement.executeQuery();
            if (resultSet.next()) {
                id = resultSet.getLong("id");
            }

        } catch (SQLException e) {
            throw new DAOException("Errore nella risposta al messaggio: " + e.getMessage(), e);
        }

        return new Message(id, contenuto, LocalDateTime.now(), null, rispostaA);
    }
}
