package com.vertex.vos.Utilities;

import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.*;
import java.util.stream.Collectors;

public class TableViewCopyUtil<T> {

    public TableViewCopyUtil(TableView<T> tableView) {
        tableView.setOnKeyPressed(event -> handleCopy(event, tableView));
    }

    private void handleCopy(KeyEvent event, TableView<T> tableView) {
        if (event.isControlDown() && event.getCode() == KeyCode.C) {
            copySelectedRowsToClipboard(tableView);
        }
    }

    private void copySelectedRowsToClipboard(TableView<T> tableView) {
        Set<Integer> selectedRows = tableView.getSelectionModel().getSelectedCells()
                .stream()
                .map(TablePosition::getRow)
                .collect(Collectors.toSet()); // Get unique row indices

        if (selectedRows.isEmpty()) return;

        List<TableColumn<T, ?>> flatColumns = getFlatColumns(tableView.getColumns());

        String copiedData = selectedRows.stream()
                .sorted()
                .map(row -> copyRowData(flatColumns, row))
                .collect(Collectors.joining("\n"));

        ClipboardContent content = new ClipboardContent();
        content.putString(copiedData);
        Clipboard.getSystemClipboard().setContent(content);
    }

    private String copyRowData(List<TableColumn<T, ?>> columns, int rowIndex) {
        return columns.stream()
                .map(column -> {
                    Object cellData = column.getCellData(rowIndex);
                    return (cellData == null) ? "" : cellData.toString();
                })
                .collect(Collectors.joining("\t")); // Tab-separated for Excel compatibility
    }

    private List<TableColumn<T, ?>> getFlatColumns(List<TableColumn<T, ?>> columns) {
        List<TableColumn<T, ?>> flatColumns = new ArrayList<>();
        for (TableColumn<T, ?> column : columns) {
            if (column.getColumns().isEmpty()) {
                flatColumns.add(column);
            } else {
                flatColumns.addAll(getFlatColumns(column.getColumns())); // Recursive flattening
            }
        }
        return flatColumns;
    }
}
