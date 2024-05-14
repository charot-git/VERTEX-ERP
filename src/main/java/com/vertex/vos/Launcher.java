package com.vertex.vos;

import com.vertex.vos.LoadingScreenController;
import com.vertex.vos.LoginForm;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.LocationCache;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.Objects;
import java.util.concurrent.*;

public class Launcher extends Application {
    private static Stage loadingStage;
    private static LoadingScreenController loadingController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        final boolean[] errorOccurred = {false}; // Flag to track error status

        try {
            // Show loading screen
            showLoadingScreen();
            new Thread(() -> {
                try {
                    // Simulate some initialization work with progress updates
                    for (int i = 1; i <= 100 && !errorOccurred[0]; i++) {
                        final int progress = i;
                        Platform.runLater(() -> loadingController.setLoadingProgress(progress / 100.0));

                        // Update subtext based on progress
                        if (progress <= 25) {
                            Platform.runLater(() -> loadingController.setSubText("Caching locations"));
                            LocationCache.initialize();
                        } else if (progress <= 50) {
                            Platform.runLater(() -> loadingController.setSubText("Connecting to the database"));

                            Future<Boolean> connectionFuture = new FutureTask<>(DatabaseConnectionPool::testConnection);
                            ExecutorService executor = Executors.newSingleThreadExecutor();
                            executor.submit((FutureTask<Boolean>) connectionFuture);

                            try {
                                boolean isConnected = connectionFuture.get(3, TimeUnit.SECONDS);
                                if (isConnected) {
                                    // Connection successful
                                } else {
                                    Platform.runLater(() -> {
                                        DialogUtils.showErrorMessage("Database Connection Error", "Failed to connect to the database. Please check your configuration.");
                                        loadingStage.close();
                                        Platform.exit();
                                    });
                                    errorOccurred[0] = true; // Set error flag
                                }
                            } catch (TimeoutException e) {
                                // Connection timed out
                                Platform.runLater(() -> {
                                    DialogUtils.showErrorMessage("Database Connection Error", "Failed to connect to the database. Please check your configuration.");
                                    loadingStage.close();
                                    Platform.exit();
                                });
                                errorOccurred[0] = true; // Set error flag
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                                Platform.runLater(() -> {
                                    DialogUtils.showErrorMessage("Error", "An unexpected error occurred. Please try again later.");
                                    loadingStage.close();
                                    Platform.exit();
                                });
                                errorOccurred[0] = true; // Set error flag
                            } finally {
                                executor.shutdown();
                            }
                        } else if (progress <= 75) {
                            Platform.runLater(() -> loadingController.setSubText("Loading User Interface"));
                        } else {
                            Platform.runLater(() -> loadingController.setSubText("A little further"));
                        }

                        Thread.sleep(30); // Simulate work being done
                    }

                    // Initialize the login form in the background if no error occurred
                    if (!errorOccurred[0]) {
                        Platform.runLater(this::showLogin);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (ExceptionInInitializerError e) {
            e.printStackTrace();
            DialogUtils.showErrorMessage("Database Connection Error", "Failed to connect to the database. Please check your configuration.");
            Platform.exit();
        }
    }
    Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/vos.png")));


    private void showLoadingScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("LoadingScreen.fxml"));
            AnchorPane root = loader.load();
            loadingController = loader.getController();
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);

            // Find the ImageView containing the logo
            ImageView logoView = (ImageView) root.lookup("#icon");
            logoView.setImage(logo);

            // Create a ScaleTransition for pulsating effect
            ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(1), logoView);
            scaleTransition.setByX(0.2); // Scale factor for X
            scaleTransition.setByY(0.2); // Scale factor for Y
            scaleTransition.setCycleCount(Animation.INDEFINITE);
            scaleTransition.setAutoReverse(true);
            scaleTransition.setInterpolator(Interpolator.EASE_BOTH);
            scaleTransition.play();

            loadingStage = new Stage();
            loadingStage.getIcons().add(logo);
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
            boolean connected = loginForm.showLoginForm(loginStage);
            if (connected) {
                loadingStage.close();
            } else {
                DialogUtils.showErrorMessage("Connection Error", "Cannot connect to the server. Please try again later.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
