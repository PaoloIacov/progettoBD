package controller;

import model.dao.LoginDAO;
import model.domain.Credentials;
import view.LoginView;

public class LoginController implements GeneralController {
    private final LoginView loginView;
    private final LoginDAO loginDAO;
    private Credentials cred;

    public LoginController(LoginView loginView, LoginDAO loginDAO) {
        this.loginView = loginView;
        this.loginDAO = loginDAO;
    }

    @Override
    public void start() {
        while (true) {
            try {

                loginView.showLoginScreen();
                Credentials inputCred = loginView.getCredentialsInput();


                cred = loginDAO.login(inputCred.getUsername(), inputCred.getPassword());

                if (cred != null) {
                    loginView.showLoginSuccess(cred);
                    break;
                } else {
                    loginView.showLoginError("Username o password errati. Riprova.");
                }
            } catch (Exception e) {
                loginView.showError("Errore di sistema: " + e.getMessage());
            }
        }
    }

    public Credentials getCred() {
        return cred;
    }
}
