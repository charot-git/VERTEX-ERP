package com.vertex.vos;

import com.vertex.vos.Objects.SupplierCreditDebitMemo;
import com.vertex.vos.Utilities.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Setter;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SupplierCreditDebitListController implements Initializable {
    public TableColumn<SupplierCreditDebitMemo, String> targetCol;
    public TableColumn<SupplierCreditDebitMemo, String> reasonCol;
    @FXML
    private TableColumn<SupplierCreditDebitMemo, String> documentNumberCol;
    @FXML
    private TableColumn<SupplierCreditDebitMemo, Double> amountCol;
    @FXML
    private TableColumn<SupplierCreditDebitMemo, String> statusCol;
    @FXML
    private TableColumn<SupplierCreditDebitMemo, String> chartOfAccountCol;
    @FXML
    private VBox addButton;
    @FXML
    private Label creditLabel;
    @FXML
    private ComboBox<String> creditTarget;
    @FXML
    private TextField documentNumber;
    @FXML
    private Label header;
    @FXML
    private TableView<SupplierCreditDebitMemo> memoTable;

    @Setter
    private AnchorPane contentPane;
    private MemoDataLoader memoDataLoader;

    SupplierDAO supplierDAO = new SupplierDAO();

    private String registrationType; // Can be "Credit" or "Debit"

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeTableColumns();
        memoDataLoader = null; // Initialize memoDataLoader
    }

    private void initializeTableColumns() {
        documentNumberCol.setCellValueFactory(new PropertyValueFactory<>("memoNumber"));
        reasonCol.setCellValueFactory(new PropertyValueFactory<>("reason"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        chartOfAccountCol.setCellValueFactory(new PropertyValueFactory<>("chartOfAccountName"));
        targetCol.setCellValueFactory(new PropertyValueFactory<>("targetName"));
    }

    public void setRegistrationType(String registrationType) {
        this.registrationType = registrationType;
        updateHeader(); // Update the header based on registrationType
        loadMemoData(); // Load memo data based on registrationType
    }

    private void updateHeader() {
        if (registrationType != null) {
            header.setText("Supplier " + registrationType + " Memo");
        }
    }

    private void loadMemoData() {
        if (registrationType != null) {
            memoDataLoader = createMemoDataLoader(registrationType);
            if (memoDataLoader != null) {
                memoDataLoader.loadMemoData(memoTable); // Load memo data based on registrationType
            } else {
                System.err.println("No MemoDataLoader found for registrationType: " + registrationType);
            }
        }
    }

    private MemoDataLoader createMemoDataLoader(String registrationType) {
        creditTarget.setItems(supplierDAO.getAllSupplierNames());
        targetCol.setText("Supplier Name");
        return switch (registrationType) {
            case "Credit" -> {
                addButton.setOnMouseClicked(mouseEvent -> addNewSupplierCreditMemo());
                yield new SupplierCreditMemoLoader();
            }
            case "Debit" -> {
                addButton.setOnMouseClicked(mouseEvent -> addNewSupplierDebitMemo());
                yield new SupplierDebitMemoLoader();
            }
            default -> {
                DialogUtils.showErrorMessage("Error", "Please contact your system administrator");
                yield null;
            }
        };
    }

    private void addNewSupplierCreditMemo() {
        openSupplierMemoForm("New Supplier Credit Memo", "addNewSupplierCreditMemo");
    }

    private void addNewSupplierDebitMemo() {
        openSupplierMemoForm("New Supplier Debit Memo", "addNewSupplierDebitMemo");
    }

    private void openSupplierMemoForm(String title, String methodName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SupplierCreditDebitForm.fxml"));
            Parent root = loader.load();

            SupplierCreditDebitFormController controller = loader.getController();
            controller.setCreditDebitListController(this);
            controller.getClass().getMethod(methodName).invoke(controller);

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException | ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    public void openCustomerSupplierSelection() {
    }
}