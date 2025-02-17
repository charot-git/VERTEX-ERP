package com.vertex.vos;

import com.vertex.vos.DAO.SalesReturnDAO;
import com.vertex.vos.Objects.*;
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
import lombok.Setter;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ResourceBundle;

public class SalesReturnsListController implements Initializable {

    public Button addNew;
    public TableColumn<SalesReturn, String> statusColumn;
    public TableColumn<SalesReturn, String> salesmanColumn;
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
        addNew.setOnMouseClicked(event -> {
            openNewSalesReturnForm();
        });
        salesReturnTable.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                SalesReturn selectedSalesReturn = salesReturnTable.getSelectionModel().getSelectedItem();
                if (selectedSalesReturn != null) {
                    openExistingSalesReturnForm(selectedSalesReturn);
                }
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeTableColumns();
        ObservableList<String> storeNames = customerDAO.getCustomerStoreNames();
        storeNameFilter.setItems(storeNames);
        ComboBoxFilterUtil.setupComboBoxFilter(storeNameFilter, storeNames);
    }

    private void openExistingSalesReturnForm(SalesReturn selectedSalesReturn) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SalesReturnForm.fxml"));
            Parent root = loader.load();
            SalesReturnFormController controller = loader.getController();
            SalesReturn salesReturn = salesReturnDAO.getSalesReturnByReturnNumber(selectedSalesReturn.getReturnNumber());
            controller.loadSalesReturn(salesReturn, this);
            Stage stage = new Stage();
            stage.setTitle("Sales Return " + selectedSalesReturn.getReturnNumber());
            stage.setMaximized(true);
            controller.setStage(stage);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Unable to open receiving.");
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
        salesmanColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSalesman().getSalesmanName()));
        returnDateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getReturnDate()));
        totalAmountColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotalAmount()).asObject());
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
    }


    public void loadSalesReturnForSelection(Salesman selectedSalesman, Customer selectedCustomer, SalesInvoiceHeader salesInvoiceHeader, SalesInvoiceTemporaryController salesInvoiceTemporaryController) {
        ObservableList<SalesReturn> salesReturnsForSelection = FXCollections.observableList(salesReturnDAO.getSalesReturnsForSelection(selectedSalesman, selectedCustomer, salesInvoiceHeader));
        salesReturnTable.setItems(salesReturnsForSelection);
        storeNameFilter.setValue(selectedCustomer.getStoreName());

        salesReturnTable.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                SalesReturn selectedSalesReturn = salesReturnTable.getSelectionModel().getSelectedItem();
                if (selectedSalesReturn != null) {
                    try {
                        salesInvoiceTemporaryController.salesReturn = salesReturnDAO.getSalesReturnByReturnNumber(selectedSalesReturn.getReturnNumber());
                        salesInvoiceTemporaryController.loadSalesReturnDetails();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    stage.close();
                }
            }
        });
    }

    @Setter
    Stage stage;
}
