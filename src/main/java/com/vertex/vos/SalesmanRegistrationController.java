package com.vertex.vos;

import com.vertex.vos.Constructors.Salesman;
import com.vertex.vos.Constructors.User;
import com.vertex.vos.Constructors.UserSession;
import com.vertex.vos.Utilities.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class SalesmanRegistrationController implements Initializable {

    @FXML
    private ComboBox<String> baranggayComboBox;

    @FXML
    private Label baranggayErr;

    @FXML
    private CheckBox canCollect;

    @FXML
    private ComboBox<String> cityComboBox;

    @FXML
    private Label cityErr;

    @FXML
    private ComboBox<String> companyCodeComboBox;

    @FXML
    private Label companyCodeErr;
    @FXML
    private ComboBox<String> priceTypeComboBox;

    @FXML
    private Label priceTypeErr;

    @FXML
    private Button confirmButton;

    @FXML
    private Label confirmationLabel;

    @FXML
    private DatePicker dateAddedDatePicker;

    @FXML
    private Label dateAddedErr;

    @FXML
    private Label dateOfFormationLabel1;

    @FXML
    private ComboBox<String> divisionComboBox;

    @FXML
    private Label divisionErr;

    @FXML
    private ComboBox<String> inventoryDayComboBox;

    @FXML
    private Label inventoryDayErr;

    @FXML
    private CheckBox isActive;

    @FXML
    private CheckBox isInventory;

    @FXML
    private ComboBox<String> operationComboBox;

    @FXML
    private Label operationErr;

    @FXML
    private ComboBox<String> provinceComboBox;
    @FXML
    private ComboBox<String> branchComboBox;
    @FXML
    private Label branchErr;
    @FXML
    private Label provinceErr;

    @FXML
    private Label salesmanCodeErr;

    @FXML
    private TextField salesmanCodeTextField;

    @FXML
    private Label salesmanContactNoErr;

    @FXML
    private TextField salesmanContactNoTextField;

    @FXML
    private Label salesmanEmailErr;

    @FXML
    private TextField salesmanEmailTextField;

    @FXML
    private ImageView salesmanLogo;

    @FXML
    private Label salesmanNameErr;

    @FXML
    private ComboBox<String> salesmanNameTextField;
    @FXML
    private ComboBox<String> supplierComboBox;

    @FXML
    private Label supplierTypeErr;

    @FXML
    private Label tinNumberErr;

    @FXML
    private TextField tinNumberTextField;

    @FXML
    private Label truckPlateErr;

    @FXML
    private VBox infoBox;

    @FXML
    private TextField truckPlateTextField;
    EmployeeDAO employeeDAO = new EmployeeDAO();
    CompanyDAO companyDAO = new CompanyDAO();
    SupplierDAO supplierDAO = new SupplierDAO();
    DivisionDAO divisionDAO = new DivisionDAO();
    OperationDAO operationDAO = new OperationDAO();
    BranchDAO branchDAO = new BranchDAO();

    SalesmanDAO salesmanDAO = new SalesmanDAO();

    void salesmanRegistration() {
        salesmanNameTextField.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                User selectedUser = employeeDAO.getUserByFullName(newValue);
                if (selectedUser != null) {
                    salesmanEmailTextField.setText(selectedUser.getUser_email());
                    salesmanContactNoTextField.setText(selectedUser.getUser_contact());
                    tinNumberTextField.setText(selectedUser.getUser_tin());
                    provinceComboBox.setValue(selectedUser.getUser_province());
                    cityComboBox.setValue(selectedUser.getUser_city());
                    baranggayComboBox.setValue(selectedUser.getUser_brgy());
                    infoBox.setDisable(true);
                } else {
                    salesmanEmailTextField.setText("");
                    salesmanContactNoTextField.setText("");
                    tinNumberTextField.setText("");
                    provinceComboBox.setValue("");
                    cityComboBox.setValue("");
                    baranggayComboBox.setValue("");
                    infoBox.setDisable(false);
                }

                confirmButton.setOnMouseClicked(event -> registerSalesman(selectedUser));
            }
        });

        companyCodeComboBox.setItems(companyDAO.getAllCompanyNames());
        supplierComboBox.setItems(supplierDAO.getAllSupplierNames());
        divisionComboBox.setItems(divisionDAO.getAllDivisionNames());
        operationComboBox.setItems(operationDAO.getAllOperationNames());
        branchComboBox.setItems(branchDAO.getAllBranchNames());

        ObservableList<String> priceType = FXCollections.observableArrayList("A", "B", "C", "D", "E");
        ObservableList<String> daysOfWeek = FXCollections.observableArrayList(
                "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");
        inventoryDayComboBox.setItems(daysOfWeek);
        priceTypeComboBox.setItems(priceType);
        isInventory.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                inventoryDayComboBox.setDisable(!newValue);
            }
        });

    }

    private void registerSalesman(User selectedUser) {
        Salesman salesman = new Salesman();
        salesman.setEmployeeId(selectedUser.getUser_id());
        salesman.setSalesmanCode(salesmanCodeTextField.getText());
        salesman.setSalesmanName(salesmanNameTextField.getSelectionModel().getSelectedItem());
        salesman.setTruckPlate(truckPlateTextField.getText());
        salesman.setDivisionId(divisionDAO.getDivisionIdByName(divisionComboBox.getSelectionModel().getSelectedItem()));
        salesman.setBranchCode(branchDAO.getBranchIdByName(branchComboBox.getSelectionModel().getSelectedItem()));
        salesman.setOperation(operationDAO.getOperationIdByName(operationComboBox.getSelectionModel().getSelectedItem()));
        String selectedDay = inventoryDayComboBox.getSelectionModel().getSelectedItem();
        int inventoryDay = 0;

        switch (selectedDay) {
            case "MONDAY":
                inventoryDay = 1;
                break;
            case "TUESDAY":
                inventoryDay = 2;
                break;
            case "WEDNESDAY":
                inventoryDay = 3;
                break;
            case "THURSDAY":
                inventoryDay = 4;
                break;
            case "FRIDAY":
                inventoryDay = 5;
                break;
            case "SATURDAY":
                inventoryDay = 6;
                break;
            case "SUNDAY":
                inventoryDay = 7;
                break;
            default:
                // Handle invalid selection or set default value
                break;
        }
        salesman.setInventoryDay(inventoryDay);
        salesman.setPriceType(priceTypeComboBox.getSelectionModel().getSelectedItem());
        salesman.setActive(isActive.isSelected());
        salesman.setInventory(isInventory.isSelected());
        salesman.setCanCollect(canCollect.isSelected());
        salesman.setEncoderId(UserSession.getInstance().getUserId());

        LocalDate selectedDate = dateAddedDatePicker.getValue();
        if (selectedDate != null) {
            LocalDateTime modifiedDateTime = selectedDate.atStartOfDay();
            salesman.setModifiedDate(modifiedDateTime);
        } else {
            salesman.setModifiedDate(LocalDateTime.now());
        }

        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Salesman Registration", "Register " + selectedUser.getUser_fname() + " as a salesman?", "", false);
        boolean confirmed = confirmationAlert.showAndWait();
        if (confirmed) {
            boolean success = salesmanDAO.createSalesman(salesman);
            if (success) {
                DialogUtils.showConfirmationDialog("Registration Success", salesman.getSalesmanName());
                tableManagerController.loadSalesmanTable();
            } else {
                DialogUtils.showErrorMessage("Error", "Something went wrong");
            }
        } else {
            DialogUtils.showErrorMessage("Cancelled", "Cancelled registration of salesman");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        salesmanNameTextField.setItems(employeeDAO.getAllEmployeeNamesWhereDepartment(6));
    }

    TableManagerController tableManagerController;

    public void setTableManager(TableManagerController tableManagerController) {
        this.tableManagerController = tableManagerController;
    }

    public void setSalesmanData(Salesman rowData) {
        if (rowData != null) {
            salesmanNameTextField.setValue(rowData.getSalesmanName());
            salesmanCodeTextField.setText(rowData.getSalesmanCode());
            truckPlateTextField.setText(rowData.getTruckPlate());
            divisionComboBox.setValue(divisionDAO.getDivisionNameById(rowData.getDivisionId()));
            branchComboBox.setValue(branchDAO.getBranchNameById(rowData.getBranchCode()));
            operationComboBox.setValue(operationDAO.getOperationNameById(rowData.getOperation()));
            inventoryDayComboBox.setValue(getDayOfWeek(rowData.getInventoryDay()));
            priceTypeComboBox.setValue(rowData.getPriceType());
            isActive.setSelected(rowData.isActive());
            isInventory.setSelected(rowData.isInventory());
            canCollect.setSelected(rowData.isCanCollect());
            companyCodeComboBox.setValue(String.valueOf(rowData.getCompanyCode()));
            supplierComboBox.setValue(supplierDAO.getSupplierNameById(rowData.getSupplierCode()));


            User selectedUser = employeeDAO.getUserByFullName(rowData.getSalesmanName());
            if (selectedUser != null) {
                salesmanEmailTextField.setText(selectedUser.getUser_email());
                salesmanContactNoTextField.setText(selectedUser.getUser_contact());
                tinNumberTextField.setText(selectedUser.getUser_tin());
                provinceComboBox.setValue(selectedUser.getUser_province());
                cityComboBox.setValue(selectedUser.getUser_city());
                baranggayComboBox.setValue(selectedUser.getUser_brgy());
                dateAddedDatePicker.setValue(selectedUser.getUser_dateOfHire().toLocalDate());
            }
        }
    }

    private String getDayOfWeek(int day) {
        switch (day) {
            case 1:
                return "MONDAY";
            case 2:
                return "TUESDAY";
            case 3:
                return "WEDNESDAY";
            case 4:
                return "THURSDAY";
            case 5:
                return "FRIDAY";
            case 6:
                return "SATURDAY";
            case 7:
                return "SUNDAY";
            default:
                return "";
        }
    }


}
