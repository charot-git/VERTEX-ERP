package com.vertex.vos;

import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import lombok.Setter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Locale.Builder;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class PurchaseOrderEntryController implements Initializable {

    @Setter
    private PurchaseOrderConfirmationController purchaseOrderConfirmationController;
    @Setter
    private AnchorPane contentPane; // Declare contentPane variable
    @FXML
    private AnchorPane POAnchorPane;
    @FXML
    private HBox leadTimeBox;
    @FXML
    private VBox leadTimeReceivingBox;
    @FXML
    private DatePicker leadTimeReceivingDatePicker;
    @FXML
    private VBox leadTimePaymentBox;
    @FXML
    private DatePicker leadTimePaymentDatePicker;
    @FXML
    private Label gross;
    @FXML
    private Label discounted;

    ErrorUtilities errorUtilities = new ErrorUtilities();

    PurchaseOrderDAO purchaseOrderDAO = new PurchaseOrderDAO();
    private final BranchDAO branchDAO = new BranchDAO();
    DiscountDAO discountDAO = new DiscountDAO();

    ProductDAO productDAO = new ProductDAO();

    private final PurchaseOrderDAO orderDAO = new PurchaseOrderDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    PaymentTermsDAO paymentTermsDAO = new PaymentTermsDAO();
    DeliveryTermsDAO deliveryTermsDAO = new DeliveryTermsDAO();
    PurchaseOrderProductDAO orderProductDAO = new PurchaseOrderProductDAO();
    @FXML
    private Label date;
    @FXML
    private VBox totalBoxLabels;
    @FXML
    private VBox addBoxes;
    @FXML
    HBox confirmBox;
    @FXML
    private Label grandTotal;
    @FXML
    private CheckBox receiptCheckBox;
    @FXML
    private Label withholding;
    @FXML
    private Label vat;
    @FXML
    private Label paymentTerms;
    @FXML
    private Label receivingTerms;
    @FXML
    private HBox totalBox;
    @FXML
    private HBox addBranchBox;
    @FXML
    private HBox addProductBox;
    @FXML
    private VBox POBox;
    @FXML
    private Label purchaseOrderNo;
    @FXML
    private ComboBox<String> supplier;
    @FXML
    private Button confirmButton;
    private String type;
    private double price;
    private final double grandTotals = 0.0; // Variable to store the grand total
    private final double vatTotals = 0.0;
    private final double withholdingTotals = 0.0;
    private PurchaseOrder selectedPurchaseOrder;
    @FXML
    private TabPane branchTabPane;
    @FXML
    private Tab branchTab;
    @FXML
    private VBox addProductButton;
    @FXML
    private VBox addBranchButton;
    @FXML
    private VBox POContent;
    @FXML
    private VBox totalVBox;
    @FXML
    private Label statusLabel;
    @FXML
    private ImageView statusImage;
    private final DocumentNumbersDAO orderNumberDAO = new DocumentNumbersDAO();

    private int po_number = 0;

    public void setPurchaseOrderType(String type) {
        this.type = type;
    }

    private final HistoryManager historyManager = new HistoryManager();
    private final int currentNavigationId = -1;
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    private final ObservableList<ProductsInTransact> productsList = FXCollections.observableArrayList();
    TableView<ProductsInTransact> productsAddedTable = new TableView<>();
    private final List<Branch> branches = new ArrayList<>(); // Declare your branch list

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        productsAddedTable.setPlaceholder(progressIndicator);
        leadTimeBox.getChildren().removeAll(leadTimePaymentBox, leadTimeReceivingBox);
        receivingTerms.setText("");
        paymentTerms.setText("");
        statusLabel.setText("PO REQUEST");
        receiptCheckBox.setSelected(true);
        comboBoxBehaviour();
        Platform.runLater(() -> {
            if (type == null) {

            } else {
                encoderUI(type);
            }
        });
        addBranchButton.setOnMouseClicked(mouseEvent -> addBranchToTables());
    }

    private Stage branchStage;

    private void addBranchToTables() {
        if (branchStage == null || !branchStage.isShowing()) {
            openBranchStage();
        } else {
            errorUtilities.shakeWindow(branchStage);
            branchStage.toFront();
        }
    }

    private void openBranchStage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("tableManager.fxml"));
            Parent content = loader.load();

            TableManagerController controller = loader.getController();
            controller.setRegistrationType("branch_selection_po");
            controller.setPurchaseOrderEntryController(this);
            branchStage = new Stage();
            branchStage.setTitle("Add branch for PO " + po_number);
            branchStage.setScene(new Scene(content));
            branchStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private Stage productStage;

    private void addProductToTable(PurchaseOrder purchaseOrder) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ProductSelectionBySupplier.fxml"));
            Parent root = loader.load();
            ProductSelectionPerSupplier controller = loader.getController();

            controller.addProductForStockIn(purchaseOrder);
            controller.setPOController(this);

            Stage stage = new Stage();
            stage.setTitle("Add Products");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getSupplierId() {
        SupplierDAO supplierDAO = new SupplierDAO();
        return supplierDAO.getSupplierIdByName(supplier.getSelectionModel().getSelectedItem());
    }

    private void comboBoxBehaviour() {
        TextFieldUtils.setComboBoxBehavior(supplier);
    }

    private void encoderUI(String type) {
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        populateSupplierNames(type);
        confirmButton.setDisable(true);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a");
        LocalDateTime currentDateTime = LocalDateTime.now();
        String formattedDateTime = currentDateTime.format(formatter);
        po_number = orderNumberDAO.getNextPurchaseOrderNumber();
        date.setText(formattedDateTime);
        int nextPurchaseOrderId = po_number;
        if (nextPurchaseOrderId != -1) {
            purchaseOrderNo.setText("PURCHASE ORDER NO " + nextPurchaseOrderId);
            Tab encoderTab = new Tab("Order Quantities");
            branchTabPane.getTabs().add(encoderTab);
            setEncoderUI(encoderTab);
        } else {
            purchaseOrderNo.setText("ERROR: Unable to generate purchase order ID");
        }
        receiptCheckBox.setVisible(false);
        totalBox.getChildren().remove(totalBoxLabels);
        int transactionTypeId = 0;
        if (type.equals("trade")) {
            transactionTypeId = 1;
        } else if (type.equals("non-trade")) {
            transactionTypeId = 2;
        }
        int status = 1;
        supplier.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Supplier selectedSupplier = supplierDAO.getSupplierById(supplierDAO.getSupplierIdByName(newValue));
                purchaseOrder.setSupplierName(selectedSupplier.getId());
                purchaseOrder.setSupplierNameString(selectedSupplier.getSupplierName());
                purchaseOrder.setReceivingType(deliveryTermsDAO.getDeliveryIdByName(selectedSupplier.getDeliveryTerms()));
                purchaseOrder.setPaymentType(paymentTermsDAO.getPaymentTermIdByName(selectedSupplier.getPaymentTerms()));
            }
        });

        purchaseOrder.setPurchaseOrderNo(po_number);

        purchaseOrder.setPriceType("Cost Per Unit");
        purchaseOrder.setDateEncoded(LocalDateTime.now());
        purchaseOrder.setDate(LocalDate.now());
        purchaseOrder.setTime(LocalTime.now());
        purchaseOrder.setDatetime(LocalDateTime.now());
        purchaseOrder.setEncoderId(UserSession.getInstance().getUserId());
        purchaseOrder.setTransactionType(transactionTypeId);
        purchaseOrder.setInventoryStatus(status);
        supplier.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                purchaseOrder.setSupplierName(getSupplierId());
            }
        });

        addProductButton.setOnMouseClicked(mouseEvent -> {
            addProductToTable(purchaseOrder);
        });

        confirmButton.setOnMouseClicked(mouseEvent -> {
            entryPO(purchaseOrder);
        });
    }

    SendEmail emailSender = new SendEmail();


    private void entryPO(PurchaseOrder purchaseOrder) {
        ConfirmationAlert confirm = new ConfirmationAlert("New PO Request", "PO NUMBER" + po_number, "Ensure entry is correct.", false);
        boolean userConfirmed = confirm.showAndWait();
        if (userConfirmed) {
            boolean headerRegistered = false;
            if (productsAddedTable.getItems().isEmpty()) {
                DialogUtils.showErrorMessage("Error", "Your PO is empty");
            } else {
                headerRegistered = orderDAO.entryPurchaseOrder(purchaseOrder);
            }
            if (headerRegistered) {
                entryPODetails(purchaseOrder);
            } else {
                DialogUtils.showErrorMessage("Error", "Error in requesting purchase order");
            }
        }
    }

    private void entryPODetails(PurchaseOrder purchaseOrder) {
        boolean allProductsEntered = true; // Flag to track all products

        for (ProductsInTransact product : productsAddedTable.getItems()) {
            ProductsInTransact productDetails = new ProductsInTransact();
            productDetails.setOrderId(po_number);
            int productId = product.getProductId();
            productDetails.setProductId(productId);

            boolean productEntered = false; // Flag to track each product
            boolean validQuantityExists = false; // Flag to track if valid quantity exists for the product

            for (Branch branch : branches) {
                int branchId = branch.getId();
                int branchQuantity = product.getBranchQuantity(branch);

                // Check if branchQuantity is valid
                if (branchQuantity > 0) {
                    validQuantityExists = true;
                    productDetails.setOrderedQuantity(branchQuantity);
                    productDetails.setUnitPrice(product.getUnitPrice());
                    productDetails.setBranchId(branchId);

                    boolean productsEntried = orderProductDAO.entryProductPerPO(productDetails);
                    if (productsEntried) {
                        productEntered = true;
                    } else {
                        allProductsEntered = false; // Set all products flag to false if any product fails
                        break; // Stop processing this product if there's an error
                    }
                }
            }

            // If no valid quantities were found for this product, mark it as not entered
            if (!productEntered && validQuantityExists) {
                allProductsEntered = false;
                break;
            }
        }

        if (allProductsEntered) {
            emailSender.sendEmailAsync("wennie_francisco@men2corp.com", "New Purchase Order Request", "A purchase order has been requested with the following details: " + purchaseOrder.getSupplierNameString() + " " + purchaseOrder.getPurchaseOrderNo());
            ConfirmationAlert confirmationAlert = new ConfirmationAlert("Success!", "Your PO Request is now Pending", "Create new PO?", true);
            boolean b = confirmationAlert.showAndWait();
            if (b) {
                refreshEntry(type);
            } else {
                Stage stage = (Stage) confirmButton.getScene().getWindow();
                stage.close();
                branchStage.close();
                productStage.close();
            }
        } else {
            DialogUtils.showErrorMessage("Error", "Error in requesting PO for all products");
        }
    }


    private void refreshEntry(String type) {
        productsList.clear();
        branches.clear();
        branchTabPane.getTabs().clear();
        supplier.setDisable(false);
        confirmButton.setDisable(true);
        productsAddedTable.getColumns().clear();
        productStage.close();
        branchStage.close();
        encoderUI(type);
    }

    private void setEncoderUI(Tab encoderTab) {
        encoderTab.setContent(productsAddedTable);
        productsAddedTable.setEditable(true);
        productsAddedTable.setFocusTraversable(true);
        TableColumn<ProductsInTransact, String> descriptionColumn = new TableColumn<>("Product Description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setPrefWidth(300);

        TableColumn<ProductsInTransact, String> unitColumn = new TableColumn<>("Unit");
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
        unitColumn.setPrefWidth(120);

        TableColumn<ProductsInTransact, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("productCategoryString"));
        categoryColumn.setPrefWidth(120);

        TableColumn<ProductsInTransact, String> brandColumn = new TableColumn<>("Brand");
        brandColumn.setCellValueFactory(new PropertyValueFactory<>("productBrandString"));
        brandColumn.setPrefWidth(120);

        productsAddedTable.getColumns().addAll(categoryColumn, brandColumn, descriptionColumn, unitColumn);
        productsAddedTable.setItems(productsList);
    }


    private void populateSupplierNames(String type) {
        type = type.toUpperCase();
        supplier.setDisable(false);
        List<String> supplierNames = supplierDAO.getAllSupplierNamesWhereType(type);
        ObservableList<String> observableSupplierNames = FXCollections.observableArrayList(supplierNames);
        supplier.setItems(observableSupplierNames);
        ComboBoxFilterUtil.setupComboBoxFilter(supplier, observableSupplierNames);
    }


    private void populateSupplierDetails(String supplierName) {
        supplier.setDisable(false);
        String sqlQuery = "SELECT payment_terms, delivery_terms FROM suppliers WHERE supplier_name = ?";

        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, supplierName); // Set the supplier name parameter in the query

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String paymentTerms = resultSet.getString("payment_terms");
                    String deliveryTerms = resultSet.getString("delivery_terms");

                    displayPaymentAndDeliveryTerms(paymentTerms, deliveryTerms);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayPaymentAndDeliveryTerms(String payment, String delivery) {
        paymentTerms.setText(payment);
        receivingTerms.setText(delivery);
    }

    void addProductToBranchTables(ProductsInTransact productsInTransact) {
        productsList.add(productsInTransact);
    }


    private TableColumn<ProductsInTransact, Integer>[] branchColumns; // Declare branchColumns as a class-level variable

    private void initializeBranchColumns(int numberOfBranches) {
        branchColumns = new TableColumn[numberOfBranches];
        for (int i = 0; i < numberOfBranches; i++) {
            final int index = i;
            branchColumns[i] = new TableColumn<>(branches.get(index).getBranchName()); // Set the column label to Branch Name
            branchColumns[i].setCellValueFactory(cellData -> {
                ProductsInTransact product = cellData.getValue();
                int branchQuantity = product.getBranchQuantity(branches.get(index)); // Fetch branch quantity from product
                return Bindings.createObjectBinding(() -> branchQuantity);
            });
            branchColumns[i].setCellFactory(tc -> new TextFieldTableCell<>(new IntegerStringConverter()));
            branchColumns[i].setPrefWidth(140);
            branchColumns[i].setMaxWidth(100);
            setupBranchColumnEditHandler(branchColumns[i], i); // Set up event handler separately
            productsAddedTable.getColumns().add(branchColumns[i]);
        }
    }

    private void setupBranchColumnEditHandler(TableColumn<ProductsInTransact, Integer> column, int columnIndex) {
        column.setOnEditCommit(event -> {
            ProductsInTransact product = event.getRowValue();
            int newValue = event.getNewValue() != null ? event.getNewValue() : 0;
            product.setBranchQuantity(branches.get(columnIndex), newValue);
            confirmButton.setDisable(false);

            // Get the TableView
            TableView<ProductsInTransact> tableView = productsAddedTable;

            // Get the index of the current row
            int currentRowIndex = tableView.getSelectionModel().getSelectedIndex();

            // Calculate the index of the next row
            int nextRowIndex = currentRowIndex + 1;

            // Check if the next row index is within bounds
            if (nextRowIndex < tableView.getItems().size()) {
                // Focus on the next row and the same column
                tableView.getSelectionModel().select(nextRowIndex);
                tableView.getFocusModel().focus(nextRowIndex, column);
            }

            // Request focus for the TableView
            tableView.requestFocus();
        });
    }


    public void addBranchToTable(int branchId) {
        Branch branchSelected = branchDAO.getBranchById(branchId);
        if (branchSelected != null) {
            if (!branches.stream().anyMatch(existingBranch -> existingBranch.getId() == branchSelected.getId())) {
                branches.add(branchSelected);
                int numberOfBranches = branches.size();
                if (branchColumns == null) {
                    initializeBranchColumns(numberOfBranches);
                } else if (numberOfBranches > branchColumns.length) {
                    TableColumn<ProductsInTransact, Integer> newColumn = getProductsInTransactIntegerTableColumn(branchSelected, numberOfBranches);
                    setupBranchColumnEditHandler(newColumn, numberOfBranches - 1); // Set up event handler for the new column
                    branchColumns = Arrays.copyOf(branchColumns, branchColumns.length + 1);
                    branchColumns[branchColumns.length - 1] = newColumn;
                    productsAddedTable.getColumns().add(newColumn);
                    productsAddedTable.refresh();
                    productsAddedTable.requestFocus();
                }
            } else {
                DialogUtils.showErrorMessage("Error", "Branch already exists in the list");
            }
        } else {
            DialogUtils.showErrorMessage("Error", "Error in adding branch to PO");
        }
    }

    private TableColumn<ProductsInTransact, Integer> getProductsInTransactIntegerTableColumn(Branch branchSelected, int numberOfBranches) {
        TableColumn<ProductsInTransact, Integer> newColumn = new TableColumn<>(branchSelected.getBranchName());
        newColumn.setCellValueFactory(cellData -> {
            ProductsInTransact product = cellData.getValue();
            int branchQuantity = product.getBranchQuantity(branches.get(numberOfBranches - 1)); // Fetch branch quantity from product
            return Bindings.createObjectBinding(() -> branchQuantity);
        });
        newColumn.setCellFactory(tc -> new TextFieldTableCell<>(new IntegerStringConverter())); // Add cell factory
        newColumn.setPrefWidth(140);
        newColumn.setMaxWidth(100);
        return newColumn;
    }

    void fixedValues() {
        supplier.setDisable(true);
    }

    public void setUIPerStatus(PurchaseOrder purchaseOrder, Scene scene) throws SQLException {
        fixedValues();
        if (purchaseOrder != null) {
            LocalDateTime dateTime = purchaseOrder.getDateEncoded();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDate = dateTime.format(formatter);
            scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.isControlDown()) {
                    switch (event.getCode()) {
                        case RIGHT:
                            int selectedIndex = branchTabPane.getSelectionModel().getSelectedIndex();
                            if (selectedIndex < branchTabPane.getTabs().size() - 1) {
                                branchTabPane.getSelectionModel().select(selectedIndex + 1);
                            }
                            event.consume();
                            break;
                        case LEFT:
                            int selectedIndexLeft = branchTabPane.getSelectionModel().getSelectedIndex();
                            if (selectedIndexLeft > 0) {
                                branchTabPane.getSelectionModel().select(selectedIndexLeft - 1);
                            }
                            event.consume();
                            break;
                        default:
                            break;
                    }
                }
            });
            purchaseOrderNo.setText("PURCHASE ORDER NO " + purchaseOrder.getPurchaseOrderNo());
            date.setText(formattedDate);
            statusLabel.setText(purchaseOrder.getInventoryStatusString());
            supplier.setValue(purchaseOrder.getSupplierNameString());
            POBox.getChildren().remove(addBoxes);
            int po_status = purchaseOrder.getInventoryStatus();
            Platform.runLater(() -> {
                fixedValues();
                try {
                    switch (po_status) {
                        case 1:
                            Tab branchTab = createBranchTab(purchaseOrder);
                            branchTabPane.getTabs().add(branchTab); // Assuming branchTabPane is the TabPane
                            loadPOForApproval(purchaseOrder, branchTab); // Adjusted to take a single Tab
                            leadTimeBox.getChildren().add(leadTimeReceivingBox);
                            break;

                        case 2:
                        case 3:
                            loadPOForPendingReceiving(purchaseOrder);
                            leadTimeBox.getChildren().add(leadTimePaymentBox);
                            break;

                        case 4:
                            loadPOForVouchering(purchaseOrder);
                            break;

                        case 5:
                            loadPOForReceiving(purchaseOrder);
                            break;

                        case 6:
                        case 9:
                        case 10:
                            loadPOForReceived(purchaseOrder);
                            break;

                        case 7:
                            loadPOForRestoringPO(purchaseOrder);
                            break;

                        default:
                            // Handle any unexpected status or log an error
                            System.err.println("Unexpected PO status: " + po_status);
                            break;
                    }
                } catch (SQLException e) {
                    // Handle exceptions that may occur during data retrieval or processing
                    e.printStackTrace();
                    DialogUtils.showErrorMessage("Error", "Failed to process purchase order: " + e.getMessage());
                }
            });
            ;
        }
    }

    private void loadPOForPendingReceiving(PurchaseOrder purchaseOrder) {
        branchTabPane.getTabs().clear(); // Clear existing tabs before adding new ones

        Tab tab = new Tab("Pending Receiving");
        branchTabPane.getTabs().add(tab);

        TableView<ProductsInTransact> productsTable = new TableView<>();
        productsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<ProductsInTransact, String> productColumn = new TableColumn<>("Product Description");
        productColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));

        TableColumn<ProductsInTransact, String> productUnitCol = new TableColumn<>("Unit");
        productUnitCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUnit()));

        TableColumn<ProductsInTransact, String> branch = new TableColumn<>("Branch Name");
        branch.setCellValueFactory(cellData -> {
            ProductsInTransact product = cellData.getValue();
            String branchName = branchDAO.getBranchNameById(product.getBranchId());
            return new SimpleStringProperty(branchName);
        });

        TableColumn<ProductsInTransact, String> productQuantityCol = new TableColumn<>("Ordered Quantity");
        productQuantityCol.setCellValueFactory(cellData -> {
            ProductsInTransact product = cellData.getValue();
            String quantity = String.valueOf(product.getOrderedQuantity());
            return new SimpleStringProperty(quantity);
        });

        productsTable.getColumns().addAll(branch, productColumn, productUnitCol, productQuantityCol);

        // Set the ProgressIndicator as the placeholder
        ProgressIndicator progressIndicator = new ProgressIndicator();
        productsTable.setPlaceholder(progressIndicator);

        // Populate the TableView asynchronously
        loadProductsForToBeReceivedForViewing(productsTable, purchaseOrder);
        tab.setContent(productsTable);
    }

    private void loadProductsForToBeReceivedForViewing(TableView<ProductsInTransact> productsTable, PurchaseOrder purchaseOrder) {
        ObservableList<String> branchNames = purchaseOrderDAO.getBranchNamesForPurchaseOrder(purchaseOrder.getPurchaseOrderNo());
        for (String branchName : branchNames) {
            int branchId = branchDAO.getBranchIdByName(branchName);
            List<ProductsInTransact> products = orderProductDAO.getProductsForReceiving(purchaseOrder.getPurchaseOrderNo(), branchId);
            productsTable.getItems().addAll(products);
        }
    }

    private void loadPOForReceived(PurchaseOrder purchaseOrder) {
        populateSupplierDetails(purchaseOrder.getSupplierNameString());

        CompletableFuture.supplyAsync(() -> {
            try {
                return orderProductDAO.getReceiptNumbersForPurchaseOrder(purchaseOrder.getPurchaseOrderNo());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(invoices -> {
            Platform.runLater(() -> {
                branchTabPane.getTabs().clear(); // Clear existing tabs before adding new ones

                for (String invoice : invoices) {
                    Tab tab = new Tab(invoice);

                    // Create a TableView for the products
                    TableView<ProductsInTransact> productsTable = new TableView<>();
                    productsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
                    productsTable.getColumns().addAll(createProductColumns());

                    // Set the ProgressIndicator as the placeholder
                    ProgressIndicator progressIndicator = new ProgressIndicator();
                    productsTable.setPlaceholder(progressIndicator);

                    // Populate the TableView asynchronously
                    populateProductsInTransactTablesPerTabAsync(productsTable, purchaseOrder, invoice);

                    tab.setContent(productsTable);
                    branchTabPane.getTabs().add(tab);
                }

                // Update the UI elements
                gross.setText(String.valueOf(purchaseOrder.getTotalGrossAmount()));
                discounted.setText(String.valueOf(purchaseOrder.getTotalDiscountedAmount()));
                withholding.setText(String.valueOf(purchaseOrder.getWithholdingTaxAmount()));
                vat.setText(String.valueOf(purchaseOrder.getVatAmount()));
                grandTotal.setText(String.valueOf(purchaseOrder.getTotalAmount()));
                confirmButton.setVisible(false);
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                DialogUtils.showErrorMessage("Error loading purchase order", e.getMessage());
            });
            return null;
        });
    }

    private void populateProductsInTransactTablesPerTabAsync(TableView<ProductsInTransact> productsTable, PurchaseOrder purchaseOrder, String invoice) {
        // Create a ProgressIndicator and set it as the placeholder
        ProgressIndicator progressIndicator = new ProgressIndicator();
        productsTable.setPlaceholder(progressIndicator);

        CompletableFuture.supplyAsync(() -> {
            try {
                return loadProductsForInvoice(purchaseOrder, invoice);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(branchProducts -> Platform.runLater(() -> {
            productsTable.getItems().clear();
            productsTable.getItems().addAll(branchProducts);

            if (branchProducts.isEmpty()) {
                productsTable.setPlaceholder(new Label("No products found."));
            }
        })).exceptionally(e -> {
            Platform.runLater(() -> {
                e.printStackTrace();
                productsTable.setPlaceholder(new Label("Failed to load products."));
            });
            return null;
        });
    }

    private ObservableList<ProductsInTransact> loadProductsForInvoice(PurchaseOrder purchaseOrder, String invoice) throws SQLException {
        ObservableList<ProductsInTransact> products = FXCollections.observableArrayList();
        List<ProductsInTransact> productList = orderProductDAO.getProductsPerInvoiceForReceived(purchaseOrder.getPurchaseOrderNo(), invoice);
        for (ProductsInTransact product : productList) {
            int receivedQuantity = orderProductDAO.getReceivedQuantityForInvoice(purchaseOrder.getPurchaseOrderNo(), product.getProductId(), invoice);
            product.setReceivedQuantity(receivedQuantity);
            products.add(product);
        }
        return products;
    }

    private List<TableColumn<ProductsInTransact, ?>> createProductColumns() {
        List<TableColumn<ProductsInTransact, ?>> columns = new ArrayList<>();

        TableColumn<ProductsInTransact, String> productNameColumn = new TableColumn<>("Description");
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<ProductsInTransact, Integer> receivedQuantityColumn = new TableColumn<>("Quantity");
        receivedQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("receivedQuantity"));

        TableColumn<ProductsInTransact, Double> unitPriceColumn = new TableColumn<>("Unit Price");
        unitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));

        TableColumn<ProductsInTransact, Double> discountedAmountColumn = new TableColumn<>("Net Amount");
        discountedAmountColumn.setCellValueFactory(new PropertyValueFactory<>("discountedAmount"));

        TableColumn<ProductsInTransact, Double> vatAmountColumn = new TableColumn<>("VAT Amount");
        vatAmountColumn.setCellValueFactory(cellData -> {
            ProductsInTransact product = cellData.getValue();
            BigDecimal totalAmount = BigDecimal.valueOf(product.getTotalAmount());
            BigDecimal vatAmount = VATCalculator.calculateVat(totalAmount);
            return new SimpleObjectProperty<>(vatAmount.doubleValue());
        });

        TableColumn<ProductsInTransact, Double> withholdingAmountColumn = getWithholdingAmountColumn();

        TableColumn<ProductsInTransact, Double> totalAmountColumn = new TableColumn<>("Total Amount");
        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        TableColumn<ProductsInTransact, Double> amountToPayColumn = getPayablesAmountColumn();

        columns.add(productNameColumn);
        columns.add(receivedQuantityColumn);
        columns.add(unitPriceColumn);
        columns.add(discountedAmountColumn);
        columns.add(vatAmountColumn);
        columns.add(withholdingAmountColumn);
        columns.add(totalAmountColumn);
        columns.add(amountToPayColumn);

        return columns;
    }

    private static TableColumn<ProductsInTransact, Double> getPayablesAmountColumn() {
        TableColumn<ProductsInTransact, Double> amountToPayColumn = new TableColumn<>("Amount To Pay");
        amountToPayColumn.setCellValueFactory(cellData -> {
            ProductsInTransact product = cellData.getValue();
            BigDecimal totalAmount = BigDecimal.valueOf(product.getTotalAmount());
            BigDecimal vatAmount = VATCalculator.calculateVat(totalAmount);
            BigDecimal amountToPay = totalAmount.add(vatAmount);
            return new SimpleObjectProperty<>(amountToPay.doubleValue());
        });
        return amountToPayColumn;
    }


    private static TableColumn<ProductsInTransact, Double> getWithholdingAmountColumn() {
        TableColumn<ProductsInTransact, Double> withholdingAmountColumn = new TableColumn<>("Withholding Amount");
        withholdingAmountColumn.setCellValueFactory(cellData -> {
            ProductsInTransact product = cellData.getValue();
            BigDecimal totalAmount = BigDecimal.valueOf(product.getTotalAmount());
            BigDecimal withholdingAmount = EWTCalculator.calculateWithholding(totalAmount);
            return new SimpleObjectProperty<>(withholdingAmount.doubleValue());
        });
        return withholdingAmountColumn;
    }


    private void loadPOForRestoringPO(PurchaseOrder purchaseOrder) {
    }

    private void loadPOForDone(PurchaseOrder purchaseOrder) {
    }

    private void loadPOForReceiving(PurchaseOrder purchaseOrder) {

    }

    private void loadPOForVouchering(PurchaseOrder purchaseOrder) {

    }

    private void loadPOForBudgeting(PurchaseOrder purchaseOrder) {
        Platform.runLater(this::fixedValues);
        boolean taxed = purchaseOrder.getReceiptRequired();
        receiptCheckBox.setSelected(taxed);
        receiptCheckBox.setDisable(true);
        leadTimePaymentDatePicker.setValue(LocalDate.now());
        populateSupplierDetails(purchaseOrder.getSupplierNameString());

        BigDecimal grossAmount = purchaseOrder.getTotalGrossAmount();
        BigDecimal discountedAmount = purchaseOrder.getTotalDiscountedAmount();
        BigDecimal withholdingTaxAmount = BigDecimal.ZERO;
        BigDecimal vatTaxAmount = BigDecimal.ZERO;
        BigDecimal totalAmount = purchaseOrder.getTotalAmount();

        if (taxed) {
            withholdingTaxAmount = purchaseOrder.getWithholdingTaxAmount();
            vatTaxAmount = purchaseOrder.getVatAmount();
        }

        NumberFormat pesoFormat = NumberFormat.getCurrencyInstance(new Builder().setLanguage("en").setRegion("PH").build());
        String formattedGross = pesoFormat.format(grossAmount);
        String formattedDiscounted = pesoFormat.format(discountedAmount);
        String formattedWithholding = pesoFormat.format(withholdingTaxAmount);
        String formattedVAT = pesoFormat.format(vatTaxAmount);
        String formattedTotal = pesoFormat.format(totalAmount);

        gross.setText(formattedGross);
        discounted.setText(formattedDiscounted);
        withholding.setText(formattedWithholding);
        vat.setText(formattedVAT);
        grandTotal.setText(formattedTotal);

        totalBoxLabels.getChildren().clear();
        if (taxed) {
            totalBoxLabels.getChildren().addAll(gross, discounted, vat, withholding, grandTotal);
        } else {
            totalBoxLabels.getChildren().addAll(gross, discounted, grandTotal);
        }
        confirmButton.setText("BUDGET");
    }

    private void loadPOForApproval(PurchaseOrder purchaseOrder, Tab branchTab) throws SQLException {
        branchTabPane.getTabs().addAll(branchTab);
        leadTimeReceivingDatePicker.setValue(LocalDate.now());
        confirmButton.setText("APPROVE");
        receiptCheckBox.setSelected(true);
        confirmButton.setOnMouseClicked(event -> {
            try {
                approvePO(purchaseOrder, productsAddedTable, branchTab.getText());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }


    private Tab createBranchTab(PurchaseOrder purchaseOrder) throws SQLException {
        // Get the branch associated with the purchase order
        Branch branch = purchaseOrderDAO.getBranchForPurchaseOrder(purchaseOrder.getPurchaseOrderNo());

        if (branch == null) {
            throw new SQLException("No branch found for the provided purchase order.");
        }

        // Create the tab for the branch
        Node content;
        try {
            content = createBranchContent(purchaseOrder, branch, receiptCheckBox);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create content for branch tab", e);
        }

        Tab branchTab = new Tab(branch.getBranchName());
        branchTab.setContent(content);


        return branchTab;
    }


    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();


    private Node createBranchContent(PurchaseOrder purchaseOrder, Branch branch, CheckBox receiptCheckBox) throws SQLException {
        int status = purchaseOrder.getInventoryStatus();
        boolean isReceiptRequired = purchaseOrder.getReceiptRequired();
        receiptCheckBox.setSelected(isReceiptRequired);
        TableView<ProductsInTransact> productsTable = createProductsTable(status, receiptCheckBox);


        CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
            populateProductsInTransactTablesPerTabAsync(productsTable, purchaseOrder, branch);
        }, executorService).thenRun(() -> {
            Platform.runLater(() -> {
                if (status == 2) {

                }
            });
        });

        ListChangeListener<ProductsInTransact> itemsChangeListener = change -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved() || change.wasUpdated()) {
                    task.thenRunAsync(() -> {
                    }, executorService); // Trigger the task when items change
                }
            }
        };

        ListChangeListener<TableColumn<ProductsInTransact, ?>> columnsChangeListener = change -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved()) {
                    task.thenRunAsync(() -> {
                    }, executorService); // Trigger the task when columns change
                }
            }
        };

        productsTable.getItems().addListener(itemsChangeListener);
        productsTable.getColumns().addListener(columnsChangeListener);

        return productsTable;
    }


    private void populateProductsInTransactTablesPerTabAsync(TableView<ProductsInTransact> productsTable, PurchaseOrder purchaseOrder, Branch branch) {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        productsTable.setPlaceholder(progressIndicator);

        CompletableFuture.supplyAsync(() -> {
                    try {
                        return getProductsInTransactForBranch(purchaseOrder, branch.getId());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .thenAccept(branchProducts -> Platform.runLater(() -> {
                    productsTable.getItems().clear();
                    productsTable.getItems().addAll(branchProducts);

                    if (branchProducts.isEmpty()) {
                        productsTable.setPlaceholder(new Label("No products found."));
                    }
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        e.printStackTrace();
                        productsTable.setPlaceholder(new Label("Failed to load products."));
                    });
                    return null;
                });
    }

    private TableView<ProductsInTransact> createProductsTable(int status, CheckBox receiptCheckBox) {
        TableView<ProductsInTransact> productsTable = new TableView<>();
        productsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        productsTable.setEditable(true);
        productsTable.setFocusTraversable(true);

        productsTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.C && event.isControlDown()) {
                TablePosition<?, ?> pos = productsTable.getSelectionModel().getSelectedCells().get(0);
                int row = pos.getRow();
                TableColumn<?, ?> col = pos.getTableColumn();
                Object cellData = col.getCellData(row);

                if (cellData != null) {
                    ClipboardContent content = new ClipboardContent();
                    content.putString(cellData.toString());
                    Clipboard.getSystemClipboard().setContent(content);
                }
            }
        });

        TableColumn<ProductsInTransact, String> productDescriptionCol = new TableColumn<>("Description");
        productDescriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        productDescriptionCol.setMaxWidth(170);

        TableColumn<ProductsInTransact, String> productUnitCol = new TableColumn<>("Unit");
        productUnitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));

        TableColumn<ProductsInTransact, Double> productPricePerUnitCol = getPricePerUnitCol(status);

        TableColumn<ProductsInTransact, Integer> productQuantityPerBranch = quantityControl(status, productsTable);

        TableColumn<ProductsInTransact, Double> totalGrossAmountCol = getTotalGrossAmountCol(status);

        TableColumn<ProductsInTransact, String> discountTypeCol = getDiscountTypePerProduct();

        TableColumn<ProductsInTransact, String> discountValueCol = getDiscountValueColumn(status);

        TableColumn<ProductsInTransact, Double> discountedTotalCol = getDiscountedTotalColumn(discountValueCol, totalGrossAmountCol);

        TableColumn<ProductsInTransact, Double> vatAmountCol = getVatAmountCol(discountedTotalCol);

        TableColumn<ProductsInTransact, Double> withholdingAmountCol = getWithholdingAmountCol(discountedTotalCol);

        TableColumn<ProductsInTransact, Double> totalNetAmountCol = getTotalNetAmountcol(discountedTotalCol, vatAmountCol, withholdingAmountCol, this.receiptCheckBox);
        productsTable.getColumns().clear();

        receiptCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            productsTable.getColumns().clear(); // Clear existing columns

            if (newValue) {
                productsTable.getColumns().addAll(productDescriptionCol, productUnitCol, productPricePerUnitCol, productQuantityPerBranch, totalGrossAmountCol, discountTypeCol, discountValueCol, discountedTotalCol, vatAmountCol, withholdingAmountCol, totalNetAmountCol);
            } else {
                productsTable.getColumns().addAll(productDescriptionCol, productUnitCol, productPricePerUnitCol, productQuantityPerBranch, totalGrossAmountCol, discountTypeCol, discountValueCol, discountedTotalCol, totalNetAmountCol);
            }
        });
        if (receiptCheckBox.isSelected()) {
            productsTable.getColumns().addAll(productDescriptionCol, productUnitCol, productPricePerUnitCol, productQuantityPerBranch, totalGrossAmountCol, discountTypeCol, discountValueCol, discountedTotalCol, vatAmountCol, withholdingAmountCol, totalNetAmountCol);
        } else {
            productsTable.getColumns().addAll(productDescriptionCol, productUnitCol, productPricePerUnitCol, productQuantityPerBranch, totalGrossAmountCol, discountTypeCol, discountValueCol, discountedTotalCol, totalNetAmountCol);
        }
        return productsTable;
    }


    private static TableColumn<ProductsInTransact, Double> getPricePerUnitCol(int status) {
        TableColumn<ProductsInTransact, Double> productPricePerUnitCol = new TableColumn<>();
        productPricePerUnitCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        productPricePerUnitCol.setText("Price Per Unit");
        return productPricePerUnitCol;
    }

    private static TableColumn<ProductsInTransact, Double> getTotalGrossAmountCol(int status) {
        TableColumn<ProductsInTransact, Double> totalGrossAmountCol = new TableColumn<>("Total Gross Amount");
        totalGrossAmountCol.setCellValueFactory(cellData -> {
            ProductsInTransact product = cellData.getValue();
            double pricePerUnit = 0;
            pricePerUnit = product.getUnitPrice();

            int quantity = product.getOrderedQuantity();
            double totalGrossAmount = pricePerUnit * quantity;
            totalGrossAmount = Double.parseDouble(String.format("%.2f", totalGrossAmount));
            product.setGrossAmount(totalGrossAmount);
            return new SimpleDoubleProperty(totalGrossAmount).asObject();
        });
        return totalGrossAmountCol;
    }


    private TableColumn<ProductsInTransact, Double> getTotalNetAmountcol(TableColumn<ProductsInTransact, Double> discountedTotalCol, TableColumn<ProductsInTransact, Double> vatAmountCol, TableColumn<ProductsInTransact, Double> withholdingAmountCol, CheckBox receiptCheckBox) {

        TableColumn<ProductsInTransact, Double> totalAmountCol = new TableColumn<>("Payment Amount");
        totalAmountCol.setCellValueFactory(cellData -> {
            ProductsInTransact product = cellData.getValue();

            double paymentAmount = discountedTotalCol.getCellObservableValue(product).getValue();

            if (receiptCheckBox.isSelected()) {
                double withholdingAmount = withholdingAmountCol.getCellObservableValue(product).getValue();
                double vatAmount = vatAmountCol.getCellObservableValue(product).getValue();
                paymentAmount = paymentAmount + vatAmount - withholdingAmount;
            }

            product.setPaymentAmount(paymentAmount);

            paymentAmount = Double.parseDouble(String.format("%.2f", paymentAmount));
            return new SimpleDoubleProperty(paymentAmount).asObject();
        });

        return totalAmountCol;
    }


    private TableColumn<ProductsInTransact, Integer> quantityControl(int status, TableView<ProductsInTransact> productsTable) {
        TableColumn<ProductsInTransact, Integer> productQuantityPerBranch = new TableColumn<>("Quantity");
        productQuantityPerBranch.setCellValueFactory(cellData -> {
            int quantity = cellData.getValue().getOrderedQuantity();
            return new SimpleIntegerProperty(quantity).asObject();
        });

        if (status == 1) {
            productQuantityPerBranch.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
            productQuantityPerBranch.setOnEditCommit(event -> {
                try {
                    TableView.TableViewSelectionModel<ProductsInTransact> selectionModel = productsTable.getSelectionModel();
                    ProductsInTransact product = selectionModel.getSelectedItem();
                    product.setOrderedQuantity(event.getNewValue());
                    int selectedIndex = productsTable.getSelectionModel().getSelectedIndex();
                    productsTable.getItems().set(selectedIndex, product);
                    productsTable.requestFocus();
                } catch (NumberFormatException | NullPointerException e) {
                    e.printStackTrace();
                }
            });
        } else {
            productQuantityPerBranch.setCellFactory(col -> new TableCell<ProductsInTransact, Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null); // Handle null cases if necessary
                    } else {
                        setText(String.valueOf(item)); // Display the string representation of the integer
                    }
                }
            });
        }
        return productQuantityPerBranch;
    }


    private TableColumn<ProductsInTransact, Double> getWithholdingAmountCol(TableColumn<ProductsInTransact, Double> discountedTotalCol) {
        TableColumn<ProductsInTransact, Double> withholdingAmountCol = new TableColumn<>("EWT");
        withholdingAmountCol.setCellValueFactory(cellData -> {
            ProductsInTransact product = cellData.getValue();

            // Retrieve the discounted total amount
            double totalDiscountedAmount = discountedTotalCol.getCellObservableValue(product).getValue();

            // Calculate EWT based on the discounted amount and EWT rate
            BigDecimal withholdingAmount = EWTCalculator.calculateWithholding(BigDecimal.valueOf(totalDiscountedAmount));

            // Store the calculated withholding amount in the product
            product.setWithholdingAmount(withholdingAmount.doubleValue());

            // Format withholding amount to two decimal places
            withholdingAmount = withholdingAmount.setScale(2, RoundingMode.HALF_UP);

            return new SimpleDoubleProperty(withholdingAmount.doubleValue()).asObject();
        });
        withholdingAmountCol.setMinWidth(50);
        return withholdingAmountCol;
    }


    private TableColumn<ProductsInTransact, Double> getVatAmountCol(TableColumn<ProductsInTransact, Double> discountedTotalCol) {
        TableColumn<ProductsInTransact, Double> vatAmountCol = new TableColumn<>("VAT");
        vatAmountCol.setCellValueFactory(cellData -> {
            ProductsInTransact product = cellData.getValue();

            double totalDiscountedAmount = discountedTotalCol.getCellObservableValue(product).getValue();

            BigDecimal vatAmount = VATCalculator.calculateVat(BigDecimal.valueOf(totalDiscountedAmount));

            product.setVatAmount(vatAmount.doubleValue());

            vatAmount = vatAmount.setScale(2, RoundingMode.HALF_UP);

            return new SimpleDoubleProperty(vatAmount.doubleValue()).asObject();
        });

        vatAmountCol.setMinWidth(50);
        return vatAmountCol;
    }


    private static TableColumn<ProductsInTransact, Double> getDiscountedTotalColumn(TableColumn<ProductsInTransact, String> discountValueCol, TableColumn<ProductsInTransact, Double> totalGrossAmountCol) {
        TableColumn<ProductsInTransact, Double> discountedTotalCol = new TableColumn<>("Net Price");
        discountedTotalCol.setCellValueFactory(cellData -> {
            ProductsInTransact product = cellData.getValue();

            // Parse discount value from String to Double, handle non-numeric values
            String discountValueStr = discountValueCol.getCellObservableValue(product).getValue();
            double discountValue = 0.0; // Defau    lt value if parsing fails
            try {
                discountValue = Double.parseDouble(discountValueStr);
            } catch (NumberFormatException e) {
                // Handle non-numeric values here (like "No Discount")
                // For example, if discountValueStr is "No Discount", set discountValue to 0.0 or handle appropriately
                discountValue = 0.0; // Set default value or handle as per your application's logic
            }

            // Retrieve total gross amount directly from the cell value
            double totalGrossAmount = totalGrossAmountCol.getCellData(product);

            // Calculate discounted total amount
            double totalDiscountedAmount = totalGrossAmount - discountValue;

            // Round to two decimal places
            totalDiscountedAmount = Math.round(totalDiscountedAmount * 100.0) / 100.0;

            // Set discounted amount in the product object (if needed)
            product.setDiscountedAmount(totalDiscountedAmount);

            return new SimpleDoubleProperty(totalDiscountedAmount).asObject();
        });

        return discountedTotalCol;
    }


    private TableColumn<ProductsInTransact, String> getDiscountValueColumn(int status) {
        TableColumn<ProductsInTransact, String> discountValueCol = new TableColumn<>("Discount Value");
        discountValueCol.setCellValueFactory(cellData -> {
            ProductsInTransact product = cellData.getValue();
            int discountTypeId = product.getDiscountTypeId();

            double listPrice = product.getUnitPrice();
            BigDecimal listPriceBD = BigDecimal.valueOf(listPrice);

            List<BigDecimal> lineDiscounts = discountDAO.getLineDiscountsByDiscountTypeId(discountTypeId);

            if (lineDiscounts.isEmpty()) {
                return new SimpleStringProperty("No Discount");
            }

            BigDecimal discountedPrice = DiscountCalculator.calculateDiscountedPrice(listPriceBD, lineDiscounts);

            return new SimpleStringProperty(String.format("%.2f", discountedPrice.doubleValue()));
        });

        discountValueCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(item); // Sets the text to the formatted value or "No Discount"
                }
            }
        });

        return discountValueCol;
    }


    private TableColumn<ProductsInTransact, String> getDiscountTypePerProduct() {
        TableColumn<ProductsInTransact, String> discountTypeCol = new TableColumn<>("Discount Type");
        discountTypeCol.setCellValueFactory(cellData -> {
            ProductsInTransact product = cellData.getValue();

            int discountTypeId = product.getDiscountTypeId(); // Retrieve the discount type ID directly from the ProductsInTransact object

            try {
                String discountTypeName = discountDAO.getDiscountTypeById(discountTypeId);
                return new SimpleStringProperty(discountTypeName != null ? discountTypeName : "No Discount");
            } catch (SQLException e) {
                return new SimpleStringProperty("Error fetching discount type");
            }
        });
        return discountTypeCol;
    }

    private List<ProductsInTransact> getProductsInTransactForBranch(PurchaseOrder purchaseOrder, int branchId) throws SQLException {
        return orderProductDAO.getProductsInTransactForBranch(purchaseOrder, branchId);
    }

    public Map<String, Double> calculateTotalOfTable(TableView<ProductsInTransact> productsTable) {
        double total = productsTable.getItems().stream().mapToDouble(ProductsInTransact::getPaymentAmount).sum();
        double ewt = productsTable.getItems().stream().mapToDouble(ProductsInTransact::getWithholdingAmount).sum();
        double vat = productsTable.getItems().stream().mapToDouble(ProductsInTransact::getVatAmount).sum();
        double grossAmount = productsTable.getItems().stream().mapToDouble(ProductsInTransact::getGrossAmount).sum();
        double discountedAmount = productsTable.getItems().stream().mapToDouble(ProductsInTransact::getDiscountedAmount).sum();

        Map<String, Double> totals = new HashMap<>();
        totals.put("total", total);
        totals.put("ewt", ewt);
        totals.put("vat", vat);
        totals.put("grossAmount", grossAmount);
        totals.put("discountedAmount", discountedAmount);
        return totals;
    }


    private void approvePO(PurchaseOrder purchaseOrder, TableView<ProductsInTransact> productsTable, String tab) throws SQLException {
        CompletableFuture.runAsync(() -> {
            try {
                // Calculate total amounts
                double vatTotal = calculateTotalOfTable(productsTable).get("vat");
                double ewtTotal = calculateTotalOfTable(productsTable).get("ewt");
                double grossTotal = calculateTotalOfTable(productsTable).get("grossAmount");
                double discountedTotal = calculateTotalOfTable(productsTable).get("discountedAmount");
                double grandTotal = calculateTotalOfTable(productsTable).get("total");


                boolean approve = approvePurchaseOrder(purchaseOrder, vatTotal, ewtTotal, grandTotal, grossTotal, discountedTotal);
                boolean allUpdated = updateProducts(approve);

                // Handle the result of the approval process
                if (allUpdated) {
                    Platform.runLater(() -> showConfirmationAndPrintReceipt(purchaseOrder));
                } else {
                    Platform.runLater(this::showErrorMessage);
                }
            } catch (SQLException | IllegalStateException e) {
                e.printStackTrace();
                Platform.runLater(this::showErrorMessage);
            }
        }, executorService);
    }


    private boolean approvePurchaseOrder(PurchaseOrder purchaseOrder, double vatTotal, double ewtTotal, double grandTotal, double grossTotal, double discountedTotal) throws SQLException {
        return purchaseOrderDAO.approvePurchaseOrder(purchaseOrder, UserSession.getInstance().getUserId(), receiptCheckBox.isSelected(), vatTotal, ewtTotal, grandTotal, grossTotal, discountedTotal, LocalDateTime.now(), leadTimeReceivingDatePicker.getValue());
    }

    private boolean updateProducts(boolean approve) throws SQLException {
        boolean allUpdated = true;
        if (approve) {
            for (Tab tab : branchTabPane.getTabs()) {
                if (tab.getContent() instanceof TableView<?> tableView) {
                    allUpdated &= updateProductsInTab(tableView);
                } else {
                    System.out.println("Table content is not of type ProductsInTransact");
                }
            }
        }
        return allUpdated;
    }

    private boolean updateProductsInTab(TableView<?> tableView) throws SQLException {
        ObservableList<?> items = tableView.getItems();
        if (items.size() > 0 && items.get(0) instanceof ProductsInTransact) {
            TableView<ProductsInTransact> table = (TableView<ProductsInTransact>) tableView;
            ObservableList<ProductsInTransact> products = table.getItems();

            for (ProductsInTransact product : products) {
                int quantity = product.getOrderedQuantity();
                double vatAmount = product.getVatAmount();
                double ewtAmount = product.getWithholdingAmount();
                double totalAmount = product.getPaymentAmount();
                boolean updatedQuantity = orderProductDAO.quantityOverride(product.getOrderProductId(), quantity);
                boolean updatedApproval = orderProductDAO.approvePurchaseOrderProduct(product.getOrderProductId(), vatAmount, ewtAmount, totalAmount);
                if (!updatedQuantity || !updatedApproval) {
                    return false;
                }
            }
        }
        return true;
    }

    private void showConfirmationAndPrintReceipt(PurchaseOrder purchaseOrder) {
        DialogUtils.showConfirmationDialog("Approved", "Purchase No" + purchaseOrder.getPurchaseOrderNo() + " has been approved");
        purchaseOrderConfirmationController.refreshData();
        openPrintStage(purchaseOrder);
    }

    private void openPrintStage(PurchaseOrder purchaseOrder) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PurchaseOrderReceiptPrintables.fxml"));
            Parent root = fxmlLoader.load();
            PurchaseOrderReceiptPrintablesController controller = fxmlLoader.getController();
            controller.printApprovedPO(purchaseOrder.getPurchaseOrderNo());
            Stage stage = new Stage();
            stage.setTitle("Print PO " + purchaseOrder.getPurchaseOrderNo());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Unable to open sales order.");
            e.printStackTrace();
        }
    }

    private void showErrorMessage() {
        DialogUtils.showErrorMessage("Error", "Error in approving this PO, please contact your I.T department.");
    }
}
