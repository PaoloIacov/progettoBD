package view;

import model.domain.Conversation;
import model.domain.Message;

import java.io.IOException;
import java.util.List;

public class ConversationView {

    public static void showConversations(List<Conversation> conversations) {
        if (conversations == null || conversations.isEmpty()) {
            System.out.println("\n****************************");
            System.out.println("* Nessuna Conversazione Disponibile *");
            System.out.println("****************************\n");
            return;
        }

        System.out.println("\n================= Lista delle Conversazioni =================");
        System.out.println("------------------------------------------------------------");

        // Ciclo su ogni conversazione e stampa i dettagli con una formattazione migliorata
        for (int i = 0; i < conversations.size(); i++) {
            Conversation conversation = conversations.get(i);
            System.out.printf("[%d] Descrizione: %-20s | Progetto: %-15s | Tipo: %-10s\n",
                    (i + 1), conversation.getDescription(), conversation.getProjectName(), conversation.getType());
            System.out.println("------------------------------------------------------------");
        }
        System.out.println("============================================================\n");
    }

    public static int showConversationMenu(List<Conversation> conversations) throws IOException {
        showConversations(conversations);

        System.out.println("Seleziona una conversazione per visualizzarne i messaggi (inserisci il numero): ");
        System.out.println("0) Torna indietro");

        int conversationChoice;
        while (true) {
            try {
                conversationChoice = Integer.parseInt(InputManager.getInput());
                if (conversationChoice >= 0 && conversationChoice <= conversations.size()) {
                    System.out.println("Hai selezionato la conversazione: " + conversationChoice);
                    break;
                }
                System.out.println("Opzione non valida, riprova.");
            } catch (NumberFormatException e) {
                System.out.println("Input non valido. Inserisci un numero.");
            }
        }
        return conversationChoice;
    }


    public static void showError(String errorMessage) {
        System.out.println("Errore: " + errorMessage);
    }
}
