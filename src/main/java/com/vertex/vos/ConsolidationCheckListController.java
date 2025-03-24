package com.vertex.vos;

import com.vertex.vos.Enums.ConsolidationStatus;
import com.vertex.vos.Enums.DispatchStatus;
import com.vertex.vos.Enums.SalesOrderStatus;
import com.vertex.vos.Objects.Consolidation;
import com.vertex.vos.Objects.DispatchPlan;
import com.vertex.vos.Objects.SalesOrder;
import com.vertex.vos.Objects.User;
import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.EmployeeDAO;
import com.vertex.vos.Utilities.PicklistPrintables;
import com.vertex.vos.Utilities.WarehouseBrandLinkDAO;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import lombok.Getter;
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Timer;

public class ConsolidationCheckListController implements Initializable {

    public TableColumn<ChecklistDTO, String> productBrand;
    public TableColumn<ChecklistDTO, String> productCategory;
    public TableColumn<ChecklistDTO, String> productSupplier;
    public BorderPane borderPane;
    public TextField warehouseField;
    public ListView<String> warehouseManBrands;
    public Button printButton;
    public TextField barcodeField;

    @FXML
    private TableView<ChecklistDTO> checkListProducts;

    @FXML
    private TextField checkerField;

    @FXML
    private Button confirmButton;

    @FXML
    private DatePicker createdDate;

    @FXML
    private Label docno;

    @FXML
    private TableColumn<ChecklistDTO, Integer> orderedQuantity;

    @FXML
    private TableColumn<ChecklistDTO, String> productName;

    @FXML
    private TableColumn<ChecklistDTO, String> productUnit;

    @FXML
    private TableColumn<ChecklistDTO, Integer> servedQuantity;

    @FXML
    private Label status;
    @Getter
    @Setter
    private Consolidation consolidation;

    @Setter
    private ConsolidationListController consolidationListController;

    private final StringBuilder barcode = new StringBuilder();
    private Timer barcodeTimer;
    private static final int BARCODE_TIMEOUT = 300; // Time in milliseconds
    private static final int CHARACTER_INTERVAL_THRESHOLD = 150; // Max interval between characters for barcodes
    private long lastCharacterTime = 0;

    EmployeeDAO employeeDAO = new EmployeeDAO();
    ObservableList<User> warehouseManList = FXCollections.observableArrayList(employeeDAO.getAllEmployeesWhereDepartment(5));
    WarehouseBrandLinkDAO warehouseBrandLinkDAO = new WarehouseBrandLinkDAO();

    User selectedWarehouseman;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        barcodeField.addEventFilter(KeyEvent.KEY_PRESSED, this::handleBarcodeInput);
        productSupplier.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getSupplierName()));
        productName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductName()));
        productBrand.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductBrandString()));
        productCategory.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductCategoryString()));
        productUnit.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getUnitOfMeasurementString()));
        orderedQuantity.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getOrderedQuantity()).asObject());
        servedQuantity.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getServedQuantity()).asObject());

        TextFields.bindAutoCompletion(warehouseField, warehouseManList.stream().map(user -> user.getUser_fname() + " " + user.getUser_lname()).toArray());

        warehouseField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                warehouseManBrands.getItems().clear();
                return;
            }
            Platform.runLater(() -> {
                selectedWarehouseman = warehouseManList.stream()
                        .filter(user -> (user.getUser_fname() + " " + user.getUser_lname()).equals(newValue))
                        .findFirst().orElse(null);


                if (selectedWarehouseman != null) {
                    warehouseManBrands.getItems().setAll(warehouseBrandLinkDAO.getBrandNames(selectedWarehouseman));
                }
            });
        });

        confirmButton.setOnAction(event -> handleConfirmButton());

        printButton.setOnAction(actionEvent -> printProductsForWarehousemen());
    }

    private void printProductsForWarehousemen() {
        if (Objects.isNull(selectedWarehouseman)) {
            DialogUtils.showErrorMessage("Error", "Please select a warehouseman");
            return;
        }

        if (warehouseManBrands.getItems().isEmpty()) {
            DialogUtils.showErrorMessage("Error", "No brands linked to this warehouseman");
            return;
        }

        ObservableList<ChecklistDTO> productsForChecklist = FXCollections.observableArrayList(checkListProducts.getItems());
        ObservableList<ChecklistDTO> productsForWarehouseman = FXCollections.observableArrayList();
        warehouseManBrands.getItems().forEach(brandName -> {
            productsForChecklist.forEach(checklistDTO -> {
                if (checklistDTO.getProduct().getProductBrandString().equals(brandName)) {
                    productsForWarehouseman.add(checklistDTO);
                }
            });
        });

        Platform.runLater(()-> {
            PicklistPrintables.exportChecklistToWord(consolidation, productsForWarehouseman, consolidation.getCheckedBy(), selectedWarehouseman);
        });
    }

    private void handleConfirmButton() {
        consolidation.setStatus(ConsolidationStatus.PICKING);
        consolidation.getDispatchPlans().forEach(dispatchPlan -> dispatchPlan.setStatus(DispatchStatus.PICKING));
        consolidation.getStockTransfers().forEach(stockTransfer -> stockTransfer.setStatus("PICKING"));

        for (DispatchPlan dispatchPlan : consolidation.getDispatchPlans()) {
            for (SalesOrder salesOrder : dispatchPlan.getSalesOrders()) {
                salesOrder.setOrderStatus(SalesOrderStatus.FOR_PICKING);
            }
        }

        if (consolidationListController.startPicking(consolidation)) {
            confirmButton.setDisable(true);
            status.setText(consolidation.getStatus().toString());
        }
    }

    private void handleBarcodeInput(KeyEvent event) {
        if (event.getText().isEmpty()) return;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCharacterTime > CHARACTER_INTERVAL_THRESHOLD) {
            barcode.setLength(0); // Clear buffer if input is slow (user typing)
        }
        lastCharacterTime = currentTime;
        barcode.append(event.getText());

        if (barcodeTimer != null) {
            barcodeTimer.cancel();
        }

        barcodeTimer = new Timer();
        barcodeTimer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    processBarcode(barcode.toString());
                    barcode.setLength(0);
                });
            }
        }, BARCODE_TIMEOUT);

    }

    private void processBarcode(String barcode) {
        boolean barcodeExists = checkListProducts.getItems().stream()
                .anyMatch(item -> item.getProduct().getBarcode().equals(barcode));

        if (barcodeExists) {
            checkListProducts.getItems().forEach(item -> {
                if (item.getProduct().getBarcode().equals(barcode)) {
                    checkListProducts.getSelectionModel().select(item);
                    checkListProducts.scrollTo(item);
                }
            });
        } else {
            DialogUtils.showErrorMessage("Error", "No barcode associated to this consolidation");
        }
    }

    public void updateFields(ObservableList<ChecklistDTO> productsForChecklist) {
        docno.setText(consolidation.getConsolidationNo());
        status.setText(consolidation.getStatus().toString());
        createdDate.setValue(consolidation.getCreatedAt().toLocalDateTime().toLocalDate());
        checkerField.setText(consolidation.getCheckedBy().getUser_fname() + " " + consolidation.getCheckedBy().getUser_lname());
        checkListProducts.getItems().setAll(productsForChecklist);

        if (consolidation.getStatus().equals(ConsolidationStatus.PICKING)) {
            confirmButton.setDisable(true);
        }

        checkListProducts.getSortOrder().addAll(productSupplier, productBrand, productCategory);

    }
}
