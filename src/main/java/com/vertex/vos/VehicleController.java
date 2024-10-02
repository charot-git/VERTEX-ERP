package com.vertex.vos;

import com.vertex.vos.Objects.Vehicle;
import com.vertex.vos.Utilities.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class VehicleController implements Initializable {

    @FXML
    private Button confirmButton;

    @FXML
    private Label headerLabel;

    @FXML
    private TextField maxLoad;

    @FXML
    private ComboBox<String> status;

    @FXML
    private TextField truckPlate;

    @FXML
    private ComboBox<String> vehicleType;

    @FXML
    private ComboBox<String> branchLink;

    private final VehicleDAO vehicleDAO = new VehicleDAO();
    private final BranchDAO branchDAO = new BranchDAO();

    private TableManagerController tableManagerController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set up input restrictions
        TextFieldUtils.addDoubleInputRestriction(maxLoad);

        // Populate ComboBoxes with data
        vehicleType.setItems(vehicleDAO.getAllVehicleTypeNames());
        branchLink.setItems(branchDAO.getAllVehicleBranches());

        // Initialize ComboBox for status
        status.setItems(FXCollections.observableArrayList(
                "Active", "Inactive", "Under Maintenance", "Retired", "In Use"));
    }

    void setTableManager(TableManagerController tableManagerController) {
        this.tableManagerController = tableManagerController;
    }

    void addNewVehicle() {
        headerLabel.setText("Register Vehicle");
        confirmButton.setOnMouseClicked(mouseEvent -> registerVehicle());
    }

    private boolean validateFields() {
        if (truckPlate.getText().isEmpty()) {
            DialogUtils.showErrorMessage("Error", "Please enter a plate number.");
            return false;
        }

        if (vehicleType.getSelectionModel().getSelectedItem() == null) {
            DialogUtils.showErrorMessage("Error", "Please select a vehicle type.");
            return false;
        }

        if (maxLoad.getText().isEmpty() || !TextFieldUtils.isNumeric(maxLoad.getText())) {
            DialogUtils.showErrorMessage("Error", "Please enter a valid maximum load.");
            return false;
        }

        if (status.getSelectionModel().getSelectedItem() == null) {
            DialogUtils.showErrorMessage("Error", "Please select a status.");
            return false;
        }

        if (branchLink.getSelectionModel().getSelectedItem() == null) {
            DialogUtils.showErrorMessage("Error", "Please select a branch.");
            return false;
        }

        return true;
    }

    private void registerVehicle() {
        if (!validateFields()) {
            return;
        }

        String plate = truckPlate.getText();
        String alertMessage = String.format("Register %s?", plate);
        ConfirmationAlert alert = new ConfirmationAlert("Register Vehicle", alertMessage, "Please double check values", true);
        boolean confirmed = alert.showAndWait();
        if (confirmed) {
            Vehicle newVehicle = createVehicleFromFields();
            if (vehicleDAO.insertVehicle(newVehicle)) {
                String successMessage = String.format("%s has been added", plate);
                DialogUtils.showCompletionDialog("Success", successMessage);
                tableManagerController.loadVehicleTable();
            } else {
                DialogUtils.contactYourDeveloper("Vehicle");
            }
        }
        confirmButton.setText("Add");
    }

    private Vehicle createVehicleFromFields() {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehiclePlate(truckPlate.getText());
        vehicle.setVehicleType(vehicleDAO.getVehicleTypeIdByName(vehicleType.getSelectionModel().getSelectedItem()));
        vehicle.setMinimumLoad(Double.parseDouble(maxLoad.getText()));
        vehicle.setStatus(status.getSelectionModel().getSelectedItem());
        vehicle.setBranchId(branchDAO.getBranchIdByName(branchLink.getSelectionModel().getSelectedItem()));
        return vehicle;
    }

    void initData(Vehicle selectedVehicle) {
        if (selectedVehicle != null) {
            headerLabel.setText("Update Vehicle");
            confirmButton.setOnMouseClicked(mouseEvent -> initializeUpdate(selectedVehicle));

            truckPlate.setText(selectedVehicle.getVehiclePlate());
            vehicleType.getSelectionModel().select(selectedVehicle.getVehicleTypeString());
            maxLoad.setText(String.valueOf(selectedVehicle.getMinimumLoad()));
            status.getSelectionModel().select(selectedVehicle.getStatus());
            branchLink.getSelectionModel().select(branchDAO.getBranchNameById(selectedVehicle.getBranchId()));
        }

        confirmButton.setText("Update");
    }

    private void initializeUpdate(Vehicle selectedVehicle) {
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Update Vehicle", "Update " + truckPlate.getText() + "?", "Please double check values", true);
        boolean confirmed = confirmationAlert.showAndWait();
        if (confirmed) {
            selectedVehicle.setVehiclePlate(truckPlate.getText());
            selectedVehicle.setVehicleType(vehicleDAO.getVehicleTypeIdByName(vehicleType.getSelectionModel().getSelectedItem()));
            selectedVehicle.setMinimumLoad(Double.parseDouble(maxLoad.getText()));
            selectedVehicle.setStatus(status.getSelectionModel().getSelectedItem());
            selectedVehicle.setBranchId(branchDAO.getBranchIdByName(branchLink.getSelectionModel().getSelectedItem()));

            if (vehicleDAO.updateVehicle(selectedVehicle)) {
                DialogUtils.showCompletionDialog("Success", selectedVehicle.getVehiclePlate() + " has been updated");
                tableManagerController.loadVehicleTable();
            } else {
                DialogUtils.contactYourDeveloper("Vehicle");
            }
        }
    }
}
