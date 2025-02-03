package com.vertex.vos;

import com.vertex.vos.Objects.Collection;
import com.vertex.vos.Objects.Salesman;
import com.vertex.vos.Objects.User;
import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.EmployeeDAO;
import com.vertex.vos.Utilities.SalesmanDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class CollectionFormController {

    public ButtonBar buttonBar;
    @FXML
    private Button addAdjustmentButton;

    @FXML
    private Button addInvoiceButton;

    @FXML
    private Button addMemoButton;

    @FXML
    private Button addPaymentButton;

    @FXML
    private Button addReturnsButton;

    @FXML
    private TableColumn<?, ?> amountColInv;

    @FXML
    private TableColumn<?, ?> amountColMem;

    @FXML
    private DatePicker collectionDateDatePicker;

    @FXML
    private TableView<?> collectionDetailsTableView;

    @FXML
    private TextField collectorNameTextField;

    @FXML
    private TableColumn<?, ?> customeCodeColMem;

    @FXML
    private TableColumn<?, ?> customerCodeColInv;

    @FXML
    private TableColumn<?, ?> customerCodeColRet;

    @FXML
    private DatePicker dateEncodedDatePicker;

    @FXML
    private Label docNo;

    @FXML
    private TableColumn<?, ?> docNoColInv;

    @FXML
    private TableColumn<?, ?> invoiceDateColInv;

    @FXML
    private TableColumn<?, ?> invoiceNoColInv;

    @FXML
    private TableColumn<?, ?> invoiceTypeColInv;

    @FXML
    private TableColumn<?, ?> memoDateColMem;

    @FXML
    private TableColumn<?, ?> memoNumberColMem;

    @FXML
    private Tab memoTab;

    @FXML
    private TableView<?> memoTable;

    @FXML
    private TableColumn<?, ?> memoTypeColMem;

    @FXML
    private Label paymentBalance;

    @FXML
    private TableColumn<?, ?> pendingColMem;

    @FXML
    private Button postButton;

    @FXML
    private TableColumn<?, ?> reasonColMem;

    @FXML
    private TextArea remarks;

    @FXML
    private TableColumn<?, ?> remarksColInv;

    @FXML
    private TableColumn<?, ?> returnNoColRet;

    @FXML
    private Tab returnsTab;

    @FXML
    private TableView<?> returnsTable;

    @FXML
    private Tab salesInvoiceTab;

    @FXML
    private TableView<?> salesInvoiceTable;

    @FXML
    private TableColumn<?, ?> salesTypeColInv;

    @FXML
    private TextField salesmanNameTextField;

    @FXML
    private Button saveButton;

    @FXML
    private TableColumn<?, ?> storeNameColInv;

    @FXML
    private TableColumn<?, ?> storeNameColMem;

    @FXML
    private TableColumn<?, ?> storeNameColRet;

    @FXML
    private TableColumn<?, ?> supplierColMem;

    @FXML
    private Label transactionBalance;

    @FXML
    private TabPane transactionTabPane;

    SalesmanDAO salesmanDAO = new SalesmanDAO();
    EmployeeDAO employeeDAO = new EmployeeDAO();

    Salesman salesman;
    User selectedEmployee;
    Collection collection;

    CollectionListController collectionListController;

    Stage parentStage;

    public void createCollection(Stage stage, int collectionNumber, CollectionListController collectionListController) {
        collection = new Collection();

        collection.setDocNo("CEX-" + collectionNumber);

        this.collectionListController = collectionListController;
        this.parentStage = stage;
        List<Salesman> salesmen = salesmanDAO.getAllSalesmen();
        List<String> salesmanNames = salesmen.stream().map(Salesman::getSalesmanName).toList();

        List<User> employees = employeeDAO.getAllEmployees();
        List<String> employeeNames = employees.stream()
                .map(e -> e.getUser_fname() + " " + e.getUser_lname())
                .toList();

        TextFields.bindAutoCompletion(salesmanNameTextField, salesmanNames);
        TextFields.bindAutoCompletion(collectorNameTextField, employeeNames);

        buttonBar.getButtons().remove(postButton);

        salesmanNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {

            salesman = salesmen.stream()
                    .filter(s -> s.getSalesmanName().equals(salesmanNameTextField.getText()))
                    .findFirst()
                    .orElse(null);

            collection.setSalesmanId(salesman);
            assert salesman != null;
            collectorNameTextField.setText(salesman.getSalesmanName());

        });
        collectorNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            selectedEmployee = employees.stream()
                    .filter(e -> (e.getUser_fname() + " " + e.getUser_lname()).equals(collectorNameTextField.getText()))
                    .findFirst()
                    .orElse(null);
            assert selectedEmployee != null;
            collection.setCollectedBy(selectedEmployee);
        });

        addPaymentButton.setOnMouseClicked(event -> {
            openPaymentForm();
        });
    }

    private void openPaymentForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CollectionPaymentForm.fxml"));
            Parent root = loader.load();
            CollectionPaymentFormController controller = loader.getController();
            Stage stage = new Stage();
            stage.setTitle("Collection Payment for " + collection.getDocNo());
            stage.setMaximized(false);
            stage.setResizable(false);
            controller.createNewCollectionPayment(parentStage, stage ,collection, this);

            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Unable to open collection.");
            e.printStackTrace();
        }

    }

}
