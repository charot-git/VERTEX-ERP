package com.vertex.vos;

import com.vertex.vos.DAO.PickListDAO;
import com.vertex.vos.Enums.DocumentType;
import com.vertex.vos.Enums.PickListStatus;
import com.vertex.vos.Objects.Branch;
import com.vertex.vos.Objects.PickList;
import com.vertex.vos.Objects.PickListItem;
import com.vertex.vos.Objects.User;
import com.vertex.vos.Utilities.BranchDAO;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

import java.net.URL;
import java.sql.Timestamp;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PickListFormController implements Initializable {
    @FXML
    private Button confirmButton;

    @FXML
    private Button printButton;

    @FXML
    private CheckBox isPrintedCheckBox;

    @FXML
    private ComboBox<PickListStatus> statusComboBox;

    @FXML
    private DatePicker pickedDatePicker;

    @FXML
    private Label header;

    @FXML
    private TableColumn<PickListItem, Timestamp> createdAt;

    @FXML
    private TableColumn<PickListItem, String> docNoCol;

    @FXML
    private TableColumn<PickListItem, DocumentType> docTypeCol;

    @FXML
    private TableColumn<PickListItem, Integer> orderedQuantityCol;

    @FXML
    private TableColumn<PickListItem, Integer> pickedQuantityCol;

    @FXML
    private TableColumn<PickListItem, String> productNameCol;

    @FXML
    private TableColumn<PickListItem, String> productUnitCol;

    @FXML
    private TableColumn<PickListItem, String> statusCol;

    @FXML
    private TableView<PickListItem> pickListDetails;

    @FXML
    private TextField branchTextField;

    @FXML
    private TextField pickedByTextField;

    @Setter
    private PickListsController pickListsController;

    PickListDAO pickListDAO = new PickListDAO();

    BranchDAO branchDAO = new BranchDAO();

    PickList pickList;

    ObservableList<Branch> branches = FXCollections.observableArrayList();

    public void createNewPickList() {
        pickList = new PickList();
        pickList.setPickNo(pickListDAO.generateNextPickListNo());
        TextFields.bindAutoCompletion(branchTextField, branches.stream().map(Branch::getBranchName).toList());
        TextFields.bindAutoCompletion(pickedByTextField, pickListsController.getWarehouseEmployee().stream().map(u -> u.getUser_fname() + " " + u.getUser_lname()).collect(Collectors.toList()));
        header.setText(pickList.getPickNo());

        branchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            pickList.setBranch(branches.stream().filter(b -> b.getBranchName().equals(newValue)).findFirst().orElse(null));
            loadPickListItems();
        });
        pickedByTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            pickList.setPickedBy(pickListsController.getWarehouseEmployee().stream().filter(u -> (u.getUser_fname() + " " + u.getUser_lname()).equals(newValue)).findFirst().orElse(null));
            loadPickListItems();
        });
        pickedDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            pickList.setPickDate(Timestamp.valueOf(newValue.atStartOfDay()));
            loadPickListItems();
        });
        statusComboBox.valueProperty().addListener((observable, oldValue, newValue) -> pickList.setStatus(newValue));
        isPrintedCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> pickList.setPrinted(newValue));

        pickListDetails.setItems(pickList.getPickListItems());
    }

    private void loadPickListItems() {
        if (pickList.getBranch() != null && pickList.getPickedBy() != null && pickList.getPickDate() != null) {
            pickListDetails.setPlaceholder(new ProgressIndicator());
            CompletableFuture.supplyAsync(() -> pickListDAO.getPickListItemsForEncoding(pickList))
                    .thenAccept(pickListItems -> {
                        pickList.setPickListItems(pickListItems);
                        pickListDetails.setItems(pickList.getPickListItems());
                        if (pickListItems.isEmpty()) {
                            pickListDetails.setPlaceholder(new Label("No items to pick."));
                        }
                    });
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupColumns();
        statusComboBox.setItems(FXCollections.observableArrayList(PickListStatus.values()));
        initializeEncodingData();
    }

    private void initializeEncodingData() {
        branches.setAll(branchDAO.getAllActiveBranches());
    }

    private void setupColumns() {
        createdAt.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCreatedAt()));
        docNoCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDocNo()));
        docTypeCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDocType()));
        orderedQuantityCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getOrderedQuantity()));
        pickedQuantityCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPickedQuantity()));
        productNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductName()));
        productUnitCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getUnitOfMeasurementString()));
        statusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().toString()));
    }
}
