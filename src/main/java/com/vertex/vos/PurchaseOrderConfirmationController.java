package com.vertex.vos;

import com.vertex.vos.Objects.PurchaseOrder;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

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
    private TableColumn<PurchaseOrder, LocalDateTime> date_requested;
    @FXML
    private TableColumn<PurchaseOrder, String> inventory_status;
    @FXML
    private TableColumn <PurchaseOrder, String> payment_status;
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    @FXML
    private TextField poSearchBar;
    @FXML
    private TextField supplierSearchBar;

    ErrorUtilities errorUtilities = new ErrorUtilities();

    private final PurchaseOrderDAO purchaseOrderDAO = new PurchaseOrderDAO();


    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    private final HistoryManager historyManager = new HistoryManager();

    private final int currentNavigationId = -1; // Initialize to a default value

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        TextFieldUtils.addNumericInputRestriction(poSearchBar);

        if (dataSource.isRunning()) {
            try {
                poSearchBar.textProperty().addListener((observable, oldValue, newValue) -> {
                    filterTable(newValue, supplierSearchBar.getText());
                });

                supplierSearchBar.textProperty().addListener((observable, oldValue, newValue) -> {
                    filterTable(poSearchBar.getText(), newValue);
                });
                loadDataFromDatabase();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            DialogUtils.showErrorMessage("No connection from host", "Please check your connection or message your technical team");
        }

    }

    private void filterTable(String poSearchText, String supplierSearchText) {
        List<PurchaseOrder> allPurchaseOrders = tablePOConfirmation.getItems();
        if (poSearchText.isEmpty() && supplierSearchText.isEmpty()) {
            tablePOConfirmation.getItems().clear();
            tablePOConfirmation.getItems().addAll(allPurchaseOrders);
        } else {
            List<PurchaseOrder> filteredList = allPurchaseOrders.stream()
                    .filter(po -> String.valueOf(po.getPurchaseOrderNo()).contains(poSearchText))
                    .filter(po -> po.getSupplierNameString().toLowerCase().contains(supplierSearchText))
                    .toList();

            tablePOConfirmation.getItems().clear();
            tablePOConfirmation.getItems().addAll(filteredList);
        }
    }


    private void loadDataFromDatabase() throws SQLException {
        List<PurchaseOrder> purchaseOrders = purchaseOrderDAO.getAllPurchaseOrders();
        populateTable(purchaseOrders);
    }

    private void populateTable(List<PurchaseOrder> purchaseOrders) {
        id.setCellValueFactory(new PropertyValueFactory<>("purchaseOrderNo"));
        supplier_name.setCellValueFactory(new PropertyValueFactory<>("supplierNameString"));
        type_of_transaction.setCellValueFactory(new PropertyValueFactory<>("transactionTypeString"));
        date_requested.setCellValueFactory(new PropertyValueFactory<>("dateEncoded"));
        date_requested.setCellFactory(column -> new TableCell<PurchaseOrder, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime dateTime, boolean empty) {
                super.updateItem(dateTime, empty);
                if (dateTime == null || empty) {
                    setText(null);
                } else {
                    setText(DateTimeUtils.formatDateTime(dateTime));
                }
            }
        });
        inventory_status.setCellValueFactory(new PropertyValueFactory<>("inventoryStatusString"));
        payment_status.setCellValueFactory(new PropertyValueFactory<>("paymentStatusString"));
        tablePOConfirmation.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                handleRowInteraction();
            }
        });

        tablePOConfirmation.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleRowInteraction();
            }
        });

        tablePOConfirmation.getItems().addAll(purchaseOrders);
    }

    public void refreshData() {
        try {
            tablePOConfirmation.getItems().clear();
            loadDataFromDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private final Map<PurchaseOrder, Stage> openPurchaseOrderStages = new HashMap<>();

    private void handleRowInteraction() {
        PurchaseOrder selectedPurchaseOrder = tablePOConfirmation.getSelectionModel().getSelectedItem();
        if (selectedPurchaseOrder != null) {
            Platform.runLater(() -> {
                if (openPurchaseOrderStages.containsKey(selectedPurchaseOrder)) {
                    Stage existingStage = openPurchaseOrderStages.get(selectedPurchaseOrder);
                    errorUtilities.shakeWindow(existingStage);
                    existingStage.toFront();
                } else {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("purchaseOrderEntryAccounting.fxml"));
                    try {
                        Parent root = loader.load();
                        PurchaseOrderEntryController controller = loader.getController();
                        Stage stage = new Stage();
                        Scene scene = new Scene(root);
                        stage.setMaximized(true);
                        stage.setScene(scene);
                        stage.setTitle("Purchase Order Details");
                        controller.setPurchaseOrderConfirmationController(this);
                        controller.fixedValues();
                        controller.setUIPerStatus(selectedPurchaseOrder, scene);
                        stage.setOnHidden(e -> openPurchaseOrderStages.remove(selectedPurchaseOrder));
                        stage.show();
                        openPurchaseOrderStages.put(selectedPurchaseOrder, stage); // Store the reference
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

}
