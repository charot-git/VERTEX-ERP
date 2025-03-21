package com.vertex.vos.Utilities;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class MaintenanceAlert {
    public static void showMaintenanceAlert() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Module Maintenance");
        alert.setHeaderText(null);
        alert.setContentText("Module under maintenance, please check back later.");
        alert.showAndWait();
    }
}
