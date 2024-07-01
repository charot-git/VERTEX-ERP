package com.vertex.vos;

import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class stockTransferController {
    @FXML
    public Label stockTransferID;
    public VBox leadDateBox;
    public DatePicker leadDate;
    public Label leadDateErr;
    AnchorPane contentPane;

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

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
        stockTransferID.setText("Stock Transfer #" + String.valueOf(stockTransferNo));

        // Add a listener to the sourceBranch ComboBox
        sourceBranch.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                targetBranchBox.setDisable(newValue == null);
                ObservableList<String> allBranchNames = branchDAO.getAllBranchNames();
                if (newValue != null) {
                    allBranchNames.remove(newValue);
                    addProductButton.setDisable(false);
                    addProductButton.setOnMouseClicked(mouseEvent -> addProductToTable(newValue));
                }
                targetBranch.setItems(allBranchNames);
                ComboBoxFilterUtil.setupComboBoxFilter(targetBranch,allBranchNames);
            }
        });

        initializeTable();
        confirmButton.setOnMouseClicked(mouseEvent -> {
            try {
                initializeStockTransfer();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void initializeStockTransfer() throws SQLException {
        StockTransfer stockTransfer = new StockTransfer();
        stockTransfer.setOrderNo(String.valueOf(stockTransferNo));
        stockTransfer.setDateRequested(Date.valueOf(LocalDate.now()));
        stockTransfer.setSourceBranch(branchDAO.getBranchIdByName(sourceBranch.getSelectionModel().getSelectedItem()));
        stockTransfer.setTargetBranch(branchDAO.getBranchIdByName(targetBranch.getSelectionModel().getSelectedItem()));
        stockTransfer.setLeadDate(Date.valueOf(leadDate.getValue()));
        stockTransfer.setStatus("REQUESTED");

        boolean allTransfersSuccessful = true;
        for (ProductsInTransact product : productsList) {
            stockTransfer.setProductId(product.getProductId());
            stockTransfer.setOrderedQuantity(product.getOrderedQuantity());
            stockTransfer.setAmount(product.getTotalAmount());
            boolean transfer = stockTransferDAO.insertStockTransfer(stockTransfer);
            if (!transfer) {
                allTransfersSuccessful = false;
                break;
            }
        }

        if (allTransfersSuccessful) {
            DialogUtils.showConfirmationDialog("Success", "Stock transfer request now pending");
            tableManagerController.loadStockTransfer();
            ConfirmationAlert confirmationAlert = new ConfirmationAlert("Create new transfer?", "Do you want to create a new stock transfer?", "Yes or no.", true);
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

    private void addProductToTable(String newValue) {
        int sourceBranchId = branchDAO.getBranchIdByName(newValue);

        if (sourceBranchId == -1) {
            DialogUtils.showErrorMessage("Error", "Branch is invalid, please contact your system administrator");
        } else {
            openProductStage(sourceBranchId, newValue);
        }
    }

    ErrorUtilities errorUtilities = new ErrorUtilities();
    private Stage productStage;
    private final ObservableList<ProductsInTransact> productsList = FXCollections.observableArrayList();

    private void openProductStage(int sourceBranchId, String newValue) {
        if (productStage == null || !productStage.isShowing()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("tableManager.fxml"));
                Parent content = loader.load();

                TableManagerController controller = loader.getController();
                controller.setRegistrationType("stock_transfer_products");
                controller.loadBranchProductsTable(sourceBranchId);
                controller.setStockTransferController(this);

                productStage = new Stage();
                productStage.setTitle("Add product for branch " + newValue);
                productStage.setScene(new Scene(content));
                productStage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            errorUtilities.shakeWindow(productStage);
            productStage.toFront();
        }
    }

    private void initializeTable() {
        transferTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        transferTable.setEditable(true);

        TableColumn<ProductsInTransact, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<ProductsInTransact, String> unitColumn = new TableColumn<>("Unit");
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));

        TableColumn<ProductsInTransact, Integer> quantityAvailableColumn = new TableColumn<>("Quantity Available");
        quantityAvailableColumn.setCellValueFactory(new PropertyValueFactory<>("receivedQuantity")); // Update PropertyValueFactory

        TableColumn<ProductsInTransact, Integer> orderQuantityColumn = getOrderQuantityColumn();


        TableColumn<ProductsInTransact, Double> totalAmountColumn = getTotalAmountColumn();

        // Add columns to the transferTable
        transferTable.getColumns().addAll(descriptionColumn, unitColumn, quantityAvailableColumn, orderQuantityColumn, totalAmountColumn);
    }

    private static TableColumn<ProductsInTransact, Double> getTotalAmountColumn() {
        TableColumn<ProductsInTransact, Double> totalAmountColumn = new TableColumn<>("Total Amount");
        totalAmountColumn.setCellValueFactory(cellData -> {
            ProductsInTransact product = cellData.getValue();
            double orderedQuantity = product.getOrderedQuantity();
            double price = product.getUnitPrice(); // Assuming you have a getPricePerUnit() method in ProductsInTransact
            return new SimpleDoubleProperty(orderedQuantity * price).asObject();
        });
        return totalAmountColumn;
    }

    private static TableColumn<ProductsInTransact, Integer> getOrderQuantityColumn() {
        TableColumn<ProductsInTransact, Integer> orderQuantityColumn = new TableColumn<>("Order Quantity");
        orderQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("orderedQuantity"));
        orderQuantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        orderQuantityColumn.setOnEditCommit(event -> {
            ProductsInTransact product = event.getRowValue();
            product.setOrderedQuantity(event.getNewValue());
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
            productsList.add(newProduct);

            transferTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        } else {
            DialogUtils.showErrorMessage("Error", "This product already exists in the list.");
        }
    }

    public void initData(int ORDER_NO) {
        new Thread(() -> {
            StockTransfer selectedTransfer;
            try {
                selectedTransfer = stockTransferDAO.getStockTransferDetails(String.valueOf(ORDER_NO));
                Platform.runLater(() -> {
                    confirmBox.getChildren().remove(confirmButton);
                    stockTransferID.setText("Stock Transfer #" + selectedTransfer.getOrderNo());
                    sourceBranch.setValue(branchDAO.getBranchNameById(selectedTransfer.getSourceBranch()));
                    targetBranch.setValue(branchDAO.getBranchNameById(selectedTransfer.getTargetBranch()));
                    leadDate.setValue(selectedTransfer.getLeadDate().toLocalDate());
                    statusLabel.setText(selectedTransfer.getStatus());
                    transferTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

                    initTable(selectedTransfer);
                });
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void initTable(StockTransfer selectedTransfer) {
        try {
            List<ProductsInTransact> products = stockTransferDAO.getProductsAndQuantityByOrderNo(selectedTransfer.getOrderNo());

            transferTable.getColumns().clear();

            // Create columns
            TableColumn<ProductsInTransact, String> descriptionColumn = new TableColumn<>("Description");
            descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

            TableColumn<ProductsInTransact, String> unitColumn = new TableColumn<>("Unit");
            unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));

            TableColumn<ProductsInTransact, Integer> quantityColumn = new TableColumn<>("Quantity");
            quantityColumn.setCellValueFactory(new PropertyValueFactory<>("orderedQuantity"));

            // Add columns to table
            transferTable.getColumns().addAll(descriptionColumn, unitColumn, quantityColumn);

            // Populate data into table
            transferTable.setItems(FXCollections.observableArrayList(products));
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception
        }
    }

    TableManagerController tableManagerController;

    void setTableManager(TableManagerController tableManagerController) {
        this.tableManagerController = tableManagerController;
    }
}
