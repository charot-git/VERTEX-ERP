package com.vertex.vos;

import com.vertex.vos.Objects.Consolidation;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
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
        productName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductName()));
        productBrand.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductBrandString()));
        productCategory.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductCategoryString()));
        productUnit.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getUnitOfMeasurementString()));
        orderedQuantity.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getOrderedQuantity()).asObject());
        servedQuantity.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getServedQuantity()).asObject());

    }

    public void updateFields(ObservableList<ChecklistDTO> productsForChecklist) {
        docno.setText(consolidation.getConsolidationNo());
        status.setText(consolidation.getStatus().toString());
        createdDate.setValue(consolidation.getCreatedAt().toLocalDateTime().toLocalDate());
        checkerField.setText(consolidation.getCheckedBy().getUser_fname() + " " + consolidation.getCheckedBy().getUser_lname());
        checkListProducts.getItems().addAll(productsForChecklist);
    }

}
