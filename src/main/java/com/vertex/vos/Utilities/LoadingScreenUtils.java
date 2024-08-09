package com.vertex.vos.Utilities;

import com.vertex.vos.LoadingScreenController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;

public class LoadingScreenUtils {

    private static Stage loadingStage;
    private static final Object lock = new Object(); // Lock object for synchronization

    public static void showLoadingScreen() {
        Platform.runLater(() -> {
            synchronized (lock) {
                if (loadingStage == null || !loadingStage.isShowing()) {
                    try {
                        URL fxmlLocation = LoadingScreenUtils.class.getResource("/com/vertex/vos/loadingScreen.fxml");
                        if (fxmlLocation == null) {
                            throw new IllegalStateException("FXML file not found at /com/vertex/vos/loadingScreen.fxml");
                        }
                        FXMLLoader loader = new FXMLLoader(fxmlLocation);
                        Parent root = loader.load();
                        LoadingScreenController loadingController = loader.getController();
                        loadingController.setSubText("Loading, please wait...");
                        loadingController.pulsateImage();

                        loadingStage = new Stage();
                        loadingStage.setScene(new Scene(root));
                        loadingStage.initStyle(StageStyle.UNDECORATED); // Optional: remove window decorations
                        loadingStage.initModality(Modality.APPLICATION_MODAL); // Optional: make it modal
                        loadingStage.show();
                    } catch (IOException e) {
                        e.printStackTrace(); // Handle the exception according to your needs
                    }
                }
            }
        });
    }

    public static void hideLoadingScreen() {
        Platform.runLater(() -> {
            synchronized (lock) {
                if (loadingStage != null && loadingStage.isShowing()) {
                    loadingStage.close();
                }
            }
        });
    }
}
