package com.vertex.vos;

import com.vertex.vos.Constructors.PurchaseOrder;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PurchaseOrderConfirmationController implements Initializable {


    private AnchorPane contentPane; // Declare contentPane variable
    @FXML
    private TableView<PurchaseOrder> tablePOConfirmation;
    @FXML
    private TableColumn<Integer, Integer> id;
    @FXML
    private TableColumn<String, String> supplier_name;
    @FXML
    private TableColumn<String, String> type_of_transaction;
    @FXML
    private TableColumn<Timestamp, Timestamp> date_requested;
    @FXML
    private TableColumn<String, String> status;
    @FXML
    private TableColumn<Timestamp, Timestamp> date_status;

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    @FXML
    private TextField poSearchBar;
    @FXML
    private TextField supplierSearchBar;

    private final PurchaseOrderDAO purchaseOrderDAO = new PurchaseOrderDAO();

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    private final HistoryManager historyManager = new HistoryManager();

    private int currentNavigationId = -1; // Initialize to a default value

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        if (dataSource.isRunning()) {
            try {
                loadDataFromDatabase();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            DialogUtils.showErrorMessage("No connection from host", "Please check your connection or message your technical team");
        }

    }

    private void loadDataFromDatabase() throws SQLException {
        List<PurchaseOrder> purchaseOrders = purchaseOrderDAO.getAllPurchaseOrders();
        populateTable(purchaseOrders);
    }

    private void populateTable(List<PurchaseOrder> purchaseOrders) {
        id.setCellValueFactory(new PropertyValueFactory<>("purchaseOrderId"));
        supplier_name.setCellValueFactory(new PropertyValueFactory<>("supplierNameString"));
        type_of_transaction.setCellValueFactory(new PropertyValueFactory<>("transactionTypeString"));
        date_requested.setCellValueFactory(new PropertyValueFactory<>("dateEncoded"));
        status.setCellValueFactory(new PropertyValueFactory<>("statusString"));
        date_status.setCellValueFactory(new PropertyValueFactory<>("dateEncoded"));
        tablePOConfirmation.setOnMouseClicked(this::handleRowClick);
        tablePOConfirmation.getItems().addAll(purchaseOrders);
    }

    public void refreshData() {
        try {
            tablePOConfirmation.getItems().clear();
            loadDataFromDatabase();
        } catch (SQLException e) {
            // Handle exception
        }
    }


    private void handleRowClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            // Get selected PurchaseOrder object from the clicked row
            PurchaseOrder selectedPurchaseOrder = tablePOConfirmation.getSelectionModel().getSelectedItem();

            if (selectedPurchaseOrder != null) {
                // Open PurchaseOrderEntryController with selected PurchaseOrder details
                FXMLLoader loader = new FXMLLoader(getClass().getResource("purchaseOrderEntryAccounting.fxml"));
                Parent root;
                try {
                    root = loader.load();
                    PurchaseOrderEntryController controller = loader.getController();
                    controller.setPurchaseOrderConfirmationController(this); // Pass reference

                    int PO_NUMBER = selectedPurchaseOrder.getPurchaseOrderNo();
                    controller.setUIPerStatus(PO_NUMBER);


                    Stage stage = new Stage();
                    Scene scene = new Scene(root);
                    stage.setMaximized(true);
                    stage.setScene(scene);
                    stage.setTitle("Purchase Order Details");
                    stage.showAndWait();
                } catch (IOException e) {
                    e.printStackTrace();
                    // Handle FXMLLoader exception
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
