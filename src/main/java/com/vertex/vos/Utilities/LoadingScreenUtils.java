package com.vertex.vos.Utilities;

import com.vertex.vos.LoadingScreenController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class LoadingScreenUtils {

    private static Stage loadingStage;

    public static void showLoadingScreen() {
        if (loadingStage == null || !loadingStage.isShowing()) {
            try {
                FXMLLoader loader = new FXMLLoader(LoadingScreenUtils.class.getResource("/com/vertex/vos/LoadingScreen.fxml"));
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

    public static void hideLoadingScreen() {
        if (loadingStage != null && loadingStage.isShowing()) {
            loadingStage.close();
        }
    }
}
