package com.vertex.vos;

import com.vertex.vos.DAO.StockTransferProductSelectionDAO;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.vertex.vos.Utilities.LoadingScreenUtils.hideLoadingScreen;
import static com.vertex.vos.Utilities.LoadingScreenUtils.showLoadingScreen;

public class StockTransferController implements Initializable {
    @FXML
    public Label stockTransferID;
    public VBox leadDateBox;
    public DatePicker leadDate;
    public Label leadDateErr;
    public Button receiveButton;
    public Button addProduct;
    public Button removeProduct;
    public TextField orderedQuantity;
    public TextField availableQuantity;
    public TextField productNameTextField;
    public BorderPane parentBorderPane;
    public VBox productPane;
    public ComboBox<String> uomComboBox;
    @Setter
    AnchorPane contentPane;

    @FXML
    private VBox addBoxes;

    @FXML
    private VBox addProductButton;

    @FXML
    private Label addProductLabel;

    @FXML
    private HBox confirmBox;

    @FXML
    private Button confirmButton;

    @FXML
    private Label date;

    @FXML
    private Label grandTotal;

    @FXML
    private ComboBox<Branch> sourceBranch;

    @FXML
    private VBox sourceBranchBox;

    @FXML
    private Label sourceBranchErr;

    @FXML
    private HBox statusBox;

    @FXML
    private ImageView statusImage;

    @FXML
    private Label statusLabel;

    @FXML
    private ComboBox<Branch> targetBranch;

    @FXML
    private VBox targetBranchBox;

    @FXML
    private Label targetBranchErr;

    @FXML
    private HBox totalBox;

    @FXML
    private VBox totalBoxLabels;

    @FXML
    private VBox totalVBox;

    @FXML
    private TableView<ProductsInTransact> transferTable;
    int stockTransferNo;

    InventoryDAO inventoryDAO = new InventoryDAO();
    BranchDAO branchDAO = new BranchDAO();
    StockTransferDAO stockTransferDAO = new StockTransferDAO();

    public void createNewGoodStockTransfer() {
        stockTransferNo = stockTransferDAO.generateStockTransferNumber();
        ObservableList<Branch> branches = branchDAO.getBranches();
        ObservableList<String> branchWithInventory = inventoryDAO.getBranchNamesWithInventory();

        sourceBranch.setItems(FXCollections.observableArrayList(
                branches.stream().filter(branch -> branchWithInventory.contains(branch.getBranchName()) && !branch.isReturn()).collect(Collectors.toList())
        ));
        targetBranchBox.setDisable(true);
        addProductButton.setDisable(true);
        stockTransferID.setText("ST-" + stockTransferNo);
        statusLabel.setText("ENTRY REQUEST");

        // Add a listener to the sourceBranch ComboBox
        sourceBranch.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Branch>() {
            @Override
            public void changed(ObservableValue<? extends Branch> observableValue, Branch oldValue, Branch newValue) {
                targetBranchBox.setDisable(newValue == null);
                if (newValue != null) {
                    addProductButton.setDisable(false);
                    addProductButton.setOnMouseClicked(mouseEvent -> isTouchScreen(newValue));
                }

                ObservableList<Branch> targetBranches = FXCollections.observableArrayList(branches.stream().filter(branch -> !branch.getBranchName().equals(newValue.getBranchName())).collect(Collectors.toList()));
                targetBranch.setItems(targetBranches);
                ComboBoxFilterUtil.setupComboBoxFilter(targetBranch, targetBranches);
            }
        });

        initializeTable();

