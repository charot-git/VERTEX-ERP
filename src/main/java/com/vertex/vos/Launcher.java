package com.vertex.vos;

import com.vertex.vos.LoadingScreenController;
import com.vertex.vos.LoginForm;
import com.vertex.vos.Utilities.LocationCache;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Launcher extends Application {
    private static Stage loadingStage;
    private static LoadingScreenController loadingController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Show loading screen
        showLoadingScreen();

        // Start a background thread to initialize the login form
        new Thread(() -> {
            try {
                // Simulate some initialization work with progress updates
                for (int i = 1; i <= 100; i++) {
                    final int progress = i;
                    Platform.runLater(() -> loadingController.setLoadingProgress(progress / 100.0));

                    // Update subtext based on progress
                    if (progress <= 25) {
                        Platform.runLater(() -> loadingController.setSubText("Caching locations"));
                    } else if (progress <= 50) {
                        Platform.runLater(() -> loadingController.setSubText("Initiating your chat"));
                    } else if (progress <= 75) {
                        Platform.runLater(() -> loadingController.setSubText("Processing data"));
                    } else {
                        Platform.runLater(() -> loadingController.setSubText("A little further"));
                    }

                    Thread.sleep(30); // Simulate work being done
                }

                // Initialize the login form in the background
                Platform.runLater(this::showLogin);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }




    private void showLoadingScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("LoadingScreen.fxml"));
            AnchorPane root = loader.load();
            loadingController = loader.getController();
            Scene scene = new Scene(root);

            loadingStage = new Stage();
            loadingStage.initStyle(StageStyle.UNDECORATED);
            loadingStage.initStyle(StageStyle.TRANSPARENT);
            loadingStage.setScene(scene);
            loadingStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showLogin() {
        try {
            Stage loginStage = new Stage();
            LoginForm loginForm = new LoginForm();
            loginForm.showLoginForm(loginStage);
            loadingStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
