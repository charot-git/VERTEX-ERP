package com.vertex.vos;

import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class CreditDebitFormController implements Initializable {

    private String registrationType;

    @FXML
    private VBox account;

    @FXML
    private ComboBox<String> accountComboBox;

    @FXML
    private Label accountErr;

    @FXML
    private VBox addProductButton;

    @FXML
    private Label addProductLabel;

    @FXML
    private HBox confirmBox;

    @FXML
    private Button confirmButton;

    @FXML
    private Label date;

    @FXML
    private Label docNoLabel;

    @FXML
    private VBox glCOABox;

    @FXML
    private ComboBox<String> glCOAComboBox;

    @FXML
    private Label glCOAErr;

    @FXML
    private Label grandTotal;

    @FXML
    private VBox memoDateBox;

    @FXML
    private DatePicker memoDateDatePicker;

    @FXML
    private Label memoDateErr;

    @FXML
    private TextArea remarks;

    @FXML
    private Label remarksErr;

    @FXML
    private ImageView statusImage;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox taxBox;

    @FXML
    private Label taxErr;

    @FXML
    private ComboBox<?> taxIdComboBox;

    @FXML
    private HBox totalBox;

    @FXML
    private VBox totalBoxLabels;

    @FXML
    private VBox totalVBox;

    @FXML
    private VBox transactionTypeBox;

    @FXML
    private ComboBox<String> transactionTypeComboBox;

    @FXML
    private Label transactionTypeLabel;

    @FXML
    private Label typeErr;

    @FXML
    private Label vat;

    @FXML
    private Label withholding;
    @FXML
    private Label documentTypeLabel;
    @FXML
    private Label accountLabel;

    public void setRegistrationType(String registrationType) {
        this.registrationType = registrationType;
    }

    TransactionTypeDAO transactionTypeDAO = new TransactionTypeDAO();
    ChartOfAccountsDAO chartOfAccountsDAO = new ChartOfAccountsDAO();

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    private void showSupplierCustomerSelection() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(registrationType.toUpperCase());
        alert.setHeaderText("Create memo for supplier or customer?");

        ButtonType supplierButton = new ButtonType("Supplier");
        ButtonType customerButton = new ButtonType("Customer");
        ButtonType cancelButton = new ButtonType("Cancel");

        alert.getButtonTypes().setAll(supplierButton, customerButton, cancelButton);

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == supplierButton) {
                accountLabel.setText("Supplier");
                performInitialization("supplier");
            } else if (buttonType == customerButton) {
                accountLabel.setText("Customer");
                performInitialization("customer");
            } else {
                // Canceled or closed the dialog
                // Handle cancellation or closing
                System.out.println("Dialog closed");
            }
        });
    }

    // Method to perform the rest of the initialization
    private void performInitialization(String memoType) {
        Platform.runLater(() -> {
            documentTypeLabel.setText(registrationType.toUpperCase());
            date.setText(DateTimeUtils.formatDateTime(LocalDateTime.now()));
            transactionTypeLabel.setText("Transaction Type");
            statusLabel.setText("ENTRY");
            comboBoxUtils(memoType);
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(this::showSupplierCustomerSelection);
    }


    private void comboBoxUtils(String memoType) {
        TextFieldUtils.setComboBoxBehavior(transactionTypeComboBox);
        TextFieldUtils.setComboBoxBehavior(glCOAComboBox);
        TextFieldUtils.setComboBoxBehavior(accountComboBox);
        transactionTypeComboBox.setItems(FXCollections.observableArrayList(transactionTypeDAO.getAllTransactionTypeNames()));
        glCOAComboBox.setItems(FXCollections.observableArrayList(chartOfAccountsDAO.getAllAccountTitles()));
        if (memoType.equals("supplier")){
            String sqlQuery = "SELECT supplier_name FROM suppliers";

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    ObservableList<String> supplierNames = FXCollections.observableArrayList();

                    // Iterate through the result set and add supplier names to the ObservableList
                    while (resultSet.next()) {
                        String supplierName = resultSet.getString("supplier_name");
                        supplierNames.add(supplierName);
                    }
                    // Set the ObservableList as the items for the supplier ComboBox
                    accountComboBox.setItems(supplierNames);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
}
