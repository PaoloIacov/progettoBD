package view;

import model.domain.Credentials;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LoginView {

    private BufferedReader reader;

    public LoginView() {
        reader = new BufferedReader(new InputStreamReader(System.in));
    }

    public void showLoginScreen() {
        System.out.println("=== Login ===");
    }

    public Credentials getCredentialsInput() throws IOException {
        System.out.print("Inserisci il tuo username: ");
        String username = reader.readLine();
        System.out.print("Inserisci la tua password: ");
        String password = reader.readLine();

        return new Credentials(username, password, null);
    }

    public void showLoginSuccess(Credentials credentials) {
        System.out.println("Login riuscito! Benvenuto, " + credentials.getUsername() + ". Sei identificato come: " + credentials.getRole());
    }

    public void showLoginError(String errorMessage) {
        System.out.println("Errore di login: " + errorMessage);
    }

    public void showError(String errorMessage) {
        System.out.println("Errore: " + errorMessage);
    }
}
