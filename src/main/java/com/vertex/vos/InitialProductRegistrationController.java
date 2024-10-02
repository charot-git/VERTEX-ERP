package com.vertex.vos;

import com.vertex.vos.Utilities.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
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
    @FXML
    private HBox initialRegBox;
    @FXML
    private VBox parentBox;
    @FXML
    private ComboBox<String> parentComboBox;
    @FXML
    private TextField unitCount;
    @FXML
    private VBox unitCountBox;
    @FXML
    private HBox initialRegBoxOptional;
    @FXML
    private VBox parentVBox;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        unit.setItems(unitDAO.getUnitNames());
        brand.setItems(brandDAO.getBrandNames());
        parentVBox.getChildren().remove(initialRegBoxOptional);
    }

    void initializeProduct(String barcodeFromBarcodeScanner) {
        barcode.setText(barcodeFromBarcodeScanner);
        barcode.setEditable(false);

        brand.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                parentComboBox.setItems(productDAO.getProductDescriptionsByBrand(brandDAO.getBrandIdByName(brand.getSelectionModel().getSelectedItem())));
            }
        });

        unit.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!"Pieces".equals(newValue)) {
                    if (!parentVBox.getChildren().contains(initialRegBoxOptional)) {
                        parentVBox.getChildren().add(parentVBox.getChildren().indexOf(initialRegBox) + 1, initialRegBoxOptional);
                        resizeStageToFitContent();
                    }
                } else {
                    parentVBox.getChildren().remove(initialRegBoxOptional);
                }
            }
        });
        confirm.setOnMouseClicked(event -> registerProduct());
    }

    private void resizeStageToFitContent() {
        Stage stage = (Stage) confirm.getScene().getWindow();
        double newHeight = stage.getHeight() + initialRegBoxOptional.getHeight();
        double newWidth = stage.getWidth() + initialRegBoxOptional.getWidth();
        stage.setHeight(newHeight);
        stage.setWidth(newWidth);
        stage.sizeToScene(); // Resize stage to fit content
    }



    private void registerProduct() {
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Product Registration", "Register " + descirption.getText() + "?", "Please double check before proceeding." , false);
        boolean b = confirmationAlert.showAndWait();
        if (b) {
            int unitID = unitDAO.getUnitIdByName(unit.getSelectionModel().getSelectedItem());
            int brandID = brandDAO.getBrandIdByName(brand.getSelectionModel().getSelectedItem());
            int parentId = 0;
            int UNIT_COUNT = 0;
            if (!"Pieces".equals(unit.getSelectionModel().getSelectedItem())) {
                parentId = productDAO.getProductIdByDescription(parentComboBox.getSelectionModel().getSelectedItem());
                UNIT_COUNT = Integer.parseInt(unitCount.getText());
            }
            int success = productDAO.addInitialProduct(barcode.getText(), descirption.getText(), unitID, brandID, parentId, UNIT_COUNT);
            if (success != -1) {
                DialogUtils.showCompletionDialog("Success", descirption.getText() + " has been successfully added to the system");
                Stage stage = (Stage) confirm.getScene().getWindow();
                tableManagerController.loadProductTable();
                stage.close();
            } else {
                DialogUtils.showErrorMessage("Error", "The product has not been registered, please contact your system administrator");
            }
        }
    }

    private TableManagerController tableManagerController;

    public void setTableManagerController(TableManagerController tableManagerController) {
        this.tableManagerController = tableManagerController;
    }

    void initializeProductforNonBarcode(int barcodeFromBarcodeScanner) {
        barcode.setDisable(true);
        brand.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                parentComboBox.setItems(productDAO.getProductDescriptionsByBrand(brandDAO.getBrandIdByName(brand.getSelectionModel().getSelectedItem())));
            }
        });

        unit.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!"Pieces".equals(newValue)) {
                    if (!parentVBox.getChildren().contains(initialRegBoxOptional)) {
                        parentVBox.getChildren().add(parentVBox.getChildren().indexOf(initialRegBox) + 1, initialRegBoxOptional);
                        resizeStageToFitContent();
                    }
                } else {
                    parentVBox.getChildren().remove(initialRegBoxOptional);
                }
            }
        });
        confirm.setOnMouseClicked(event -> registerProductForNonBarcode());
    }

    private void registerProductForNonBarcode() {
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Product Registration", "Register " + descirption.getText() + "?", "Please double check before proceeding." , false);
        boolean b = confirmationAlert.showAndWait();
        if (b) {
            int unitID = unitDAO.getUnitIdByName(unit.getSelectionModel().getSelectedItem());
            int brandID = brandDAO.getBrandIdByName(brand.getSelectionModel().getSelectedItem());
            int parentId = 0;
            int UNIT_COUNT = 0;
            if (!"Pieces".equals(unit.getSelectionModel().getSelectedItem())) {
                parentId = productDAO.getProductIdByDescription(parentComboBox.getSelectionModel().getSelectedItem());
                UNIT_COUNT = Integer.parseInt(unitCount.getText());
            }
            int success = productDAO.addInitialProduct(barcode.getText(), descirption.getText(), unitID, brandID, parentId, UNIT_COUNT);
            if (success != -1) {
                DialogUtils.showCompletionDialog("Success", descirption.getText() + " has been successfully added to the system");
                Stage stage = (Stage) confirm.getScene().getWindow();
                stage.close();
            } else {
                DialogUtils.showErrorMessage("Error", "The product has not been registered, please contact your system administrator");
            }
        }
    }
}
