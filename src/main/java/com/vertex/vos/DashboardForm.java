package com.vertex.vos;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class DashboardForm extends Application {
    private DashboardController dashBoardController;
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("dashBoard.fxml"));
        Parent root = loader.load();
        dashBoardController = loader.getController();

        // Create a StackPane as the root node
        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(root);

        Scene scene = new Scene(stackPane, 800, 600); // Set initial dimensions, but these can be adjusted
        primaryStage.setScene(scene);
        primaryStage.setTitle("VOS");

        primaryStage.setResizable(true);

        VBox closeButton = (VBox) scene.lookup("#closeBox");

        VBox minimizeButton = (VBox) scene.lookup("#minimizeBox");

        VBox maximizeButton = (VBox) scene.lookup("#maximizeBox");
        // Set click listener for the closeButton
        closeButton.setOnMouseClicked(event -> {
            Platform.exit();
        });
        minimizeButton.setOnMouseClicked(event -> {
            primaryStage.setIconified(true);

        });
        maximizeButton.setOnMouseClicked(event -> {
            primaryStage.setMaximized(true);
        });
        primaryStage.setMaximized(true);
        // Bind the scene dimensions to the StackPane's dimensions
        stackPane.prefWidthProperty().bind(scene.widthProperty());
        stackPane.prefHeightProperty().bind(scene.heightProperty());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public DashboardController getDashBoardController() {
        return dashBoardController;
    }
}
