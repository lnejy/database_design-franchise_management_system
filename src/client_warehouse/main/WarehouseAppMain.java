package client_warehouse.main;

import client_warehouse.view.WarehouseMainView;
import javax.swing.*;

public class WarehouseAppMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WarehouseMainView());
    }
}