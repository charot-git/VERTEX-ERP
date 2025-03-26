package com.vertex.vos.Utilities;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.function.Consumer;

public class WindowLoader {
    private static final String LOADING_STYLE = "-fx-border-color: #3498db; -fx-border-width: 2px; -fx-border-style: dashed;";

    public static void openWindowAsync(VBox triggerButton, String fxmlPath, String title, Consumer<Object> controllerInitializer) {
        if (triggerButton.getUserData() instanceof Stage stage && stage.isShowing()) {
            stage.toFront();
            return;
        }

        // Store original style
        String originalStyle = triggerButton.getStyle();

        // Apply loading effect
        Platform.runLater(() -> {
            triggerButton.setDisable(true);
            triggerButton.setStyle(LOADING_STYLE);
        });

        Task<Parent> loadTask = new Task<>() {
            @Override
            protected Parent call() throws IOException {
                FXMLLoader loader = new FXMLLoader(WindowLoader.class.getResource("/" + fxmlPath));
                Parent root = loader.load();
                if (controllerInitializer != null) {
                    controllerInitializer.accept(loader.getController());
                }
                return root;
            }
        };

        loadTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                triggerButton.setDisable(false);
                triggerButton.setStyle(originalStyle); // Restore original style

                Stage newStage = new Stage();
                newStage.setTitle(title);
                newStage.setMaximized(true);
                newStage.setScene(new Scene(loadTask.getValue()));
                newStage.show();
                newStage.setOnCloseRequest(e -> triggerButton.setUserData(null));
                triggerButton.setUserData(newStage);
            });
        });

        loadTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                triggerButton.setDisable(false);
                triggerButton.setStyle(originalStyle); // Restore original style
                DialogUtils.showErrorMessage("Error", "Unable to open " + title + " (" + fxmlPath + ").");
            });
            loadTask.getException().printStackTrace();
        });

        new Thread(loadTask).start();
    }
}
