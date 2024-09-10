package controller;

import model.dao.ProjectDAO;
import model.domain.Credentials;
import view.AdminView;
import model.dao.UserDAO;
import model.domain.Project;

import java.io.IOException;
import exception.DAOException;
import java.util.List;

public class AdminController implements GeneralController {
    private final Credentials credentials;

    public AdminController(Credentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public void start() {
        while (true) {
            int choice;
            try {
                choice = AdminView.showAdminMenu();
            } catch (IOException e) {
                throw new RuntimeException("Errore di input/output: " + e.getMessage(), e);
            }

            switch (choice) {
                case 1 -> createNewEmployee();
                case 2 -> createNewProject();
                case 3 -> closeProject();
                case 4 -> deleteEmployee();
                case 5 -> showProjects();
                case 6 -> {
                    try {
                        ProjectManagerController projectManagerController = new ProjectManagerController(credentials);
                        projectManagerController.start(); // Potrebbe lanciare IOException
                    } catch (IOException e) {
                        AdminView.showError("Errore di input/output durante le operazioni del project manager: " + e.getMessage());
                    }
                }
                case 7 -> {
                    EmployeeController employeeController = new EmployeeController(credentials);
                    employeeController.start();
                }
                case 8 -> System.exit(0);
                default -> throw new RuntimeException("Scelta non valida");
            }
        }
    }

    private void createNewEmployee() {
        System.out.println("Creazione di un nuovo dipendente...");

        try {
            // Chiedi all'utente di inserire i dettagli del nuovo dipendente
            String username = AdminView.getInput("Inserisci l'username del dipendente:");
            String password = AdminView.getInput("Inserisci la password del dipendente:");
            String name = AdminView.getInput("Inserisci il nome del dipendente:");
            String surname = AdminView.getInput("Inserisci il cognome del dipendente:");

            int roleId = 0;
            while (true) {
                try {
                    String roleInput = AdminView.getInput("Inserisci l'ID del ruolo del dipendente (1 = DIPENDENTE, 2 = CAPO_PROGETTO, 3 = AMMINISTRATORE):");
                    roleId = Integer.parseInt(roleInput);

                    // Verifica se il ruolo è valido
                    if (roleId >= 1 && roleId <= 3) {
                        break;  // Esce dal ciclo se il ruolo è valido
                    } else {
                        AdminView.showError("ID ruolo non valido. Deve essere un numero tra 1 e 3.");
                    }
                } catch (NumberFormatException e) {
                    AdminView.showError("Errore: inserisci un numero intero per il ruolo.");
                }
            }

            // Utilizza UserDAO per aggiungere il dipendente
            UserDAO userDAO = new UserDAO();
            userDAO.addEmployee(username, password, name, surname, roleId);

            System.out.println("Dipendente creato con successo!");

        } catch (DAOException e) {
            AdminView.showError("Errore durante la creazione del dipendente: " + e.getMessage());
        }
    }


    private void createNewProject() {
        System.out.println("Creazione di un nuovo progetto...");

        try {
            String nome = AdminView.getInput("Inserisci il nome del progetto:");
            String descrizione = AdminView.getInput("Inserisci la descrizione del progetto:");

            ProjectDAO projectDAO = new ProjectDAO();
            Project newProject = projectDAO.addProject(nome, descrizione);

            System.out.println("Progetto creato con successo!");

        } catch (DAOException e) {
            AdminView.showError("Errore durante la creazione del progetto: " + e.getMessage());
        }
    }


    private void closeProject() {
        System.out.println("Chiusura di un progetto...");

        try {
            // Chiedi all'utente di inserire il nome del progetto da chiudere
            String nomeProgetto = AdminView.getInput("Inserisci il nome del progetto da chiudere:");

            // Verifica che il nome del progetto non sia vuoto
            if (nomeProgetto == null || nomeProgetto.trim().isEmpty()) {
                AdminView.showError("Nome del progetto non può essere vuoto.");
                return;
            }

            // Utilizza il DAO per chiudere il progetto
            ProjectDAO projectDAO = new ProjectDAO();
            projectDAO.closeProject(nomeProgetto);

            System.out.println("Progetto '" + nomeProgetto + "' chiuso con successo!");

        } catch (DAOException e) {
            AdminView.showError("Errore durante la chiusura del progetto: " + e.getMessage());
        }
    }

    private void deleteEmployee() {
        System.out.println("Eliminazione di un dipendente...");
        String username;

        try {
            username = AdminView.getInput("Inserisci lo username del dipendente da eliminare: ");
            UserDAO userDAO = new UserDAO();
            userDAO.deleteEmployee(username);
            System.out.println("Dipendente '" + username + "' eliminato con successo.");
        } catch (DAOException e) {
            AdminView.showError("Errore durante l'eliminazione del dipendente: " + e.getMessage());
        }
    }

    private void showProjects() {
        System.out.println("Visualizzazione dei progetti disponibili...");

        try {
            ProjectDAO projectDAO = new ProjectDAO();
            List<Project> projects = projectDAO.getAllProjects();  // Recupera tutti i progetti

            if (projects.isEmpty()) {
                System.out.println("Non ci sono progetti disponibili.");
            } else {
                AdminView.showProjects(projects);  // Mostra i progetti attraverso la view
            }

        } catch (DAOException e) {
            AdminView.showError("Errore durante il recupero dei progetti: " + e.getMessage());
        }
    }
}