        //transferTable change listener
        transferTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Platform.runLater(() -> {
                    grandTotal.setText("Total Amount: " + calculateTotalAmount());
                });
            }
        });

        confirmButton.setOnMouseClicked(mouseEvent -> {
            try {
                initializeStockTransfer();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void isTouchScreen(Branch branch) {
        if (Platform.isSupported(ConditionalFeature.INPUT_TOUCH) && Platform.isSupported(ConditionalFeature.INPUT_METHOD)) {
            openProductStage(branch.getId(), branch.getBranchName());
        } else {
            openProductPane(branch.getId(), branch.getBranchName());
        }
    }

    private void openProductPane(int sourceBranchId, String newValue) {
        if (sourceBranchId == -1) {
            DialogUtils.showErrorMessage("Error", "Branch is invalid, please contact your system administrator");
        } else {
            if (parentBorderPane.getLeft() == null) {
                parentBorderPane.setLeft(productPane);
                addProductLabel.setText("Hide Product Selection");
                setUpProductSelection(sourceBranchId);
            } else {
                parentBorderPane.setLeft(null);
                addProductLabel.setText("Add Product to " + newValue);
            }
        }
    }

    private void setUpProductSelection(int sourceBranchId) {
        addProduct.setDefaultButton(true);
        StockTransferProductSelectionDAO stockTransferProductSelectionDAO = new StockTransferProductSelectionDAO();

        List<String> productNamesWithInventory = stockTransferProductSelectionDAO.getProductNamesWithInventory(sourceBranchId);
        TextFields.bindAutoCompletion(productNameTextField, productNamesWithInventory);

        // Handle product name selection to load UOMs
        productNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                List<String> unitsOfSelectedProductName = stockTransferProductSelectionDAO.getProductUnitsWithInventory(sourceBranchId, newValue);
                uomComboBox.setItems(FXCollections.observableArrayList(unitsOfSelectedProductName));
            }
        });

        // Handle UOM selection to fetch inventory details
        uomComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty() && productNameTextField.getText() != null && !productNameTextField.getText().isEmpty()) {
                ProductsInTransact selectedProduct = stockTransferProductSelectionDAO.getProductWithInventory(sourceBranchId, productNameTextField.getText(), newValue);
                if (selectedProduct != null) {
                    availableQuantity.setText(String.valueOf(selectedProduct.getAvailableQuantity()));

                    // Modify addProduct button action to check for duplicates
                    addProduct.setOnAction(event -> {
                        boolean productExists = transferTable.getItems().stream()
                                .anyMatch(p -> p.getDescription().equals(selectedProduct.getDescription()) &&
                                        p.getUnit().equals(selectedProduct.getUnit()));

                        if (productExists) {
                            DialogUtils.showErrorMessage("Duplicate Entry", "This product is already in the table.");
                        } else {
                            selectedProduct.setOrderedQuantity(Integer.parseInt(orderedQuantity.getText()));
                            productsList.add(selectedProduct);
                            transferTable.refresh();
                            transferTable.getSelectionModel().clearSelection();
                            clearProductFields();
                            productNameTextField.requestFocus();
                        }
                    });
                }
            }
        });
    }


    // Declare listener as a class-level variable
    private ChangeListener<String> orderedQuantityListener;


    // Helper method to populate product fields
    private void populateProductFields(ProductsInTransact product) {
        productNameTextField.setText(product.getDescription());
        uomComboBox.setValue(product.getUnit());
        availableQuantity.setText(String.valueOf(product.getAvailableQuantity()));
        orderedQuantity.setText(String.valueOf(product.getOrderedQuantity()));
    }

    // Helper method to clear product fields
    private void clearProductFields() {
        productNameTextField.clear();
        uomComboBox.getItems().clear();
        availableQuantity.clear();
        orderedQuantity.clear();
    }

    // Helper method to remove the selected product
    private void removeSelectedProduct(ProductsInTransact product) {
        removedProducts.add(product);
        transferTable.getItems().remove(product);
        transferTable.refresh();
    }


    List<ProductsInTransact> removedProducts = new ArrayList<>();


    private void initializeStockTransferForUpdate(StockTransfer selectedTransfer) throws SQLException {
        StockTransfer stockTransfer = new StockTransfer();
        stockTransfer.setStockNo(selectedTransfer.getStockNo());
        stockTransfer.setSourceBranch(selectedTransfer.getSourceBranch());
        stockTransfer.setTargetBranch(selectedTransfer.getTargetBranch());
        stockTransfer.setLeadDate(selectedTransfer.getLeadDate());
        stockTransfer.setDateRequested(selectedTransfer.getDateRequested());
        stockTransfer.setStatus("REQUESTED");

        List<StockTransfer> stockTransferBatch = new ArrayList<>();
        boolean atLeastOneNonZeroQuantity = false;

        for (ProductsInTransact product : transferTable.getItems()) {
            if (product.getOrderedQuantity() == 0) {
                continue;
            }

            atLeastOneNonZeroQuantity = true;

            StockTransfer productTransfer = getStockTransfer(product, stockTransfer);
            stockTransferBatch.add(productTransfer);
        }

        // Delete removed products before inserting updates
        if (!removedProducts.isEmpty()) {
            boolean deleteSuccess = stockTransferDAO.deleteStockTransfers(removedProducts, selectedTransfer.getStockNo(), selectedTransfer.getTargetBranch());
            if (!deleteSuccess) {
                DialogUtils.showErrorMessage("Error", "Failed to remove some stock transfer entries.");
                return; // Stop execution if deletion fails
            }
        }

        if (!atLeastOneNonZeroQuantity) {
            DialogUtils.showErrorMessage("Error", "All quantities are zero.");
            return;
        }

        // Perform batch update
        boolean allUpdatesSuccessful = stockTransferDAO.insertStockTransfers(stockTransferBatch);

        if (allUpdatesSuccessful) {
            DialogUtils.showCompletionDialog("Success", "Stock transfer update now pending");
            stockTransferListController.loadStockTransfer();
            closeStage();
        } else {
            DialogUtils.showErrorMessage("Error", "Please contact your system administrator");
        }
    }


    private static StockTransfer getStockTransfer(ProductsInTransact product, StockTransfer stockTransfer) {
        StockTransfer productTransfer = new StockTransfer();
        productTransfer.setStockNo(stockTransfer.getStockNo());
        productTransfer.setSourceBranch(stockTransfer.getSourceBranch());
        productTransfer.setTargetBranch(stockTransfer.getTargetBranch());
        productTransfer.setLeadDate(stockTransfer.getLeadDate());
        productTransfer.setStatus(stockTransfer.getStatus());
        productTransfer.setDateRequested(stockTransfer.getDateRequested());
        productTransfer.setDateReceived(stockTransfer.getDateReceived());
        productTransfer.setEncoderId(stockTransfer.getEncoderId());
        productTransfer.setReceiverId(stockTransfer.getReceiverId());
        productTransfer.setProductId(product.getProductId());
        productTransfer.setOrderedQuantity(product.getOrderedQuantity());
        productTransfer.setReceivedQuantity(product.getReceivedQuantity());
        productTransfer.setAmount(product.getUnitPrice() * product.getReceivedQuantity());
        return productTransfer;
    }


    private void initializeStockTransfer() throws SQLException {
        StockTransfer stockTransfer = new StockTransfer();
        stockTransfer.setStockNo("ST-" + stockTransferNo);
        stockTransfer.setDateRequested(Date.valueOf(LocalDate.now()));
        stockTransfer.setSourceBranch(sourceBranch.getSelectionModel().getSelectedItem().getId());
        stockTransfer.setTargetBranch(targetBranch.getSelectionModel().getSelectedItem().getId());
        stockTransfer.setLeadDate(Date.valueOf(leadDate.getValue()));
        stockTransfer.setStatus("REQUESTED");
        stockTransfer.setEncoderId(UserSession.getInstance().getUserId());

        List<StockTransfer> stockTransferBatch = new ArrayList<>();
        boolean atLeastOneNonZeroQuantity = false;

        for (ProductsInTransact product : productsList) {
            if (product.getOrderedQuantity() == 0) {
                continue;
            }

            atLeastOneNonZeroQuantity = true;

            StockTransfer productTransfer = getStockTransfer(product, stockTransfer);
            stockTransferBatch.add(productTransfer);
        }

        if (!atLeastOneNonZeroQuantity) {
            DialogUtils.showErrorMessage("Error", "All quantities are zero.");
            return;
        }

        // Perform batch insert/update
        boolean allTransfersSuccessful = stockTransferDAO.insertStockTransfers(stockTransferBatch);

        if (allTransfersSuccessful) {
            DialogUtils.showCompletionDialog("Success", "Stock transfer request now pending");
            if (sourceBranch.getSelectionModel().getSelectedItem().isReturn()) {
                stockTransferListController.loadBadStockTransfer();
            } else {
                stockTransferListController.loadStockTransfer();

            }

            ConfirmationAlert confirmationAlert = new ConfirmationAlert(
                    "Create new transfer?", "Do you want to create a new stock transfer?", "Yes or no.", true);
            boolean yes = confirmationAlert.showAndWait();
            if (yes) {
                resetUI();
            } else {
                closeStage();
            }
        } else {
            DialogUtils.showErrorMessage("Error", "Please contact your system administrator");
        }
    }


    private void resetUI() throws SQLException {
        productsList.clear();
        transferTable.getItems().clear();
        stockTransferNo = stockTransferDAO.generateStockTransferNumber();
        stockTransferID.setText("Stock Transfer");
        sourceBranch.getSelectionModel().clearSelection();
        targetBranch.getSelectionModel().clearSelection();
        leadDate.setValue(null);
    }

    private void closeStage() {
        Platform.runLater(() -> {
            Stage stage = (Stage) confirmButton.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        });
    }

    /*private void addProductToTable(String newValue) {
        int sourceBranchId = branchDAO.getBranchIdByName(newValue);

        if (sourceBranchId == -1) {
            DialogUtils.showErrorMessage("Error", "Branch is invalid, please contact your system administrator");
        } else {
            openProductStage(sourceBranchId, newValue);
        }
    }*/

    ErrorUtilities errorUtilities = new ErrorUtilities();
    private Stage productStage = null;
    private final ObservableList<ProductsInTransact> productsList = FXCollections.observableArrayList();

    private void openProductStage(int sourceBranchId, String newValue) {
        if (productStage == null || !productStage.isShowing()) {
            showLoadingScreen(); // Show loading screen

            CompletableFuture.supplyAsync(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("StockTransferProductSelection.fxml"));
                    Parent content = loader.load();

                    StockTransferProductSelectionController controller = loader.getController();
                    controller.loadData(sourceBranchId);
                    controller.setStockTransferController(this);
                    Platform.runLater(() -> stockTransferStage.setOnCloseRequest(event -> {
                        productStage.close();
                    }));
                    return content;
                } catch (IOException e) {
                    throw new RuntimeException("Failed to load product stage", e);
                }
            }).thenAcceptAsync(content -> Platform.runLater(() -> {
                hideLoadingScreen(); // Hide loading screen
                productStage = new Stage();
                productStage.setTitle("Add product for branch " + newValue);
                productStage.setScene(new Scene(content));
                productStage.show();
                productStage.toFront();
            })).exceptionally(e -> {
                Platform.runLater(() -> {
                    hideLoadingScreen(); // Hide loading screen
                    DialogUtils.showErrorMessage("Error", "Failed to load product stage: " + e.getMessage());
                });
                return null;
            });
        } else {
            productStage.toFront();
        }
    }


    private void initializeTable() {
        transferTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        transferTable.setEditable(true);

        TableColumn<ProductsInTransact, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<ProductsInTransact, String> unitColumn = new TableColumn<>("Unit");
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));

        TableColumn<ProductsInTransact, Integer> quantityAvailableColumn = new TableColumn<>("Quantity Available");
        quantityAvailableColumn.setCellValueFactory(new PropertyValueFactory<>("availableQuantity"));

        TableColumn<ProductsInTransact, Integer> orderQuantityColumn = getOrderQuantityColumn(transferTable, grandTotal);

        TableColumn<ProductsInTransact, Double> totalAmountColumn = getTotalAmountColumn();

        grandTotal.setText("Total Amount: " + transferTable.getItems().stream().mapToDouble(ProductsInTransact::getTotalAmount).sum());

        transferTable.getColumns().addAll(descriptionColumn, unitColumn, orderQuantityColumn, quantityAvailableColumn, totalAmountColumn);
    }

    private static TableColumn<ProductsInTransact, Double> getTotalAmountColumn() {
        TableColumn<ProductsInTransact, Double> totalAmountColumn = new TableColumn<>("Total Amount");
        totalAmountColumn.setCellValueFactory(cellData -> {
            ProductsInTransact product = cellData.getValue();
            double orderedQuantity = product.getOrderedQuantity();
            double price = product.getUnitPrice();
            return new SimpleDoubleProperty(orderedQuantity * price).asObject();
        });
        return totalAmountColumn;
    }

    private static TableColumn<ProductsInTransact, Integer> getOrderQuantityColumn(TableView<ProductsInTransact> transferTable, Label grandTotal) {
        TableColumn<ProductsInTransact, Integer> orderQuantityColumn = getProductsInTransactIntegerTableColumn("Order Quantity", transferTable);
        return orderQuantityColumn;
    }


    ProductDAO productDAO = new ProductDAO();

    void addProductToBranchTables(int productId) {
        sourceBranchBox.setDisable(true);
        Product product = productDAO.getProductDetails(productId);
        addProductToList(product);
        transferTable.setItems(productsList);
    }

    private void addProductToList(Product product) {
        boolean productExists = productsList.stream()
                .anyMatch(existingProduct -> existingProduct.getProductId() == product.getProductId());

        Branch branch = sourceBranch.getSelectionModel().getSelectedItem();

        if (!productExists) {
            ProductsInTransact newProduct = new ProductsInTransact();
            newProduct.setProductId(product.getProductId());
            newProduct.setDescription(product.getDescription());
            newProduct.setUnit(product.getUnitOfMeasurementString());
            newProduct.setUnitPrice(product.getCostPerUnit());
            newProduct.setReceivedQuantity(inventoryDAO.getQuantityByBranchAndProductID(branch.getId(), product.getProductId())); // Set the actual quantity available
            newProduct.setOrderedQuantity(0);

            productsList.add(newProduct);

            transferTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        } else {
            DialogUtils.showErrorMessage("Error", "This product already exists in the list.");
        }
    }

    Date dateRequested;

    @Setter
    StockTransferListController stockTransferListController;

    public void initData(StockTransfer stockTransfer, StockTransferListController stockTransferListController) {
        confirmButton.setText("Update");
        this.stockTransferListController = stockTransferListController;

        transferTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                removedProducts.add(transferTable.getSelectionModel().getSelectedItem());
                productsList.remove(transferTable.getSelectionModel().getSelectedItem());
            }
        });


        new Thread(() -> {
            StockTransfer selectedTransfer;
            try {
                selectedTransfer = stockTransferDAO.getStockTransferDetails(stockTransfer.getStockNo());
                Platform.runLater(() -> {

                    if (selectedTransfer.getStatus().equals("RECEIVED")) {
                        parentBorderPane.setDisable(true);
                    }

                    stockTransferID.setText(selectedTransfer.getStockNo());
                    sourceBranch.setValue(branchDAO.getBranchById(selectedTransfer.getSourceBranch()));
                    targetBranch.setValue(branchDAO.getBranchById(selectedTransfer.getTargetBranch()));
                    leadDate.setValue(selectedTransfer.getLeadDate().toLocalDate());
                    statusLabel.setText(selectedTransfer.getStatus());
                    transferTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
                    dateRequested = selectedTransfer.getDateRequested();
                    System.out.println(dateRequested);
                    date.setText(selectedTransfer.getDateRequested().toLocalDate().toString());

                    initTable(selectedTransfer);

                    addProductButton.setOnMouseClicked(mouseEvent -> {
                        isTouchScreen(sourceBranch.getSelectionModel().getSelectedItem());
                    });

                    grandTotal.setText("Total Amount: " + calculateTotalAmount());

                    confirmButton.setOnMouseClicked(mouseEvent -> {
                        try {
                            initializeStockTransferForUpdate(selectedTransfer);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

                    receiveButton.setOnMouseClicked(mouseEvent -> {
                        initializeStockTransferForReceive(selectedTransfer);
                    });
                });
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void initializeStockTransferForReceive(StockTransfer selectedTransfer) {
        StockTransfer stockTransfer = new StockTransfer();
        stockTransfer.setStockNo(selectedTransfer.getStockNo());
        stockTransfer.setSourceBranch(selectedTransfer.getSourceBranch());
        stockTransfer.setTargetBranch(selectedTransfer.getTargetBranch());
        stockTransfer.setLeadDate(selectedTransfer.getLeadDate());
        stockTransfer.setStatus("RECEIVED");
        stockTransfer.setDateReceived(Timestamp.valueOf(LocalDateTime.now()));
        stockTransfer.setReceiverId(UserSession.getInstance().getUserId());
        stockTransfer.setDateRequested(selectedTransfer.getDateRequested());

        List<StockTransfer> stockTransferBatch = new ArrayList<>();
        boolean atLeastOneNonZeroQuantity = false;

        for (ProductsInTransact product : transferTable.getItems()) {
            if (product.getOrderedQuantity() == 0) {
                continue;
            }

            atLeastOneNonZeroQuantity = true;

            StockTransfer productTransfer = getStockTransfer(product, stockTransfer);
            stockTransferBatch.add(productTransfer);
        }

        if (!atLeastOneNonZeroQuantity) {
            DialogUtils.showErrorMessage("Error", "All quantities are zero.");
            return;
        }

        // Perform batch update
        boolean allUpdatesSuccessful = stockTransferDAO.insertStockTransfers(stockTransferBatch);

        if (allUpdatesSuccessful) {
            DialogUtils.showCompletionDialog("Success", "Stock transfer update now pending");
            stockTransferListController.loadStockTransfer();

            ConfirmationAlert confirmationAlert = new ConfirmationAlert(
                    "Update another transfer?", "Do you want to update another stock transfer?", "Yes or no.", true);
            boolean yes = confirmationAlert.showAndWait();
            if (yes) {
                try {
                    resetUI();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                closeStage();
            }
        } else {
            DialogUtils.showErrorMessage("Error", "Please contact your system administrator");
        }
    }


    private void initTable(StockTransfer selectedTransfer) {
        try {
            transferTable.getColumns().clear();
            if (selectedTransfer.getStatus().equals("REQUESTED")) {
                transferTable.setEditable(true);
            }
            List<ProductsInTransact> products = stockTransferDAO.getProductsAndQuantityByOrderNo(selectedTransfer.getStockNo());
            for (ProductsInTransact product : products) {
                product.setAvailableQuantity(stockTransferDAO.getAvailableQuantityForProduct(product.getProductId(), selectedTransfer.getSourceBranch()));
            }
            productsList.addAll(products);
            // Create columns
            TableColumn<ProductsInTransact, String> descriptionColumn = new TableColumn<>("Description");
            descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

            TableColumn<ProductsInTransact, String> unitColumn = new TableColumn<>("Unit");
            unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));

            TableColumn<ProductsInTransact, Integer> orderedQuantityColumn = getProductsInTransactIntegerTableColumn("Ordered Quantity", transferTable);

            TableColumn<ProductsInTransact, Integer> receivedQuantityColumn = getProductsInTransactIntegerTableColumn();

            transferTable.getColumns().addAll(descriptionColumn, unitColumn, orderedQuantityColumn, receivedQuantityColumn);

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception
        }
    }

    private TableColumn<ProductsInTransact, Integer> getProductsInTransactIntegerTableColumn() {
        TableColumn<ProductsInTransact, Integer> receivedQuantityColumn = new TableColumn<>("Received Quantity");
        receivedQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("receivedQuantity"));
        receivedQuantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        receivedQuantityColumn.setOnEditCommit(event -> {
            ProductsInTransact product = event.getRowValue();
            product.setReceivedQuantity(event.getNewValue());
            product.setPaymentAmount(product.getOrderedQuantity() * product.getUnitPrice());
            product.setTotalAmount(product.getPaymentAmount());
            transferTable.refresh();
            transferTable.requestFocus();
        });
        return receivedQuantityColumn;
    }

    private static TableColumn<ProductsInTransact, Integer> getProductsInTransactIntegerTableColumn(String Ordered_Quantity, TableView<ProductsInTransact> transferTable) {
        TableColumn<ProductsInTransact, Integer> orderedQuantityColumn = new TableColumn<>(Ordered_Quantity);
        orderedQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("orderedQuantity"));
        orderedQuantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        orderedQuantityColumn.setOnEditCommit(event -> {
            ProductsInTransact product = event.getRowValue();
            if (event.getNewValue() > product.getAvailableQuantity()) {
                DialogUtils.showErrorMessage("Error", "Quantity cannot be greater than available quantity");
                event.consume();
            } else {
                product.setOrderedQuantity(event.getNewValue());
                product.setPaymentAmount(product.getOrderedQuantity() * product.getUnitPrice());
                product.setTotalAmount(product.getPaymentAmount());
                transferTable.refresh();
            }
            transferTable.requestFocus();
        });
        return orderedQuantityColumn;
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        parentBorderPane.setLeft(null);
        TableViewFormatter.formatTableView(transferTable);
        addProductLabel.setText("Add Product");

        TextFieldUtils.addNumericInputRestriction(orderedQuantity);
        TextFieldUtils.addNumericInputRestriction(availableQuantity);
        transferTable.setItems(productsList);


        sourceBranch.setCellFactory(lv -> new ListCell<Branch>() {
            @Override
            protected void updateItem(Branch item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getBranchName());
                }
            }
        });

        sourceBranch.setConverter(new StringConverter<Branch>() {
            @Override
            public String toString(Branch branch) {
                return (branch == null) ? null : branch.getBranchName();
            }

            @Override
            public Branch fromString(String string) {
                return sourceBranch.getItems().stream()
                        .filter(branch -> branch.getBranchName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });


        targetBranch.setCellFactory(lv -> new ListCell<Branch>() {
            @Override
            protected void updateItem(Branch item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getBranchName());
                }
            }
        });

        targetBranch.setConverter(new StringConverter<Branch>() {
            @Override
            public String toString(Branch branch) {
                return (branch == null) ? null : branch.getBranchName();
            }

            @Override
            public Branch fromString(String string) {
                return targetBranch.getItems().stream()
                        .filter(branch -> branch.getBranchName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    private String calculateTotalAmount() {
        double totalAmount = 0;
        for (ProductsInTransact product : transferTable.getItems()) {
            totalAmount += product.getPaymentAmount();
        }
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return df.format(totalAmount);
    }

    @Setter
    Stage stockTransferStage;

    public void createNewBadStockTransfer() {
        stockTransferNo = stockTransferDAO.generateStockTransferNumber();
        ObservableList<Branch> branches = branchDAO.getBranches();
        ObservableList<String> branchWithInventory = inventoryDAO.getBranchNamesWithInventory();

        sourceBranch.setItems(FXCollections.observableArrayList(
                branches.stream().filter(branch -> branchWithInventory.contains(branch.getBranchName()) && branch.isReturn()).collect(Collectors.toList())
        ));
        targetBranchBox.setDisable(true);
        addProductButton.setDisable(true);
        stockTransferID.setText("BOT-" + stockTransferNo);
        statusLabel.setText("ENTRY REQUEST");

        // Add a listener to the sourceBranch ComboBox
        sourceBranch.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Branch>() {
            @Override
            public void changed(ObservableValue<? extends Branch> observableValue, Branch oldValue, Branch newValue) {
                targetBranchBox.setDisable(newValue == null);
                if (newValue != null) {
                    addProductButton.setDisable(false);
                    addProductButton.setOnMouseClicked(mouseEvent -> isTouchScreen(newValue));
                }

                ObservableList<Branch> targetBranches = FXCollections.observableArrayList(branches.stream().filter(branch -> !branch.getBranchName().equals(newValue.getBranchName())).collect(Collectors.toList()));
                targetBranch.setItems(targetBranches);
                ComboBoxFilterUtil.setupComboBoxFilter(targetBranch, targetBranches);
            }
        });

        initializeTable();

        //transferTable change listener
        transferTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Platform.runLater(() -> {
                    grandTotal.setText("Total Amount: " + calculateTotalAmount());
                });
            }
        });

        confirmButton.setOnMouseClicked(mouseEvent -> {
            try {
                initializeStockTransfer();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
