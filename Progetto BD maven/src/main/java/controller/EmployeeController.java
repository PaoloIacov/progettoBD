package controller;

import exception.DAOException;
import model.dao.ConversationDAO;
import model.domain.Conversation;
import model.domain.Credentials;
import view.EmployeeView;
import model.dao.UserDAO;
import model.domain.User;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class EmployeeController implements GeneralController {

    private final Credentials credentials;
    private final ConversationDAO conversationDAO;
    private final UserDAO userDAO = new UserDAO();

    public EmployeeController(Credentials credentials) {
        this.credentials = credentials;
        this.conversationDAO = new ConversationDAO();  // Inizializzazione DAO per le conversazioni
    }

    @Override
    public void start() {
        while (true) {
            int choice;
            try {
                choice = EmployeeView.showEmployeeMenu();
            } catch (IOException e) {
                EmployeeView.showError("Errore di input/output: " + e.getMessage());
                continue;
            }

            switch (choice) {
                case 1 -> showConversations();  // Visualizzazione delle conversazioni
                case 2 -> showEmployeeInfo();  // Visualizzazione delle informazioni dell'utente
                case 3 -> System.exit(0);
                default -> EmployeeView.showError("Scelta non valida.");
            }
        }
    }

    private void showConversations() {
        System.out.println("Visualizzazione delle conversazioni per l'utente: " + credentials.getUsername());
        try {
            // Recupera le conversazioni dell'utente
            List<Conversation> conversations = conversationDAO.getConversationsByEmployee(credentials.getUsername());

            if (conversations.isEmpty()) {
                EmployeeView.showError("Non ci sono conversazioni da mostrare.");
            } else {
                try {
                    // Creazione e utilizzo del ConversationController
                    ConversationController conversationController = new ConversationController(credentials);
                    conversationController.showConversations(conversations);  // Passaggio delle conversazioni al controller
                } catch (SQLException e) {
                    EmployeeView.showError("Errore nella creazione del ConversationController: " + e.getMessage());
                    e.printStackTrace();  // Opzionale: per stampare lo stack trace completo
                }
            }
        } catch (DAOException e) {
            EmployeeView.showError("Errore nel recupero delle conversazioni: " + e.getMessage());
        }
    }
    private void showEmployeeInfo() {
        System.out.println("Visualizzazione delle informazioni per l'utente: " + credentials.getUsername());

        try {
            // Chiamata al DAO per recuperare le informazioni del dipendente
            User user = userDAO.getEmployeeInfo(credentials.getUsername());

            // Se il dipendente è stato trovato, mostra le informazioni tramite la View
            if (user != null) {
                EmployeeView.showEmployeeInfo(
                        user.getUsername(),
                        user.getName(),
                        user.getSurname(),
                        user.getRole()
                );
            } else {
                EmployeeView.showError("Dipendente con username '" + credentials.getUsername() + "' non trovato.");
            }

        } catch (DAOException e) {
            EmployeeView.showError("Errore durante il recupero delle informazioni del dipendente: " + e.getMessage());
        }
    }

}
