package com.vertex.vos;

import com.vertex.vos.Objects.ProductsInTransact;
import com.vertex.vos.Utilities.*;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class PickListDetailsController {

    public Button exportButton;
    @FXML
    private TableColumn<ProductsInTransact, Boolean> checkedCol;

    @FXML
    private TableColumn<ProductsInTransact, String> descriptionCol;

    @FXML
    private Label employeeName;

    @FXML
    private TableView<ProductsInTransact> pickList;

    @FXML
    private TableColumn<ProductsInTransact, String> quantityCol;

    @FXML
    private Label soNumber;

    @FXML
    private TableColumn<ProductsInTransact, String> unitCol;

    EmployeeDAO employeeDAO = new EmployeeDAO();
    SalesOrderDAO salesOrderDAO = new SalesOrderDAO();
    ProductDAO productDAO = new ProductDAO();
    WarehouseBrandLinkDAO brandLinkDAO = new WarehouseBrandLinkDAO();



    @FXML
    public void initialize() {
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("orderedQuantity"));
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));

        exportButton.setOnMouseClicked(mouseEvent -> openExportDialog());
    }

    private void openExportDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(new Stage());

        if (file != null) {
            try {
                ExcelExporter.exportToExcel(pickList, file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
