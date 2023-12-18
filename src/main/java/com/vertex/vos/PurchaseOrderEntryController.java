package com.vertex.vos;

import com.vertex.vos.Constructors.*;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PurchaseOrderEntryController implements Initializable {

    private PurchaseOrderConfirmationController purchaseOrderConfirmationController;
    private AnchorPane contentPane; // Declare contentPane variable

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    public void setPurchaseOrderConfirmationController(PurchaseOrderConfirmationController controller) {
        this.purchaseOrderConfirmationController = controller;
    }

    ErrorUtilities errorUtilities = new ErrorUtilities();

    PurchaseOrderDAO purchaseOrderDAO = new PurchaseOrderDAO();
    private final BranchDAO branchDAO = new BranchDAO();
    DiscountDAO discountDAO = new DiscountDAO();

    ProductDAO productDAO = new ProductDAO();

    private final PurchaseOrderDAO orderDAO = new PurchaseOrderDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    PaymentTermsDAO paymentTermsDAO = new PaymentTermsDAO();
    DeliveryTermsDAO deliveryTermsDAO = new DeliveryTermsDAO();
    TransactionTypeDAO transactionTypeDAO = new TransactionTypeDAO();
    PurchaseOrderProductDAO orderProductDAO = new PurchaseOrderProductDAO();
    StatusDAO statusDAO = new StatusDAO();
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
    private ComboBox branch;
    @FXML
    private ComboBox supplier;
    @FXML
    private Button confirmButton;
    private String type;
    private double vatValue;
    private double withholdingValue;
    private double price;
    private double grandTotals = 0.0; // Variable to store the grand total
    private double vatTotals = 0.0;
    private double withholdingTotals = 0.0;
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
    private final PurchaseOrderNumberDAO orderNumberDAO = new PurchaseOrderNumberDAO();

    private int po_number = 0;

    public void setPurchaseOrderType(String type) {
        this.type = type;
    }

    private final HistoryManager historyManager = new HistoryManager();
    private int currentNavigationId = -1;
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    private final ObservableList<ProductsInTransact> productsList = FXCollections.observableArrayList();
    TableView<ProductsInTransact> productsAddedTable = new TableView<>();
    private final List<Branch> branches = new ArrayList<>(); // Declare your branch list

    String cssPath = getClass().getResource("/com/vertex/vos/assets/table.css").toExternalForm();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        productsAddedTable.getStylesheets().add(cssPath);
        productsAddedTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        summaryTable.getStylesheets().add(cssPath);
        summaryTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

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

        addProductButton.setOnMouseClicked(mouseEvent -> addProductToTables());
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

    public void addProductToTables() {
        int supplierId = getSupplierId();

        if (supplierId > 0) {
            openProductStage(supplierId);
        } else {
            DialogUtils.showErrorMessage("No supplier selected", "Supplier ID is empty or invalid.");
        }
    }

    private int getSupplierId() {
        SupplierDAO supplierDAO = new SupplierDAO();
        ProductsPerSupplierDAO productsPerSupplierDAO = new ProductsPerSupplierDAO();
        return supplierDAO.getSupplierIdByName((String) supplier.getSelectionModel().getSelectedItem());
    }

    private void openProductStage(int supplierId) {
        if (productStage == null || !productStage.isShowing()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("tableManager.fxml"));
                Parent content = loader.load();

                TableManagerController controller = loader.getController();
                controller.setRegistrationType("purchase_order_products");
                controller.loadSupplierProductsTable(supplierId, productsList);
                controller.setPurchaseOrderEntryController(this);

                productStage = new Stage();
                productStage.setTitle("Add product for PO " + po_number);
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

    private void comboBoxBehaviour() {
        TextFieldUtils.setComboBoxBehavior(supplier);
    }

    private void encoderUI(String type) {
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
        populateSupplierNames(type);
        receiptCheckBox.setVisible(false);
        totalBox.getChildren().remove(totalBoxLabels);
        confirmButton.setOnMouseClicked(mouseEvent -> {
            try {
                entryPO();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void entryPO() throws SQLException {
        ConfirmationAlert confirm = new ConfirmationAlert("New PO Request", "PO NUMBER" + po_number, "Ensure entry is correct.");
        boolean userConfirmed = confirm.showAndWait();
        if (userConfirmed) {
            int supplierId = supplierDAO.getSupplierIdByName(String.valueOf(supplier.getSelectionModel().getSelectedItem()));
            int receivingTypeId = deliveryTermsDAO.getDeliveryTermIdByName(receivingTerms.getText());
            int paymentTypeId = paymentTermsDAO.getPaymentTermIdByName(paymentTerms.getText());
            int transactionTypeId = 0;
            if (type.equals("trade")) {
                transactionTypeId = 1;

            } else if (type.equals("non-trade")) {
                transactionTypeId = 2;
            }
            int status = 1;
            PurchaseOrder purchaseOrder = new PurchaseOrder();
            purchaseOrder.setPurchaseOrderNo(po_number);
            purchaseOrder.setSupplierName(supplierId);
            purchaseOrder.setReceivingType(receivingTypeId);
            purchaseOrder.setPaymentType(paymentTypeId);
            purchaseOrder.setPriceType("Cost Per Unit");
            purchaseOrder.setDateEncoded(LocalDateTime.now());
            purchaseOrder.setDate(LocalDate.now());
            purchaseOrder.setTime(LocalTime.now());
            purchaseOrder.setDatetime(LocalDateTime.now());
            purchaseOrder.setEncoderId(UserSession.getInstance().getUserId());
            purchaseOrder.setTransactionType(transactionTypeId);
            purchaseOrder.setStatus(status);

            boolean headerRegistered = orderDAO.entryPurchaseOrder(purchaseOrder);
            if (headerRegistered) {
                entryPODetails();
            } else {
                DialogUtils.showErrorMessage("Error", "Error in requesting purchase order");
            }
        }
    }

    private void entryPODetails() throws SQLException {
        boolean allProductsEntered = true; // Flag to track all products

        for (ProductsInTransact product : productsAddedTable.getItems()) {
            ProductsInTransact productDetails = new ProductsInTransact();
            productDetails.setPurchaseOrderId(po_number);
            String productDescription = product.getDescription();
            int productId = product.getProductId();
            productDetails.setProductId(productId);

            boolean productEntered = false; // Flag to track each product

            for (Branch branch : branches) {
                int branchId = branch.getId();
                int branchQuantity = product.getBranchQuantity(branch);
                productDetails.setOrderedQuantity(branchQuantity);
                productDetails.setUnitPrice(product.getUnitPrice());
                productDetails.setBranchId(branchId);

                if (branchQuantity > 0) {
                    boolean productsEntried = orderProductDAO.entryProductPerPO(productDetails);
                    if (productsEntried) {
                        productEntered = true; // Set product flag to true if entered
                    } else {
                        allProductsEntered = false; // Set all products flag to false if any product fails
                        break; // Stop processing this product if there's an error
                    }
                }
            }

            if (!productEntered) {
                allProductsEntered = false;
                break;
            }
        }

        if (allProductsEntered) {
            DialogUtils.showConfirmationDialog("Success", "Your PO Request is now Pending");
            refreshEntry(type);
        } else {
            DialogUtils.showErrorMessage("Error", "Error in requesting PO for all products");
        }
    }


    private void refreshEntry(String type) {
        productsList.clear();
        branches.clear();
        branchTabPane.getTabs().clear();
        supplier.setDisable(false);
        productsAddedTable.getColumns().clear();
        encoderUI(type);
    }


    private void setEncoderUI(Tab encoderTab) {
        encoderTab.setContent(productsAddedTable);
        productsAddedTable.setEditable(true);
        TableColumn<ProductsInTransact, String> descriptionColumn = new TableColumn<>("Product Description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setPrefWidth(150);
        descriptionColumn.setMaxWidth(200);

        TableColumn<ProductsInTransact, String> unitColumn = new TableColumn<>("Unit");
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
        unitColumn.setPrefWidth(140);
        unitColumn.setMaxWidth(100);
        productsAddedTable.getColumns().addAll(descriptionColumn, unitColumn);
        productsAddedTable.setItems(productsList);
    }


    private void initializeTaxes() {
        String query = "SELECT WithholdingRate, VATRate FROM tax_rates WHERE TaxID = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            int taxId = 1;

            preparedStatement.setInt(1, taxId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                withholdingValue = resultSet.getDouble("WithholdingRate");
                vatValue = resultSet.getDouble("VATRate");
            } else {
                // Handle the case where the specified TaxID was not found
                System.out.println("Tax rates not found for TaxID: " + taxId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
    }

    private void populateSupplierNames(String type) {
        type = type.toUpperCase();
        supplier.setDisable(false);
        // SQL query to fetch supplier names from the suppliers table based on type
        String sqlQuery = "SELECT supplier_name FROM suppliers WHERE supplier_type = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, type); // Set the type parameter in the query

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                ObservableList<String> supplierNames = FXCollections.observableArrayList();

                // Iterate through the result set and add supplier names to the ObservableList
                while (resultSet.next()) {
                    String supplierName = resultSet.getString("supplier_name");
                    supplierNames.add(supplierName);
                }
                // Set the ObservableList as the items for the supplier ComboBox
                supplier.setItems(supplierNames);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        supplier.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // Check if a new supplier is selected and perform actions based on the selection
            if (newValue != null) {
                populateSupplierDetails(newValue.toString());
            }
        });
    }

    private void populateSupplierDetails(String supplierName) {
        supplier.setDisable(false);
        String sqlQuery = "SELECT payment_terms, delivery_terms FROM suppliers WHERE supplier_name = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

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

    void addProductToBranchTables(int productId) {
        Product product = productDAO.getProductDetails(productId);
        addProductToTable(product);
    }


    private void addProductToTable(Product product) {
        // Check if the product already exists in the list based on its ID
        boolean productExists = productsList.stream()
                .anyMatch(existingProduct -> existingProduct.getProductId() == product.getProductId());

        if (!productExists) {
            // If the product doesn't exist in the list, add it
            ProductsInTransact newProduct = new ProductsInTransact();
            newProduct.setProductId(product.getProductId());
            newProduct.setDescription(product.getDescription());
            newProduct.setUnit(product.getUnitOfMeasurementString());
            newProduct.setUnitPrice(product.getCostPerUnit());
            newProduct.setUnitPrice(product.getPriceA());
            productsList.add(newProduct);

            supplier.setDisable(true);
            productsAddedTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        } else {
            // Show an error message or handle the duplicate product scenario here
            DialogUtils.showErrorMessage("Error", "This product already exists in the list.");
        }
    }


    private TableColumn<ProductsInTransact, Integer>[] branchColumns; // Declare branchColumns as a class-level variable

    @SuppressWarnings("unchecked")
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
            product.setBranchQuantity(branches.get(columnIndex), newValue); // Updating quantity for the branch
        });
    }

    public void addBranchToTable(int branchId) {
        Branch branchSelected = branchDAO.getBranchById(branchId);
        if (branchSelected != null) {
            if (!branches.stream().anyMatch(existingBranch -> existingBranch.getId() == branchSelected.getId())) {
                branches.add(branchSelected);
                int numberOfBranches = branches.size();
                if (branchColumns == null) {
                    initializeBranchColumns(numberOfBranches); // If columns are not initialized
                } else if (numberOfBranches > branchColumns.length) {
                    TableColumn<ProductsInTransact, Integer> newColumn = getProductsInTransactIntegerTableColumn(branchSelected, numberOfBranches);
                    setupBranchColumnEditHandler(newColumn, numberOfBranches - 1); // Set up event handler for the new column
                    branchColumns = Arrays.copyOf(branchColumns, branchColumns.length + 1);
                    branchColumns[branchColumns.length - 1] = newColumn;
                    productsAddedTable.getColumns().add(newColumn);
                    productsAddedTable.refresh();
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

    void setUIPerStatus(int poNumber) throws SQLException {
        PurchaseOrder purchaseOrder = purchaseOrderDAO.getPurchaseOrderByOrderNo(poNumber);
        if (purchaseOrder != null) {
            fixedValues();
            LocalDateTime dateTime = purchaseOrder.getDateEncoded();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDate = dateTime.format(formatter);

            purchaseOrderNo.setText("PURCHASE ORDER NO " + poNumber);
            date.setText(formattedDate);
            statusLabel.setText(purchaseOrder.getStatusString());
            supplier.setValue(purchaseOrder.getSupplierNameString());
            POBox.getChildren().remove(addBoxes);
            int po_status = purchaseOrder.getStatus();

            Platform.runLater(() -> {
                List<Tab> tabs = null;
                try {
                    tabs = createBranchTabs(purchaseOrder);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                switch (po_status) {
                    case 1:
                        try {
                            loadPOForVerification(purchaseOrder, tabs);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case 2:
                        try {
                            loadPOForApproval(purchaseOrder, tabs);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case 3:
                        loadPOForBudgeting(purchaseOrder);
                        break;
                    case 4:
                        loadPOForVouchering(purchaseOrder);
                        break;
                    case 5:
                        loadPOForReceiving(purchaseOrder);
                        break;
                    case 6:
                        loadPOForDone(purchaseOrder);
                        break;
                    case 7:
                        loadPOForRestoringPO(purchaseOrder);
                        break;
                    default:
                        break;
                }
            });
        }

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
    }

    private void loadPOForVerification(PurchaseOrder purchaseOrder, List<Tab> tabs) throws SQLException {
        branchTabPane.getTabs().addAll(tabs);
        confirmButton.setText("VERIFY");

        confirmButton.setOnMouseClicked(event -> {
            try {
                verifyPO(purchaseOrder.getPurchaseOrderNo());
                printGrandTotalOfAllTabs(branchTabPane.getTabs());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void loadPOForApproval(PurchaseOrder purchaseOrder, List<Tab> tabs) throws SQLException {
        branchTabPane.getTabs().addAll(tabs);
        Tab quantitySummaryTab = new Tab("Quantity Summary");
        Node quantitySummaryContent = createSummaryContent(tabs);
        quantitySummaryTab.setContent(quantitySummaryContent);
        branchTabPane.getTabs().addFirst(quantitySummaryTab);
        confirmButton.setText("APPROVE");
        confirmButton.setOnMouseClicked(event -> {
            try {
                approvePO(purchaseOrder.getPurchaseOrderNo(), tabs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            printGrandTotalOfAllTabs(tabs);
        });
    }

    TableView<Map.Entry<String, Map<String, Integer>>> summaryTable = new TableView<>();

    private void refreshSummaryTable(List<Tab> branchTabs) {
        summaryTable.getColumns().clear();
        TableColumn<Map.Entry<String, Map<String, Integer>>, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getKey()));
        List<TableColumn<Map.Entry<String, Map<String, Integer>>, Integer>> branchQuantityCols = new ArrayList<>();
        Map<String, Map<String, Integer>> descriptionBranchQuantityMap = new HashMap<>();
        for (Tab tab : branchTabs) {
            if (tab.getContent() instanceof TableView) {
                TableView<ProductsInTransact> productsTable = (TableView<ProductsInTransact>) tab.getContent();

                for (ProductsInTransact product : productsTable.getItems()) {
                    String description = product.getDescription();
                    String branch = tab.getText();
                    int quantity = product.getOrderedQuantity();

                    descriptionBranchQuantityMap
                            .computeIfAbsent(description, k -> new HashMap<>())
                            .merge(branch, quantity, Integer::sum);
                }
            }
        }
        TableColumn<Map.Entry<String, Map<String, Integer>>, Integer> totalQuantityCol = new TableColumn<>("Total Quantity");
        totalQuantityCol.setCellValueFactory(data -> {
            Map<String, Integer> branchQuantityMap = data.getValue().getValue();
            int totalQuantity = branchQuantityMap.values().stream().mapToInt(Integer::intValue).sum();
            return new SimpleIntegerProperty(totalQuantity).asObject();
        });

        Set<String> uniqueBranches = getUniqueBranches(branchTabs);
        for (String branch : uniqueBranches) {
            TableColumn<Map.Entry<String, Map<String, Integer>>, Integer> branchQuantityCol = new TableColumn<>(branch);
            branchQuantityCol.setCellValueFactory(data -> {
                Map<String, Integer> branchQuantityMap = data.getValue().getValue();
                return new SimpleIntegerProperty(branchQuantityMap.getOrDefault(branch, 0)).asObject();
            });
            branchQuantityCols.add(branchQuantityCol);
        }
        TableColumn<Map.Entry<String, Map<String, Integer>>, String> productBrandCol = new TableColumn<>("Brand");
        productBrandCol.setCellValueFactory(data -> {
            String description = data.getValue().getKey();
            ProductSEO productSEO = productDAO.getProductSEOByDescription(description);
            return new SimpleStringProperty(productSEO.getProductBrand());
        });
        TableColumn<Map.Entry<String, Map<String, Integer>>, String> productCategoryCol = new TableColumn<>("Category");
        productCategoryCol.setCellValueFactory(data -> {
            String description = data.getValue().getKey();
            ProductSEO productSEO = productDAO.getProductSEOByDescription(description);
            return new SimpleStringProperty(productSEO.getProductCategory());
        });
        TableColumn<Map.Entry<String, Map<String, Integer>>, String> productSegmentCol = new TableColumn<>("Segment");
        productSegmentCol.setCellValueFactory(data -> {
            String description = data.getValue().getKey();
            // Assuming you have a method to retrieve product segment from description
            ProductSEO productSEO = productDAO.getProductSEOByDescription(description);
            return new SimpleStringProperty(productSEO.getProductSegment());
        });
        summaryTable.getColumns().addAll(productBrandCol, productCategoryCol, productSegmentCol, descriptionCol, totalQuantityCol);
        summaryTable.getColumns().addAll(branchQuantityCols);
        summaryTable.getItems().clear();
        summaryTable.getItems().addAll(descriptionBranchQuantityMap.entrySet());
    }

    private Node createSummaryContent(List<Tab> tabs) {
        TableColumn<Map.Entry<String, Map<String, Integer>>, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getKey()));
        Map<String, Map<String, Integer>> descriptionBranchQuantityMap = new HashMap<>();

        TableColumn<Map.Entry<String, Map<String, Integer>>, Integer> totalQuantityCol = new TableColumn<>("Total Quantity");
        totalQuantityCol.setCellValueFactory(data -> {
            Map<String, Integer> branchQuantityMap = data.getValue().getValue();
            int totalQuantity = branchQuantityMap.values().stream().mapToInt(Integer::intValue).sum();
            return new SimpleIntegerProperty(totalQuantity).asObject();
        });

        TableColumn<Map.Entry<String, Map<String, Integer>>, String> productBrandCol = new TableColumn<>("Brand");
        productBrandCol.setCellValueFactory(data -> {
            String description = data.getValue().getKey();
            ProductSEO productSEO = productDAO.getProductSEOByDescription(description);
            return new SimpleStringProperty(productSEO.getProductBrand());
        });

        TableColumn<Map.Entry<String, Map<String, Integer>>, String> productCategoryCol = new TableColumn<>("Category");
        productCategoryCol.setCellValueFactory(data -> {
            String description = data.getValue().getKey();
            ProductSEO productSEO = productDAO.getProductSEOByDescription(description);
            return new SimpleStringProperty(productSEO.getProductCategory());
        });

        TableColumn<Map.Entry<String, Map<String, Integer>>, String> productClassCol = new TableColumn<>("Class");
        productClassCol.setCellValueFactory(data -> {
            String description = data.getValue().getKey();
            // Assuming you have a method to retrieve product class from description
            ProductSEO productSEO = productDAO.getProductSEOByDescription(description);
            return new SimpleStringProperty(productSEO.getProductClass());
        });
        TableColumn<Map.Entry<String, Map<String, Integer>>, String> productSegmentCol = new TableColumn<>("Segment");
        productSegmentCol.setCellValueFactory(data -> {
            String description = data.getValue().getKey();
            // Assuming you have a method to retrieve product segment from description
            ProductSEO productSEO = productDAO.getProductSEOByDescription(description);
            return new SimpleStringProperty(productSEO.getProductSegment());
        });
        TableColumn<Map.Entry<String, Map<String, Integer>>, String> productNatureCol = new TableColumn<>("Nature");
        productNatureCol.setCellValueFactory(data -> {
            String description = data.getValue().getKey();
            // Assuming you have a method to retrieve product nature from description
            ProductSEO productSEO = productDAO.getProductSEOByDescription(description);
            return new SimpleStringProperty(productSEO.getProductNature());
        });
        TableColumn<Map.Entry<String, Map<String, Integer>>, String> productSectionCol = new TableColumn<>("Section");
        productSectionCol.setCellValueFactory(data -> {
            String description = data.getValue().getKey();
            // Assuming you have a method to retrieve product section from description
            ProductSEO productSEO = productDAO.getProductSEOByDescription(description);
            return new SimpleStringProperty(productSEO.getProductSection());
        });
        for (Tab tab : tabs) {
            if (tab.getContent() instanceof TableView) {
                TableView<ProductsInTransact> productsTable = (TableView<ProductsInTransact>) tab.getContent();
                for (ProductsInTransact product : productsTable.getItems()) {
                    String description = product.getDescription();
                    String branch = tab.getText(); // Use the tab's text as the branch name
                    int quantity = product.getOrderedQuantity();

                    if (!descriptionBranchQuantityMap.containsKey(description)) {
                        descriptionBranchQuantityMap.put(description, new HashMap<>());
                    }

                    Map<String, Integer> branchQuantityMap = descriptionBranchQuantityMap.get(description);
                    branchQuantityMap.put(branch, branchQuantityMap.getOrDefault(branch, 0) + quantity);
                }
            }
        }

        summaryTable.getColumns().addAll(productBrandCol, productCategoryCol, productSegmentCol, descriptionCol, totalQuantityCol);
        Set<String> uniqueBranches = getUniqueBranches(tabs);
        for (String branch : uniqueBranches) {
            TableColumn<Map.Entry<String, Map<String, Integer>>, Integer> branchQuantityCol = new TableColumn<>(branch);
            branchQuantityCol.setCellValueFactory(data -> {
                Map<String, Integer> branchQuantityMap = data.getValue().getValue();
                return new SimpleIntegerProperty(branchQuantityMap.getOrDefault(branch, 0)).asObject();
            });
            summaryTable.getColumns().add(branchQuantityCol);
        }
        summaryTable.getItems().clear();
        // Add data to table
        for (Map.Entry<String, Map<String, Integer>> entry : descriptionBranchQuantityMap.entrySet()) {
            summaryTable.getItems().add(entry);
        }
        return summaryTable;
    }

    private Set<String> getUniqueBranches(List<Tab> tabs) {
        Set<String> uniqueBranches = new HashSet<>();
        for (Tab tab : tabs) {
            uniqueBranches.add(tab.getText());
        }
        return uniqueBranches;
    }
    private List<Tab> createBranchTabs(PurchaseOrder purchaseOrder) throws SQLException {
        List<Branch> branches = purchaseOrderDAO.getBranchesForPurchaseOrder(purchaseOrder.getPurchaseOrderNo());
        List<Tab> branchTabs = new ArrayList<>();
        List<Node> tabContents = new ArrayList<>();

        for (Branch branch : branches) {
            Tab branchTab = new Tab(branch.getBranchName());
            Node content = createBranchContent(purchaseOrder, branch, receiptCheckBox, branchTabs);
            branchTab.setContent(content);
            branchTabs.add(branchTab);
            tabContents.add(content);
        }

        // Add all tabs outside the loop in a single operation
        branchTabPane.getTabs().addAll(branchTabs);

        // Process each content if needed
        for (Node content : tabContents) {
            // Process each content if needed
        }

        printGrandTotalOfAllTabs(branchTabs); // Calculate and print grand totals after all tabs are added

        return branchTabs;
    }


    private Node createBranchContent(PurchaseOrder purchaseOrder, Branch branch, CheckBox receiptCheckBox, List<Tab> branchTabs) throws SQLException {
        int status = purchaseOrder.getStatus();
        boolean isReceiptRequired = purchaseOrder.getReceiptRequired();
        receiptCheckBox.setSelected(isReceiptRequired);
        if (status == 1 || status == 2) {
            TableView<ProductsInTransact> productsTable = createProductsTable(status, receiptCheckBox, branchTabs);
            populateProductsInTransactTablesPerTab(productsTable, purchaseOrder, branch);
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            Runnable task = () -> Platform.runLater(() -> printGrandTotalOfAllTabs(branchTabs));

            productsTable.getItems().addListener((ListChangeListener<ProductsInTransact>) change -> {
                while (change.next()) {
                    if (change.wasAdded() || change.wasRemoved() || change.wasUpdated()) {
                        executorService.schedule(task, 50, TimeUnit.MILLISECONDS);
                        if (status == 2) {
                            Platform.runLater(() -> refreshSummaryTable(branchTabs));
                        }
                    }
                }
            });
            productsTable.getColumns().addListener((ListChangeListener<TableColumn<ProductsInTransact, ?>>) change -> {
                while (change.next()) {
                    if (change.wasAdded() || change.wasRemoved()) {
                        executorService.schedule(task, 100, TimeUnit.MILLISECONDS); // Adjust the delay as needed
                        if (status == 2) {
                            Platform.runLater(() -> refreshSummaryTable(branchTabs));
                        }
                    }
                }
            });
            return productsTable;
        } else {
            return new Label("Content not available for this status.");
        }
    }

    private TableView<ProductsInTransact> createProductsTable(int status, CheckBox receiptCheckBox, List<Tab> branchTabs) {
        initializeTaxes();
        TableView<ProductsInTransact> productsTable = new TableView<>();

        productsTable.getStylesheets().add(cssPath);
        productsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        productsTable.setEditable(true);

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

        TableColumn<ProductsInTransact, Double> productPricePerUnitOverrideCol = priceControl(status, productsTable);

        TableColumn<ProductsInTransact, Integer> productQuantityPerBranch = quantityControl(status, productsTable);

        TableColumn<ProductsInTransact, Double> totalGrossAmountCol = getTotalGrossAmountCol(status);

        TableColumn<ProductsInTransact, String> discountTypeCol = getDiscountTypePerProduct();

        TableColumn<ProductsInTransact, Double> discountValueCol = getDiscountValueColumn();

        TableColumn<ProductsInTransact, Double> discountedTotalCol = getDiscountedTotalColumn(discountValueCol, totalGrossAmountCol);

        TableColumn<ProductsInTransact, Double> vatAmountCol = getVatAmountCol(discountedTotalCol);

        TableColumn<ProductsInTransact, Double> withholdingAmountCol = getWithholdingAmountCol(discountedTotalCol);

        TableColumn<ProductsInTransact, Double> totalNetAmountCol = getTotalNetAmountcol(discountedTotalCol, vatAmountCol, withholdingAmountCol, this.receiptCheckBox);
        productsTable.getColumns().clear();

        receiptCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            productsTable.getColumns().clear(); // Clear existing columns

            if (newValue) {
                // Add columns related to withholding and VAT when receiptCheckBox is selected
                productsTable.getColumns().addAll(
                        productDescriptionCol, productUnitCol, productPricePerUnitCol, productPricePerUnitOverrideCol, productQuantityPerBranch,
                        totalGrossAmountCol, discountTypeCol, discountValueCol, discountedTotalCol,
                        vatAmountCol, withholdingAmountCol, totalNetAmountCol
                );
            } else {
                // Exclude withholding and VAT columns when receiptCheckBox is not selected
                productsTable.getColumns().addAll(
                        productDescriptionCol, productUnitCol, productPricePerUnitCol, productPricePerUnitOverrideCol, productQuantityPerBranch,
                        totalGrossAmountCol, discountTypeCol, discountValueCol, discountedTotalCol, totalNetAmountCol
                );
            }
        });
        // Set initial columns based on the initial state of receiptCheckBox
        if (receiptCheckBox.isSelected()) {
            productsTable.getColumns().addAll(
                    productDescriptionCol, productUnitCol, productPricePerUnitCol, productPricePerUnitOverrideCol, productQuantityPerBranch,
                    totalGrossAmountCol, discountTypeCol, discountValueCol, discountedTotalCol,
                    vatAmountCol, withholdingAmountCol, totalNetAmountCol
            );
        } else {
            productsTable.getColumns().addAll(
                    productDescriptionCol, productUnitCol, productPricePerUnitCol, productPricePerUnitOverrideCol, productQuantityPerBranch,
                    totalGrossAmountCol, discountTypeCol, discountValueCol, discountedTotalCol, totalNetAmountCol
            );
        }
        return productsTable;
    }

    private static TableColumn<ProductsInTransact, Double> getPricePerUnitCol(int status) {
        TableColumn<ProductsInTransact, Double> productPricePerUnitCol = new TableColumn<>();

        if (status == 1) {
            productPricePerUnitCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
            productPricePerUnitCol.setText("Price Per Unit");
        } else {
            productPricePerUnitCol.setCellValueFactory(new PropertyValueFactory<>("approvedPrice"));
            productPricePerUnitCol.setText("Approved Price");

            productPricePerUnitCol.setCellFactory(column -> new TableCell<ProductsInTransact, Double>() {
                private final Tooltip tooltip = new Tooltip();

                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                        setTooltip(null); // Clear any existing tooltip
                        setStyle(""); // Clear cell style if no item or empty
                    } else {
                        // Get the current row's ProductsInTransact object
                        ProductsInTransact product = getTableView().getItems().get(getIndex());

                        // Check if unitPrice and approvedPrice are different
                        if (product.getUnitPrice() != product.getApprovedPrice()) {
                            setText(String.valueOf(item));
                            tooltip.setText("Original Price: " + product.getUnitPrice());
                            if (isFocused() || isSelected()) {
                                setStyle("-fx-background-color: #5A90CFAA; -fx-background-radius: 5;");
                            } else {
                                setStyle("-fx-background-color: #5A90CF; -fx-background-radius: 5;");
                            }
                            setTooltip(tooltip);
                        } else {
                            setText(String.valueOf(item));
                            setStyle(""); // Clear cell style if unitPrice and approvedPrice are the same
                            setTooltip(null); // Clear the tooltip if not needed
                        }
                    }
                }
            });
        }
        return productPricePerUnitCol;
    }


    private static TableColumn<ProductsInTransact, Double> getTotalGrossAmountCol(int status) {
        TableColumn<ProductsInTransact, Double> totalGrossAmountCol = new TableColumn<>("Total Gross Amount");
        totalGrossAmountCol.setCellValueFactory(cellData -> {
            ProductsInTransact product = cellData.getValue();
            double pricePerUnit = 0;
            if (status == 1) {
                pricePerUnit = product.getUnitPrice();
                if (product.getUnitPrice() == 0) {
                    pricePerUnit = product.getOverridePrice();
                } else if (product.getOverridePrice() > 0) {
                    pricePerUnit = product.getOverridePrice();
                    product.setApprovedPrice(product.getOverridePrice());
                }
            } else if (status == 2) {
                pricePerUnit = product.getApprovedPrice();
            }
            int quantity = product.getOrderedQuantity();
            double totalGrossAmount = pricePerUnit * quantity;
            product.setGrossAmount(totalGrossAmount);
            return new SimpleDoubleProperty(totalGrossAmount).asObject();
        });
        return totalGrossAmountCol;
    }


    private TableColumn<ProductsInTransact, Double> getTotalNetAmountcol(
            TableColumn<ProductsInTransact, Double> discountedTotalCol,
            TableColumn<ProductsInTransact, Double> vatAmountCol,
            TableColumn<ProductsInTransact, Double> withholdingAmountCol,
            CheckBox receiptCheckBox) {

        TableColumn<ProductsInTransact, Double> totalAmountCol = new TableColumn<>("Net Amount");
        totalAmountCol.setCellValueFactory(cellData -> {
            ProductsInTransact product = cellData.getValue();

            double netAmount = discountedTotalCol.getCellObservableValue(product).getValue();

            if (receiptCheckBox.isSelected()) {
                double vatAmount = vatAmountCol.getCellObservableValue(product).getValue();
                double withholdingAmount = withholdingAmountCol.getCellObservableValue(product).getValue();
                netAmount += vatAmount - withholdingAmount;
            }

            product.setNetAmount(netAmount);

            netAmount = Double.parseDouble(String.format("%.2f", netAmount));
            return new SimpleDoubleProperty(netAmount).asObject();
        });

        return totalAmountCol;
    }


    private TableColumn<ProductsInTransact, Integer> quantityControl(int status, TableView<ProductsInTransact> productsTable) {
        TableColumn<ProductsInTransact, Integer> productQuantityPerBranch = new TableColumn<>("Quantity");
        productQuantityPerBranch.setCellValueFactory(cellData -> {
            int quantity = cellData.getValue().getOrderedQuantity();
            return new SimpleIntegerProperty(quantity).asObject();
        });

        if (status == 2) {
            productQuantityPerBranch.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
            productQuantityPerBranch.setOnEditCommit(event -> {
                try {
                    TableView.TableViewSelectionModel<ProductsInTransact> selectionModel = productsTable.getSelectionModel();
                    ProductsInTransact product = selectionModel.getSelectedItem();
                    product.setOrderedQuantity(event.getNewValue());
                    int selectedIndex = productsTable.getSelectionModel().getSelectedIndex();
                    productsTable.getItems().set(selectedIndex, product);
                } catch (NumberFormatException | NullPointerException e) {
                    // Handle invalid input or null selection
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
            double withholdingAmount = totalDiscountedAmount * withholdingValue;

            product.setWithholdingAmount(withholdingAmount);

            withholdingAmount = Double.parseDouble(String.format("%.2f", withholdingAmount));

            return new SimpleDoubleProperty(withholdingAmount).asObject();
        });
        withholdingAmountCol.setMinWidth(50);
        return withholdingAmountCol;
    }


    private TableColumn<ProductsInTransact, Double> getVatAmountCol(TableColumn<ProductsInTransact, Double> discountedTotalCol) {
        TableColumn<ProductsInTransact, Double> vatAmountCol = new TableColumn<>("VAT");
        vatAmountCol.setCellValueFactory(cellData -> {
            ProductsInTransact product = cellData.getValue();

            double totalDiscountedAmount = discountedTotalCol.getCellObservableValue(product).getValue();

            double vatAmount = totalDiscountedAmount * vatValue;

            product.setVatAmount(vatAmount);

            vatAmount = Double.parseDouble(String.format("%.2f", vatAmount));

            return new SimpleDoubleProperty(vatAmount).asObject();
        });

        vatAmountCol.setMinWidth(50);
        return vatAmountCol;
    }

    private static TableColumn<ProductsInTransact, Double> getDiscountedTotalColumn(TableColumn<ProductsInTransact, Double> discountValueCol, TableColumn<ProductsInTransact, Double> totalGrossAmountCol) {
        TableColumn<ProductsInTransact, Double> discountedTotalCol = new TableColumn<>("Total Discounted Amount");
        discountedTotalCol.setCellValueFactory(cellData -> {
            ProductsInTransact product = cellData.getValue();

            double discountPercentage = discountValueCol.getCellObservableValue(product).getValue();

            double totalGrossAmount = totalGrossAmountCol.getCellObservableValue(product).getValue();

            double discountValue = (totalGrossAmount * discountPercentage) / 100.0;

            double totalDiscountedAmount = totalGrossAmount - discountValue;

            totalDiscountedAmount = Double.parseDouble(String.format("%.2f", totalDiscountedAmount));

            product.setDiscountedAmount(totalDiscountedAmount);
            return new SimpleDoubleProperty(totalDiscountedAmount).asObject();
        });
        return discountedTotalCol;
    }

    private TableColumn<ProductsInTransact, Double> getDiscountValueColumn() {
        TableColumn<ProductsInTransact, Double> discountValueCol = new TableColumn<>("Discount Value");
        discountValueCol.setCellValueFactory(cellData -> {
            ProductsInTransact product = cellData.getValue();
            int discountTypeId = product.getDiscountTypeId();
            try {
                BigDecimal totalPercentage = discountDAO.getSumOfPercentagesByType(discountTypeId);

                double discountValue = (totalPercentage != null) ? totalPercentage.doubleValue() : 0.0;

                return new SimpleDoubleProperty(discountValue).asObject();
            } catch (SQLException e) {
                e.printStackTrace();
                return new SimpleDoubleProperty(0).asObject();
            }
        });

        discountValueCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(String.format("%.2f%%", item)); // Formats the Double value with "%" suffix
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

    private TableColumn<ProductsInTransact, Double> priceControl(int status, TableView<ProductsInTransact> productsTable) {
        TableColumn<ProductsInTransact, Double> productPricePerUnitCol = new TableColumn<>("Price override");
        productPricePerUnitCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getOverridePrice()).asObject());
        if (status == 1) {
            productPricePerUnitCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
            productPricePerUnitCol.setOnEditCommit(event -> {
                ProductsInTransact product = event.getRowValue();
                product.setOverridePrice(event.getNewValue());
                int selectedIndex = productsTable.getSelectionModel().getSelectedIndex();
                productsTable.getItems().set(selectedIndex, product);
            });
        } else {
            productPricePerUnitCol.setCellFactory(col -> new TableCell<ProductsInTransact, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.valueOf(item)); // Or any formatting you need
                    }
                }
            });
        }
        if (status != 1) {
            productPricePerUnitCol.setVisible(false);
        }
        return productPricePerUnitCol;
    }

    private void populateProductsInTransactTablesPerTab(TableView<ProductsInTransact> productsTable, PurchaseOrder purchaseOrder, Branch branch) throws SQLException {
        productsTable.getItems().clear();
        List<ProductsInTransact> branchProducts = getProductsInTransactForBranch(purchaseOrder, branch.getId());
        productsTable.getItems().addAll(branchProducts);
    }

    private List<ProductsInTransact> getProductsInTransactForBranch(PurchaseOrder purchaseOrder, int branchId) throws SQLException {
        return orderProductDAO.getProductsInTransactForBranch(purchaseOrder, branchId);
    }

    private Map<String, Double> calculateTotalOfTable(TableView<ProductsInTransact> productsTable) {
        double total = 0.0;
        double ewt = 0.0;
        double vat = 0.0;
        double grossAmount = 0.0;
        double discountedAmount = 0.0;
        for (ProductsInTransact product : productsTable.getItems()) {
            total += product.getNetAmount();
            ewt += product.getWithholdingAmount();
            vat += product.getVatAmount();
            grossAmount += product.getGrossAmount(); // Assuming a method getGrossAmount() exists in ProductsInTransact class
            discountedAmount += product.getDiscountedAmount(); // Assuming a method getDiscountedAmount() exists in ProductsInTransact class
        }

        Map<String, Double> totals = new HashMap<>();
        totals.put("total", total);
        totals.put("ewt", ewt);
        totals.put("vat", vat);
        totals.put("grossAmount", grossAmount);
        totals.put("discountedAmount", discountedAmount);
        return totals;
    }


    public Map<String, Double> calculateGrandTotalOfAllTabs(List<Tab> branchTabs) {
        double grandTotal = 0.0;
        double ewtTotal = 0.0;
        double vatTotal = 0.0;
        double grossTotal = 0.0;
        double discountedTotal = 0.0;

        for (Tab branchTab : branchTabs) {
            Node content = branchTab.getContent();

            if (content instanceof TableView) {
                TableView<ProductsInTransact> productsTable = (TableView<ProductsInTransact>) content;
                Map<String, Double> totals = calculateTotalOfTable(productsTable);

                grandTotal += totals.get("total");
                ewtTotal += totals.get("ewt");
                vatTotal += totals.get("vat");
                grossTotal += totals.get("grossAmount"); // Include gross amount
                discountedTotal += totals.get("discountedAmount"); // Include discounted amount
            }
        }

        Map<String, Double> grandTotals = new HashMap<>();
        grandTotals.put("grandTotal", grandTotal);
        grandTotals.put("ewtTotal", ewtTotal);
        grandTotals.put("vatTotal", vatTotal);
        grandTotals.put("grossTotal", grossTotal); // Add gross total to the grand totals
        grandTotals.put("discountedTotal", discountedTotal); // Add discounted total to the grand totals
        return grandTotals;
    }

    public void printGrandTotalOfAllTabs(List<Tab> branchTabs) {
        boolean taxed = receiptCheckBox.isSelected();

        Map<String, Double> grandTotals = calculateGrandTotalOfAllTabs(branchTabs);
        double GRANT_TOTAL = grandTotals.get("grandTotal");
        double EWT_TOTAL = grandTotals.get("ewtTotal");
        double VAT_TOTAL = grandTotals.get("vatTotal");

        grandTotal.setText("Grand Total: " + String.format("%.2f", GRANT_TOTAL));
        withholding.setText("EWT Total: " + String.format("%.2f", EWT_TOTAL));
        vat.setText("VAT Total: " + String.format("%.2f", VAT_TOTAL));

        totalBoxLabels.getChildren().removeAll(vat, withholding, grandTotal);

        if (taxed) {
            totalBoxLabels.getChildren().addAll(vat, withholding, grandTotal);
        } else {
            totalBoxLabels.getChildren().add(grandTotal);
        }
    }

    private void approvePO(int purchaseOrderNo, List<Tab> tabs) throws SQLException {
        Map<String, Double> grandTotals = calculateGrandTotalOfAllTabs(tabs);
        double grandTotal = grandTotals.get("grandTotal");
        double ewtTotal = grandTotals.get("ewtTotal");
        double vatTotal = grandTotals.get("vatTotal");
        double grossTotal = grandTotals.get("grossTotal");
        double discountedTotal = grandTotals.get("discountedTotal");
        boolean approve = purchaseOrderDAO.approvePurchaseOrder(
                purchaseOrderNo,
                UserSession.getInstance().getUserId(),
                receiptCheckBox.isSelected(),
                vatTotal,
                ewtTotal,
                grandTotal,
                grossTotal,
                discountedTotal,
                LocalDateTime.now()
        );
        boolean allUpdated = true;
        if (approve) for (Tab tab : branchTabPane.getTabs()) {
            if (tab.getContent() instanceof TableView) {
                TableView<?> tableView = (TableView<?>) tab.getContent();
                ObservableList<?> items = tableView.getItems();
                if (items.size() > 0 && items.get(0) instanceof ProductsInTransact) {
                    TableView<ProductsInTransact> table = (TableView<ProductsInTransact>) tableView;
                    ObservableList<ProductsInTransact> products = table.getItems();

                    for (ProductsInTransact product : products) {
                        // Your logic here for ProductsInTransact
                        int quantity = product.getOrderedQuantity();
                        double vatAmount = product.getVatAmount();
                        double ewtAmount = product.getWithholdingAmount();
                        double totalAmount = product.getNetAmount();

                        boolean updatedQuantity = orderProductDAO.quantityOverride(product.getPurchaseOrderProductId(), quantity);
                        boolean updatedApproval = orderProductDAO.approvePurchaseOrderProduct(product.getPurchaseOrderProductId(), vatAmount, ewtAmount, totalAmount);

                        if (!updatedQuantity || !updatedApproval) {
                            allUpdated = false;
                        }
                    }
                } else {
                    // Handle the case where items are not of type ProductsInTransact
                    System.out.println("Table content is not of type ProductsInTransact");
                    // Additional handling or error message if needed
                }
            }
        }

        if (allUpdated) {
            DialogUtils.showConfirmationDialog("Approved", "Purchase No" + purchaseOrderNo + " has been approved");
            purchaseOrderConfirmationController.refreshData();
            Stage stage = (Stage) branchTabPane.getScene().getWindow();
            stage.close();
        } else {
            DialogUtils.showErrorMessage("Error", "Error in approving this PO, please contact your I.T department.");
        }
    }
    private void verifyPO(int purchaseOrderNo) throws SQLException {
        boolean verified = purchaseOrderDAO.verifyPurchaseOrder(purchaseOrderNo, UserSession.getInstance().getUserId(), receiptCheckBox.isSelected());
        boolean allUpdated = true; // Flag to track if all updates were successful
        if (verified) {
            for (Tab tab : branchTabPane.getTabs()) {
                if (tab.getContent() instanceof TableView) {
                    TableView<ProductsInTransact> table = (TableView<ProductsInTransact>) tab.getContent();
                    ObservableList<ProductsInTransact> products = table.getItems();

                    for (ProductsInTransact product : products) {
                        double approvedPrice = 0.0;
                        if (product.getApprovedPrice() != 0) {
                            approvedPrice = product.getApprovedPrice();
                        } else {
                            approvedPrice = product.getUnitPrice();
                        }
                        boolean updated = orderProductDAO.updateApprovedPrice(product.getPurchaseOrderProductId(), approvedPrice);
                        if (!updated) {
                            allUpdated = false; // If any update fails, set the flag to false
                        }
                    }
                }
            }
            if (allUpdated) {
                DialogUtils.showConfirmationDialog("Verified", "Purchase No" + purchaseOrderNo + " has been verified");
                purchaseOrderConfirmationController.refreshData();
                Stage stage = (Stage) branchTabPane.getScene().getWindow();
                stage.close();
            } else {
                DialogUtils.showErrorMessage("Error", "Purchase No" + purchaseOrderNo + " has failed verification");
            }

        }
    }

}
