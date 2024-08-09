package com.vertex.vos;

import com.vertex.vos.Utilities.DialogUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Objects;

public class LoginForm extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        if (!initializeLoginForm(primaryStage)) {
            Platform.exit();
        }
    }

    private boolean initializeLoginForm(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("loginForm.fxml"));
            Parent root = loader.load();
            Scene scene = createScene(root);

            configurePrimaryStage(primaryStage, scene);
            configureButtons(scene);
            return true;
        } catch (Exception e) {
            DialogUtils.showErrorMessage("Error", "An error occurred while loading the login form: " + e.getMessage());
            return false;
        }
    }

    private Scene createScene(Parent root) {
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        return scene;
    }

    private void configurePrimaryStage(Stage primaryStage, Scene scene) {
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/vos.png")));
        primaryStage.getIcons().add(icon);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Welcome VOS");
        primaryStage.centerOnScreen();
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void configureButtons(Scene scene) {
        Button signInButton = (Button) scene.lookup("#signInButton");
        ImageView closeButton = (ImageView) scene.lookup("#closeButton");
        ImageView minimizeButton = (ImageView) scene.lookup("#minimizeButton");
        ImageView maximizeButton = (ImageView) scene.lookup("#maximizeButton");

        if (signInButton != null && closeButton != null) {
            minimizeButton.setVisible(false);
            maximizeButton.setVisible(false);
            closeButton.setOnMouseClicked(event -> Platform.exit());
            closeButton.setVisible(true);
        }
    }

    public static void main(String[] args) {
        System.setProperty("prism.order", "d3d");
        launch(args);
    }
}
