package com.vertex.vos;

import com.vertex.vos.Objects.BalanceType;
import com.vertex.vos.Objects.CustomerMemo;
import com.vertex.vos.Utilities.CustomerMemoDAO;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CustomerCreditDebitListController implements Initializable {

    public TableColumn<CustomerMemo, String> salesmanCodeCol;
    @FXML
    private TableColumn<CustomerMemo, Double> amountCol;

    @FXML
    private TableColumn<CustomerMemo, Double> appliedAmountCol;

    @FXML
    private TextField chartOfAccountField;

    @FXML
    private TableColumn<CustomerMemo, String> coaCol;

    @FXML
    private Button confirmButton;

    @FXML
    private TableColumn<CustomerMemo, String> customerCodeCol;

    @FXML
    private TextField customerField;

    @FXML
    private DatePicker dateFrom;

    @FXML
    private DatePicker dateTo;

    @FXML
    private TableColumn<CustomerMemo, String> isPending;

    @FXML
    private CheckBox isPendingCheckBox;

    @FXML
    private TableColumn<CustomerMemo, String> numberCol;

    @FXML
    private TextField numberField;

    @FXML
    private TextField salesmanField;

    @FXML
    private TableColumn<CustomerMemo, String> salesmanNameCol;

    @FXML
    private TableColumn<CustomerMemo, String> statusCol;

    @FXML
    private ComboBox<String> statusField;

    @FXML
    private TableColumn<CustomerMemo, String> storeNameCol;

    @FXML
    private TableColumn<CustomerMemo, String> supplierCol;

    @FXML
    private TextField supplierField;

    @Setter
    private BalanceType balanceType;

    @FXML
    public TableView<CustomerMemo> memoTable;

    private final CustomerMemoDAO customerMemoDAO = new CustomerMemoDAO();
    public ObservableList<CustomerMemo> customerMemos = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        memoTable.setItems(customerMemos);

        statusField.setItems(FXCollections.observableArrayList(
                Arrays.stream(CustomerMemo.MemoStatus.values())
                        .map(Enum::name)
                        .collect(Collectors.toList())
        ));

        numberCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getMemoNumber()));
        coaCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getChartOfAccount().getAccountTitle()));
        customerCodeCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCustomer().getCustomerCode()));
        salesmanNameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getSalesman().getSalesmanName()));
        salesmanCodeCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getSalesman().getSalesmanCode()));
        amountCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getAmount()));
        appliedAmountCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getAppliedAmount()));
        storeNameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCustomer().getStoreName()));
        supplierCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getSupplier().getSupplierName()));
        statusCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getStatus().toDisplayString()));
        isPending.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getIsPending() ? "Yes" : "No"));

        Platform.runLater(() -> confirmButton.setOnAction(actionEvent -> {
            createNewMemo(balanceType);
        }));

        memoTable.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                openSelectedMemo(memoTable.getSelectionModel().getSelectedItem());
            }
        });
    }

    private void openSelectedMemo(CustomerMemo selectedItem) {
        if (memoStage != null && memoStage.isShowing()) {
            memoStage.requestFocus();
            return;
        }
        try {
            if (balanceType != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("CustomerMemoForm.fxml"));
                Parent root = loader.load();

                CustomerMemoFormController controller = loader.getController();
                controller.setBalanceType(balanceType);
                controller.openExistingMemo(selectedItem);
                controller.initializeEncodingData();
                controller.setCustomerCreditDebitListController(this);

                memoStage = new Stage();
                memoStage.setTitle(selectedItem.getBalanceType().getBalanceName() + " " + selectedItem.getMemoNumber());
                memoStage.setScene(new Scene(root));
                memoStage.setMaximized(true);
                memoStage.showAndWait();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Stage memoStage;

    private void createNewMemo(BalanceType balanceType) {
        if (memoStage != null && memoStage.isShowing()) {
            memoStage.requestFocus();
            return;
        }

        try {
            if (balanceType != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("CustomerMemoForm.fxml"));
                Parent root = loader.load();

                CustomerMemoFormController controller = loader.getController();
                controller.setBalanceType(balanceType);
                controller.createNewMemo();
                controller.initializeEncodingData();
                controller.setCustomerCreditDebitListController(this);

                memoStage = new Stage();
                memoStage.setTitle("New Customer " + balanceType.getBalanceName() + " Memo");
                memoStage.setScene(new Scene(root));
                memoStage.setMaximized(true);
                memoStage.showAndWait();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void loadData() {
        if (balanceType != null) {
            customerMemos.setAll(customerMemoDAO.getAllCustomersWhereBalanceType(balanceType));
        }
    }
}
