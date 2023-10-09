package com.example.vos;

import com.zaxxer.hikari.HikariDataSource;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.vos.DatabaseConnectionPool; // Make sure to import your package
import javafx.stage.StageStyle;

public class dashBoardForm extends Application {
    private DashBoardController dashBoardController;
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

        ImageView closeButton = (ImageView) scene.lookup("#closeButton");

        ImageView minimizeButton = (ImageView) scene.lookup("#minimizeButton");

        ImageView maximizeButton = (ImageView) scene.lookup("#maximizeButton");

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

    public DashBoardController getDashBoardController() {
        return dashBoardController;
    }
}
