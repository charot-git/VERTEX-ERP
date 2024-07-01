package com.vertex.vos;

import com.vertex.vos.Objects.CreditDebitMemo;
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

public class CreditDebitFormController {

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


    private void comboBoxUtils(String memoType) {
        TextFieldUtils.setComboBoxBehavior(glCOAComboBox);
        TextFieldUtils.setComboBoxBehavior(accountComboBox);
        glCOAComboBox.setItems(FXCollections.observableArrayList(chartOfAccountsDAO.getAllAccountTitlesForMemo()));
        if (memoType.equals("supplier")) {
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
                    accountComboBox.setItems(supplierNames);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    CreditDebitListController creditDebitListController;

    public void setCreditDebitListController(CreditDebitListController creditDebitListController) {
        this.creditDebitListController = creditDebitListController;
    }

    DocumentNumbersDAO numbersDAO = new DocumentNumbersDAO();

    public void addNewSupplierCreditMemo() {
        comboBoxUtils("supplier");
        int documentNumber = numbersDAO.getNextSupplierCreditNumber();
        CreditDebitMemo memo = new CreditDebitMemo();
        memo.setMemoNumber(String.valueOf(documentNumber));
        memo.setStatus("Memo entry");
        memo.setType(1);
        docNoLabel.setText("Document No #" + memo.getMemoNumber());
        statusLabel.setText(memo.getStatus());
        documentTypeLabel.setText("Credit");
    }
}
