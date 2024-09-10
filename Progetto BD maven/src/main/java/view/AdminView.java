package view;

import java.io.IOException;
import java.util.Scanner;
import java.util.List;
import model.domain.Project;

public class AdminView {
    public static int showAdminMenu() throws IOException {
        int choice = 0;

        System.out.println("*********************************");
        System.out.println("*   DASHBOARD ADMIN   *");
        System.out.println("*********************************\n");
        System.out.println("*** Scegli un'azione ***\n");
        System.out.println("1) Crea un nuovo dipendente");
        System.out.println("2) Crea un nuovo progetto");
        System.out.println("3) Chiudi un progetto");
        System.out.println("4) Elimina un dipendente");
        System.out.println("5) Mostra i progetti");
        System.out.println("6) Passa alle operazioni di livello capo progetto");
        System.out.println("7) Passa alle operazioni di livello dipendente");
        System.out.println("8) Esci");

        while (true) {
            System.out.print("Scegli l'operazione: ");
            try {
                String input = InputManager.getInput();
                choice = Integer.parseInt(input);

                if (choice >= 1 && choice <= 8) {
                    break;
                } else {
                    System.out.println("Opzione non valida, riprova");
                }
            } catch (NumberFormatException e) {
                System.out.println("Inserisci un numero valido.");
            }
        }

        return choice;
    }

    public static String getInput(String prompt) {
        Scanner scanner = new Scanner(System.in);
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public static void showError(String errorMessage) {
        System.out.println("Errore: " + errorMessage);
    }

    public static void showProjects(List<Project> projects) {
        System.out.println("\n====== Lista dei Progetti ======");

        for (Project project : projects) {
            System.out.println("Nome: " + project.getProjectName());
            System.out.println("Descrizione: " + project.getDescription());
            System.out.println("Stato: " + project.getState());
            System.out.println("-------------------------------");
        }
    }

}
