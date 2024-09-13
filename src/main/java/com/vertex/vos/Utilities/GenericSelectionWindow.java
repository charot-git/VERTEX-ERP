package com.vertex.vos.Utilities;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GenericSelectionWindow<T> {

    private T selectedItem;
    String tableCss = Objects.requireNonNull(getClass().getResource("/com/vertex/vos/assets/table.css")).toExternalForm();
    String styleCss = Objects.requireNonNull(getClass().getResource("/com/vertex/vos/assets/style.css")).toExternalForm();
    String globalCss = Objects.requireNonNull(getClass().getResource("/com/vertex/vos/assets/global.css")).toExternalForm();

    public T showSelectionWindow(Stage parentStage, String title, ObservableList<T> items) {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(parentStage);
        stage.setTitle(title);
        stage.setMaximized(true);

        // Create a FilteredList from the original list
        FilteredList<T> filteredItems = new FilteredList<>(items, p -> true);

        // Create a TextField for filtering
        TextField filterField = new TextField();
        filterField.setPromptText("Filter...");
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredItems.setPredicate(item -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true; // If filter text is empty, display all items
                }

                String lowerCaseFilter = newValue.toLowerCase();

                // You can modify this logic to customize what fields to search in.
                for (Field field : item.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    try {
                        Object value = field.get(item);
                        if (value != null && value.toString().toLowerCase().contains(lowerCaseFilter)) {
                            return true; // Match found
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                return false; // No match found
            });
        });

        TableView<T> tableView = new TableView<>();
        tableView.getColumns().addAll(createColumns(items.get(0)));
        tableView.setItems(filteredItems);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // OK and Cancel buttons
        Button okButton = new Button("OK");
        Button cancelButton = new Button("Cancel");

        okButton.setOnAction(e -> {
            selectedItem = tableView.getSelectionModel().getSelectedItem();
            stage.close();
        });

        cancelButton.setOnAction(e -> {
            selectedItem = null;
            stage.close();
        });

        HBox buttonBox = new HBox(10, okButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setTop(filterField);
        root.setCenter(tableView);
        root.setBottom(buttonBox);
        root.getStylesheets().add(styleCss);
        root.getStylesheets().add(globalCss);
        root.getStylesheets().add(tableCss);

        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.showAndWait();

        return selectedItem;
    }

    private TableColumn<T, ?>[] createColumns(T item) {
        Field[] fields = item.getClass().getDeclaredFields();
        List<TableColumn<T, ?>> columnList = new ArrayList<>();

        for (Field field : fields) {
            field.setAccessible(true);

            // Check for DisplayName annotation
            DisplayName displayNameAnnotation = field.getAnnotation(DisplayName.class);
            if (displayNameAnnotation != null && displayNameAnnotation.exclude()) {
                continue; // Skip fields marked for exclusion
            }

            // Get display name or default to field name
            String columnName = (displayNameAnnotation != null && !displayNameAnnotation.value().isEmpty()) ?
                    displayNameAnnotation.value() :
                    field.getName();

            TableColumn<T, Object> column = new TableColumn<>(columnName);
            column.setCellValueFactory(cellData -> {
                try {
                    return new SimpleObjectProperty<>(field.get(cellData.getValue()));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    return new SimpleObjectProperty<>("");
                }
            });
            columnList.add(column);
        }

        // Convert the list to an array
        @SuppressWarnings("unchecked")
        TableColumn<T, ?>[] columnsArray = new TableColumn[columnList.size()];
        return columnList.toArray(columnsArray);
    }
}
