package com.vertex.vos;

import com.vertex.vos.Constructors.ProductsInTransact;
import com.vertex.vos.Constructors.PurchaseOrder;
import com.vertex.vos.Utilities.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;
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

    private int currentNavigationId = -1; // Initialize to a default value

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
        productsInTransact = FXCollections.observableArrayList();

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

    private void populateBranchPerPoId(PurchaseOrder purchaseOrder) throws SQLException {
        int poId = purchaseOrder.getPurchaseOrderNo();
        ObservableList<String> branchNames = purchaseOrderDAO.getBranchNamesForPurchaseOrder(poId);
        branchComboBox.setItems(branchNames);

        branchComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            int branchId = branchDAO.getBranchIdByName(newValue);
            try {
                productsInTransact.addAll(purchaseOrderProductDAO.getProductsInTransactForBranch(purchaseOrder, branchId));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        addInvoiceButton.setOnMouseClicked(mouseEvent -> addTab());

    }

    private ObservableList<String> invoiceNumbers = FXCollections.observableArrayList();

    private void addTab() {
        String invoiceNumber = EntryAlert.showEntryAlert("Add Invoice", "Enter Invoice Number", "Please enter the invoice number:");
        if (!invoiceNumber.isEmpty()) {
            invoiceNumbers.add(invoiceNumber);
            updateTabPane();
        }
    }

    private ObservableList<ProductsInTransact> productsInTransact;

    private void updateTabPane() {
        invoiceTabs.getTabs().clear();

        for (String invoice : invoiceNumbers) {
            Tab newTab = new Tab(invoice);

            TableView<ProductsInTransact> tableView = new TableView<>();
            tableView.setItems(productsInTransact);

            TableColumn<ProductsInTransact, String> productColumn = new TableColumn<>("Description");
            productColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

            TableColumn<ProductsInTransact, Integer> quantityColumn = new TableColumn<>("Unit");
            quantityColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));

            TableColumn<ProductsInTransact, Integer> receivedQuantityColumn = new TableColumn<>("Quantity");
            receivedQuantityColumn.setCellValueFactory(cellData -> {
                ProductsInTransact product = cellData.getValue();
                int invoiceId = Integer.parseInt(invoice);
                return Bindings.createIntegerBinding(() -> product.getInvoiceQuantity(invoiceId)).asObject();
            });
            receivedQuantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
            receivedQuantityColumn.setOnEditCommit(event -> {
                ProductsInTransact product = event.getRowValue();
                int invoiceId = Integer.parseInt(invoice);
                product.setInvoiceQuantity(invoiceId, event.getNewValue());
            });

            tableView.getColumns().addAll(productColumn, quantityColumn, receivedQuantityColumn);
            tableView.setEditable(true);

            newTab.setContent(tableView);
            invoiceTabs.getTabs().add(newTab);
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
