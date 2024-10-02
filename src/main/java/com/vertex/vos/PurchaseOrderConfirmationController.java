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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.Setter;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class PurchaseOrderConfirmationController implements Initializable {


    @Setter
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
    private TableColumn<PurchaseOrder, String> payment_status;
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    @FXML
    private TextField poSearchBar;
    @FXML
    private TextField supplierSearchBar;

    ErrorUtilities errorUtilities = new ErrorUtilities();

    private final PurchaseOrderDAO purchaseOrderDAO = new PurchaseOrderDAO();


    private final HistoryManager historyManager = new HistoryManager();

    private final int currentNavigationId = -1; // Initialize to a default value

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        ProgressIndicator progressIndicator = new ProgressIndicator();
        tablePOConfirmation.setPlaceholder(progressIndicator);

        TextFieldUtils.addNumericInputRestriction(poSearchBar);

        if (dataSource.isRunning()) {
            poSearchBar.textProperty().addListener((observable, oldValue, newValue) -> {
                filterTable(newValue, supplierSearchBar.getText());
            });

            supplierSearchBar.textProperty().addListener((observable, oldValue, newValue) -> {
                filterTable(poSearchBar.getText(), newValue);
            });
            loadDataFromDatabase();
        } else {
            DialogUtils.showErrorMessage("No connection from host", "Please check your connection or message your technical team");
        }

        // Create context menu and menu items
        ContextMenu contextMenu = new ContextMenu();
        MenuItem openMenuItem = new MenuItem("Open");
        MenuItem deleteMenuItem = new MenuItem("Delete");

        contextMenu.getItems().addAll(openMenuItem, deleteMenuItem);

        // Set context menu to the TableView
        tablePOConfirmation.setContextMenu(contextMenu);

        // Action for "Open"
        openMenuItem.setOnAction(event -> handleRowInteraction());

        // Action for "Delete"
        deleteMenuItem.setOnAction(event -> handleDeleteAction());
    }

    private void handleDeleteAction() {
        PurchaseOrder selectedPurchaseOrder = tablePOConfirmation.getSelectionModel().getSelectedItem();
        if (selectedPurchaseOrder != null) {
            boolean confirmed = DialogUtils.showConfirmationDialog("Delete Purchase Order",
                    "Are you sure you want to delete this Purchase Order?");

            if (confirmed) {
                purchaseOrderDAO.deletePurchaseOrder(selectedPurchaseOrder);
                refreshData();
                DialogUtils.showCompletionDialog("Success", "Purchase Order deleted successfully.");
            }
        } else {
            DialogUtils.showErrorMessage("No Selection", "Please select a purchase order to delete.");
        }
    }

    private void filterTable(String poSearchText, String supplierSearchText) {
        List<PurchaseOrder> allPurchaseOrders = tablePOConfirmation.getItems();
        if (poSearchText.isEmpty() && supplierSearchText.isEmpty()) {
            refreshData();
        } else {
            List<PurchaseOrder> filteredList = allPurchaseOrders.stream()
                    .filter(po -> String.valueOf(po.getPurchaseOrderNo()).contains(poSearchText))
                    .filter(po -> po.getSupplierNameString().toLowerCase().contains(supplierSearchText))
                    .toList();

            tablePOConfirmation.getItems().setAll(filteredList);
        }
    }


    private void loadDataFromDatabase() {
        CompletableFuture<List<PurchaseOrder>> futurePurchaseOrders = CompletableFuture.supplyAsync(purchaseOrderDAO::getAllPurchaseOrders);
        futurePurchaseOrders.thenAccept(this::populateTable);
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
        tablePOConfirmation.getItems().clear();
        loadDataFromDatabase();
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
