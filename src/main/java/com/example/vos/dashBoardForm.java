package com.example.vos;

import com.zaxxer.hikari.HikariDataSource;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.vos.DatabaseConnectionPool; // Make sure to import your package

public class dashBoardForm extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("dashBoard.fxml"));
        Parent root = loader.load();
        DashBoardController controller = loader.getController();

        // Create a StackPane as the root node
        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(root);

        Scene scene = new Scene(stackPane, 800, 600); // Set initial dimensions, but these can be adjusted
        primaryStage.setScene(scene);
        primaryStage.setTitle("VOS");

        primaryStage.setResizable(true); // Allow the stage to be resizable

        // Maximize the stage
        primaryStage.setMaximized(true);

        // Bind the scene dimensions to the StackPane's dimensions
        stackPane.prefWidthProperty().bind(scene.widthProperty());
        stackPane.prefHeightProperty().bind(scene.heightProperty());

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
