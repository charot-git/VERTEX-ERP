package com.vertex.vos;

import com.vertex.vos.DAO.SalesInvoiceDAO;
import com.vertex.vos.DAO.SalesInvoicePaymentsDAO;
import com.vertex.vos.Objects.SalesInvoiceHeader;
import com.vertex.vos.Objects.SalesInvoicePayment;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.StatusDAO;
import com.vertex.vos.Utilities.TextFieldUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import org.controlsfx.control.textfield.TextFields;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.ResourceBundle;

public class SalesInvoicePaymentController implements Initializable {

    public TableView<SalesInvoicePayment> paymentsTable;
    @FXML
    private Label DocNo;

    @FXML
    private Label InvNo;

    @FXML
    private Button addButton;

    @FXML
    private TableColumn<SalesInvoicePayment, Double> amountCol;

    @FXML
    private Label balanceAmount;

    @FXML
    private TableColumn<SalesInvoicePayment, String> bankCol;

    @FXML
    private TextField bankNameField;

    @FXML
    private Button confirmButton;

    @FXML
    private TableColumn<SalesInvoicePayment, Date> dateEncodedCol;

    @FXML
    private DatePicker dateEncodedPicker;

    @FXML
    private TableColumn<SalesInvoicePayment, Date> datePaidCol;

    @FXML
    private DatePicker datePaidPicker;

    @FXML
    private Label paidAmount;

    @FXML
    private TextField paidAmountField;

    @FXML
    private Label payableAmount;

    @FXML
    private ComboBox<String> paymentStatusComboBox;

    @FXML
    private TextField paymentTypeField;

    @FXML
    private TableColumn<SalesInvoicePayment, String> refNoCol;

    @FXML
    private TextField referenceNoField;

    @FXML
    private TableColumn<SalesInvoicePayment, String> typeCol;

    SalesInvoiceHeader salesInvoiceHeader;

    SalesInvoiceDAO salesInvoiceDAO = new SalesInvoiceDAO();

    SalesInvoicePaymentsDAO salesInvoicePaymentsDAO = new SalesInvoicePaymentsDAO();

    StatusDAO statusDAO = new StatusDAO();

    CollectionFormController collectionFormController;

    ObservableList<SalesInvoicePayment> deleteList = FXCollections.observableArrayList();

