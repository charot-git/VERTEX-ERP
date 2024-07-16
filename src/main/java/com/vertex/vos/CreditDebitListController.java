package com.vertex.vos;

import com.vertex.vos.Objects.CreditDebitMemo;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class CreditDebitListController implements Initializable {
    public TableColumn<CreditDebitMemo, String> targetCol;
    public TableColumn<CreditDebitMemo, String> reasonCol;
    @FXML
    private TableColumn<CreditDebitMemo, String> documentNumberCol;
    @FXML
    private TableColumn<CreditDebitMemo, Double> amountCol;
    @FXML
    private TableColumn<CreditDebitMemo, String> statusCol;
    @FXML
    private TableColumn<CreditDebitMemo, String> chartOfAccountCol;
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
    private TableView<CreditDebitMemo> memoTable;

    private AnchorPane contentPane;
    private MemoDataLoader memoDataLoader;

    SupplierDAO supplierDAO = new SupplierDAO();
    CustomerDAO customerDAO = new CustomerDAO();

    private String targetType; // Can be "Supplier" or "Customer"
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

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    @FXML
    public void openCustomerSupplierSelection() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CustomerSupplierSelection.fxml"));
            Parent root = loader.load();

            Stage stage = createPopupStage(root); // Create a new stage for the pop-up
            CustomerSupplierSelectionController controller = loader.getController();
            controller.setCreditDebitListController(this); // Pass the reference to this controller

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your application's needs
        }
    }

    private Stage createPopupStage(Parent root) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle("Select Customer or Supplier");
        stage.setScene(new Scene(root));
        return stage;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
        creditLabel.setText(targetType + " Name");
        updateHeader(); // Update the header based on targetType and registrationType
        loadMemoData(); // Load memo data based on targetType and registrationType
    }

    public void setRegistrationType(String registrationType) {
        this.registrationType = registrationType;
        updateHeader(); // Update the header based on targetType and registrationType
        loadMemoData(); // Load memo data based on targetType and registrationType
    }

    private void updateHeader() {
        if (targetType != null && registrationType != null) {
            header.setText(targetType + " " + registrationType + " Memo");
        }
    }

    private void loadMemoData() {
        if (targetType != null && registrationType != null) {
            memoDataLoader = createMemoDataLoader(targetType, registrationType);
            if (memoDataLoader != null) {
                memoDataLoader.loadMemoData(memoTable); // Load memo data based on targetType and registrationType
            } else {
                System.err.println("No MemoDataLoader found for targetType: " + targetType + ", registrationType: " + registrationType);
            }
        }
    }

    private MemoDataLoader createMemoDataLoader(String targetType, String registrationType) {
        switch (targetType) {
            case "Customer":
                creditTarget.setItems(customerDAO.getCustomerStoreNames());
                targetCol.setText("Store Name");
                switch (registrationType) {
                    case "Credit":
                        addButton.setOnMouseClicked(mouseEvent -> addNewCustomerCreditMemo());
                        return new CustomerCreditMemoLoader();
                    case "Debit":
                        addButton.setOnMouseClicked(mouseEvent -> addNewCustomerDebitMemo());
                        return new CustomerDebitMemoLoader();
                }
                break;
            case "Supplier":
                creditTarget.setItems(supplierDAO.getAllSupplierNames());
                targetCol.setText("Supplier Name");
                switch (registrationType) {
                    case "Credit":
                        addButton.setOnMouseClicked(mouseEvent -> addNewSupplierCreditMemo());
                        return new SupplierCreditMemoLoader();
                    case "Debit":
                        addButton.setOnMouseClicked(mouseEvent -> addNewSupplierDebitMemo());
                        return new SupplierDebitMemoLoader();
                }
                break;
            default:
                DialogUtils.showErrorMessage("Error", "Please contact your system administrator");
                break;
        }
        return null; // Handle if no loader is found
    }

    private void addNewCustomerDebitMemo() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CreditDebitForm.fxml"));
            Parent root = loader.load();

            CreditDebitFormController controller = loader.getController();
            controller.setCreditDebitListController(this);
            controller.addNewCustomerDebitMemo();

            Stage stage = new Stage();
            stage.setTitle("New Supplier Credit Memo");
            stage.setScene(new Scene(root));

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addNewCustomerCreditMemo() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CreditDebitForm.fxml"));
            Parent root = loader.load();

            CreditDebitFormController controller = loader.getController();
            controller.setCreditDebitListController(this);
            controller.addNewCustomerCreditMemo();

            Stage stage = new Stage();
            stage.setTitle("New Supplier Credit Memo");
            stage.setScene(new Scene(root));

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addNewSupplierCreditMemo() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CreditDebitForm.fxml"));
            Parent root = loader.load();

            CreditDebitFormController controller = loader.getController();
            controller.setCreditDebitListController(this);
            controller.addNewSupplierCreditMemo();

            Stage stage = new Stage();
            stage.setTitle("New Supplier Credit Memo");
            stage.setScene(new Scene(root));

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addNewSupplierDebitMemo() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CreditDebitForm.fxml"));
            Parent root = loader.load();

            CreditDebitFormController controller = loader.getController();
            controller.setCreditDebitListController(this);
            controller.addNewSupplierDebitMemo();

            Stage stage = new Stage();
            stage.setTitle("New Supplier Debit Memo");
            stage.setScene(new Scene(root));

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your application's needs
        }
    }


}
