package com.vertex.vos.Utilities;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.GridPane;
import javafx.scene.control.DatePicker;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

public class DateTimeDialog extends Dialog<LocalDate> {
    private final DatePicker datePicker;

    public DateTimeDialog(String title, String headerText, String contentText, LocalDate initialDate) {
        setTitle(title);
        setHeaderText(headerText);
        setContentText(contentText);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        datePicker = new DatePicker(initialDate);
        grid.add(datePicker, 0, 0);

        getDialogPane().setContent(grid);

        getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/vertex/vos/assets/style.css")).toExternalForm());

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return datePicker.getValue();
            }
            return null;
        });
    }
}