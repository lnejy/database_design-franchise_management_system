package client_store.main;

import client_store.controller.LoginController;
import client_store.view.LoginView;

public class StoreAppMain {
    public static void main(String[] args) {
        // Swing 스레드 안전성 보장
        javax.swing.SwingUtilities.invokeLater(() -> {
            LoginView view = new LoginView();
            new LoginController(view);
        });
    }
}