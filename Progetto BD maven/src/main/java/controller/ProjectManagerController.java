package controller;

import model.dao.*;
import model.domain.*;
import model.domain.enums.*;
import view.ProjectManagerView;
import view.InputManager;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import exception.DAOException;
import model.domain.Project;

public class ProjectManagerController implements GeneralController {
    private final Credentials credentials;
    private final ProjectDAO projectDAO;
    private final ConversationDAO conversationDAO;
    private Project assignedProject;

    public ProjectManagerController(Credentials credentials) {
        this.credentials = credentials;
        this.projectDAO = new ProjectDAO();
        this.conversationDAO = new ConversationDAO();
    }

    public void start() throws IOException {
        selectAssignedProject();  // Aggiungi questo metodo per selezionare il progetto all'inizio

        while (true) {
            int choice;
            try {
                choice = ProjectManagerView.showProjectManagerMenu();
            } catch (IOException e) {
                throw new RuntimeException("Errore di input/output: " + e.getMessage(), e);
            }

            switch (choice) {
                case 1 -> addEmployeeToProject();
                case 2 -> addEmployeeToConversation();
                case 3 -> removeEmployeeFromProject();
                case 4 -> removeEmployeeFromConversation();
                case 5 -> createNewConversation();
                case 6 -> closeConversation();
                case 7 -> {
                    EmployeeController employeeController = new EmployeeController(credentials);
                    employeeController.start();
                }
                case 8 -> System.exit(0);
                default -> throw new RuntimeException("Scelta non valida");
            }
        }
    }

    private void selectAssignedProject() {
        try {
            List<Project> projects;

            if (credentials.getRole() == Role.AMMINISTRATORE) { // Se l'utente è un admin
                // L'admin può accedere a tutti i progetti
                projects = projectDAO.getAllProjects();
            } else {
                // Se è un project manager, ottiene solo i progetti assegnati
                projects = projectDAO.getProjectsByEmployee(credentials.getUsername());
            }

            if (projects.isEmpty()) {
                System.out.println("Nessun progetto disponibile per l'utente: " + credentials.getUsername());
                System.out.println("Il programma verrà chiuso.");
                System.exit(0); // Chiude il programma
            }

            // Mostra i progetti assegnati e permette di selezionare uno
            int selectedProjectIndex;
            try {
                selectedProjectIndex = ProjectManagerView.selectProject(projects); // Gestisci IOException
            } catch (IOException e) {
                System.out.println("Errore di input/output durante la selezione del progetto: " + e.getMessage());
                System.exit(1); // Esce con codice di errore
                return; // Esci se c'è un errore
            }

            if (selectedProjectIndex >= 0 && selectedProjectIndex < projects.size()) {
                this.assignedProject = projects.get(selectedProjectIndex);
                System.out.println("Progetto selezionato: " + this.assignedProject.getProjectName());
            } else {
                System.out.println("Selezione non valida.");
                System.exit(0); // Chiude il programma in caso di selezione non valida
            }

        } catch (DAOException e) {
            throw new RuntimeException("Errore durante il recupero dei progetti: " + e.getMessage(), e);
        }
    }


    private void addEmployeeToProject() {
        if (assignedProject == null) {
            System.out.println("Errore: Nessun progetto selezionato.");
            return;
        }

        System.out.println("Aggiunta di un dipendente al progetto " + assignedProject.getProjectName() + " per l'utente: " + credentials.getUsername());

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Inserisci lo username del dipendente da aggiungere al progetto: ");
            String usernameDipendente = reader.readLine();
            projectDAO.execute("addEmployeeToProject", usernameDipendente, assignedProject.getProjectName());
            System.out.println("Dipendente " + usernameDipendente + " aggiunto con successo al progetto " + assignedProject.getProjectName() + ".");
        } catch (IOException | DAOException e) {
            System.out.println("Errore durante l'aggiunta del dipendente al progetto: " + e.getMessage());
        }
    }

