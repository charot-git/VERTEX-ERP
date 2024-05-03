package com.vertex.vos;

import com.vertex.vos.Constructors.ProductsInTransact;
import com.vertex.vos.Constructors.PurchaseOrder;
import com.vertex.vos.Utilities.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.converter.IntegerStringConverter;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

public class ReceivingIOperationsController implements Initializable {

    @FXML
    public TextField invoiceReceipt;
    @FXML
    public DatePicker receiptDate;
    @FXML
    public VBox addInvoiceButton;
    @FXML
    public TabPane invoiceTabs;

    private AnchorPane contentPane;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TextFieldUtils.setComboBoxBehavior(poNumberTextField);
        TextFieldUtils.setComboBoxBehavior(branchComboBox);
        poNumberTextField.setItems(purchaseOrderDAO.getAllPOForReceiving());
        poNumberTextField.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            try {
                PurchaseOrder purchaseOrder = purchaseOrderDAO.getPurchaseOrderByOrderNo(Integer.parseInt(poNumberTextField.getSelectionModel().getSelectedItem()));
                populateBranchPerPoId(purchaseOrder);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        quantitySummaryTab = new Tab("Quantity Summary");
        initializeSummaryTable();
        invoiceTabs.getTabs().add(quantitySummaryTab);
    }

    private TableView<ProductsInTransact> quantitySummaryTable; // Declare quantitySummaryTable here

    private void initializeSummaryTable() {
        // Create the summary table
        quantitySummaryTable = new TableView<>();

        // Set up columns for the summary table
        TableColumn<ProductsInTransact, String> productColumn = new TableColumn<>("Description");
        productColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<ProductsInTransact, String> unitColumn = new TableColumn<>("Unit");
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));

        TableColumn<ProductsInTransact, Integer> orderedQuantityColumn = new TableColumn<>("Ordered Quantity");
        orderedQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("orderedQuantity"));

        TableColumn<ProductsInTransact, Integer> receivedQuantityColumn = new TableColumn<>("Received Quantity");
        receivedQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("receivedQuantity"));

        // Add columns to the summary table
        quantitySummaryTable.getColumns().addAll(productColumn, unitColumn, orderedQuantityColumn, receivedQuantityColumn);

        // Add the summary table to its container
        quantitySummaryTab.setContent(quantitySummaryTable);
    }

    private void receivePO(PurchaseOrder purchaseOrder, List<Tab> tabs) {
        for (Tab tab : tabs) {
            if (tab.getContent() instanceof TableView) {
                TableView<?> tableView = (TableView<?>) tab.getContent();
                ObservableList<?> items = tableView.getItems();
                if (items.size() > 0 && items.get(0) instanceof ProductsInTransact) {
                    TableView<ProductsInTransact> table = (TableView<ProductsInTransact>) tableView;
                    ObservableList<ProductsInTransact> products = table.getItems();

                    for (ProductsInTransact product : products) {
                        String invoiceNumber = tab.getText();
                        try {
                            purchaseOrderProductDAO.receivePurchaseOrderProduct(product, purchaseOrder, LocalDate.now(), invoiceNumber);
                        } catch (SQLException e) {
                            e.printStackTrace();
                            DialogUtils.showErrorMessage("Error", "Error in receiving this Purchase Order, please contact your I.T department.");
                            return; // Stop further processing
                        }
                    }
                }
            }
        }
        DialogUtils.showConfirmationDialog("Received", "Purchase Order " + purchaseOrder.getPurchaseOrderNo() + " has been received successfully.");
    }

    private Tab quantitySummaryTab;

    private void populateBranchPerPoId(PurchaseOrder purchaseOrder) throws SQLException {
        int poId = purchaseOrder.getPurchaseOrderNo();
        ObservableList<String> branchNames = purchaseOrderDAO.getBranchNamesForPurchaseOrder(poId);
        branchComboBox.setItems(branchNames);
        branchComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                try {
                    int branchId = branchDAO.getBranchIdByName(newValue);
                    List<String> receiptNumbers = purchaseOrderProductDAO.getReceiptNumbersForPurchaseOrder(poId, branchId);
                    for (String receiptNo : receiptNumbers) {
                        invoiceNumbers.add(receiptNo);
                        receivedInvoiceNumbers.add(receiptNo);
                        updateTabPane();
                        populatePrePopulatedTabs(purchaseOrder, branchId); // Call populatePrePopulatedTabs before adding the tab
                    }
                } catch (SQLException e) {
                    e.printStackTrace(); // Handle SQLException appropriately
                }
            }
        });

        addInvoiceButton.setOnMouseClicked(mouseEvent -> addTab());
        confirmButton.setOnMouseClicked(event -> {
            receivePO(purchaseOrder, invoiceTabs.getTabs());
        });
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
                        tableConfiguration(tableView);

                        // Populate the table with data
                        populateTableData(tabProductsInTransact, purchaseOrder, branchId, invoice);

                        tab.setContent(tableView);
                        invoiceTabs.getTabs().add(tab);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle SQLException appropriately
        }
    }



    private void populateTableData(ObservableList<ProductsInTransact> tabProductsInTransact, PurchaseOrder purchaseOrder, int branchId, String invoice) {
        try {
            List<ProductsInTransact> products = purchaseOrderProductDAO.getProductsForReceiving(purchaseOrder.getPurchaseOrderNo(), branchId);

            tabProductsInTransact.addAll(products);
        } catch (SQLException e) {
            e.printStackTrace(); // Handle SQLException appropriately
        }
    }


    private void tableConfiguration(TableView<ProductsInTransact> tableView) {
        TableColumn<ProductsInTransact, String> productColumn = new TableColumn<>("Description");
        productColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<ProductsInTransact, String> unitColumn = new TableColumn<>("Unit");
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));

        TableColumn<ProductsInTransact, Integer> receivedQuantityColumn = new TableColumn<>("Quantity");
        receivedQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("receivedQuantity"));
        receivedQuantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        receivedQuantityColumn.setOnEditCommit(event -> {
            ProductsInTransact product = event.getRowValue();
            product.setReceivedQuantity(event.getNewValue());
        });
        tableView.getColumns().addAll(productColumn, unitColumn, receivedQuantityColumn);
        tableView.setEditable(true);
        tableView.setFocusTraversable(true);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    private void populatePrePopulatedTabs(PurchaseOrder purchaseOrder, int branchId) throws SQLException {
        for (Tab tab : invoiceTabs.getTabs()) {
            String invoiceNumber = tab.getText();
            TableView<ProductsInTransact> tableView = (TableView<ProductsInTransact>) tab.getContent();
            ObservableList<ProductsInTransact> products = tableView.getItems();

            for (ProductsInTransact product : products) {
                int receivedQuantity = purchaseOrderProductDAO.getReceivedQuantityForInvoice(purchaseOrder.getPurchaseOrderNo(), product.getProductId(), branchId, invoiceNumber);
                product.setReceivedQuantity(receivedQuantity);
            }
        }
    }

    DiscountDAO discountDAO = new DiscountDAO();

    InventoryDAO inventoryDAO = new InventoryDAO();


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
}
