package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.ComboBoxFilterUtil;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextInputDialog;
import javafx.util.StringConverter;

import java.util.Optional;

public class EntryAlert {
    public static <T> T showEntryComboBox(String title, String headerText, String contentText, ObservableList<T> items, StringConverter<T> converter) {
        ComboBox<T> comboBox = new ComboBox<>();
        comboBox.setItems(items);
        comboBox.setConverter(converter);


        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        comboBox.setStyle("-fx-background-color: #fff; " +
                "-fx-border-color: #155E98; " +
                "-fx-border-radius: 30px; " +
                "-fx-background-radius: 30px; " +
                "-fx-padding: 5px; " +
                "-fx-pref-height: 40px; " +
                "-fx-pref-width: 300px;");

        comboBox.requestFocus();
        comboBox.setEditable(true);
        ComboBoxFilterUtil.setupComboBoxFilter(comboBox, items);

        alert.getDialogPane().setContent(comboBox);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            return comboBox.getSelectionModel().getSelectedItem();
        } else {
            return null;
        }
    }

    public static String showEntryAlert(String title, String headerText, String contentText) {
        TextInputDialog entryAlert = new TextInputDialog();
        entryAlert.setTitle(title);
        entryAlert.setHeaderText(headerText);
        entryAlert.setContentText(contentText);

        Optional<String> result = entryAlert.showAndWait();

        if (result.isPresent()) {
            // Return the entered text if it's present
            return result.get();
        } else {
            // Return an empty string or handle the absence of input as needed
            return ""; // or return null or any other default value
        }
    }

    private static void displayInformationAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
