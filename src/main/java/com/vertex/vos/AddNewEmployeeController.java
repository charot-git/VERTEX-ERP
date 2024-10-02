package com.vertex.vos;

import com.vertex.vos.Objects.DateField;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.TextFieldUtils;
import com.zaxxer.hikari.HikariDataSource;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

import static com.vertex.vos.Utilities.TextFieldUtils.addNumericInputRestriction;

public class AddNewEmployeeController implements Initializable, DateSelectedCallback {

    public Button addUser;
    private DateField currentDateField; // Enum to track the current date field being edited
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();


    @FXML
    private TextField userFname;
    @FXML
    private TextField userMname;
    @FXML
    private TextField userLname;
    @FXML
    private TextField userEmail;
    @FXML
    private TextField userContact;
    @FXML
    private TextField userDepartment;
    @FXML
    private TextField userPosition;
    @FXML
    private TextField userProvince;
    @FXML
    private TextField userCity;
    @FXML
    private TextField userBrgy;
    @FXML
    private TextField userSSS;
    @FXML
    private TextField userPhilHealth;
    @FXML
    private TextField userTIN;
    @FXML
    private TextField dateOfHire;
    @FXML
    private TextField userBirthDay;

    public void openCalendarViewForBirthDay(MouseEvent mouseEvent) {
        currentDateField = DateField.BIRTHDAY;
        openCalendarView();
    }

    public void openCalendarViewForDateHired(MouseEvent mouseEvent) {
        currentDateField = DateField.DATE_HIRED;
        openCalendarView();
    }

    private void openCalendarView() {
        // Create a new instance of CalendarView
        CalendarView calendarView = new CalendarView(this);
        Stage stage = new Stage();
        calendarView.start(stage);
    }

    public void registerUser() {
        String userBirthDayText = userBirthDay.getText();
        String dateOfHireText = dateOfHire.getText();

        // Check if date fields are not empty
        if (userBirthDayText.isEmpty() || dateOfHireText.isEmpty()) {
            // Handle empty date fields, show an error message or return early
            System.out.println("Date fields cannot be empty.");
            return;
        }

// Parse strings to LocalDate
        LocalDate birthday = LocalDate.parse(userBirthDayText);
        LocalDate dateOfHire = LocalDate.parse(dateOfHireText);

// Convert LocalDate to java.sql.Date
        java.sql.Date sqlBirthday = java.sql.Date.valueOf(birthday);
        java.sql.Date sqlDateOfHire = java.sql.Date.valueOf(dateOfHire);


        try (Connection connection = dataSource.getConnection()) {
            String sql = "INSERT INTO user (user_fname, user_mname, user_lname, user_bday, user_brgy, user_city, user_province, user_contact, user_department, user_email, user_tin, user_philhealth, user_sss, user_dateOfHire, user_position) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, userFname.getText());
                statement.setString(2, userMname.getText());
                statement.setString(3, userLname.getText());
                statement.setDate(4, sqlBirthday);
                statement.setString(5, userBrgy.getText());
                statement.setString(6, userCity.getText());
                statement.setString(7, userProvince.getText());
                statement.setString(8, userContact.getText());
                statement.setString(9, userDepartment.getText());
                statement.setString(10, userEmail.getText());
                statement.setString(11, userTIN.getText());
                statement.setString(12, userPhilHealth.getText());
                statement.setString(13, userSSS.getText());
                statement.setDate(14, sqlDateOfHire);
                statement.setString(15, userPosition.getText());

                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    // Successfully inserted new employee
                    ResultSet generatedKeys = statement.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int userId = generatedKeys.getInt(1); // Retrieve the generated user_id
                        DialogUtils.showCompletionDialog("Success" , "User " + userFname.getText() + " has been registered with user Id " + userId);
                        Stage stage = (Stage) userFname.getScene().getWindow();
                        stage.close();
                    }
                } else {
                    // Insertion failed, handle the error
                    System.out.println("Failed to add new employee.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle SQL exception
        }
    }


    @Override
    public void onDateSelected(LocalDate selectedDate) {
        if (currentDateField == DateField.BIRTHDAY) {
            userBirthDay.setText(selectedDate.toString());
        } else if (currentDateField == DateField.DATE_HIRED) {
            dateOfHire.setText(selectedDate.toString());
        }
        currentDateField = null;
    }
    private boolean validateFields() {
        if (userFname.getText().isEmpty() ||
                userLname.getText().isEmpty() ||
                userEmail.getText().isEmpty() ||
                userContact.getText().isEmpty() ||
                userDepartment.getText().isEmpty() ||
                userPosition.getText().isEmpty() ||
                userProvince.getText().isEmpty() ||
                userCity.getText().isEmpty() ||
                userBrgy.getText().isEmpty() ||
                userSSS.getText().isEmpty() ||
                userPhilHealth.getText().isEmpty() ||
                userTIN.getText().isEmpty() ||
                dateOfHire.getText().isEmpty() ||
                userBirthDay.getText().isEmpty()) {
            DialogUtils.showErrorMessage("Error", "Please fill in all required fields.");
            return false;
        }

        if (!TextFieldUtils.isNumeric(userContact.getText()) ||
                !TextFieldUtils.isNumeric(userSSS.getText()) ||
                !TextFieldUtils.isNumeric(userPhilHealth.getText()) ||
                !TextFieldUtils.isNumeric(userTIN.getText())) {
            DialogUtils.showErrorMessage("Error", "Contact numbers, SSS, PhilHealth, and TIN must be numeric.");
            return false;
        }

        // Check the length of each field
        if (userContact.getText().length() != 11) {
            DialogUtils.showErrorMessage("Error", "Contact number must be 11 digits.");
            return false;
        }

        if (userSSS.getText().length() != 10) {
            DialogUtils.showErrorMessage("Error", "SSS number must be 10 digits.");
            return false;
        }

        if (userPhilHealth.getText().length() != 12) {
            DialogUtils.showErrorMessage("Error", "PhilHealth number must be 12 digits.");
            return false;
        }

        if (userTIN.getText().length() != 12) {
            DialogUtils.showErrorMessage("Error", "TIN must be 12 digits.");
            return false;
        }

        if (!TextFieldUtils.isValidEmail(userEmail.getText())) {
            DialogUtils.showErrorMessage("Error", "Invalid email address.");
            return false;
        }
        if (!isEmailUniqueInDatabase(userEmail.getText())) {
            DialogUtils.showErrorMessage("Error", "Email address already exists.");
            return false;
        }
        return true;
    }

    private boolean isEmailUniqueInDatabase(String email) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT COUNT(*) FROM user WHERE user_email = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, email);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        int count = resultSet.getInt(1);
                        return count == 0; // If count is 0, email is unique; otherwise, it's not
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the SQL exception (log it or throw a custom exception)
        }
        return false; // Return false in case of any exception or database error
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addNumericInputRestriction(userContact);
        addNumericInputRestriction(userSSS);
        addNumericInputRestriction(userPhilHealth);
        addNumericInputRestriction(userTIN);
        
        addUser.setOnMouseClicked(event -> {
            if (validateFields()) {
                registerUser();
            } else {
                // Validation failed, handle it here if needed
                System.out.println("Validation failed. Please check your input.");
            }
        });
    }
}
