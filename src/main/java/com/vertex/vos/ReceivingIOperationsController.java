package com.vertex.vos;

import com.vertex.vos.Constructors.ComboBoxFilterUtil;
import com.vertex.vos.Constructors.ProductsInTransact;
import com.vertex.vos.Constructors.PurchaseOrder;
import com.vertex.vos.Constructors.UserSession;
import com.vertex.vos.Utilities.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class ReceivingIOperationsController implements Initializable {

    @FXML
    public TextField invoiceReceipt;
    @FXML
    public DatePicker receiptDate;
    @FXML
    public VBox addInvoiceButton;
    @FXML
    public TabPane invoiceTabs;
    public ComboBox<String> receivingTypeComboBox;
    public Label receivingTypeErr;
    public VBox poNoBox;
    public VBox branchBox;
    public VBox receivingTypeBox;
    public VBox buttonsBox;
    public VBox addProductButton;

    private AnchorPane contentPane;
    @FXML
    private CheckBox isInvoice;

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    private final HistoryManager historyManager = new HistoryManager();

    private final int currentNavigationId = -1; // Initialize to a default value

    @FXML
    private ComboBox<String> branchComboBox;

    @FXML
    private Label branchErr;

    @FXML
    private Label companyNameHeaderLabel;

    @FXML
    private Button confirmButton;
    @FXML
    private Label poErr;
    @FXML
    private ComboBox<String> poNumberTextField;
    PurchaseOrderDAO purchaseOrderDAO = new PurchaseOrderDAO();
    BranchDAO branchDAO = new BranchDAO();
    PurchaseOrderProductDAO purchaseOrderProductDAO = new PurchaseOrderProductDAO();
    ReceivingTypeDAO receivingTypeDAO = new ReceivingTypeDAO();
    private final PurchaseOrderNumberDAO orderNumberDAO = new PurchaseOrderNumberDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TextFieldUtils.setComboBoxBehavior(poNumberTextField);
        TextFieldUtils.setComboBoxBehavior(branchComboBox);
        TextFieldUtils.setComboBoxBehavior(receivingTypeComboBox);
        isInvoice.setVisible(false);
        buttonsBox.getChildren().remove(addProductButton);
        ObservableList<String> receivingTypes = receivingTypeDAO.getAllReceivingTypes();
        receivingTypeComboBox.setItems(receivingTypes);
        ComboBoxFilterUtil.setupComboBoxFilter(receivingTypeComboBox, receivingTypes);

        receivingTypeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) {
                branchBox.setDisable(false);
                poNoBox.setDisable(false);
                try {
                    initializeReceivingType(newValue);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                branchBox.setDisable(true);
                poNoBox.setDisable(true);
            }
        });

        quantitySummaryTab = new Tab("Quantity Summary");
        initializeSummaryTable();
        invoiceTabs.getTabs().add(quantitySummaryTab);
    }

    private void initializeReceivingType(String receivingType) throws SQLException {
        poNumberTextField.setItems(purchaseOrderDAO.getAllPOForReceiving(receivingTypeDAO.getReceivingTypeByName(receivingType)));

        if (receivingType.equals("CASH ON DELIVERY") || receivingType.equals("CASH WITH ORDER")) {
            initializeCashReceivingType();
        } else if (receivingType.equals("GENERAL RECEIVE")) {
            initializeGeneralReceiveType();
        } else {
            System.err.println("Unrecognized receiving type: " + receivingType);
        }
    }

    private void initializeCashReceivingType() {
        isInvoice.setVisible(false);
        poNumberTextField.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) {
                try {
                    PurchaseOrder purchaseOrder = purchaseOrderDAO.getPurchaseOrderByOrderNo(Integer.parseInt(newValue));
                    populateBranchPerPoId(purchaseOrder);
                    confirmButton.setOnMouseClicked(event -> {
                        receivePO(purchaseOrder, invoiceTabs.getTabs(), branchDAO.getBranchIdByName(branchComboBox.getSelectionModel().getSelectedItem()));
                    });
                } catch (SQLException e) {
                    e.printStackTrace();
                    DialogUtils.showErrorMessage("Error", "Failed to retrieve purchase order details. Please contact your I.T department.");
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    DialogUtils.showErrorMessage("Error", "Invalid purchase order number format.");
                }
            }
        });
    }

    private void initializeGeneralReceiveType() {
        PurchaseOrder generalReceivePO = new PurchaseOrder();
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("General Receiving", "New receive?", "", true);
        boolean confirmed = confirmationAlert.showAndWait();
        if (confirmed) {
            int poNumber = orderNumberDAO.getNextPurchaseOrderNumber();
            isInvoice.setVisible(true);
            isInvoice.setSelected(true);
            buttonsBox.getChildren().add(addProductButton);
            receivingTypeBox.setDisable(true);
            poNumberTextField.getItems().clear();
            poNumberTextField.setValue(String.valueOf(poNumber));
            branchComboBox.setItems(branchDAO.getAllNonMovingBranchNames());
            generalReceivePO.setPurchaseOrderNo(poNumber);
            generalReceivePO.setReceiverId(UserSession.getInstance().getUserId());
            generalReceivePO.setEncoderId(UserSession.getInstance().getUserId());
            generalReceivePO.setApproverId(UserSession.getInstance().getUserId());
            generalReceivePO.setDateApproved(LocalDateTime.now());
            generalReceivePO.setTransactionType(1);
            generalReceivePO.setReceiptRequired(isInvoice.isSelected());
            isInvoice.selectedProperty().addListener((observable, oldValue, newValue) -> {
                generalReceivePO.setReceiptRequired(newValue);
            });
            generalReceivePO.setStatus(3);
            generalReceivePO.setDateEncoded(LocalDateTime.now());
            generalReceivePO.setDateReceived(LocalDateTime.now());
            generalReceivePO.setPriceType("General Receive Price");
            generalReceivePO.setReceivingType(3);

            addInvoiceButton.setOnMouseClicked(mouseEvent -> addTabForGeneralReceiving(generalReceivePO));
            addProductButton.setDisable(true);

        } else {
            poNumberTextField.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.trim().isEmpty()) {
                    try {
                        PurchaseOrder purchaseOrder = purchaseOrderDAO.getPurchaseOrderByOrderNo(Integer.parseInt(newValue));
                        populateBranchPerPoIdGeneralReceive(purchaseOrder);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        DialogUtils.showErrorMessage("Error", "Failed to retrieve purchase order details. Please contact your I.T department.");
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        DialogUtils.showErrorMessage("Error", "Invalid purchase order number format.");
                    }
                }
            });
        }

        confirmButton.setOnMouseClicked(mouseEvent -> receivePOForGeneralReceive(generalReceivePO, invoiceTabs.getTabs()));
    }

    private void populateBranchPerPoIdGeneralReceive(PurchaseOrder purchaseOrder) throws SQLException {
        int poId = purchaseOrder.getPurchaseOrderNo();
        ObservableList<String> branchNames = purchaseOrderDAO.getBranchNamesForPurchaseOrder(poId);
        branchComboBox.setItems(branchNames);
        branchComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                try {
                    int branchId = branchDAO.getBranchIdByName(newValue);
                    List<ProductsInTransact> products = purchaseOrderProductDAO.getProductsForReceiving(purchaseOrder.getPurchaseOrderNo(), branchId);
                    getSummaryTableData(products);
                    setReceivedQuantityInSummaryTable(products, purchaseOrder, branchId);

                    List<String> receiptNumbers = purchaseOrderProductDAO.getReceiptNumbersForPurchaseOrder(poId, branchId);
                    for (String receiptNo : receiptNumbers) {
                        invoiceNumbers.add(receiptNo);
                        receivedInvoiceNumbers.add(receiptNo);
                        populatePrePopulatedTabs(purchaseOrder, branchId);
                        updateTabPaneForGeneralReceive();
                    }
                } catch (SQLException e) {
                    e.printStackTrace(); // Handle SQLException appropriately
                }
            }
        });
        addInvoiceButton.setOnMouseClicked(mouseEvent -> addTab());
        confirmButton.setOnMouseClicked(event -> {
            receivePOForGeneralReceive(purchaseOrder, invoiceTabs.getTabs());
        });
    }

    private void receivePOForGeneralReceive(PurchaseOrder generalReceivePO, ObservableList<Tab> tabs) {
        try {
            int poNumber = Integer.parseInt(poNumberTextField.getSelectionModel().getSelectedItem());
            generalReceivePO.setPurchaseOrderNo(poNumber);

            if (!purchaseOrderDAO.entryGeneralReceive(generalReceivePO)) {
                DialogUtils.showErrorMessage("Error", "Failed to create a general receiving entry. Please contact your I.T department.");
                return;
            }

            int branchId = branchDAO.getBranchIdByName(branchComboBox.getSelectionModel().getSelectedItem());
            List<ProductsInTransact> allProducts = new ArrayList<>();

            for (Tab tab : tabs) {
                if (!(tab.getContent() instanceof TableView<?> tableView)) continue;
                ObservableList<?> items = tableView.getItems();

                if (items.isEmpty() || !(items.get(0) instanceof ProductsInTransact)) continue;

                TableView<ProductsInTransact> table = (TableView<ProductsInTransact>) tableView;
                String invoiceNumber = tab.getText();

                for (ProductsInTransact product : table.getItems()) {
                    product.setBranchId(branchId);
                    product.setOrderedQuantity(product.getReceivedQuantity());
                    allProducts.add(product);
                    if (!purchaseOrderProductDAO.receivePurchaseOrderProduct(product, generalReceivePO, LocalDate.now(), invoiceNumber)) {
                        DialogUtils.showErrorMessage("Error", "Failed to receive product: " + product.getDescription() + ". Please check the details or contact your I.T department.");
                        return;
                    }
                }
            }

            if (!purchaseOrderProductDAO.bulkEntryProductsPerPO(allProducts)) {
                DialogUtils.showErrorMessage("Error", "Failed to receive products. Please check the details or contact your I.T department.");
                return;
            }

            if (purchaseOrderProductDAO.batchUpdateReceiveForProducts(allProducts)) {
                List<ProductsInTransact> summarizedProducts = purchaseOrderProductDAO.getProductsForReceiving(generalReceivePO.getPurchaseOrderNo(), branchId);
                setReceivedQuantityInSummaryTable(summarizedProducts, generalReceivePO, branchId);
                DialogUtils.showConfirmationDialog("Received", "Purchase Order " + generalReceivePO.getPurchaseOrderNo() + " has been received successfully.");
            } else {
                DialogUtils.showErrorMessage("Error", "Failed to update received products. Please check the details or contact your I.T department.");
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            DialogUtils.showErrorMessage("Error", "Invalid purchase order number format.");
        } catch (SQLException e) {
            e.printStackTrace();
            DialogUtils.showErrorMessage("Error", "Error in processing the request. Please contact your I.T department.");
        }
    }


    private void addTabForGeneralReceiving(PurchaseOrder generalReceivePO) {
        String invoiceNumber = EntryAlert.showEntryAlert("Add Invoice", "Enter Invoice Number", "Please enter the invoice number:");
        if (!invoiceNumber.isEmpty() && !receivedInvoiceNumbers.contains(invoiceNumber)) {
            invoiceNumbers.add(invoiceNumber);
            receivedInvoiceNumbers.add(invoiceNumber);
            updateTabPaneForGeneralReceive();
            addProductButton.setDisable(false);
            addProductButton.setOnMouseClicked(mouseEvent -> addProductToTable(generalReceivePO));
        } else {
            DialogUtils.showErrorMessage("Duplicate Invoice", "Invoice number already exists or is empty.");
        }
    }

    private void updateTabPaneForGeneralReceive() {
        receivingTypeBox.setDisable(true);
        poNoBox.setDisable(true);
        branchBox.setDisable(true);
        for (String invoice : invoiceNumbers) {
            if (!invoice.equals("Quantity Summary")) {
                boolean tabExists = false;
                for (Tab existingTab : invoiceTabs.getTabs()) {
                    if (existingTab.getText().equals(invoice)) {
                        tabExists = true;
                        break;
                    }
                }
                if (!tabExists) {
                    Tab tab = new Tab(invoice);
                    TableView<ProductsInTransact> tableView = new TableView<>();
                    tableConfiguration(tableView, invoice);
                    tableView.setItems(generalReceiveProducts);
                    tab.setContent(tableView);
                    invoiceTabs.getTabs().add(tab);
                }
            }
        }
    }


    private void addProductToTable(PurchaseOrder generalReceivePO) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ProductSelectionForGeneralReceive.fxml"));
            Parent root = loader.load();
            ProductSelectionForGeneralReceiveController controller = loader.getController();

            controller.addProductToTable(poNumberTextField.getSelectionModel().getSelectedItem(), generalReceivePO);
            controller.setTargetController(this);

            Stage stage = new Stage();
            stage.setTitle("Add Products");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TableView<ProductsInTransact> quantitySummaryTable; // Declare quantitySummaryTable here
    Button postButton = new Button();

    private void initializeSummaryTable() {
        quantitySummaryTable = new TableView<>();
        quantitySummaryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<ProductsInTransact, String> productColumn = new TableColumn<>("Description");
        productColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        TableColumn<ProductsInTransact, String> unitColumn = new TableColumn<>("Unit");
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));

        TableColumn<ProductsInTransact, Integer> orderedQuantityColumn = new TableColumn<>("Ordered Quantity");
        orderedQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("orderedQuantity"));

        TableColumn<ProductsInTransact, Integer> receivedQuantityColumn = new TableColumn<>("Received Quantity");
        receivedQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("receivedQuantity"));

        receivingTypeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals("GENERAL RECEIVE")) {
                quantitySummaryTable.getColumns().clear();
                quantitySummaryTable.getColumns().addAll(productColumn, unitColumn, receivedQuantityColumn);

            } else {
                quantitySummaryTable.getColumns().clear();
                quantitySummaryTable.getColumns().addAll(productColumn, unitColumn, orderedQuantityColumn, receivedQuantityColumn);

            }
        });

        postButton.setText("POST");

        HBox buttonBox = new HBox(postButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        VBox container = new VBox();
        container.getChildren().addAll(quantitySummaryTable, buttonBox);
        container.setAlignment(Pos.CENTER);
        quantitySummaryTab.setContent(container);
    }

    private void postReceiving(List<ProductsInTransact> products) {
        String selectedPONumber = poNumberTextField.getValue();
        String selectedBranch = branchComboBox.getValue();

        if (selectedPONumber == null || selectedBranch == null) {
            DialogUtils.showErrorMessage("Error", "Please select a PO number and branch.");
            return;
        }

        boolean proceedWithDiscrepancy = false; // Declare the variable here

        try {
            int poNumber = Integer.parseInt(selectedPONumber);
            PurchaseOrder purchaseOrder = purchaseOrderDAO.getPurchaseOrderByOrderNo(poNumber);
            int branchId = branchDAO.getBranchIdByName(selectedBranch);

            // Check for discrepancies between ordered and received quantities
            if (!checkForDiscrepancies(products)) {
                // Display confirmation alert if user still wants to proceed despite discrepancy
                ConfirmationAlert confirmationAlert = new ConfirmationAlert("Confirmation", "Confirm Reception Posting", "There are discrepancies between ordered and received quantities. Do you still want to proceed?", true);
                proceedWithDiscrepancy = confirmationAlert.showAndWait(); // Assign the value here
                if (!proceedWithDiscrepancy) {
                    return; // User chose not to proceed
                }
            }

            ConfirmationAlert confirmationAlert = new ConfirmationAlert("Confirmation", "Confirm Reception Posting", "Are you sure you want to post the reception?", true);
            boolean proceed = confirmationAlert.showAndWait();

            if (proceed) {
                boolean allItemsProcessedSuccessfully = processReception(products, branchId);
                if (allItemsProcessedSuccessfully) {
                    DialogUtils.showConfirmationDialog("Success", "Reception processed successfully.");
                    purchaseOrderDAO.receivePurchaseOrder(purchaseOrder, proceedWithDiscrepancy);
                } else {
                    DialogUtils.showErrorMessage("Error", "An error occurred while processing the reception.");
                }
            }
        } catch (NumberFormatException e) {
            DialogUtils.showErrorMessage("Error", "Invalid PO number.");
        } catch (SQLException e) {
            e.printStackTrace();
            DialogUtils.showErrorMessage("Error", "An error occurred while processing the reception.");
        }
    }


    private boolean checkForDiscrepancies(List<ProductsInTransact> products) {
        for (ProductsInTransact product : products) {
            if (product.getReceivedQuantity() != product.getOrderedQuantity()) {
                return false;
            }
        }
        return true;
    }

    private boolean processReception(List<ProductsInTransact> products, int branchId) throws SQLException {
        InventoryDAO inventoryDAO = new InventoryDAO();
        PurchaseOrderProductDAO purchaseOrderProductDAO = new PurchaseOrderProductDAO();

        boolean allItemsProcessedSuccessfully = true;
        for (ProductsInTransact product : products) {
            try {
                boolean processedSuccessfully = inventoryDAO.addOrUpdateInventory(product);
                if (!processedSuccessfully) {
                    allItemsProcessedSuccessfully = false;
                    boolean updateSuccess = purchaseOrderProductDAO.updateReceiveForProducts(product);
                    if (!updateSuccess) {
                        DialogUtils.showErrorMessage("Error", "Failed to update receive status for product.");
                    }
                    break;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                allItemsProcessedSuccessfully = false;
                boolean updateSuccess = purchaseOrderProductDAO.updateReceiveForProducts(product);
                if (!updateSuccess) {
                    DialogUtils.showErrorMessage("Error", "Failed to update receive status for product.");
                }
                break;
            }
        }
        return allItemsProcessedSuccessfully;
    }

    private Tab quantitySummaryTab;

    private void getSummaryTableData(List<ProductsInTransact> products) {
        quantitySummaryTable.getItems().clear();
        quantitySummaryTable.getItems().addAll(products);
        postButton.setOnMouseClicked(event -> postReceiving(products));
    }


    private void receivePO(PurchaseOrder purchaseOrder, List<Tab> tabs, int branchIdByName) {
        boolean success = true;

        for (Tab tab : tabs) {
            if (!(tab.getContent() instanceof TableView<?>)) {
                continue;
            }

            TableView<ProductsInTransact> tableView = (TableView<ProductsInTransact>) tab.getContent();
            ObservableList<ProductsInTransact> products = tableView.getItems();

            if (products.isEmpty()) {
                continue;
            }

            String invoiceNumber = tab.getText();

            try {
                for (ProductsInTransact product : products) {
                    boolean received = purchaseOrderProductDAO.receivePurchaseOrderProduct(product, purchaseOrder, LocalDate.now(), invoiceNumber);
                    if (!received) {
                        success = false;
                        break;
                    }
                }

                if (success) {
                    List<ProductsInTransact> summarizedProducts = purchaseOrderProductDAO.getProductsForReceiving(purchaseOrder.getPurchaseOrderNo(), branchIdByName);
                    setReceivedQuantityInSummaryTable(summarizedProducts, purchaseOrder, branchIdByName);
                } else {
                    break;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                DialogUtils.showErrorMessage("Error", "Error in receiving this Purchase Order, please contact your I.T department.");
                return;
            }
        }

        if (success) {
            DialogUtils.showConfirmationDialog("Received", "Purchase Order " + purchaseOrder.getPurchaseOrderNo() + " has been received successfully.");
        } else {
            DialogUtils.showErrorMessage("Error", "Some items could not be received. Please check the details or contact your I.T department.");
        }
    }


    private void populateBranchPerPoId(PurchaseOrder purchaseOrder) throws SQLException {
        int poId = purchaseOrder.getPurchaseOrderNo();
        ObservableList<String> branchNames = purchaseOrderDAO.getBranchNamesForPurchaseOrder(poId);
        branchComboBox.setItems(branchNames);
        branchComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                try {
                    int branchId = branchDAO.getBranchIdByName(newValue);
                    List<ProductsInTransact> products = purchaseOrderProductDAO.getProductsForReceiving(purchaseOrder.getPurchaseOrderNo(), branchId);
                    getSummaryTableData(products);
                    setReceivedQuantityInSummaryTable(products, purchaseOrder, branchId);

                    List<String> receiptNumbers = purchaseOrderProductDAO.getReceiptNumbersForPurchaseOrder(poId, branchId);
                    for (String receiptNo : receiptNumbers) {
                        invoiceNumbers.add(receiptNo);
                        receivedInvoiceNumbers.add(receiptNo);
                        updateTabPane();
                        populatePrePopulatedTabs(purchaseOrder, branchId);
                    }
                } catch (SQLException e) {
                    e.printStackTrace(); // Handle SQLException appropriately
                }
            }
        });
        addInvoiceButton.setOnMouseClicked(mouseEvent -> addTab());
    }

    private void setReceivedQuantityInSummaryTable(List<ProductsInTransact> products, PurchaseOrder purchaseOrder, int branchId) {
        try {
            for (ProductsInTransact product : products) {
                int totalReceivedQuantity = purchaseOrderProductDAO.getTotalReceivedQuantityForProductInPO(purchaseOrder.getPurchaseOrderNo(), product.getProductId(), branchId);
                product.setReceivedQuantity(totalReceivedQuantity);
            }
            getSummaryTableData(products);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private final Set<String> receivedInvoiceNumbers = new HashSet<>();
    private final ObservableList<String> invoiceNumbers = FXCollections.observableArrayList();

    private void addTab() {
        String invoiceNumber = EntryAlert.showEntryAlert("Add Invoice", "Enter Invoice Number", "Please enter the invoice number:");
        if (!invoiceNumber.isEmpty() && !receivedInvoiceNumbers.contains(invoiceNumber)) {
            invoiceNumbers.add(invoiceNumber);
            receivedInvoiceNumbers.add(invoiceNumber);
            updateTabPane();
        } else {
            DialogUtils.showErrorMessage("Duplicate Invoice", "Invoice number already exists or is empty.");
        }
    }

    private void updateTabPane() {
        try {
            PurchaseOrder purchaseOrder = purchaseOrderDAO.getPurchaseOrderByOrderNo(Integer.parseInt(poNumberTextField.getSelectionModel().getSelectedItem()));
            int branchId = branchDAO.getBranchIdByName(branchComboBox.getSelectionModel().getSelectedItem());
            receivingTypeBox.setDisable(true);
            poNoBox.setDisable(true);
            branchBox.setDisable(true);
            for (String invoice : invoiceNumbers) {
                if (!invoice.equals("Quantity Summary")) {
                    boolean tabExists = false;
                    for (Tab existingTab : invoiceTabs.getTabs()) {
                        if (existingTab.getText().equals(invoice)) {
                            tabExists = true;
                            break;
                        }
                    }
                    if (!tabExists) {
                        Tab tab = new Tab(invoice);
                        ObservableList<ProductsInTransact> tabProductsInTransact = FXCollections.observableArrayList();
                        TableView<ProductsInTransact> tableView = new TableView<>();
                        tableView.setItems(tabProductsInTransact);
                        tableConfiguration(tableView, invoice);
                        populateTableData(tabProductsInTransact, purchaseOrder, branchId);
                        tab.setContent(tableView);
                        invoiceTabs.getTabs().add(tab);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle SQLException appropriately
        }
    }


    private void populateTableData(ObservableList<ProductsInTransact> tabProductsInTransact, PurchaseOrder purchaseOrder, int branchId) {
        try {
            List<ProductsInTransact> products = purchaseOrderProductDAO.getProductsForReceiving(purchaseOrder.getPurchaseOrderNo(), branchId);
            tabProductsInTransact.addAll(products);
        } catch (SQLException e) {
            e.printStackTrace(); // Handle SQLException appropriately
        }
    }

    private void tableConfiguration(TableView<ProductsInTransact> tableView, String invoice) {
        TableColumn<ProductsInTransact, String> productColumn = new TableColumn<>("Description");
        productColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<ProductsInTransact, String> unitColumn = new TableColumn<>("Unit");
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));

        TableColumn<ProductsInTransact, Integer> receivedQuantityColumn = getReceivedQuantityColumn(tableView);

        TableColumn<ProductsInTransact, Double> unitPriceColumn = getUnitPriceColumn();

        if (receivingTypeComboBox.getSelectionModel().getSelectedItem().equals("GENERAL RECEIVE")) {
            tableView.getColumns().clear();
            tableView.getColumns().addAll(productColumn, unitColumn, unitPriceColumn, receivedQuantityColumn);

        } else {
            tableView.getColumns().clear();
            tableView.getColumns().addAll(productColumn, unitColumn, receivedQuantityColumn);

        }
        tableView.setEditable(true);
        tableView.setFocusTraversable(true);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    private TableColumn<ProductsInTransact, Double> getUnitPriceColumn() {
        TableColumn<ProductsInTransact, Double> unitPriceColumn = new TableColumn<>("Unit Price");
        unitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        unitPriceColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        unitPriceColumn.setOnEditCommit(event -> {
            ProductsInTransact product = event.getRowValue();
            product.setUnitPrice(event.getNewValue());
        });
        return unitPriceColumn;
    }


    public static TableColumn<ProductsInTransact, Integer> getReceivedQuantityColumn(TableView<ProductsInTransact> tableView) {
        TableColumn<ProductsInTransact, Integer> receivedQuantityColumn = new TableColumn<>("Quantity");
        receivedQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("receivedQuantity"));
        receivedQuantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

        receivedQuantityColumn.setOnEditCommit(event -> {
            ProductsInTransact product = event.getRowValue();
            product.setReceivedQuantity(event.getNewValue());
            tableView.requestFocus();
        });

        // Set focus to the cell when it enters edit mode
        receivedQuantityColumn.setOnEditStart(event -> {
            int row = event.getTablePosition().getRow();
            tableView.requestFocus();
            tableView.getSelectionModel().select(row, receivedQuantityColumn);
            tableView.edit(row, receivedQuantityColumn);
        });

        return receivedQuantityColumn;
    }

    private void populatePrePopulatedTabs(PurchaseOrder purchaseOrder, int branchId) throws SQLException {
        for (Tab tab : invoiceTabs.getTabs()) {
            if (tab.getContent() instanceof TableView) { // Check if content is a TableView
                TableView<ProductsInTransact> tableView = (TableView<ProductsInTransact>) tab.getContent();
                ObservableList<ProductsInTransact> products = tableView.getItems();

                for (ProductsInTransact product : products) {
                    int receivedQuantity = purchaseOrderProductDAO.getReceivedQuantityForInvoice(purchaseOrder.getPurchaseOrderNo(), product.getProductId(), branchId, tab.getText());
                    product.setReceivedQuantity(receivedQuantity);
                }
            }
        }
    }

    private void resetInputs() {
        branchComboBox.getSelectionModel().clearSelection();
        branchComboBox.getItems().clear();
        branchComboBox.setDisable(false);
        poNumberTextField.setDisable(false);
    }

    @FXML
    private VBox totalBoxLabels;

    @FXML
    private Label vat;

    @FXML
    private Label vatExempt;

    @FXML
    private Label vatZeroRated;

    @FXML
    private Label vatable;

    @FXML
    private Label amountPayable;

    private final ObservableList<ProductsInTransact> generalReceiveProducts = FXCollections.observableArrayList();

    void addProductToReceivingTable(ProductsInTransact selectedProduct) {
        if (selectedProduct != null) {
            generalReceiveProducts.add(selectedProduct);
            poNoBox.setDisable(true);
            branchBox.setDisable(true);
        }
        quantitySummaryTable.setItems(generalReceiveProducts);
    }
}
