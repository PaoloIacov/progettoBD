package view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InputManager {
    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private static boolean isClosed = false; // Per evitare di chiudere più volte

    public static String getInput() throws IOException {
        if (isClosed) {
            throw new IOException("Il flusso di input è già chiuso."); // Rileva errori di flusso chiuso
        }
        return reader.readLine();
    }

    public static int readInt(int min, int max) throws IOException {
        int choice;
        while (true) {
            try {
                System.out.print("Inserisci un numero tra " + min + " e " + max + ": ");
                String input = reader.readLine();
                choice = Integer.parseInt(input);

                if (choice >= min && choice <= max) {
                    break;
                } else {
                    System.out.println("Opzione non valida, inserisci un numero tra " + min + " e " + max + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("Input non valido. Inserisci un numero.");
            }
        }
        return choice;
    }

    public static void close() {
        if (!isClosed) {
            try {
                reader.close();
                isClosed = true;
                System.out.println("Flusso di input chiuso con successo.");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Errore durante la chiusura del flusso di input.");
            }
        }
    }
}
