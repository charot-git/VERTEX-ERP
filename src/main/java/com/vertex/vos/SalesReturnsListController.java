package com.vertex.vos;

import com.vertex.vos.DAO.SalesReturnDAO;
import com.vertex.vos.Objects.ComboBoxFilterUtil;
import com.vertex.vos.Objects.SalesReturn;
import com.vertex.vos.Utilities.CustomerDAO;
import com.vertex.vos.Utilities.DialogUtils;
import javafx.beans.property.SimpleDoubleProperty;
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

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ResourceBundle;

public class SalesReturnsListController implements Initializable {

    public Button addNew;
    public TableColumn<SalesReturn, String> statusColumn;
    @FXML
    private TableColumn<SalesReturn, String> customerColumn;

    @FXML
    private TableColumn<SalesReturn, Timestamp> returnDateColumn;

    @FXML
    private TextField returnNo;

    @FXML
    private TableColumn<SalesReturn, String> returnNoColumn;

    @FXML
    private ComboBox<String> storeNameFilter;

    @FXML
    private TableColumn<SalesReturn, Double> totalAmountColumn;
    @FXML
    private TableView<SalesReturn> salesReturnTable;  // Added TableView for displaying sales returns


    SalesReturnDAO salesReturnDAO = new SalesReturnDAO();
    CustomerDAO customerDAO = new CustomerDAO();

    public void loadSalesReturn() {
        ObservableList<SalesReturn> salesReturns = FXCollections.observableList(salesReturnDAO.getAllSalesReturns());
        salesReturnTable.setItems(salesReturns);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeTableColumns();
        ObservableList<String> storeNames = customerDAO.getCustomerStoreNames();
        storeNameFilter.setItems(storeNames);
        ComboBoxFilterUtil.setupComboBoxFilter(storeNameFilter, storeNames);
        addNew.setOnMouseClicked(event -> {
            openNewSalesReturnForm();
        });
    }

    private void openNewSalesReturnForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SalesReturnForm.fxml"));
            Parent root = loader.load();
            SalesReturnFormController controller = loader.getController();
            int salesReturnNo = salesReturnDAO.generateSalesReturnNo();
            Stage stage = new Stage();
            stage.setTitle("New Sales Return" + salesReturnNo);
            stage.setMaximized(true);
            controller.createNewSalesReturn(stage, salesReturnNo, this);

            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Unable to open receiving.");
            e.printStackTrace();
        }

    }

    private void initializeTableColumns() {
        returnNoColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReturnNumber()));
        customerColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomer().getStoreName()));
        returnDateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getReturnDate()));
        totalAmountColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotalAmount()).asObject());
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
    }
}
