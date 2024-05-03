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
import java.util.List;
import java.util.ResourceBundle;

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


    }

    private void receivePO(PurchaseOrder purchaseOrder, List<Tab> tabs) throws SQLException {
        boolean allReceived = true;
        for (Tab tab : tabs) {
            if (tab.getContent() instanceof TableView) {
                TableView<?> tableView = (TableView<?>) tab.getContent();
                ObservableList<?> items = tableView.getItems();
                if (items.size() > 0 && items.get(0) instanceof ProductsInTransact) {
                    TableView<ProductsInTransact> table = (TableView<ProductsInTransact>) tableView;
                    ObservableList<ProductsInTransact> products = table.getItems();

                    for (ProductsInTransact product : products) {
                        int receivedQuantity = product.getReceivedQuantity();
                        String invoiceNumber = tab.getText();

                        boolean updatedQuantity = purchaseOrderProductDAO.receivePurchaseOrderProduct(product, purchaseOrder, LocalDate.now(), invoiceNumber);

                        if (updatedQuantity) {
                            // Update the inventory
                            inventoryDAO.addOrUpdateInventory(product);
                        } else {
                            allReceived = false;
                            break; // Stop the loop if any product fails to be received
                        }
                    }
                } else {
                }
            }
        }

        if (allReceived) {
            DialogUtils.showConfirmationDialog("Received", "Purchase Order " + purchaseOrder.getPurchaseOrderNo() + " has been received successfully.");
        } else {
            DialogUtils.showErrorMessage("Error", "Error in receiving this Purchase Order, please contact your I.T department.");
        }
    }

    private void populateBranchPerPoId(PurchaseOrder purchaseOrder) throws SQLException {
        int poId = purchaseOrder.getPurchaseOrderNo();
        ObservableList<String> branchNames = purchaseOrderDAO.getBranchNamesForPurchaseOrder(poId);
        branchComboBox.setItems(branchNames);
        addInvoiceButton.setOnMouseClicked(mouseEvent -> addTab());
        confirmButton.setOnMouseClicked(event -> {
            try {
                receivePO(purchaseOrder, invoiceTabs.getTabs());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

    }

    private final ObservableList<String> invoiceNumbers = FXCollections.observableArrayList();

    private void addTab() {
        String invoiceNumber = EntryAlert.showEntryAlert("Add Invoice", "Enter Invoice Number", "Please enter the invoice number:");
        if (!invoiceNumber.isEmpty()) {
            invoiceNumbers.add(invoiceNumber);
            updateTabPane();
        }
    }

    private void updateTabPane() {
        invoiceTabs.getTabs().clear();

        for (String invoice : invoiceNumbers) {
            Tab newTab = new Tab(invoice);

            ObservableList<ProductsInTransact> tabProductsInTransact = FXCollections.observableArrayList();
            TableView<ProductsInTransact> tableView = new TableView<>();
            tableView.setItems(tabProductsInTransact);

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

            newTab.setContent(tableView);
            invoiceTabs.getTabs().add(newTab);

            // Populate the tab-specific ObservableList using the DAO method
            try {
                PurchaseOrder purchaseOrder = purchaseOrderDAO.getPurchaseOrderByOrderNo(Integer.parseInt(poNumberTextField.getSelectionModel().getSelectedItem()));
                int branchId = branchDAO.getBranchIdByName(branchComboBox.getSelectionModel().getSelectedItem());
                tabProductsInTransact.addAll(purchaseOrderProductDAO.getProductsForReceiving(purchaseOrder.getPurchaseOrderNo(), branchId));
            } catch (SQLException e) {
                throw new RuntimeException(e);
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
