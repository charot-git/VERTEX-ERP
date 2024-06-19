package com.vertex.vos;

import com.vertex.vos.Constructors.Vehicle;
import com.vertex.vos.TableManagerController;
import com.vertex.vos.Utilities.ConfirmationAlert;
import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.TextFieldUtils;
import com.vertex.vos.Utilities.VehicleDAO;
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

    private final VehicleDAO vehicleDAO = new VehicleDAO();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        TextFieldUtils.addDoubleInputRestriction(maxLoad);

        // Initialize ComboBox for vehicle types
        vehicleType.setItems(FXCollections.observableArrayList(
                "Truck", "Garong"));

        // Initialize ComboBox for status
        status.setItems(FXCollections.observableArrayList(
                "Active", "Inactive", "Under Maintenance", "Retired"));
    }

    TableManagerController tableManagerController;

    void setTableManager(TableManagerController tableManagerController) {
        this.tableManagerController = tableManagerController;
    }

    void registerVehicle() {
        headerLabel.setText("Register Vehicle");
        confirmButton.setOnMouseClicked(mouseEvent -> initializeRegistration());
    }

    private void initializeRegistration() {
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Register Vehicle", "Register " + truckPlate.getText() + "?", "Please double check values", true);
        boolean confirmed = confirmationAlert.showAndWait();
        if (confirmed) {
            Vehicle vehicle = new Vehicle();
            vehicle.setVehiclePlate(truckPlate.getText());
            vehicle.setVehicleType(vehicleType.getSelectionModel().getSelectedItem());
            vehicle.setMaxLoad(Double.parseDouble(maxLoad.getText()));
            vehicle.setStatus(status.getSelectionModel().getSelectedItem());
            if (vehicleDAO.insertVehicle(vehicle)){
                DialogUtils.showConfirmationDialog("Success" , vehicle.getVehiclePlate() + " has been added");
                tableManagerController.loadVehicleTable();
            }
            else {
                DialogUtils.contactYourDeveloper("Vehicle");
            }
        }
    }
}
