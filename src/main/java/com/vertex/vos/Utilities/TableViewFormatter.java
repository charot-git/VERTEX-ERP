package com.vertex.vos.Utilities;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TableViewFormatter {

    public static <S> void formatTableView(TableView<S> tableView) {
        for (TableColumn<S, ?> column : tableView.getColumns()) {
            applyColumnAlignment(column); // Set alignment at column level

            if (column.isEditable()) {
                setEditableColumnFactory(column);
            } else {
                setNonEditableColumnFactory(column);
            }
        }
    }


    private static <S, T> void setEditableColumnFactory(TableColumn<S, T> column) {
        column.setCellFactory(col -> new TableCell<>() {
            private final TextField textField = new TextField();

            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(formatItem(item));
                    alignContent(this, item); // Apply alignment
                }
            }

            @Override
            public void startEdit() {
                if (!isEmpty()) {
                    super.startEdit();
                    textField.setText(getItem().toString());
                    setGraphic(textField);
                    setText(null);
                    textField.requestFocus();
                }
            }

            @Override
            public void commitEdit(T newValue) {
                super.commitEdit(newValue);
                setText(formatItem(newValue));
                setGraphic(null);
                alignContent(this, newValue); // Ensure alignment after edit
            }
        });
    }


    private static <S, T> void setNonEditableColumnFactory(TableColumn<S, T> column) {
        column.setCellFactory(cell -> new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(formatItem(item));
                    alignContent(this, item);
                }
            }
        });
    }

    private static void alignContent(TableCell<?, ?> cell, Object item) {
        if (item instanceof String || item instanceof Integer) {
            cell.setStyle("-fx-alignment: CENTER;");
        } else if (item instanceof Double || item instanceof BigDecimal) {
            cell.setStyle("-fx-alignment: CENTER-RIGHT;");
        } else {
            cell.setStyle("-fx-alignment: CENTER-LEFT;");
        }
    }

    private static <S, T> void applyColumnAlignment(TableColumn<S, T> column) {
        Object sampleData = getSampleData(column);

        String alignmentStyle = "-fx-alignment: CENTER-LEFT; -fx-text-alignment: LEFT;"; // Default style

        if (sampleData instanceof Integer) {
            alignmentStyle = "-fx-alignment: CENTER; -fx-text-alignment: CENTER;";
        } else if (sampleData instanceof Double || sampleData instanceof BigDecimal) {
            alignmentStyle = "-fx-alignment: CENTER-RIGHT; -fx-text-alignment: RIGHT;";
        } else if (sampleData instanceof String) {
            alignmentStyle = "-fx-alignment: CENTER; -fx-text-alignment: CENTER;"; // Center text for String
        }

        column.setStyle(alignmentStyle); // Apply to column cells

        // Apply alignment to header
        column.setStyle("-fx-alignment: CENTER; -fx-text-alignment: CENTER;");
    }


    private static <S, T> Object getSampleData(TableColumn<S, T> column) {
        TableView<S> tableView = column.getTableView();
        if (tableView != null && !tableView.getItems().isEmpty()) {
            for (S row : tableView.getItems()) {
                T value = column.getCellData(row);
                if (value != null) {
                    return value;
                }
            }
        }
        return null; // No sample found
    }


    private static String formatItem(Object item) {
        if (item == null) {
            return "";
        }
        if (item instanceof Double || item instanceof BigDecimal) {
            return String.format("%,.2f", item);
        } else if (item instanceof Integer) {
            return String.format("%,d", item);
        } else if (item instanceof LocalDate) {
            return ((LocalDate) item).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } else if (item instanceof LocalDateTime) {
            return ((LocalDateTime) item).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } else if (item instanceof Timestamp) {
            return ((Timestamp) item).toLocalDateTime().format(DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a"));
        } else if (item instanceof Date) {
            return ((Date) item).toLocalDate().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
        } else if (item instanceof Text) {
            return ((Text) item).getText();
        } else {
            return item.toString();
        }
    }
}
