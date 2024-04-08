package com.vertex.vos;

import com.vertex.vos.Constructors.Unit;
import com.vertex.vos.Utilities.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class InitialProductRegistrationController implements Initializable {

    @FXML
    private TextField barcode;

    @FXML
    private Label barcodeErr;

    @FXML
    private Button confirm;

    @FXML
    private TextField descirption;

    @FXML
    private Label descriptionErr;

    @FXML
    private ComboBox<String> unit;

    @FXML
    private VBox unitBox;

    @FXML
    private Label unitErr;
    @FXML
    private ComboBox<String> brand;
    UnitDAO unitDAO = new UnitDAO();
    BrandDAO brandDAO = new BrandDAO();
    ProductDAO productDAO = new ProductDAO();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        unit.setItems(unitDAO.getUnitNames());
        brand.setItems(brandDAO.getBrandNames());
    }

    void initializeProduct(String barcodeFromBarcodeScanner) {
        barcode.setText(barcodeFromBarcodeScanner);
        barcode.setEditable(false);

        confirm.setOnMouseClicked(event -> registerProduct());
    }

    private void registerProduct() {
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Product Registration", "Register " + descirption.getText() + "?" , "Please double check before proceeding.");
        boolean b = confirmationAlert.showAndWait();
        if (b){
            int unitID = unitDAO.getUnitIdByName(unit.getSelectionModel().getSelectedItem());
            int brandID = brandDAO.getBrandIdByName(brand.getSelectionModel().getSelectedItem());
            int success = productDAO.addInitialProduct(barcode.getText(), descirption.getText(), unitID, brandID);

            if (success != -1){
                DialogUtils.showConfirmationDialog("Success" , descirption.getText() + " has been successfully added to the system");
                Stage stage = (Stage) confirm.getScene().getWindow();
                tableManagerController.loadProductTable();
                stage.close();
            }
            else {
                DialogUtils.showErrorMessage("Error" , "The product has not been registered, please contact your system administrator");
            }
        }
    }

    private TableManagerController tableManagerController;

    public void setTableManagerController(TableManagerController tableManagerController) {
        this.tableManagerController = tableManagerController;
    }
}
