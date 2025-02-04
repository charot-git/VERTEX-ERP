package com.vertex.vos;

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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import lombok.Setter;

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
    public TextField uomTextField;
    public TextField productNameTextField;
    public BorderPane parentBorderPane;
    public VBox productPane;
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
    private ComboBox<String> sourceBranch;

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
    private ComboBox<String> targetBranch;

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

    public void createNewTransfer() {
        try {
            stockTransferNo = stockTransferDAO.generateStockTransferNumber();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        ObservableList<String> branchWithInventory = inventoryDAO.getBranchNamesWithInventory();
        sourceBranch.setItems(branchWithInventory);
        ComboBoxFilterUtil.setupComboBoxFilter(sourceBranch, branchWithInventory);
        targetBranchBox.setDisable(true);
        addProductButton.setDisable(true);
        stockTransferID.setText("Stock Transfer #" + stockTransferNo);
        statusLabel.setText("ENTRY REQUEST");

        // Add a listener to the sourceBranch ComboBox
        sourceBranch.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                targetBranchBox.setDisable(newValue == null);
                ObservableList<String> allBranchNames = branchDAO.getAllBranchNames();
                if (newValue != null) {
                    allBranchNames.remove(newValue);
                    addProductButton.setDisable(false);
                    addProductButton.setOnMouseClicked(mouseEvent -> isTouchScreen(newValue));
                }
                targetBranch.setItems(allBranchNames);
                ComboBoxFilterUtil.setupComboBoxFilter(targetBranch, allBranchNames);
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

    private void isTouchScreen(String newValue) {
        int sourceBranchId = branchDAO.getBranchIdByName(newValue);
        if (Platform.isSupported(ConditionalFeature.INPUT_TOUCH) && Platform.isSupported(ConditionalFeature.INPUT_METHOD)) {
            openProductStage(sourceBranchId, newValue);
        } else {
            openProductPane(sourceBranchId, newValue);
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
        transferTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) { // Ensure a row is selected before accessing properties
                productNameTextField.setText(newValue.getDescription());
                uomTextField.setText(newValue.getUnit());
                availableQuantity.setText(String.valueOf(newValue.getReceivedQuantity()));
                orderedQuantity.setText(String.valueOf(newValue.getOrderedQuantity()));

                // Update the ordered quantity in the table and the ProductsInTransact object
                orderedQuantity.textProperty().addListener((observableValue, oldValue1, newValue1) -> {
                    if (newValue1 != null && !newValue1.isEmpty()) {
                        int orderedQuantityValue = Integer.parseInt(newValue1);
                        newValue.setOrderedQuantity(orderedQuantityValue);
                        transferTable.refresh();
                    }
                });

                // Implement remove product action
                removeProduct.setOnAction(event -> {
                    removedProducts.add(newValue);
                    transferTable.getItems().remove(newValue);
                    transferTable.refresh();
                });
            } else {
                // Optionally clear fields when no selection
                productNameTextField.clear();
                uomTextField.clear();
                availableQuantity.clear();
                orderedQuantity.clear();
            }
        });
    }

    List<ProductsInTransact> removedProducts = new ArrayList<>();


    private void initializeStockTransferForUpdate(StockTransfer selectedTransfer) throws SQLException {
        StockTransfer stockTransfer = new StockTransfer();
        stockTransfer.setOrderNo(selectedTransfer.getOrderNo());
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
            boolean deleteSuccess = stockTransferDAO.deleteStockTransfers(removedProducts, selectedTransfer.getOrderNo(), selectedTransfer.getTargetBranch());
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
            tableManagerController.loadStockTransfer();

            ConfirmationAlert confirmationAlert = new ConfirmationAlert(
                    "Update another transfer?", "Do you want to update another stock transfer?", "Yes or no.", true);
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


    private static StockTransfer getStockTransfer(ProductsInTransact product, StockTransfer stockTransfer) {
        StockTransfer productTransfer = new StockTransfer();
        productTransfer.setOrderNo(stockTransfer.getOrderNo());
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
        productTransfer.setAmount(product.getUnitPrice() * product.getOrderedQuantity());
        return productTransfer;
    }


    private void initializeStockTransfer() throws SQLException {
        StockTransfer stockTransfer = new StockTransfer();
        stockTransfer.setOrderNo(String.valueOf(stockTransferNo));
        stockTransfer.setDateRequested(Date.valueOf(LocalDate.now()));
        stockTransfer.setSourceBranch(branchDAO.getBranchIdByName(sourceBranch.getSelectionModel().getSelectedItem()));
        stockTransfer.setTargetBranch(branchDAO.getBranchIdByName(targetBranch.getSelectionModel().getSelectedItem()));
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
            tableManagerController.loadStockTransfer();

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


    private void resetUI() {
        productsList.clear();
        transferTable.getItems().clear();
        stockTransferNo = 0;
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
        quantityAvailableColumn.setCellValueFactory(new PropertyValueFactory<>("receivedQuantity")); // Update PropertyValueFactory

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
        TableColumn<ProductsInTransact, Integer> orderQuantityColumn = new TableColumn<>("Order Quantity");
        orderQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("orderedQuantity"));
        orderQuantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        orderQuantityColumn.setOnEditCommit(event -> {
            ProductsInTransact product = event.getRowValue();
            if (event.getNewValue() > product.getReceivedQuantity()) {
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

        Branch branch = branchDAO.getBranchById(branchDAO.getBranchIdByName(sourceBranch.getSelectionModel().getSelectedItem()));

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

    public void initData(int ORDER_NO, TableManagerController tableManagerController) {
        confirmButton.setText("Update");

        this.tableManagerController = tableManagerController;


        new Thread(() -> {
            StockTransfer selectedTransfer;
            try {
                selectedTransfer = stockTransferDAO.getStockTransferDetails(String.valueOf(ORDER_NO));
                Platform.runLater(() -> {

                    if (selectedTransfer.getStatus().equals("RECEIVED")){
                        parentBorderPane.setDisable(true);
                    }

                    stockTransferID.setText("Stock Transfer #" + selectedTransfer.getOrderNo());
                    sourceBranch.setValue(branchDAO.getBranchNameById(selectedTransfer.getSourceBranch()));
                    targetBranch.setValue(branchDAO.getBranchNameById(selectedTransfer.getTargetBranch()));
                    leadDate.setValue(selectedTransfer.getLeadDate().toLocalDate());
                    statusLabel.setText(selectedTransfer.getStatus());
                    transferTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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
        stockTransfer.setOrderNo(selectedTransfer.getOrderNo());
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
            try {
                tableManagerController.loadStockTransfer();
            } catch (SQLException e) {
                DialogUtils.showErrorMessage("Database Error", "Failed to reload stock transfer data.");
                return;
            }

            ConfirmationAlert confirmationAlert = new ConfirmationAlert(
                    "Update another transfer?", "Do you want to update another stock transfer?", "Yes or no.", true);
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


    private void initTable(StockTransfer selectedTransfer) {
        try {
            if (selectedTransfer.getStatus().equals("REQUESTED")) {
                transferTable.setEditable(true);
            }

            List<ProductsInTransact> products = stockTransferDAO.getProductsAndQuantityByOrderNo(selectedTransfer.getOrderNo());
            productsList.addAll(products);

            transferTable.getColumns().clear();

            for (ProductsInTransact product : products) {
                product.setReceivedQuantity(stockTransferDAO.getAvailableQuantityForProduct(product.getProductId(), selectedTransfer.getSourceBranch()));
            }

            // Create columns
            TableColumn<ProductsInTransact, String> descriptionColumn = new TableColumn<>("Description");
            descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

            TableColumn<ProductsInTransact, String> unitColumn = new TableColumn<>("Unit");
            unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));

            TableColumn<ProductsInTransact, Integer> quantityColumn = new TableColumn<>("Quantity");
            quantityColumn.setCellValueFactory(new PropertyValueFactory<>("orderedQuantity"));
            quantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
            quantityColumn.setOnEditCommit(event -> {
                ProductsInTransact product = event.getRowValue();
                if (event.getNewValue() > product.getReceivedQuantity()) {
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


            // Add columns to table
            transferTable.getColumns().addAll(descriptionColumn, unitColumn, quantityColumn);

            // Populate data into table
            transferTable.setItems(FXCollections.observableArrayList(productsList));
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception
        }
    }

    TableManagerController tableManagerController;

    void setTableManager(TableManagerController tableManagerController) {
        this.tableManagerController = tableManagerController;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        parentBorderPane.setLeft(null);
        TableViewFormatter.formatTableView(transferTable);
        addProductLabel.setText("Add Product");

        TextFieldUtils.addNumericInputRestriction(orderedQuantity);
        TextFieldUtils.addNumericInputRestriction(availableQuantity);

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

}
