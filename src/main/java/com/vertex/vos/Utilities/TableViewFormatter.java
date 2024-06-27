package com.vertex.vos.Utilities;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

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
        return switch (item) {
            case Double v -> String.format("%,.2f", item);
            case Integer i -> String.format("%,d", item);
            case Number number -> String.format("%.2f%%", number.doubleValue());
            case null, default -> {
                assert item != null;
                yield item.toString();
            }
        };
    }
}
