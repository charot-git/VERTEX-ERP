package com.vertex.vos;

import com.vertex.vos.DAO.PickListDAO;
import com.vertex.vos.Enums.PickListStatus;
import com.vertex.vos.Objects.Branch;
import com.vertex.vos.Objects.PickList;
import com.vertex.vos.Objects.User;
import com.vertex.vos.Utilities.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collection;

public class PickListsController {

    @FXML
    private Button assignBrand;

    @FXML
    private Button confirmButton;

    @FXML
    private DatePicker dateFromFilter;

    @FXML
    private DatePicker dateToFilter;

    @FXML
    private TextField pickNoFilter;

    @FXML
    private TextField pickedByFilter;

    @FXML
    private TableColumn<PickList, String> branchSourceCol;

    @FXML
    private TableColumn<PickList, Timestamp> pickDateCol;

    @FXML
    private TableColumn<PickList, String> pickNoCol;

    @FXML
    private TableColumn<PickList, String> pickedByCol;

    @FXML
    private TableColumn<PickList, String> statusCol;

    @FXML
    private ComboBox<PickListStatus> statusFilter;

    @FXML
    private TableView<PickList> pickListTableView;

    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    private final PickListDAO pickListDAO = new PickListDAO();

    ObservableList<PickList> pickLists = FXCollections.observableArrayList();

    ObservableList<User> warehouseMen = FXCollections.observableArrayList(); //

    User selectedPicker;

    int PAGE_SIZE = 35;

    int currentPage = 0;

    @FXML
    private void initialize() {

        assignBrand.setDisable(true);

        pickListTableView.setItems(pickLists);

        pickNoCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPickNo()));
        pickedByCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPickedBy().getUser_fname() + " " + cellData.getValue().getPickedBy().getUser_lname()));
        pickDateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPickDate()));
        branchSourceCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBranch().getBranchName()));
        statusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().name()));

        statusFilter.getItems().addAll(PickListStatus.values());

        warehouseMen.setAll(employeeDAO.getAllEmployeesWhereDepartment(5));

        TextFields.bindAutoCompletion(pickedByFilter, warehouseMen.stream().map(e -> e.getUser_fname() + " " + e.getUser_lname()).toList());

        pickedByFilter.textProperty().addListener((observable, oldValue, newValue) -> {
            warehouseMen.stream().filter(e -> {
                String fullName = e.getUser_fname() + " " + e.getUser_lname();
                return fullName.equals(newValue);
            }).findFirst().ifPresent(e -> {
                selectedPicker = e;
                assignBrand.setDisable(false);
                assignBrand.setText("Assign Brands To " + selectedPicker.getUser_fname() + " " + selectedPicker.getUser_lname());
                assignBrand.setOnAction(event -> openWarehouseLinker(selectedPicker));
            });
        });



    }


    private void openWarehouseLinker(User employee) {
        if (employee != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("assignBrandToWarehouseMen.fxml"));
                Parent root = loader.load();
                AssignBrandToWarehouseMenController controller = loader.getController();
                controller.initData(employee);

                Stage stage = new Stage();
                stage.setTitle("Assign Brand To " + employee.getUser_fname() + " " + employee.getUser_lname());
                stage.setScene(new Scene(root));
                stage.setResizable(false);
                stage.initStyle(StageStyle.UTILITY);
                stage.show();
            } catch (IOException e) {
                DialogUtils.showErrorMessage("Error", "Unable to open warehouse linker.");
                e.printStackTrace();
            }
        } else {
            DialogUtils.showErrorMessage("Error", "Employee not found.");
        }
    }

    @Setter
    InternalOperationsContentController internalOperationsContentController;

    public void loadPickLists() {
        pickLists.setAll(pickListDAO.getAllPickLists(PAGE_SIZE, currentPage, pickNoFilter.getText(), selectedPicker, dateFromFilter.getValue() == null ? null : Timestamp.valueOf(dateFromFilter.getValue().atStartOfDay()), dateToFilter.getValue() == null ? null : Timestamp.valueOf(dateToFilter.getValue().atStartOfDay()), statusFilter.getValue()));

        if (pickLists.isEmpty()) {
            pickListTableView.setPlaceholder(new Label("No pick lists found."));
        }

        confirmButton.setOnAction(event -> addNewPickList());
    }

    Stage newPickListStage;

    private void addNewPickList() {
        if (newPickListStage == null || !newPickListStage.isShowing()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("PickListForm.fxml"));
                Parent root = loader.load();
                PickListFormController controller = loader.getController();
                controller.setPickListsController(this);
                controller.createNewPickList();
                newPickListStage = new Stage();
                newPickListStage.setTitle("Add New Pick List");
                newPickListStage.setScene(new Scene(root));
                newPickListStage.setMaximized(true);
                newPickListStage.show();
            } catch (IOException e) {
                DialogUtils.showErrorMessage("Error", "Unable to open pick list form.");
                e.printStackTrace();
            }
        }
        else {
            newPickListStage.show();
        }
    }

    public ObservableList<User> getWarehouseEmployee() {
        return warehouseMen;
    }
}