private void addEmployeeToConversation() {
        System.out.println("Aggiunta di un dipendente a una conversazione per l'utente: " + credentials.getUsername());

        try {
            System.out.print("Inserisci l'username del dipendente da aggiungere alla conversazione: ");
            String employeeUsername = InputManager.getInput();

            System.out.print("Inserisci l'ID della conversazione: ");
            String conversationId = InputManager.getInput();

            //Aggiunge il dipendente alla conversazione
            conversationDAO.execute("addEmployeeToConversation", conversationId, employeeUsername);

        } catch (IOException e) {
            System.out.println("Errore durante la lettura dell'input: " + e.getMessage());
        } catch (DAOException e) {
            System.out.println("Errore durante l'aggiunta del dipendente alla conversazione: " + e.getMessage());
        }
    }



    private void removeEmployeeFromProject() {
        System.out.println("Rimozione di un dipendente dal progetto " + assignedProject +" per l'utente: " + credentials.getUsername());

        // Ottenere il nome utente del dipendente da rimuovere
        String employeeUsername = null;
        try {
            System.out.print("Inserisci l'username del dipendente da rimuovere: ");
            employeeUsername = InputManager.getInput();
        } catch (IOException e) {
            System.out.println("Errore di input/output: " + e.getMessage());
            return;
        }

        if (employeeUsername == null || employeeUsername.isEmpty()) {
            System.out.println("Username del dipendente non valido. Operazione annullata.");
            return;
        }

        // Verifica se il dipendente fa parte del progetto
        try {
            List<String> employeesInProject = projectDAO.getEmployeesInProject(assignedProject.getProjectName());
            if (!employeesInProject.contains(employeeUsername)) {
                System.out.println("Il dipendente non fa parte del progetto. Operazione annullata.");
                return;
            }

            // Rimuovi il dipendente dal progetto
            projectDAO.execute("removeEmployeeFromProject", assignedProject.getProjectName(), employeeUsername);
            System.out.println("Dipendente '" + employeeUsername + "' rimosso dal progetto '" + assignedProject.getProjectName() + "' con successo.");

        } catch (DAOException e) {
            System.out.println("Errore durante la rimozione del dipendente dal progetto: " + e.getMessage());
        }
    }


    private void removeEmployeeFromConversation() throws IOException {
        System.out.println("Rimozione di un dipendente da una conversazione per l'utente: " + credentials.getUsername());

        try {
            // Passo 1: Ottenere le conversazioni per il progetto assegnato
            List<Conversation> conversations = conversationDAO.getConversationsByProject(assignedProject.getProjectName());

            if (conversations.isEmpty()) {
                System.out.println("Non ci sono conversazioni disponibili per il progetto.");
                return;
            }

            // Passo 2: Visualizzare le conversazioni e ottenere l'input dell'utente
            System.out.println("Seleziona una conversazione dalla lista:");
            for (int i = 0; i < conversations.size(); i++) {
                System.out.println((i + 1) + ") " + conversations.get(i).getDescription() + " (ID: " + conversations.get(i).getID() + ")");
            }

            System.out.print("Inserisci il numero della conversazione da cui vuoi rimuovere un dipendente: ");
            String conversationInput = InputManager.getInput();

            int conversationChoice;
            try {
                conversationChoice = Integer.parseInt(conversationInput) - 1;
            } catch (NumberFormatException e) {
                System.out.println("Inserisci un numero valido.");
                return;
            }

            if (conversationChoice < 0 || conversationChoice >= conversations.size()) {
                System.out.println("Scelta non valida.");
                return;
            }

            Conversation selectedConversation = conversations.get(conversationChoice);

            // Passo 3: Ottenere l'elenco dei dipendenti nella conversazione selezionata
            List<String> employeesInConversation = conversationDAO.getEmployeesInConversation(selectedConversation.getID());

            if (employeesInConversation.isEmpty()) {
                System.out.println("Non ci sono dipendenti in questa conversazione.");
                return;
            }

            // Passo 4: Visualizzare l'elenco dei dipendenti e ottenere l'input dell'utente
            System.out.println("Seleziona un dipendente da rimuovere dalla conversazione:");
            for (int i = 0; i < employeesInConversation.size(); i++) {
                System.out.println((i + 1) + ") " + employeesInConversation.get(i));
            }

            System.out.print("Inserisci il numero del dipendente da rimuovere: ");
            String employeeInput = InputManager.getInput();

            int employeeChoice;
            try {
                employeeChoice = Integer.parseInt(employeeInput) - 1;
            } catch (NumberFormatException e) {
                System.out.println("Inserisci un numero valido.");
                return;
            }

            if (employeeChoice < 0 || employeeChoice >= employeesInConversation.size()) {
                System.out.println("Scelta non valida.");
                return;
            }

            String selectedEmployee = employeesInConversation.get(employeeChoice);

            // Passo 5: Chiamare il metodo DAO per rimuovere il dipendente dalla conversazione
            conversationDAO.removeEmployeeFromConversation(selectedConversation.getID(), selectedEmployee);

            System.out.println("Dipendente " + selectedEmployee + " rimosso dalla conversazione: " + selectedConversation.getDescription());

        } catch (DAOException e) {
            System.out.println("Errore durante la rimozione del dipendente dalla conversazione: " + e.getMessage());
        }
    }

    private void createNewConversation() {
        System.out.println("Creazione di una nuova conversazione per l'utente: " + credentials.getUsername());

        // Verifica se esiste un progetto assegnato
        if (assignedProject == null) {
            System.out.println("Errore: Non hai un progetto assegnato.");
            return;
        }

        // Chiede la descrizione della conversazione
        System.out.print("Inserisci la descrizione della nuova conversazione: ");
        String description;
        try {
            description = InputManager.getInput();
        } catch (IOException e) {
            System.out.println("Errore di input/output: " + e.getMessage());
            return;
        }

        // Chiede il tipo di conversazione
        System.out.print("Inserisci il tipo di conversazione (PUBBLICA o PRIVATA): ");
        String typeInput;
        ConversationType conversationType;
        try {
            typeInput = InputManager.getInput().toUpperCase();
            conversationType = ConversationType.valueOf(typeInput);  // Converte l'input in enum
        } catch (IllegalArgumentException | IOException e) {
            System.out.println("Errore: Tipo di conversazione non valido. Deve essere 'PUBBLICA' o 'PRIVATA'.");
            return;
        }

        // Crea la nuova conversazione usando il DAO
        try {
            Conversation newConversation = conversationDAO.createConversation(description, assignedProject.getProjectName(), conversationType);
            System.out.println("Conversazione creata con successo! ID: " + newConversation.getID());
        } catch (DAOException e) {
            System.out.println("Errore durante la creazione della conversazione: " + e.getMessage());
        }
    }


    private void closeConversation() {
        System.out.println("Chiusura di una conversazione per l'utente: " + credentials.getUsername());

        // Verifica se esiste un progetto assegnato
        if (assignedProject == null) {
            System.out.println("Errore: Non hai un progetto assegnato.");
            return;
        }

        // Ottieni tutte le conversazioni del progetto assegnato
        List<Conversation> conversations;
        try {
            conversations = conversationDAO.getConversationsByProject(assignedProject.getProjectName());
        } catch (DAOException e) {
            System.out.println("Errore durante il recupero delle conversazioni: " + e.getMessage());
            return;
        }

        if (conversations.isEmpty()) {
            System.out.println("Non ci sono conversazioni da chiudere per il progetto " + assignedProject.getProjectName());
            return;
        }

        // Mostra tutte le conversazioni disponibili
        System.out.println("Conversazioni disponibili nel progetto " + assignedProject.getProjectName() + ":");
        for (Conversation conversation : conversations) {
            System.out.println("ID: " + conversation.getID() + ", Descrizione: " + conversation.getDescription() + ", Tipo: " + conversation.getType());
        }

        // Chiedi all'utente l'ID della conversazione da chiudere
        System.out.print("Inserisci l'ID della conversazione da chiudere: ");
        String conversationIdInput;
        try {
            conversationIdInput = InputManager.getInput();
        } catch (IOException e) {
            System.out.println("Errore di input/output: " + e.getMessage());
            return;
        }

        long conversationId;
        try {
            conversationId = Long.parseLong(conversationIdInput);
        } catch (NumberFormatException e) {
            System.out.println("Errore: L'ID della conversazione deve essere un numero valido.");
            return;
        }

        // Chiude la conversazione usando il DAO
        try {
            conversationDAO.closeConversation(conversationId);
            System.out.println("Conversazione con ID " + conversationId + " chiusa con successo.");
        } catch (DAOException e) {
            System.out.println("Errore durante la chiusura della conversazione: " + e.getMessage());
        }
    }

}
