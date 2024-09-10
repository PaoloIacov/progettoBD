package controller;

import exception.DAOException;
import model.dao.MessageDAO;
import model.dao.UserDAO;
import model.domain.Conversation;
import model.domain.Credentials;
import model.domain.Message;
import view.ConversationView;
import view.MessagesView;
import view.InputManager;
import model.domain.enums.Role;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;


public class ConversationController {

    private final Credentials credentials;
    private final MessageDAO messageDAO;
    private final UserDAO userDAO;

    public ConversationController(Credentials credentials) throws SQLException {
        this.credentials = credentials;
        this.messageDAO = new MessageDAO(credentials);
        this.userDAO = new UserDAO();
    }

    public void showConversations(List<Conversation> conversations) {
        try {
            int choice = ConversationView.showConversationMenu(conversations);

            if (choice > 0 && choice <= conversations.size()) {
                Conversation selectedConversation = conversations.get(choice - 1);
                if (credentials.getRole() == Role.AMMINISTRATORE || userDAO.isUserInConversation(selectedConversation.getID(), credentials.getUsername())) {
                    showMessagesInConversation(selectedConversation);
                } else {
                    ConversationView.showError("Non hai i permessi per accedere a questa conversazione.");
                }
            }
        } catch (IOException e) {
            ConversationView.showError("Errore di input/output: " + e.getMessage());
        } catch (DAOException e) {
            ConversationView.showError("Errore nel recupero delle conversazioni: " + e.getMessage());
        }
    }

    private void showMessagesInConversation(Conversation conversation) {
        try {
            // Recupera i messaggi della conversazione
            List<Message> messages = messageDAO.getMessagesByConversation(conversation.getID());
            MessagesView.showMessages(messages);

            // Mostra il menu delle opzioni dopo aver visualizzato i messaggi
            handleMessageMenu(conversation);
        } catch (DAOException e) {
            MessagesView.showError("Errore nel recupero dei messaggi: " + e.getMessage());
        } catch (IOException e) {
            MessagesView.showError("Errore di input/output: " + e.getMessage());
        }
    }

    private void handleMessageMenu(Conversation conversation) throws IOException {
        int choice = MessagesView.showMessageMenu();
        switch (choice) {
            case 1 -> chooseReplyType(conversation);
            case 2 -> sendNewMessage(conversation);
            case 3 -> System.out.println("Tornando al menu principale...");
            default -> MessagesView.showError("Scelta non valida.");
        }
    }

    private void sendNewMessage(Conversation conversation) {
        try {
            String contenuto = MessagesView.getInputContent();
            LocalDateTime datetime = LocalDateTime.now();

            Message newMessage = messageDAO.sendMessage(contenuto, datetime, credentials.getUsername(), conversation.getID());
            MessagesView.showMessages(List.of(newMessage));

        } catch (IOException | DAOException e) {
            MessagesView.showError("Errore nell'invio del nuovo messaggio: " + e.getMessage());
        }
    }

    private void chooseReplyType(Conversation conversation) {
        try {
            // Richiedi l'ID del messaggio a cui si vuole rispondere
            System.out.print("Inserisci l'ID del messaggio a cui vuoi rispondere: ");
            Long messageId = Long.parseLong(InputManager.getInput());
            System.out.println("Hai selezionato il messaggio con ID: " + messageId);

            // Ottieni la scelta del tipo di risposta
            int replyChoice = MessagesView.chooseReplyType();

            switch (replyChoice) {
                case 1 -> replyToMessageInPrivate(conversation, messageId); // Rispondi in privato
                case 2 -> replyToMessageInPublic(conversation, messageId);  // Rispondi in pubblico
                default -> MessagesView.showError("Opzione non valida.");
            }
        } catch (IOException e) {
            MessagesView.showError("Errore di input/output: " + e.getMessage());
        } catch (NumberFormatException e) {
            MessagesView.showError("Input non valido. Inserisci un numero valido per l'ID del messaggio.");
        }
    }


    private void replyToMessageInPrivate(Conversation conversation, Long messageId) {
        try {
            // Recupera il contenuto del messaggio da inviare
            String contenuto = MessagesView.getInputContent();

            // Recupera il destinatario dal messaggio esistente
            String destinatario = messageDAO.getRecipientFromMessage(messageId);

            // Ottieni il nome del progetto dalla conversazione esistente
            String nomeProgetto = conversation.getProjectName();  // Usa il metodo della conversazione per ottenere il nome del progetto

            // Verifica che il nome del progetto sia valido
            if (nomeProgetto == null || nomeProgetto.isEmpty()) {
                MessagesView.showError("Errore: Nome del progetto non trovato.");
                return;
            }

            MessageDAO messageDAO;
            try {
                messageDAO = new MessageDAO(credentials);
            } catch (SQLException e) {
                MessagesView.showError("Errore durante l'inizializzazione del DAO: " + e.getMessage());
                return;
            }

            // Crea una nuova conversazione privata e rispondi al messaggio
            messageDAO.createPrivateConversationAndReply(contenuto, destinatario, nomeProgetto, messageId, conversation.getID());  // Aggiunto originalConversationId

        } catch (IOException e) {
            MessagesView.showError("Errore nell'input del contenuto del messaggio: " + e.getMessage());
        } catch (DAOException e) {
            MessagesView.showError("Errore nella risposta privata: " + e.getMessage());
        }
    }



    private void replyToMessageInPublic(Conversation conversation, Long messageId) {
        try {
            // Richiedi il contenuto del messaggio dall'utente
            String contenuto = MessagesView.getInputContent();

            // Ottieni il timestamp corrente
            LocalDateTime datetime = LocalDateTime.now();

            // Invia la risposta pubblica utilizzando il metodo replyToMessage di MessageDAO
            Message responseMessage = messageDAO.replyToMessage(contenuto, messageId, credentials.getUsername(), conversation.getID(), datetime);
            System.out.println("Risposta pubblica inviata con successo: " + responseMessage.getContent());

        } catch (IOException e) {
            MessagesView.showError("Errore di input/output: " + e.getMessage());
        } catch (DAOException e) {
            MessagesView.showError("Errore nella risposta pubblica: " + e.getMessage());
        }
    }
}