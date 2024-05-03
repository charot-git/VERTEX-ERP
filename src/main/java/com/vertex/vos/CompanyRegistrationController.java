package com.vertex.vos;

import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.vertex.vos.Utilities.TextFieldUtils;
import com.vertex.vos.Utilities.ConfirmationAlert;
import com.zaxxer.hikari.HikariDataSource;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

import static com.vertex.vos.Utilities.TextFieldUtils.addNumericInputRestriction;

public class CompanyRegistrationController implements Initializable, DateSelectedCallback {
    private AnchorPane contentPane; // Declare contentPane variable

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    private final HistoryManager historyManager = new HistoryManager();

    private final int currentNavigationId = -1; // Initialize to a default value

    public ImageView companyLogo;

    //upload image initialization
    private FileInputStream fileInputStream;

    public ImageView datePickerButton;
    public TextField dateOfFormation;
    public Label companyNameHeaderLabel;
    public TextField companyCodeTextField;
    public TextField companyNameTextField;
    public TextField businessTypeTextField;
    public TextField address1TextField;
    public TextField address2TextField;
    public TextField registrationNoTextField;
    public TextField companyTINTextField;
    public TextField companyContactNoTextField;
    public TextField companyEmailTextField;
    public Button confirmButton;

    //1 corresponding to validation err, sarreh im tired lready as i was coding this
    public Label companyNameLabel1;
    public Label businessTypeLabel1;
    public Label companyCodeLabel1;
    public Label address1Label1;
    public Label address2Label1;
    public Label registrationNumberLabel1;
    public Label tinNumberLabel1;
    public Label dateOfFormationLabel1;
    public Label companyNumberLabel1;
    public Label companyEmailLabel1;
    public Button chooseLogoButton;
    private boolean logoPicked = false;

    private String selectedFilePath;

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dateOfFormation.setPromptText(LocalDate.now().toString());
        addNumericInputRestriction(companyContactNoTextField);
        addNumericInputRestriction(registrationNoTextField);
        addNumericInputRestriction(companyTINTextField);


        companyNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Update the text of the associated companyNameLabel
            companyNameHeaderLabel.setText(newValue);
        });


        confirmButton.setOnMouseClicked(event -> {
            String errorMessage = validateFields();

            if (errorMessage.isEmpty()) {
                ConfirmationAlert confirmationAlert = new ConfirmationAlert("Registration Confirmation", "Register " + companyNameTextField.getText() + " ?", "todo", false);
                boolean userConfirmed = confirmationAlert.showAndWait();

                if (userConfirmed) {
                    registerCompany();
                }
            } else {
                // Display the error message to the user (for example, in a dialog box)
                System.out.println("Validation Errors:\n" + errorMessage);
            }

        });

    }

    private void registerCompany() {
        String insertQuery = "INSERT INTO company (company_name, company_type, company_code, " +
                "company_firstAddress, company_secondAddress, company_registrationNumber, " +
                "company_tin, company_dateAdmitted, company_contact, company_email, " +
                "company_logo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

            // Set values for the prepared statement
            preparedStatement.setString(1, companyNameTextField.getText());
            preparedStatement.setString(2, businessTypeTextField.getText());
            preparedStatement.setString(3, companyCodeTextField.getText());
            preparedStatement.setString(4, address1TextField.getText());
            preparedStatement.setString(5, address2TextField.getText());
            preparedStatement.setString(6, registrationNoTextField.getText());
            preparedStatement.setString(7, companyTINTextField.getText());
            preparedStatement.setDate(8, Date.valueOf(dateOfFormation.getText())); // Assuming dateOfFormation contains a valid date string
            preparedStatement.setString(9, companyContactNoTextField.getText());
            preparedStatement.setString(10, companyEmailTextField.getText());

            if (logoPicked) {
                // Read the selected image file into a byte array
                File selectedFile = new File(selectedFilePath); // Replace selectedFilePath with the actual path to the selected image file
                try (FileInputStream fileInputStream = new FileInputStream(selectedFile)) {
                    byte[] imageData = fileInputStream.readAllBytes();
                    preparedStatement.setBytes(11, imageData); // Set the image data in the prepared statement
                } catch (IOException e) {
                    e.printStackTrace(); // Handle the exception properly in your application
                    return; // Abort the registration process due to error
                }
            } else {
                preparedStatement.setNull(11, Types.BLOB); // Set logo to null if no image is picked
            }

            // Execute the update
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                // Registration successful
                System.out.println("Company registered successfully!");
                Stage stage = (Stage) companyNameHeaderLabel.getScene().getWindow();
                stage.close();

            } else {
                // Registration failed
                System.out.println("Failed to register company.");
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception properly in your application
        }
    }


    private String validateFields() {
        clearErrorLabels(); // Clear previous error messages

        StringBuilder errorMessage = new StringBuilder();
        String companyName = companyNameTextField.getText().trim();
        String companyCode = companyCodeTextField.getText().trim();
        String businessType = businessTypeTextField.getText().trim();
        String address1 = address1TextField.getText().trim();
        String address2 = address2TextField.getText().trim();
        String registrationNo = registrationNoTextField.getText().trim();
        String tin = companyTINTextField.getText().trim();
        String contactNo = companyContactNoTextField.getText().trim();
        String email = companyEmailTextField.getText().trim();
        String dateFormation = dateOfFormation.getText().trim();


        if (!logoPicked) {
            chooseLogoButton.setTextFill(Color.RED);
            errorMessage.append("Logo is required.\n");
        }
        if (email.isEmpty()) {
            setErrorMessage(companyEmailLabel1, "Email address is required.");
            companyEmailTextField.requestFocus();
            errorMessage.append("Email address is required.\n");
        }

        if (!TextFieldUtils.isValidEmail(email)) {
            setErrorMessage(companyEmailLabel1, "Invalid email address.");
            companyEmailTextField.requestFocus();
            errorMessage.append("Invalid email address.\n");
        }

        if (!TextFieldUtils.isNumeric(contactNo)) {
            setErrorMessage(companyNumberLabel1, "Contact number must be numeric.");
            companyContactNoTextField.requestFocus();
            errorMessage.append("Contact number must be numeric.\n");
        }

        if (contactNo.isEmpty()) {
            setErrorMessage(companyNumberLabel1, "Contact number is required.");
            companyContactNoTextField.requestFocus();
            errorMessage.append("Contact number is required.\n");
        }

        if (contactNo.length() != 11) {
            setErrorMessage(companyNumberLabel1, "Contact number must be 11 digits.");
            companyContactNoTextField.requestFocus();
            errorMessage.append("Contact number must be 11 digits.\n");
        }

        if (dateFormation.isEmpty()) {
            setErrorMessage(dateOfFormationLabel1, "Date of formation is required.");
            dateOfFormation.requestFocus();
            errorMessage.append("Date of formation is required.\n");
        }

        if (!TextFieldUtils.isNumeric(tin)) {
            setErrorMessage(tinNumberLabel1, "TIN must be numeric.");
            companyTINTextField.requestFocus();
            errorMessage.append("TIN must be numeric.\n");
        }

        if (tin.isEmpty()) {
            setErrorMessage(tinNumberLabel1, "TIN is required.");
            companyTINTextField.requestFocus();
            errorMessage.append("TIN is required.\n");
        }

        if (tin.length() != 12) {
            setErrorMessage(tinNumberLabel1, "TIN must be 12 digits.");
            companyTINTextField.requestFocus();
            errorMessage.append("TIN must be 12 digits.\n");
        }

        if (!TextFieldUtils.isNumeric(registrationNo)) {
            setErrorMessage(registrationNumberLabel1, "Registration number must be numeric.");
            registrationNoTextField.requestFocus();
            errorMessage.append("Registration number must be numeric.\n");
        }

        if (registrationNo.length() != 10) {
            setErrorMessage(registrationNumberLabel1, "Registration number must be 10 digits.");
            registrationNoTextField.requestFocus();
            errorMessage.append("Registration number must be 10 digits.\n");
        }

        if (registrationNo.isEmpty()) {
            setErrorMessage(registrationNumberLabel1, "Registration number is required.");
            registrationNoTextField.requestFocus();
            errorMessage.append("Registration number is required.\n");
        }

        if (address2.isEmpty()) {
            setErrorMessage(address2Label1, "Address line 2 is required.");
            address2TextField.requestFocus();
            errorMessage.append("Address line 2 is required.\n");
        }

        if (address1.isEmpty()) {
            setErrorMessage(address1Label1, "Address line 1 is required.");
            address1TextField.requestFocus();
            errorMessage.append("Address line 1 is required.\n");
        }

        if (companyCode.isEmpty()) {
            setErrorMessage(companyCodeLabel1, "Company code is required.");
            companyCodeTextField.requestFocus();
            errorMessage.append("Company code is required.\n");
        }

        if (businessType.isEmpty()) {
            setErrorMessage(businessTypeLabel1, "Business type is required.");
            businessTypeTextField.requestFocus();
            errorMessage.append("Business type is required.\n");
        }

        if (companyName.isEmpty()) {
            setErrorMessage(companyNameLabel1, "Company name is required.");
            companyNameTextField.requestFocus();
            errorMessage.append("Company name is required.\n");
        }
        return errorMessage.toString();
    }

    private void setErrorMessage(Label errorLabel, String errorMessage) {
        errorLabel.setText(errorMessage);
        errorLabel.setTextFill(Color.RED); // Set text color to red
    }

    private void clearErrorLabels() {
        companyNameLabel1.setText("");
        businessTypeLabel1.setText("");
        companyCodeLabel1.setText("");
        address1Label1.setText("");
        address2Label1.setText("");
        registrationNumberLabel1.setText("");
        tinNumberLabel1.setText("");
        dateOfFormationLabel1.setText("");
        companyNumberLabel1.setText("");
        companyEmailLabel1.setText("");
    }


    @FXML
    private void onCompanyLogoClicked(MouseEvent event) {
        // Handle the click event for the companyLogo ImageView
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));

        // Show open file dialog
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            // Store the file path of the selected image
            selectedFilePath = selectedFile.getAbsolutePath();

            // Load the selected image and apply it to the companyLogo ImageView
            Image image = new Image(selectedFile.toURI().toString());
            companyLogo.setImage(image);
            logoPicked = true;
        }
    }


    private void openCalendarView() {
        // Create a new instance of CalendarView
        CalendarView calendarView = new CalendarView(this);
        Stage stage = new Stage();
        calendarView.start(stage);
    }

    public void openCalendarViewOnClick(MouseEvent mouseEvent) {
        openCalendarView();
    }

    @Override
    public void onDateSelected(LocalDate selectedDate) {
        dateOfFormation.setText(selectedDate.toString());
    }

}
