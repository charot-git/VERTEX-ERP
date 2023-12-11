package com.vertex.vos;

import com.vertex.vos.Constructors.*;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PurchaseOrderEntryController implements Initializable {
    private AnchorPane contentPane; // Declare contentPane variable

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    ErrorUtilities errorUtilities = new ErrorUtilities();

    private final BranchDAO branchDAO = new BranchDAO();
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


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        String cssPath = getClass().getResource("/com/vertex/vos/assets/table.css").toExternalForm();
        productsAddedTable.getStylesheets().add(cssPath);
        productsAddedTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

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

    private Set<Integer> addedBranches = new HashSet<>();

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


    private Node createBranchContent(Branch branch) {
        productsAddedTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        productsAddedTable.setEditable(true);

        String cssPath = getClass().getResource("/com/vertex/vos/assets/table.css").toExternalForm();
        productsAddedTable.getStylesheets().add(cssPath);

        TableColumn<ProductsInTransact, Integer> productIdCol = new TableColumn<>("Product ID");
        productIdCol.setCellValueFactory(new PropertyValueFactory<>("productId"));

        TableColumn<ProductsInTransact, Double> unitPriceCol = new TableColumn<>("Unit Price");
        unitPriceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));

        TableColumn<ProductsInTransact, Integer> orderedQuantityCol = new TableColumn<>("Quantity");
        orderedQuantityCol.setCellValueFactory(new PropertyValueFactory<>("orderedQuantity"));

        orderedQuantityCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        orderedQuantityCol.setOnEditCommit(event -> {
            ProductsInTransact product = event.getRowValue();
            product.setOrderedQuantity(event.getNewValue());
        });

        TableColumn<ProductsInTransact, Double> vatAmountCol = new TableColumn<>("VAT");
        vatAmountCol.setCellValueFactory(new PropertyValueFactory<>("vatAmount"));

        TableColumn<ProductsInTransact, Double> withholdingAmountCol = new TableColumn<>("EWT");
        withholdingAmountCol.setCellValueFactory(new PropertyValueFactory<>("withholdingAmount"));

        TableColumn<ProductsInTransact, Double> totalAmountCol = new TableColumn<>("Total Amount");
        totalAmountCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        productsAddedTable.getColumns().addAll(productIdCol, unitPriceCol,
                orderedQuantityCol, withholdingAmountCol, vatAmountCol, totalAmountCol);

        productsAddedTable.setItems(productsList);

        return new VBox(productsAddedTable); // Return the TableView wrapped in a VBox
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
                allProductsEntered = false; // Set all products flag to false if any product is not entered
                break; // Stop processing if any product is not entered
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

    private void isTaxed() {
        withholding.setVisible(false);
        vat.setVisible(false);

        receiptCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                totalBoxLabels.getChildren().addAll(withholding, vat);
            } else {
                totalBoxLabels.getChildren().removeAll(withholding, vat);
            }
        });
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
        ProductDAO productDAO = new ProductDAO();
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

    void setUIPerStatus(int poNumber) {
    }

    void fixedValues() {
        supplier.setDisable(true);
    }
}
