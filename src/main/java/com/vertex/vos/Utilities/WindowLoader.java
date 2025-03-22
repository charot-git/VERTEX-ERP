package com.vertex.vos.Utilities;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.function.Consumer;

public class WindowLoader {
    public static void openWindowAsync(VBox triggerButton, String fxmlPath, String title, Consumer<Object> controllerInitializer) {
        if (triggerButton.getUserData() instanceof Stage stage && stage.isShowing()) {
            stage.toFront();
            return;
        }

        ProgressIndicator loadingIndicator = new ProgressIndicator();
        triggerButton.getChildren().add(loadingIndicator);

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
            triggerButton.getChildren().remove(loadingIndicator);
            Platform.runLater(() -> {
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
            triggerButton.getChildren().remove(loadingIndicator);
            DialogUtils.showErrorMessage("Error", "Unable to open " + title + " (" + fxmlPath + ").");
            loadTask.getException().printStackTrace();
        });

        new Thread(loadTask).start();
    }

}
