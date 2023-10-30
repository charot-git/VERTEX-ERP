package com.vertex.vos.Utilities;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class ToDoAlert {

    public static void showToDoAlert() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("To-Do");
        alert.setHeaderText(null);
        alert.setContentText("under implementation");
        alert.showAndWait();
    }
}
