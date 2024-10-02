package com.vertex.vos.Utilities;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class DialogUtils {
    public static void showCompletionDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static boolean showConfirmationDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();

        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static void showErrorMessage(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    public static void showErrorMessageForValidation(String title, String header ,String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    public static void contactYourDeveloper(String  type) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error in registering" + type);
        alert.setContentText("Please contact your I.T Management");
        alert.showAndWait();
    }
}
