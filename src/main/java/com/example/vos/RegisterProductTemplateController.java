package com.example.vos;

import com.zaxxer.hikari.HikariDataSource;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class RegisterProductTemplateController implements Initializable, DateSelectedCallback {

    private static final String INSERT_PRODUCT_QUERY = "INSERT INTO products " +
            "(product_name, product_code, price_per_unit, cost_per_unit, product_description, " +
            "product_discount, quantity_available, date_added, supplier_name, supplier_contact) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    @FXML
    private TextField productNameTextField;
    @FXML
    private Label productNameErr;
    @FXML
    private TextField productCodeTextField;
    @FXML
    private Label productCodeErr;
    @FXML
    private TextField pricePerUnitTextField;
    @FXML
    private Label pricePerUnitErr;
    @FXML
    private TextField costPerUnitTextField;
    @FXML
    private Label costPerUnitErr;
    @FXML
    private TextField productDescriptionTextField;
    @FXML
    private Label productDescriptionErr;
    @FXML
    private TextField productDiscountTextField;
    @FXML
    private Label productDiscountErr;
    @FXML
    private TextField quantityAvailableTextField;
    @FXML
    private Label quantityAvailableErr;
    @FXML
    private TextField dateAddedTextField;
    @FXML
    private ImageView datePickerButton;
    @FXML
    private Label dateAddedErr;
    @FXML
    private TextField supplierNameTextField;
    @FXML
    private Label supplierNameErr;
    @FXML
    private TextField supplierContactTextField;
    @FXML
    private Label supplierContactErr;
    @FXML
    private Button confirmButton;

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public void openCalendarViewOnClick(MouseEvent mouseEvent) {
        openCalendarView();
    }

    public void onDateSelected(LocalDate selectedDate) {
        dateAddedTextField.setText(selectedDate.toString());
    }


    private void openCalendarView() {
        // Create a new instance of CalendarView
        CalendarView calendarView = new CalendarView(this);
        Stage stage = new Stage();
        calendarView.start(stage);
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dateAddedTextField.setPromptText(LocalDate.now().toString());

        confirmButton.setOnMouseClicked(event -> {
                confirmationAlert confirmationAlert = new confirmationAlert("Registration Confirmation", "Register " + productNameTextField.getText() + " ?", "todo");
                boolean userConfirmed = confirmationAlert.showAndWait();

                if (userConfirmed) {
                    registerProduct();
                }
        });
    }
    private boolean registerProduct() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_PRODUCT_QUERY)) {
            preparedStatement.setString(1, productNameTextField.getText());
            preparedStatement.setString(2, productCodeTextField.getText());
            preparedStatement.setDouble(3, Double.parseDouble(pricePerUnitTextField.getText()));
            preparedStatement.setDouble(4, Double.parseDouble(costPerUnitTextField.getText()));
            preparedStatement.setString(5, productDescriptionTextField.getText());
            preparedStatement.setDouble(6, Double.parseDouble(productDiscountTextField.getText()));
            preparedStatement.setInt(7, Integer.parseInt(quantityAvailableTextField.getText()));
            preparedStatement.setDate(8, java.sql.Date.valueOf(dateAddedTextField.getText()));
            preparedStatement.setString(9, supplierNameTextField.getText());
            preparedStatement.setString(10, supplierContactTextField.getText());

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected == 1;
        } catch (SQLException e) {
            e.printStackTrace(); // Log the exception for debugging purposes
            return false;
        }
    }

}
