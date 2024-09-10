package controller;

import java.io.IOException;
import model.domain.Credentials;
import model.domain.enums.Role;
import model.dao.LoginDAO;
import view.LoginView;

public class ApplicationController implements GeneralController {
    private Credentials cred;

    @Override
    public void start() {
        LoginDAO loginDAO = new LoginDAO();
        LoginView loginView = new LoginView();
        LoginController loginController = new LoginController(loginView, loginDAO);
        loginController.start();
        cred = loginController.getCred();

        if (cred.getRole() == null) {
            throw new RuntimeException("Invalid credentials");
        }

        switch (cred.getRole()) {
            case DIPENDENTE -> new EmployeeController(cred).start();
            case CAPO_PROGETTO -> {
                try {
                    new ProjectManagerController(cred).start();  // Gestione dell'eccezione IOException
                } catch (IOException e) {
                    System.out.println("Errore di input/output: " + e.getMessage());
                }
            }
            case AMMINISTRATORE -> new AdminController(cred).start();
            default -> throw new RuntimeException("Ruolo non valido");
        }
    }
}
