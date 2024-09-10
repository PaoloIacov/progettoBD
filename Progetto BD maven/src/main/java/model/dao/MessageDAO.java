package model.dao;

import exception.DAOException;
import model.domain.Credentials;
import model.domain.Message;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO implements GeneralDAO<Object> {

    private final Connection connection;
    private final Credentials credentials;

    public MessageDAO(Credentials credentials) throws SQLException {
        this.credentials = credentials;
        this.connection = ConnectionFactory.getConnection();
        System.out.println("Connessione ottenuta con successo nel costruttore MessageDAO.");
    }

    @Override
    public Object execute(Object... params) throws DAOException {
        String operation = (String) params[0];

        switch (operation) {
            case "sendMessage":
                return sendMessage((String) params[1], (LocalDateTime) params[2], (String) params[3], (Long) params[4]);
            case "getMessagesByConversation":
                return getMessagesByConversation((Long) params[1]);
            case "replyToMessage":
                return replyToMessage((String) params[1], (Long) params[2], (String) params[3], (Long) params[4], (LocalDateTime) params[5]);
            case "replyToMessageInPrivate":
                return replyToMessageInPrivate((String) params[1], (Long) params[2], (String) params[3], (Long) params[4], (LocalDateTime) params[5]);
            case "getRecipientFromMessage":
                return getRecipientFromMessage((Long) params[1]);
            default:
                throw new DAOException("Operazione non supportata: " + operation);
        }
    }

    // Metodo per inviare un messaggio
    public Message sendMessage(String contenuto, LocalDateTime datetime, String usernameDipendente, Long conversazioneID) throws DAOException {
        try (CallableStatement callableStatement = connection.prepareCall("{call SendMessage(?, ?, ?, ?)}")) {
            callableStatement.setString(1, contenuto);
            callableStatement.setObject(2, datetime); // Utilizza direttamente LocalDateTime
            callableStatement.setString(3, usernameDipendente);
            callableStatement.setLong(4, conversazioneID);

            callableStatement.execute();

            return new Message(contenuto, datetime, usernameDipendente, conversazioneID);

        } catch (SQLException e) {
            throw new DAOException("Errore nell'invio del messaggio: " + e.getMessage(), e);
        }
    }

    // Metodo per ottenere i messaggi di una conversazione
    public List<Message> getMessagesByConversation(Long conversazioneId) throws DAOException {
        List<Message> messaggi = new ArrayList<>();

        try (CallableStatement callableStatement = connection.prepareCall("{call GetMessagesByConversation(?)}")) {
            callableStatement.setLong(1, conversazioneId);

            try (ResultSet resultSet = callableStatement.executeQuery()) {
                while (resultSet.next()) {
                    Long id = resultSet.getLong("ID");
                    String contenuto = resultSet.getString("contenuto");
                    LocalDateTime datetime = resultSet.getObject("datetime", LocalDateTime.class); // Recupera direttamente come LocalDateTime
                    String usernameDipendente = resultSet.getString("usernameDipendente");
                    Long rispostaA = resultSet.getLong("rispostaA");

                    Message message = new Message(id, contenuto, datetime, usernameDipendente, conversazioneId, rispostaA);
                    messaggi.add(message);
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Errore nel recupero dei messaggi: " + e.getMessage(), e);
        }

        return messaggi;
    }

    // Metodo per rispondere a un messaggio
    public Message replyToMessage(String contenuto, Long rispostaA, String usernameDipendente, Long conversazioneID, LocalDateTime datetime) throws DAOException {
        try (CallableStatement callableStatement = connection.prepareCall("{call ReplyToMessage(?, ?, ?, ?, ?)}")) {
            callableStatement.setString(1, contenuto);
            callableStatement.setLong(2, rispostaA);
            callableStatement.setString(3, usernameDipendente);
            callableStatement.setLong(4, conversazioneID);
            callableStatement.setObject(5, datetime);

            try (ResultSet resultSet = callableStatement.executeQuery()) {
                if (resultSet.next()) {
                    Long id = resultSet.getLong("ID");
                    return new Message(id, contenuto, datetime, usernameDipendente, rispostaA, conversazioneID);
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Errore nella risposta al messaggio: " + e.getMessage(), e);
        }

        throw new DAOException("Errore nel recupero dell'ID del messaggio di risposta.");
    }

    // Metodo per rispondere in privato creando una nuova conversazione
    public Message replyToMessageInPrivate(String contenuto, Long rispostaA, String usernameDipendente, Long conversazioneID, LocalDateTime datetime) throws DAOException {
        try (CallableStatement createConversationStatement = connection.prepareCall("{call CreatePrivateConversation(?, ?, ?, ?)}")) {
            createConversationStatement.setString(1, "Conversazione privata tra " + usernameDipendente + " e destinatario");
            createConversationStatement.setString(2, "Progetto associato"); // Modifica se necessario
            createConversationStatement.setString(3, usernameDipendente); // Utente corrente
            createConversationStatement.setString(4, "destinatario"); // Utente destinatario (da definire)

            try (ResultSet rs = createConversationStatement.executeQuery()) {
                if (rs.next()) {
                    Long newConversationId = rs.getLong(1); // ID della nuova conversazione privata

                    try (CallableStatement replyStatement = connection.prepareCall("{call ReplyToMessage(?, ?, ?, ?, ?)}")) {
                        replyStatement.setString(1, contenuto);
                        replyStatement.setLong(2, rispostaA);
                        replyStatement.setString(3, usernameDipendente);
                        replyStatement.setLong(4, newConversationId); // Usa l'ID della nuova conversazione privata
                        replyStatement.setObject(5, datetime);

                        try (ResultSet resultSet = replyStatement.executeQuery()) {
                            if (resultSet.next()) {
                                Long id = resultSet.getLong("ID");
                                return new Message(id, contenuto, datetime, usernameDipendente, rispostaA, newConversationId);
                            }
                        }
                    }
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Errore nella creazione della conversazione privata e risposta al messaggio: " + e.getMessage(), e);
        }

        throw new DAOException("Errore nel recupero dell'ID del messaggio di risposta.");
    }

    // Metodo per ottenere il destinatario da un messaggio
    public String getRecipientFromMessage(Long messageId) throws DAOException {
        String recipientUsername;

        try {
            if (connection == null) {
                throw new DAOException("La connessione al database non è stata inizializzata.");
            }

            CallableStatement callableStatement = connection.prepareCall("{call getRecipientFromMessage(?, ?)}");
            callableStatement.setLong(1, messageId);
            callableStatement.registerOutParameter(2, Types.VARCHAR);

            callableStatement.execute();

            recipientUsername = callableStatement.getString(2);

        } catch (SQLException e) {
            throw new DAOException("Errore nel recupero del destinatario del messaggio: " + e.getMessage(), e);
        }

        if (recipientUsername == null) {
            throw new DAOException("Errore: destinatario non trovato per il messaggio con ID " + messageId);
        }

        return recipientUsername;
    }


    // Metodo per creare una conversazione privata e rispondere a un messaggio
    public void createPrivateConversationAndReply(String contenuto, String destinatario, String nomeProgetto, Long originalMessageId, Long originalConversationId) throws DAOException {
        try (CallableStatement callableStatement = connection.prepareCall("{call CreatePrivateConversation(?, ?, ?, ?)}")) {
            callableStatement.setString(1, "Conversazione Privata");
            callableStatement.setString(2, nomeProgetto);
            callableStatement.setString(3, credentials.getUsername());
            callableStatement.setString(4, destinatario);

            try (ResultSet resultSet = callableStatement.executeQuery()) {
                if (resultSet.next()) {
                    Long newConversationId = resultSet.getLong("id");

                    try (CallableStatement replyToNewConversationStatement = connection.prepareCall("{call SendMessage(?, ?, ?, ?)}")) {
                        replyToNewConversationStatement.setString(1, contenuto);
                        replyToNewConversationStatement.setObject(2, LocalDateTime.now());
                        replyToNewConversationStatement.setString(3, credentials.getUsername());
                        replyToNewConversationStatement.setLong(4, newConversationId);

                        replyToNewConversationStatement.execute();
                    }

                } else {
                    throw new DAOException("Errore: Impossibile creare la conversazione privata.");
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Errore nella creazione della conversazione privata e invio del messaggio: " + e.getMessage(), e);
        }
    }
}
