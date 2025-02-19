package com.vertex.vos;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class AlreadyRunningAlert extends Application {
    @Override
    public void start(Stage primaryStage) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Application Already Running");
        alert.setHeaderText(null);
        alert.setContentText("The application is already running.");
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
