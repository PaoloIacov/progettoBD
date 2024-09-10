package view;

import model.domain.Project;

import java.io.IOException;
import java.util.List;

public class ProjectManagerView {
    public static int showProjectManagerMenu() throws IOException {
        int choice = 0;

        System.out.println("*********************************");
        System.out.println("*   DASHBOARD CAPO PROGETTO   *");
        System.out.println("*********************************\n");
        System.out.println("*** Scegli un'azione ***\n");
        System.out.println("1) Aggiungi un dipendente al progetto");
        System.out.println("2) Aggiungi un dipendente a una conversazione");
        System.out.println("3) Rimuovi un dipendente dal progetto");
        System.out.println("4) Rimuovi un dipendente da una conversazione");
        System.out.println("5) Crea una nuova conversazione");
        System.out.println("6) Chiudi una conversazione");
        System.out.println("7) Vai alle operazioni di livello dipendente");
        System.out.println("8) Esci");

        while (true) {
            System.out.print("Scegli l'operazione: ");
            try {
                String input = InputManager.getInput();  // Usa InputManager per ottenere l'input
                choice = Integer.parseInt(input);  // Converte l'input a un numero intero

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

    public static int selectProject(List<Project> projects) throws IOException {
        System.out.println("Progetti assegnati:");
        for (int i = 0; i < projects.size(); i++) {
            System.out.println((i + 1) + ") " + projects.get(i).getProjectName() + " - " + projects.get(i).getDescription());
        }

        int choice;
        while (true) {
            System.out.print("Seleziona un progetto (numero) o inserisci 0 per uscire: ");
            try {
                String input = InputManager.getInput();
                choice = Integer.parseInt(input) - 1;  // Converti a indice di lista
                if (choice >= -1 && choice < projects.size()) {  // Permetti l'uscita con 0
                    return choice;
                } else {
                    System.out.println("Opzione non valida, riprova.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Inserisci un numero valido.");
            }
        }
    }

    public static void showError(String errorMessage) {
        System.out.println("Errore: " + errorMessage);
    }
}
