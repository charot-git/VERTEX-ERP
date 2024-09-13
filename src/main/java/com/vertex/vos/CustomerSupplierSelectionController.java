package com.vertex.vos;

import com.vertex.vos.HoverAnimation;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class CustomerSupplierSelectionController implements Initializable {

    @FXML
    private VBox customerBox;

    @FXML
    private VBox supplierBox;

    private CreditDebitListController creditDebitListController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        new HoverAnimation(customerBox);
        new HoverAnimation(supplierBox);

        // Set click handlers for customer and supplier boxes
        customerBox.setOnMouseClicked(event -> selectCustomer());
        supplierBox.setOnMouseClicked(event -> selectSupplier());
    }


    private void selectCustomer() {
        if (creditDebitListController != null) {
            creditDebitListController.setTargetType("Customer"); // Call method in CreditDebitListController
            closeWindow();
        }
    }

    private void selectSupplier() {
        if (creditDebitListController != null) {
            creditDebitListController.setTargetType("Supplier"); // Call method in CreditDebitListController
            closeWindow();
        }
    }

    private void closeWindow() {
        customerBox.getScene().getWindow().hide();
    }

    public void setCreditDebitListController(CreditDebitListController creditDebitListController) {
        this.creditDebitListController = creditDebitListController;
    }
}
