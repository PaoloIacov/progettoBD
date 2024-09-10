package view;

import java.io.IOException;
import model.domain.enums.Role;

public class EmployeeView {
    public static int showEmployeeMenu() throws IOException {
        System.out.println("*********************************");
        System.out.println("*   DASHBOARD DIPENDENTE   *");
        System.out.println("*********************************\n");
        System.out.println("*** Scegli un'azione ***\n");
        System.out.println("1) Visualizza le tue conversazioni");
        System.out.println("2) Visualizza le tue informazioni");
        System.out.println("3) Esci");

        int choice = 0;
        while (true) {
            System.out.print("Scegli l'operazione: ");
            try {
                choice = Integer.parseInt(InputManager.getInput());
                if (choice >= 1 && choice <= 3) {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Input non valido. Inserisci un numero.");
            }
            System.out.println("Opzione non valida, riprova");
        }

        return choice;
    }

    public static void showEmployeeInfo(String username, String nome, String cognome, int role) {
        System.out.println("=== Informazioni ===");
        System.out.println("Username: " + username);
        System.out.println("Nome: " + nome);
        System.out.println("Cognome: " + cognome);
        System.out.println("Ruolo: " + convertRoleToString(role));  // Converti l'int in una stringa leggibile
    }

    private static String convertRoleToString(int role) {
        return switch (role) {
            case 1 -> "DIPENDENTE";
            case 2 -> "CAPO PROGETTO";
            case 3 -> "AMMINISTRATORE";
            default -> "RUOLO SCONOSCIUTO";
        };
    }

    public static void showError(String errorMessage) {
        System.out.println("Errore: " + errorMessage);
    }
}
