package com.vertex.vos;

import com.vertex.vos.Enums.ConsolidationStatus;
import com.vertex.vos.Enums.DispatchStatus;
import com.vertex.vos.Enums.SalesOrderStatus;
import com.vertex.vos.Objects.Consolidation;
import com.vertex.vos.Objects.User;
import com.vertex.vos.Objects.UserSession;
import com.vertex.vos.Utilities.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ConsolidationCheckListController implements Initializable {

    @FXML
    private TableView<ConsolidationDetails> checkListProducts;
    @FXML
    private TableColumn<ConsolidationDetails, String> productBrand, productCategory, productSupplier, productName, productUnit;
    @FXML
    private TableColumn<ConsolidationDetails, Integer> orderedQuantity, servedQuantity;
    @FXML
    private TextField warehouseField, checkerField, barcodeField;
    @FXML
    private ListView<String> warehouseManBrands;
    @FXML
    private Button printButton, confirmButton;
    @FXML
    private DatePicker createdDate;
    @FXML
    private Label docno, status;
    @FXML
    private BorderPane borderPane;
    @FXML
    private Button scanBarcodeButton;
    @Getter
    @Setter
    private Consolidation consolidation;
    @Setter
    private ConsolidationListController consolidationListController;
    private static final int CHARACTER_INTERVAL_THRESHOLD = 150; // Max interval between characters for barcodes
    private static final int BARCODE_TIMEOUT = 300;
    private Timer barcodeTimer;
    private long lastCharacterTime = 0;
    private final StringBuilder barcode = new StringBuilder();

    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final WarehouseBrandLinkDAO warehouseBrandLinkDAO = new WarehouseBrandLinkDAO();
    private final ObservableList<User> warehouseManList = FXCollections.observableArrayList(
            employeeDAO.getAllEmployeesWhereDepartment(5)
    );

    private User selectedWarehouseman;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupAutoCompleteForWarehouseField();
        setupWarehouseFieldListener();
        setupRowStylingForTable();
        setupEventHandlers();
    }

    private void setupTableColumns() {
        productSupplier.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getSupplierName()));
        productName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductName()));
        productBrand.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductBrandString()));
        productCategory.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductCategoryString()));
        productUnit.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getUnitOfMeasurementString()));
        orderedQuantity.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getOrderedQuantity()).asObject());
        servedQuantity.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getPickedQuantity()).asObject());

        servedQuantity.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getPickedQuantity()).asObject());
        checkListProducts.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                ConsolidationDetails selectedItem = checkListProducts.getSelectionModel().getSelectedItem();
                if (selectedItem.getPickedQuantity() > 0 || UserSession.getInstance().getUserPosition().equals("System Developer")) {
                    showEditPopup(selectedItem);
                }
            }
        });
    }

    private void showEditPopup(ConsolidationDetails item) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(item.getPickedQuantity()));
        dialog.setTitle("Edit Served Quantity");
        dialog.setHeaderText("Modify the Served Quantity for " + item.getProduct().getProductName());
        dialog.setContentText("Enter new quantity:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(input -> {
            try {
                int newQuantity = Integer.parseInt(input);
                item.setPickedQuantity(newQuantity);
                item.setPickedAt(Timestamp.valueOf(LocalDateTime.now()));
                checkListProducts.refresh();
            } catch (NumberFormatException e) {
                DialogUtils.showErrorMessage("Invalid Input", "Please enter a valid integer.");
            }
        });
    }

    private void setupAutoCompleteForWarehouseField() {
        TextFields.bindAutoCompletion(warehouseField, warehouseManList.stream()
                .map(user -> user.getUser_fname() + " " + user.getUser_lname())
                .toArray(String[]::new));
    }

    private void setupWarehouseFieldListener() {
        warehouseField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isBlank()) {
                warehouseManBrands.getItems().clear();
                return;
            }
            selectedWarehouseman = warehouseManList.stream()
                    .filter(user -> (user.getUser_fname() + " " + user.getUser_lname()).equals(newValue))
                    .findFirst().orElse(null);

            if (selectedWarehouseman != null) {
                warehouseManBrands.getItems().setAll(warehouseBrandLinkDAO.getBrandNames(selectedWarehouseman));
            } else {
                warehouseManBrands.getItems().clear();
            }
        });
    }

    private void setupRowStylingForTable() {
        checkListProducts.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(ConsolidationDetails item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    setStyle(item.getProduct().getBarcode() == null || item.getProduct().getBarcode().trim().isEmpty() ? "-fx-opacity: 0.5;" : "");
                }
            }
        });
    }

    private void setupEventHandlers() {
        printButton.setOnAction(event -> printProductsForWarehousemen());
        barcodeField.addEventFilter(KeyEvent.KEY_PRESSED, this::handleBarcodeInput);
        scanBarcodeButton.setOnAction(event -> openScanner());
    }

    public void openScanner() {
        scanBarcodeButton.setDisable(true); // Disable the button
        JavaFXBarcodeScanner.startBarcodeScanner(new Stage())
                .thenAccept(barcode -> Platform.runLater(() -> {
                    barcodeField.setText(barcode);
                    processBarcode(barcodeField.getText());
                    scanBarcodeButton.setDisable(false); // Enable the button
                }))
                .exceptionally(e -> {
                    e.printStackTrace(); // ✅ Print the error stack trace
                    Platform.runLater(() -> {
                        DialogUtils.showErrorMessage("Camera Error", e.getMessage());
                        scanBarcodeButton.setDisable(false); // Enable the button
                    });
                    return null;
                });
    }


    private void printProductsForWarehousemen() {
        if (selectedWarehouseman == null) {
            DialogUtils.showErrorMessage("Error", "Please select a warehouseman");
            return;
        }

        if (warehouseManBrands.getItems().isEmpty()) {
            DialogUtils.showErrorMessage("Error", "No brands linked to this warehouseman");
            return;
        }

        ObservableList<ConsolidationDetails> productsForWarehouseman = checkListProducts.getItems().stream()
                .filter(item -> warehouseManBrands.getItems().contains(item.getProduct().getProductBrandString()))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        Platform.runLater(() -> PicklistPrintables.exportChecklistToPDF(consolidation, productsForWarehouseman,
                consolidation.getCheckedBy(), selectedWarehouseman));
    }

    private void handleBarcodeInput(KeyEvent event) {
        if (event.getText().isEmpty()) return;

        long currentTime = System.currentTimeMillis();

        if (currentTime - lastCharacterTime > CHARACTER_INTERVAL_THRESHOLD) {
            barcode.setLength(0);
        }
        lastCharacterTime = currentTime;

        barcode.append(event.getText());

        // Handle ENTER key event
        if (event.getCode() == KeyCode.ENTER) {
            processBarcode(barcodeField.getText());
            barcode.setLength(0);
            barcodeField.clear();
            return;
        }

        // Cancel previous timer and schedule a new one
        if (barcodeTimer != null) {
            barcodeTimer.cancel();
        }

        barcodeTimer = new Timer();
        barcodeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    processBarcode(barcodeField.getText());
                    barcode.setLength(0);
                    barcodeField.clear();
                });
            }
        }, BARCODE_TIMEOUT);
    }


    private void processBarcode(String barcode) {
        checkListProducts.getItems().stream()
                .filter(item -> barcode.equals(item.getProduct().getBarcode()))
                .findFirst()
                .ifPresentOrElse(item -> {
                    checkListProducts.getSelectionModel().select(item);
                    checkListProducts.scrollTo(item);
                    if (consolidation.getStatus().equals(ConsolidationStatus.PICKING)) {
                        item.setPickedQuantity(item.getPickedQuantity() + 1);
                    }
                    checkListProducts.refresh();
                }, () -> DialogUtils.showErrorMessage("Error", "No barcode associated to this consolidation"));

        barcodeField.setText("");
    }

    BorderPane pickListPrinting;

    public void updateFields(ObservableList<ConsolidationDetails> productsForChecklist) {
        docno.setText(consolidation.getConsolidationNo());
        status.setText(consolidation.getStatus().toString());
        createdDate.setValue(consolidation.getCreatedAt().toLocalDateTime().toLocalDate());
        checkerField.setText(consolidation.getCheckedBy().getUser_fname() + " " + consolidation.getCheckedBy().getUser_lname());
        checkListProducts.getItems().setAll(productsForChecklist);

        pickListPrinting = (BorderPane) borderPane.getRight();

        if (consolidation.getStatus().equals(ConsolidationStatus.PICKING)) {
            borderPane.setRight(pickListPrinting);
            confirmButton.setText("Update Picking");
            confirmButton.setOnAction(actionEvent -> handlePicked());
        } else if (consolidation.getStatus().equals(ConsolidationStatus.PENDING)) {
            borderPane.setRight(null);
            confirmButton.setOnAction(event -> handlePicking());
        } else if (consolidation.getStatus().equals(ConsolidationStatus.PICKED)) {
            borderPane.setDisable(true);
        }

        checkListProducts.getSortOrder().addAll(productSupplier, productBrand, productCategory);

    }

    private void handlePicking() {
        consolidation.setStatus(ConsolidationStatus.PICKING);
        consolidation.getDispatchPlans().forEach(dispatchPlan -> dispatchPlan.setStatus(DispatchStatus.PICKING));
        consolidation.getStockTransfers().forEach(stockTransfer -> stockTransfer.setStatus("PICKING"));

        consolidation.getDispatchPlans().stream()
                .flatMap(dispatchPlan -> dispatchPlan.getSalesOrders().stream())
                .forEach(salesOrder -> salesOrder.setOrderStatus(SalesOrderStatus.FOR_PICKING));

        if (consolidationListController.startPicking(consolidation)) {
            status.setText(consolidation.getStatus().toString());
        }
    }

    private void handlePicked() {
        boolean allProductsFulfilled = checkListProducts.getItems().stream()
                .allMatch(item -> item.getOrderedQuantity() <= item.getPickedQuantity());

        for (ConsolidationDetails consolidationDetails : checkListProducts.getItems()) {
            consolidationDetails.setConsolidation(consolidation);
        }

        if (allProductsFulfilled) {
            consolidation.setStatus(ConsolidationStatus.PICKED);
            consolidation.getDispatchPlans().forEach(dispatchPlan -> dispatchPlan.setStatus(DispatchStatus.PICKED));
            consolidation.getStockTransfers().forEach(stockTransfer -> stockTransfer.setStatus("PICKED"));
            consolidation.getDispatchPlans().stream()
                    .flatMap(dispatchPlan -> dispatchPlan.getSalesOrders().stream())
                    .forEach(salesOrder -> salesOrder.setOrderStatus(SalesOrderStatus.FOR_INVOICING));
        } else {
            consolidation.setStatus(ConsolidationStatus.PICKING);
            consolidation.getDispatchPlans().forEach(dispatchPlan -> dispatchPlan.setStatus(DispatchStatus.PICKING));
            consolidation.getStockTransfers().forEach(stockTransfer -> stockTransfer.setStatus("PICKING"));
            consolidation.getDispatchPlans().stream()
                    .flatMap(dispatchPlan -> dispatchPlan.getSalesOrders().stream())
                    .forEach(salesOrder -> salesOrder.setOrderStatus(SalesOrderStatus.FOR_PICKING));
        }

        status.setText(consolidation.getStatus().toString());
        checkListProducts.refresh();


        if (consolidationListController.pickConsolidation(consolidation)) {
        }
    }

}
