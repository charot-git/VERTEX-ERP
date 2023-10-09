package com.example.vos;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class LoginForm extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("loginForm.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Welcome VOS");
        primaryStage.centerOnScreen();
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setResizable(false);
        primaryStage.show();


        Button signInButton = (Button) scene.lookup("#signInButton");

        ImageView closeButton = (ImageView) scene.lookup("#closeButton");

        ImageView minimizeButton = (ImageView) scene.lookup("#minimizeButton");

        ImageView maximizeButton = (ImageView) scene.lookup("#maximizeButton");

        minimizeButton.setVisible(false);
        maximizeButton.setVisible(false);

        // Set click listener for the closeButton
        closeButton.setOnMouseClicked(event -> {
            Platform.exit();
        });

        closeButton.setVisible(true);

        scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    signInButton.fire();
                    event.consume();
                }
            }
        });


    }

    public static void main(String[] args) {
        launch(args);
    }


}
