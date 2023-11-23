package com.vertex.vos;

import com.vertex.vos.Constructors.PurchaseOrder;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
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
    private TableColumn<String, String> branch_name;
    @FXML
    private TableColumn<String, String> supplier_name;
    @FXML
    private TableColumn<String, String> type_of_transaction;
    @FXML
    private TableColumn<Timestamp, Timestamp> date;
    @FXML
    private TableColumn<String, String> encoder_id;
    @FXML
    private TableColumn<String, String> approver_id;
    @FXML
    private TableColumn<Double, Double> total_amount;
    @FXML
    private TableColumn<PurchaseOrder, Double> vat_total;
    @FXML
    private TableColumn<Double, Double> withholding_total;
    @FXML
    private TableColumn<String, String> status;

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    private final HistoryManager historyManager = new HistoryManager();

    private int currentNavigationId = -1; // Initialize to a default value

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final int POLLING_INTERVAL_SECONDS = 5; // Poll every 60 seconds


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


        id.setCellValueFactory(new PropertyValueFactory<>("poId"));
        branch_name.setCellValueFactory(new PropertyValueFactory<>("branchName"));
        supplier_name.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        type_of_transaction.setCellValueFactory(new PropertyValueFactory<>("transactionType"));
        date.setCellValueFactory(new PropertyValueFactory<>("dateEncoded"));
        encoder_id.setCellValueFactory(new PropertyValueFactory<>("encoderName"));
        approver_id.setCellValueFactory(new PropertyValueFactory<>("approverName"));
        total_amount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        vat_total.setCellValueFactory(new PropertyValueFactory<PurchaseOrder, Double>("vatAmount"));
        withholding_total.setCellValueFactory(new PropertyValueFactory<>("withholdingTaxAmount"));
        status.setCellValueFactory(new PropertyValueFactory<>("status"));

        tablePOConfirmation.setOnMouseClicked(this::handleRowClick);

        vat_total.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<PurchaseOrder, Double>, ObservableValue<Double>>() {
            @Override
            public ObservableValue<Double> call(TableColumn.CellDataFeatures<PurchaseOrder, Double> param) {
                Double vatAmount = param.getValue().getVatAmount();
                return new SimpleObjectProperty<>(vatAmount);
            }
        });


        loadDataFromDatabase();

        startPollingTask();

    }

    private void startPollingTask() {
        // Schedule the task to run periodically
        scheduler.scheduleAtFixedRate(this::loadDataFromDatabase, 0, POLLING_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    public void stopPollingTask() {
        // Stop the polling task when it is no longer needed
        scheduler.shutdown();
    }

    private void loadDataFromDatabase() {

        tablePOConfirmation.getItems().clear();
        // Fetch data from the database using the dataSource
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM purchase_order");
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String encoderNameQuery = "SELECT user_fname, user_lname FROM user WHERE user_id = ?";
                String approverNameQuery = "SELECT user_fname, user_lname FROM user WHERE user_id = ?";
                String receiverNameQuery = "SELECT user_fname, user_lname FROM user WHERE user_id = ?";

                boolean receiptRequired = resultSet.getBoolean("receipt_required");

                // Check if receipt_required is false, and if so, set vatAmount and withholdingTaxAmount to null
                Double vatAmount = receiptRequired ? resultSet.getDouble("vat_amount") : null;
                Double withholdingTaxAmount = receiptRequired ? resultSet.getDouble("withholding_tax_amount") : null;


                try (PreparedStatement encoderStatement = connection.prepareStatement(encoderNameQuery);
                     PreparedStatement approverStatement = connection.prepareStatement(approverNameQuery);
                     PreparedStatement receiverStatement = connection.prepareStatement(receiverNameQuery)) {

                    encoderStatement.setInt(1, resultSet.getInt("encoder_id"));
                    ResultSet encoderResult = encoderStatement.executeQuery();
                    String encoderName = "";
                    if (encoderResult.next()) {
                        encoderName = encoderResult.getString("user_fname") + " " + encoderResult.getString("user_lname");
                    }

                    approverStatement.setInt(1, resultSet.getInt("approver_id"));
                    ResultSet approverResult = approverStatement.executeQuery();
                    String approverName = "";
                    if (approverResult.next()) {
                        approverName = approverResult.getString("user_fname") + " " + approverResult.getString("user_lname");
                    }

                    receiverStatement.setInt(1, resultSet.getInt("receiver_id")); // Assuming you have a receiver_id column in your database
                    ResultSet receiverResult = receiverStatement.executeQuery();
                    String receiverName = "";
                    if (receiverResult.next()) {
                        receiverName = receiverResult.getString("user_fname") + " " + receiverResult.getString("user_lname");
                    }

                    PurchaseOrder purchaseOrder = new PurchaseOrder(
                            resultSet.getInt("purchase_order_id"),
                            resultSet.getString("purchase_order_no"),
                            resultSet.getString("branch_name"),
                            resultSet.getString("supplier_name"),
                            resultSet.getString("transaction_type"),
                            resultSet.getTimestamp("date_encoded"),
                            resultSet.getInt("encoder_id"),
                            resultSet.getInt("approver_id"),
                            resultSet.getInt("receiver_id"), // Assuming you have a receiver_id column in your database
                            encoderName,
                            approverName,
                            receiverName,
                            resultSet.getDouble("total_amount"),
                            resultSet.getDouble("vat_amount"),
                            resultSet.getDouble("withholding_tax_amount"),
                            resultSet.getString("status")
                    );
                    tablePOConfirmation.getItems().add(purchaseOrder);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle database exception
        }
    }

    private void handleRowClick(MouseEvent event) {
        if (event.getClickCount() == 1) {
            // Get selected PurchaseOrder object from the clicked row
            PurchaseOrder selectedPurchaseOrder = tablePOConfirmation.getSelectionModel().getSelectedItem();

            if (selectedPurchaseOrder != null) {
                // Open PurchaseOrderEntryController with selected PurchaseOrder details
                FXMLLoader loader = new FXMLLoader(getClass().getResource("purchaseOrderEntryAccounting.fxml"));
                Parent root;
                try {
                    root = loader.load();
                    PurchaseOrderEntryController controller = loader.getController();
                    controller.setPurchaseOrder(selectedPurchaseOrder);

                    // Open a new stage to display the PurchaseOrderEntryController
                    Stage stage = new Stage();
                    Scene scene = new Scene(root);
                    stage.setMaximized(true);
                    stage.setScene(scene);
                    stage.setTitle("Purchase Order Details");
                    stage.showAndWait();
                } catch (IOException e) {
                    e.printStackTrace();
                    // Handle FXMLLoader exception
                }
            }
        }
    }
}
