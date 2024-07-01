package com.vertex.vos;

import com.vertex.vos.Objects.VersionControl;
import com.vertex.vos.Utilities.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.*;

public class Main extends Application {

    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final int VERSION = 2;
    private static final int NUM_TASKS = 100;
    private static final int CONNECTION_TIMEOUT_SECONDS = 3;
    private static final int PROGRESS_UPDATE_INTERVAL_MILLIS = 30;

    private final VersionControlDAO versionControlDAO = new VersionControlDAO();

    @Override
    public void start(Stage stage) throws Exception {
        showLoadingScreen(stage);
    }

    private void showLoadingScreen(Stage stage) {
        try {
            VersionControl activeVersion = versionControlDAO.getVersionById(VERSION);
            if (activeVersion != null && activeVersion.isActive()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("loadingScreen.fxml"));
                AnchorPane root = loader.load();
                LoadingScreenController loadingController = loader.getController();
                loadingController.pulsateImage();
                Scene scene = new Scene(root);
                scene.setFill(Color.TRANSPARENT);
                stage.initStyle(StageStyle.UNDECORATED);
                stage.initStyle(StageStyle.TRANSPARENT);
                stage.setScene(scene);
                stage.setTitle("VOS Ver" + VERSION);
                stage.show();

                initializeApp(loadingController, stage);
            } else {
                DialogUtils.showErrorMessage("Version Error", "You have an outdated version of the system.");
                Platform.exit();
            }
        } catch (IOException e) {
            handleException("Error loading FXML", e);
        } catch (Exception e) {
            handleException("Unexpected error", e);
        }
    }

    private void openLoginStage() {
        LoginForm loginForm = new LoginForm();
        try {
            loginForm.start(new Stage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean initializeApp(LoadingScreenController controller, Stage stage) {
        try {
            for (int i = 1; i <= NUM_TASKS; i++) {
                final int progress = i;
                Platform.runLater(() -> controller.setLoadingProgress(progress / (double) NUM_TASKS));
                if (progress <= 25) {
                    initializeLocationCache(controller);
                } else if (progress <= 50) {
                    if (!initializeDatabaseConnection(controller)) {
                        return false;
                    }
                } else if (progress <= 75) {
                    Platform.runLater(() -> controller.setSubText("Loading User Interface"));
                } else {
                    Platform.runLater(() -> controller.setSubText("Ready"));
                    stage.close();
                    openLoginStage();
                    return true;
                }
                Thread.sleep(PROGRESS_UPDATE_INTERVAL_MILLIS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void initializeLocationCache(LoadingScreenController controller) {
        Platform.runLater(() -> controller.setSubText("Caching locations"));
        LocationCache.initialize();
    }

    private boolean initializeDatabaseConnection(LoadingScreenController controller) {
        Platform.runLater(() -> controller.setSubText("Connecting to the database"));
        FutureTask<Boolean> connectionFuture = new FutureTask<>(DatabaseConnectionPool::testConnection);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(connectionFuture);

        try {
            boolean isConnected = connectionFuture.get(CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!isConnected) {
                DialogUtils.showErrorMessage("Database Connection Error", "Failed to connect to the database. Please check your configuration.");
                Platform.exit();
                return false; // Initialization failed
            }
        } catch (TimeoutException e) {
            handleException("Database Connection Error: Connection timed out", e);
            Platform.exit();
            return false; // Initialization failed
        } catch (InterruptedException | ExecutionException e) {
            handleException("Unexpected error while connecting to database", e);
            Platform.exit();
            return false; // Initialization failed
        } finally {
            executor.shutdown();
        }
        return true;
    }

    private void handleException(String message, Exception e) {
        log.error(message, e);
        DialogUtils.showErrorMessage("Error", "An unexpected error occurred. Please try again later.");
        Platform.exit();
    }

    public static void main(String[] args) {
        System.setProperty("prism.order", "d3d");
        launch(args);
    }
}
