package com.vertex.vos;

import com.vertex.vos.Utilities.LocationCache;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class LoadingScreenController {
    @FXML
    private ImageView icon;
    @FXML
    private ProgressBar loadingProgress;
    @FXML
    private Label subText;


    void loadDashboard(int userId) {
        {
            try {
                // Load the dashboard FXML file
                FXMLLoader loader = new FXMLLoader(getClass().getResource("dashBoard.fxml"));
                Parent root = loader.load();
                DashboardController dashboardController = loader.getController();
                Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/vos.png"));
                // Create a new stage for the dashboard
                Stage dashboardStage = new Stage();
                dashboardStage.setTitle("Vertex");
                dashboardStage.initStyle(StageStyle.UNDECORATED);

                dashboardStage.getIcons().add(image);
                // Set the scene for the dashboard stage
                Scene scene = new Scene(root); // Adjust dimensions as needed
                dashboardStage.setScene(scene);


                // Maximize the dashboard stage if desired
                dashboardStage.setMaximized(true);
                // Show the dashboard stage
                dashboardStage.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}



