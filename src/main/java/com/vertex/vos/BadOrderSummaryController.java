package com.vertex.vos;

import com.vertex.vos.DAO.SalesReturnDAO;
import com.vertex.vos.Objects.SalesReturnDetail;
import com.vertex.vos.Objects.Salesman;
import com.vertex.vos.Utilities.SalesmanDAO;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.textfield.TextFields;

import java.net.URL;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class BadOrderSummaryController implements Initializable {

    public TableView<SalesReturnDetail> boSummaryTable;
    public TableColumn<SalesReturnDetail, String> productNameCol;
    public TableColumn<SalesReturnDetail, String> productUnitCol;
    public TableColumn<SalesReturnDetail, Timestamp> dateOfTransactionCol;
    public TableColumn<SalesReturnDetail, String> documentNoCol;
    public TableColumn<SalesReturnDetail, String> customerNameCol;
    public TableColumn<SalesReturnDetail, Integer> quantityCol;
    @FXML
    private Tab badOrderCheckingTab;

    @FXML
    private Tab badOrderSummaryTab;

    @FXML
    private BorderPane boCheckingBorderPane;

    @FXML
    private DatePicker dateFromFilter;

    @FXML
    private DatePicker dateToFilter;

    @FXML
    private TextField salesmanFilter;


    @FXML
    private TabPane tabPane;

    ObservableList<SalesReturnDetail> salesReturnDetails = FXCollections.observableArrayList();

    SalesmanDAO salesmanDAO = new SalesmanDAO();

    ObservableList<Salesman> salesmanList = FXCollections.observableArrayList(salesmanDAO.getAllSalesmen());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumnsForBadOrderSummary();
    }

    private void setupTableColumnsForBadOrderSummary() {
        productNameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getProduct().getProductName()));
        productUnitCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getProduct().getUnitOfMeasurementString()));
        dateOfTransactionCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSalesReturn().getReturnDate()));
        documentNoCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getSalesReturn().getReturnNumber()));
        customerNameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getSalesReturn().getCustomer().getStoreName()));
        quantityCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getQuantity()));
        boSummaryTable.setItems(salesReturnDetails);
    }

    public void initializeDataForBadProducts() {

        TextFields.bindAutoCompletion(salesmanFilter, salesmanList.stream().map(Salesman::getSalesmanName).toList());

        salesmanFilter.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                checkFieldsForLoadingData();
                selectedSalesman = salesmanList.stream().filter(salesman -> salesman.getSalesmanName().equals(newValue)).findFirst().orElse(null);
            }
        });
        dateFromFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                checkFieldsForLoadingData();
            }
        });
        dateToFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                checkFieldsForLoadingData();
            }
        });
    }

    SalesReturnDAO salesReturnDAO = new SalesReturnDAO();
    Salesman selectedSalesman;

    private void checkFieldsForLoadingData() {
        if (selectedSalesman != null && dateFromFilter.getValue() != null && dateToFilter.getValue() != null) {
            boSummaryTable.setPlaceholder(new ProgressIndicator());
            Timestamp dateFrom = Timestamp.valueOf(dateFromFilter.getValue().atStartOfDay());
            Timestamp dateTo = Timestamp.valueOf(dateToFilter.getValue().atStartOfDay());

            CompletableFuture.supplyAsync(() -> salesReturnDAO.getBadOrderDetails(selectedSalesman, dateFrom, dateTo))
                    .thenAcceptAsync(details -> {
                        salesReturnDetails.setAll(details);


                        // Update UI on JavaFX Application Thread
                        Platform.runLater(() -> {
                        });
                    });
        }
    }


}

