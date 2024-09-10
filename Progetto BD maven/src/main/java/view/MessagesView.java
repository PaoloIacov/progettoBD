package view;

import model.domain.Message;

import java.io.IOException;
import java.util.List;

public class MessagesView {

    public static void showMessages(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            System.out.println("Non ci sono messaggi nella conversazione.");
            return;
        }

        System.out.println("*********************************");
        System.out.println("*   MESSAGGI NELLA CONVERSAZIONE   *");
        System.out.println("*********************************\n");

        for (Message message : messages) {
            System.out.println("=====================================");
            System.out.println("ID Messaggio  : " + message.getID());
            System.out.println("Autore        : " + message.getUsernameDipendente());
            System.out.println("Data e Ora    : " + message.getDateTime());
            System.out.println("Contenuto     : " + message.getContent());
            System.out.println("-------------------------------------\n");
        }
    }

    public static void showError(String errorMessage) {
        System.out.println("Errore: " + errorMessage);
    }

    public static int showMessageMenu() throws IOException {
        System.out.println("1) Rispondi a un messaggio");
        System.out.println("2) Invia un nuovo messaggio");
        System.out.println("3) Torna indietro");

        return InputManager.readInt(1, 3);
    }

    public static String getInputContent() throws IOException {
        System.out.print("Inserisci il contenuto del messaggio: ");
        return InputManager.getInput();
    }

    public static int chooseReplyType() throws IOException {
        System.out.println("Scegli il tipo di risposta:");
        System.out.println("1) Rispondi in privato");
        System.out.println("2) Rispondi in pubblico");
        return InputManager.readInt(1, 2);
    }

}
