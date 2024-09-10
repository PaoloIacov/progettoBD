package org.example;

import controller.ApplicationController;
import view.InputManager;

public class Main {
    public static void main(String[] args) {
        try {
            ApplicationController appController = new ApplicationController();
            appController.start();
        } finally {
            // Chiudi il flusso di input alla fine dell'esecuzione dell'applicazione
            InputManager.close();
        }
    }
}
