package com.vertex.vos;

import com.vertex.vos.Utilities.DepartmentDAO;
import com.vertex.vos.Utilities.EmployeeDAO;
import com.vertex.vos.Utilities.TextFieldUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class AssetsEquipmentsController implements Initializable {
    private AnchorPane contentPane; // Declare contentPane variable

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    private final HistoryManager historyManager = new HistoryManager();

    private final int currentNavigationId = -1; // Initialize to a default value

    @FXML
    private ImageView addImage;

    @FXML
    private ComboBox<String> asigneeComboBOx;

    @FXML
    private VBox quantityBox;

    @FXML
    private Label quantityErr;

    @FXML
    private TextField quantityTextField;

    @FXML
    private VBox rateBox;

    @FXML
    private Label rateErr;

    @FXML
    private TextField rateTextField;

    @FXML
    private Button confirmButton;

    @FXML
    private TextField costPerItem;

    @FXML
    private DatePicker dateAcquired;

    @FXML
    private ComboBox<String> departmentComboBox;

    @FXML
    private TextField item;

    @FXML
    private TextField itemLifeSpan;

    @FXML
    private TextField itemQuantity;

    @FXML
    private Label totalLabel;

    void assetRegistration() {
        DepartmentDAO departmentDAO = new DepartmentDAO();
        departmentComboBox.setItems(departmentDAO.getAllDepartmentNames());

        EmployeeDAO employeeDAO = new EmployeeDAO();
        asigneeComboBOx.setItems(employeeDAO.getAllUserNames());

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TextFieldUtils.setComboBoxBehavior(departmentComboBox);
        TextFieldUtils.setComboBoxBehavior(asigneeComboBOx);
        asigneeComboBOx.setDisable(true);
    }
}