    public void settlePayment(SalesInvoiceHeader salesInvoiceHeader, CollectionFormController collectionFormController) {
        this.collectionFormController = collectionFormController;
        this.salesInvoiceHeader = salesInvoiceHeader;
        DocNo.setText(salesInvoiceHeader.getOrderId());
        InvNo.setText(salesInvoiceHeader.getInvoiceNo());
        salesInvoiceHeader.getSalesInvoicePayments().setAll(salesInvoicePaymentsDAO.getPaymentsByInvoice(salesInvoiceHeader.getInvoiceId()));
        paymentStatusComboBox.setItems(FXCollections.observableArrayList(statusDAO.getAllPaymentStatuses()));
        if (salesInvoiceHeader.getSalesInvoicePayments().isEmpty()) {
            paymentsTable.setPlaceholder(new Label("No payments found."));
        }
        paymentStatusComboBox.getSelectionModel().select(salesInvoiceHeader.getPaymentStatus());
        dateEncodedPicker.setValue(collectionFormController.getCollectionDate());
        datePaidPicker.setValue(collectionFormController.getCollectionDate());
        updateAmounts();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            TextFields.bindAutoCompletion(bankNameField, collectionFormController.bankNamesList);
            TextFields.bindAutoCompletion(paymentTypeField, collectionFormController.chartOfAccountsNames);

            paymentTypeField.setText(collectionFormController.collectionDetails.getFirst().getType().getAccountTitle());

            paymentsTable.setItems(salesInvoiceHeader.getSalesInvoicePayments());
        });

        paymentsTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                deleteList.addAll(paymentsTable.getSelectionModel().getSelectedItems());
                salesInvoiceHeader.getSalesInvoicePayments().removeAll(deleteList);
                updateAmounts();
            }
        });

        TextFieldUtils.addDoubleInputRestriction(paidAmountField);

        typeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getChartOfAccount().getAccountTitle()));
        bankCol.setCellValueFactory(cellData -> cellData.getValue().getBank() != null ? new SimpleStringProperty(cellData.getValue().getBank().getName()) : null);
        refNoCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReferenceNo()));
        amountCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPaidAmount()).asObject());
        dateEncodedCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDateEncoded()));
        datePaidCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDatePaid()));
        addButton.setOnAction(event -> {
            SalesInvoicePayment salesInvoicePayment = new SalesInvoicePayment();
            collectionFormController.chartOfAccounts.stream()
                    .filter(coa -> coa.getAccountTitle().equals(paymentTypeField.getText()))
                    .findFirst()
                    .ifPresent(salesInvoicePayment::setChartOfAccount);
            collectionFormController.bankNames.stream()
                    .filter(bank -> bank.getName().equals(bankNameField.getText()))
                    .findFirst()
                    .ifPresent(salesInvoicePayment::setBank);
            salesInvoicePayment.setReferenceNo(referenceNoField.getText());
            salesInvoicePayment.setPaidAmount(Double.parseDouble(paidAmountField.getText()));
            salesInvoicePayment.setDateEncoded(Timestamp.valueOf(dateEncodedPicker.getValue().atStartOfDay()));
            salesInvoicePayment.setDatePaid(Timestamp.valueOf(datePaidPicker.getValue().atStartOfDay()));
            salesInvoicePayment.setInvoice(salesInvoiceHeader);
            salesInvoicePayment.setOrderId(salesInvoiceHeader.getOrderId());
            salesInvoiceHeader.getSalesInvoicePayments().add(salesInvoicePayment);
            updateAmounts();
        });

        confirmButton.setOnAction(event -> {
            Connection conn = null;
            try {
                // Start a database transaction
                conn = DatabaseConnectionPool.getDataSource().getConnection();
                conn.setAutoCommit(false); // Disable auto-commit to begin the transaction

                // Set payment status
                salesInvoiceHeader.setPaymentStatus(paymentStatusComboBox.getValue());

                // Add payments to the database
                if (!salesInvoiceHeader.getSalesInvoicePayments().isEmpty()) {
                    boolean added = salesInvoicePaymentsDAO.addPayments(conn, salesInvoiceHeader.getSalesInvoicePayments());
                    if (!added) {
                        throw new SQLException("Failed to add payments.");
                    }
                }
                // Update the payment status of the sales invoice
                boolean updated = salesInvoiceDAO.paidOrder(conn, salesInvoiceHeader);
                if (!updated) {
                    throw new SQLException("Failed to update payment status.");
                }

                // Delete payments from the database if necessary
                if (!deleteList.isEmpty()) {
                    boolean deleted = salesInvoicePaymentsDAO.deletePayments(conn, deleteList);
                    if (!deleted) {
                        throw new SQLException("Failed to delete payments.");
                    }
                }

                // Commit the transaction if everything is successful
                conn.commit();

                // Update UI and other components
                DialogUtils.showCompletionDialog("Success", "Payment added successfully.");
                collectionFormController.salesInvoiceTable.refresh();

            } catch (SQLException e) {
                // Rollback the transaction in case of any error
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException rollbackEx) {
                        rollbackEx.printStackTrace();
                    }
                }
                DialogUtils.showErrorMessage("Error", "An error occurred while processing payments: " + e.getMessage());
            } finally {
                // Reset the connection's auto-commit state and close the connection
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true); // Re-enable auto-commit
                        conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    private void updateAmounts() {
        double paidAmount = 0.0;
        for (SalesInvoicePayment salesInvoicePayments : salesInvoiceHeader.getSalesInvoicePayments()) {
            paidAmount += salesInvoicePayments.getPaidAmount();
        }
        this.paidAmount.setText(String.format("%.2f", paidAmount));
        this.payableAmount.setText(String.format("%.2f", salesInvoiceHeader.getTotalAmount()));
        this.balanceAmount.setText(String.format("%.2f", salesInvoiceHeader.getTotalAmount() - paidAmount));
    }
}
