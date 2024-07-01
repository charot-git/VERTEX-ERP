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
        if (!showLoginForm(primaryStage)) {
            Platform.exit();
        }
    }

    public boolean showLoginForm(Stage primaryStage) {
        try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("loginForm.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                scene.setFill(Color.TRANSPARENT);
                Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/vos.png")));

                primaryStage.getIcons().add(image);
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
                closeButton.setOnMouseClicked(event -> Platform.exit());

                closeButton.setVisible(true);

                scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        signInButton.fire();
                        event.consume();
                    }
                });

                return true;
        } catch (Exception e) {
            DialogUtils.showErrorMessage("Connection Error", "Cannot connect to the server. Please try again later.");
            return false;
        }
    }

    public static void main(String[] args) {
        System.setProperty("prism.order", "d3d");
        launch(args);
    }
}
