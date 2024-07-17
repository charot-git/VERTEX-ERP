package com.vertex.vos.Utilities;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.Timestamp;

public class TableViewFormatter {

    public static <S> void formatTableView(TableView<S> tableView) {
        for (TableColumn<S, ?> column : tableView.getColumns()) {
            if (column.getCellFactory() == null) {
                continue; // Skip columns without a cell factory
            }

            if (column.isEditable()) {
                setEditableColumnFactory(column);
            } else {
                setNonEditableColumnFactory(column);
            }
        }
    }

    private static <S, T> void setEditableColumnFactory(TableColumn<S, T> column) {
        column.setCellFactory(cell -> {
            TableCell<S, T> tableCell = new TableCell<>() {
                private final TextField textField = new TextField();

                @Override
                protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(formatItem(item));
                    }
                }
            };

            tableCell.setOnMouseClicked(event -> {
                if (!tableCell.isEmpty() && event.getClickCount() == 2) {
                    tableCell.startEdit();
                }
            });

            tableCell.setOnKeyReleased(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    tableCell.commitEdit(tableCell.getItem());
                }
            });

            return tableCell;
        });
    }

    private static <S, T> void setNonEditableColumnFactory(TableColumn<S, T> column) {
        column.setCellFactory(cell -> new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatItem(item));
                }
            }
        });
    }

    private static String formatItem(Object item) {
        if (item == null) {
            return "";
        }
        if (item instanceof Double) {
            return String.format("%,.2f", item);
        } else if (item instanceof Integer) {
            return String.format("%,d", item);
        } else if (item instanceof Number) {
            return String.format("%.2f%%", ((Number) item).doubleValue());
        } else if (item instanceof LocalDate) {
            return ((LocalDate) item).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } else if (item instanceof LocalDateTime) {
            return ((LocalDateTime) item).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } else if (item instanceof Timestamp) {
            return ((Timestamp) item).toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } else {
            return item.toString();
        }
    }
}
