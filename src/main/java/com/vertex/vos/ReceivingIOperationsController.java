package com.vertex.vos;

import com.vertex.vos.Constructors.ProductsInTransact;
import com.vertex.vos.Constructors.PurchaseOrder;
import com.vertex.vos.Utilities.BranchDAO;
import com.vertex.vos.Utilities.PurchaseOrderDAO;
import com.vertex.vos.Utilities.PurchaseOrderProductDAO;
import com.vertex.vos.Utilities.TextFieldUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.util.converter.IntegerStringConverter;
import org.w3c.dom.Text;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ReceivingIOperationsController implements Initializable {

    private AnchorPane contentPane;
    @FXML
    private TableView<ProductsInTransact> productTableView;

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
    private TableColumn<ProductsInTransact, Integer> orderedQuantity;

    @FXML
    private Label poErr;

    @FXML
    private ComboBox<String> poNumberTextField;

    @FXML
    private TableColumn<ProductsInTransact, String> productDescription;

    @FXML
    private TableColumn<ProductsInTransact, String> productUnit;

    @FXML
    private TableColumn<ProductsInTransact, Integer> receivedQuantity;

    PurchaseOrderDAO purchaseOrderDAO = new PurchaseOrderDAO();
    BranchDAO branchDAO = new BranchDAO();
    PurchaseOrderProductDAO purchaseOrderProductDAO = new PurchaseOrderProductDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        TextFieldUtils.setComboBoxBehavior(poNumberTextField);
        TextFieldUtils.setComboBoxBehavior(branchComboBox);
        productTableView.setFocusTraversable(true);

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
                loadProductsForPoPerBranch(purchaseOrder, branchId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        });

    }

    private void loadProductsForPoPerBranch(PurchaseOrder purchaseOrder, int branchId) throws SQLException {
        List<ProductsInTransact> products = purchaseOrderProductDAO.getProductsInTransactForBranch(purchaseOrder, branchId);
        ObservableList<ProductsInTransact> productsObservableList = FXCollections.observableArrayList(products);
        poNumberTextField.setDisable(true);
        productDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        orderedQuantity.setCellValueFactory(new PropertyValueFactory<>("orderedQuantity"));
        receivedQuantity.setCellValueFactory(new PropertyValueFactory<>("receivedQuantity"));
        receivedQuantity.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        receivedQuantity.setOnEditCommit(event -> {
            branchComboBox.setDisable(true);
            ProductsInTransact product = event.getTableView().getItems().get(event.getTablePosition().getRow());
            product.setReceivedQuantity(event.getNewValue());
        });

        productUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));

        productTableView.setItems(productsObservableList);
        productTableView.setEditable(true);

        confirmButton.setOnMouseClicked(event -> receivePurchaseOrderForBranch(products, purchaseOrder));
    }

    private void receivePurchaseOrderForBranch(List<ProductsInTransact> products, PurchaseOrder purchaseOrder) {
        for (ProductsInTransact product : products) {
            try {
                boolean received = purchaseOrderProductDAO.receivePurchaseOrderProduct(product, purchaseOrder);
                if (!received) {
                    System.out.println("Failed to receive product: " + product.getDescription());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
