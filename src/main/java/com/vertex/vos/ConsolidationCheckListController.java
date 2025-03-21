package com.vertex.vos;

import com.vertex.vos.Enums.ConsolidationStatus;
import com.vertex.vos.Enums.DispatchStatus;
import com.vertex.vos.Enums.SalesOrderStatus;
import com.vertex.vos.Objects.Consolidation;
import com.vertex.vos.Objects.DispatchPlan;
import com.vertex.vos.Objects.SalesOrder;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import lombok.Getter;
import lombok.Setter;

import java.net.URL;
import java.util.ResourceBundle;

public class ConsolidationCheckListController implements Initializable {

    public TableColumn<ChecklistDTO, String> productBrand;
    public TableColumn<ChecklistDTO, String> productCategory;
    public TableColumn<ChecklistDTO, String> productSupplier;
    @FXML
    private TableView<ChecklistDTO> checkListProducts;

    @FXML
    private TextField checkerField;

    @FXML
    private Button confirmButton;

    @FXML
    private DatePicker createdDate;

    @FXML
    private Label docno;

    @FXML
    private TableColumn<ChecklistDTO, Integer> orderedQuantity;

    @FXML
    private TableColumn<ChecklistDTO, String> productName;

    @FXML
    private TableColumn<ChecklistDTO, String> productUnit;

    @FXML
    private TableColumn<ChecklistDTO, Integer> servedQuantity;

    @FXML
    private Label status;
    @Getter
    @Setter
    Consolidation consolidation;

    @Setter
    ConsolidationListController consolidationListController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        productSupplier.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getSupplierName()));
        productName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductName()));
        productBrand.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductBrandString()));
        productCategory.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductCategoryString()));
        productUnit.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getUnitOfMeasurementString()));
        orderedQuantity.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getOrderedQuantity()).asObject());
        servedQuantity.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getServedQuantity()).asObject());

        checkListProducts.getSortOrder().addAll(productSupplier, productBrand, productCategory);



        confirmButton.setOnAction(event -> {
            consolidation.setStatus(ConsolidationStatus.PICKING);
            consolidation.getDispatchPlans().forEach(dispatchPlan -> dispatchPlan.setStatus(DispatchStatus.PICKING));
            consolidation.getStockTransfers().forEach(stockTransfer -> stockTransfer.setStatus("PICKING"));
            for (DispatchPlan dispatchPlan : consolidation.getDispatchPlans()) {
                for (SalesOrder salesOrder : dispatchPlan.getSalesOrders()) {
                    salesOrder.setOrderStatus(SalesOrderStatus.FOR_PICKING);
                }
            }
            if (consolidationListController.startPicking(consolidation)) {
                confirmButton.setDisable(true);
                status.setText(consolidation.getStatus().toString());
            }
        });
    }

    public void updateFields(ObservableList<ChecklistDTO> productsForChecklist) {
        docno.setText(consolidation.getConsolidationNo());
        status.setText(consolidation.getStatus().toString());
        createdDate.setValue(consolidation.getCreatedAt().toLocalDateTime().toLocalDate());
        checkerField.setText(consolidation.getCheckedBy().getUser_fname() + " " + consolidation.getCheckedBy().getUser_lname());
        checkListProducts.getItems().addAll(productsForChecklist);

        if (consolidation.getStatus().equals(ConsolidationStatus.PICKING)) {
            confirmButton.setDisable(true);
        }
    }

}
