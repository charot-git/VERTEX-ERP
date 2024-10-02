package com.vertex.vos;

import com.vertex.vos.Objects.Company;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.Objects;
import java.util.ResourceBundle;

import static com.vertex.vos.Utilities.TextFieldUtils.addNumericInputRestriction;

public class CompanyRegistrationController implements Initializable, DateSelectedCallback {

    private final HistoryManager historyManager = new HistoryManager();

    private final int currentNavigationId = -1; // Initialize to a default value

    public ImageView companyLogo;
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

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addNumericInputRestriction(companyContactNoTextField);
        addNumericInputRestriction(registrationNoTextField);
        addNumericInputRestriction(companyTINTextField);
    }

    CompanyDAO companyDAO = new CompanyDAO();

    private void registerCompany() {
        Company company = createCompanyFromForm();
        if (company != null) {
            boolean success = companyDAO.addCompany(company); // Save company details using CompanyDAO
            if (success) {
                DialogUtils.showCompletionDialog("Success", "Successfully registered");
                Stage stage = (Stage) companyNameHeaderLabel.getScene().getWindow();
                stage.close();
            } else {
                DialogUtils.showErrorMessage("Error", "Company not registered");
            }
        }
    }

    private Company createCompanyFromForm() {
        String errorMessage = validateFields();

        if (errorMessage.isEmpty()) {
            Company company = new Company();
            company.setCompanyName(companyNameTextField.getText().trim());
            company.setCompanyType(businessTypeTextField.getText().trim());
            company.setCompanyCode(companyCodeTextField.getText().trim());
            company.setCompanyFirstAddress(address1TextField.getText().trim());
            company.setCompanySecondAddress(address2TextField.getText().trim());
            company.setCompanyRegistrationNumber(registrationNoTextField.getText().trim());
            company.setCompanyTIN(companyTINTextField.getText().trim());
            company.setCompanyDateAdmitted(Date.valueOf(dateOfFormation.getText().trim())); // Assuming dateOfFormation contains a valid date string
            company.setCompanyContact(companyContactNoTextField.getText().trim());
            company.setCompanyEmail(companyEmailTextField.getText().trim());
            return company;
        } else {
            System.out.println("Validation Errors:\n" + errorMessage);
            return null;
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

    TableManagerController tableManagerController;

    public void setTableManager(TableManagerController tableManagerController) {
        this.tableManagerController = tableManagerController;
    }

    public void initData(Company selectedCompany) {
        companyNameTextField.setText(selectedCompany.getCompanyName());
        businessTypeTextField.setText(selectedCompany.getCompanyType());
        companyCodeTextField.setText(selectedCompany.getCompanyCode());
        address1TextField.setText(selectedCompany.getCompanyFirstAddress());
        address2TextField.setText(selectedCompany.getCompanySecondAddress());
        registrationNoTextField.setText(selectedCompany.getCompanyRegistrationNumber());
        companyTINTextField.setText(selectedCompany.getCompanyTIN());
        companyContactNoTextField.setText(selectedCompany.getCompanyContact());
        companyEmailTextField.setText(selectedCompany.getCompanyEmail());
        dateOfFormation.setText(selectedCompany.getCompanyDateAdmitted().toString());
        String imagePath = selectedCompany.getCompanyLogo();
        Image image;
        if (imagePath != null && !imagePath.isEmpty()) {
            image = new Image(new File(imagePath).toURI().toString());
        } else {
            image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Supplier Info.png")));
        }
        companyLogo.setImage(image);
        confirmButton.setText("Update");
        confirmButton.setOnMouseClicked(mouseEvent -> updateCompany(selectedCompany));
        chooseLogoButton.setOnMouseClicked(mouseEvent -> chooseLogo(selectedCompany));
    }

    private void updateCompany(Company selectedCompany) {
        Company company = createCompanyFromForm();
        if (company != null) {
            company.setCompanyId(selectedCompany.getCompanyId()); // Set the ID of the company to update
            boolean success = companyDAO.updateCompany(company); // Update company details using CompanyDAO
            if (success) {
                DialogUtils.showCompletionDialog("Success", "Successfully updated");
                Stage stage = (Stage) companyNameHeaderLabel.getScene().getWindow();
                stage.close();
            } else {
                DialogUtils.showErrorMessage("Error", "Company not updated");
            }
        }
    }

    public void createCompany() {
        dateOfFormation.setPromptText(LocalDate.now().toString());
        companyNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
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
                System.out.println("Validation Errors:\n" + errorMessage);
            }
        });
    }

    String logoUrl;

    public void chooseLogo(Company selectedCompany) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Logo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(chooseLogoButton.getScene().getWindow());
        if (selectedFile != null) {
            try {
                Image image = new Image(selectedFile.toURI().toString());
                companyLogo.setImage(image);
                logoPicked = true;
                chooseLogoButton.setTextFill(Color.BLACK);

                boolean b = ServerUtility.uploadImageAndGetUrlForCompany(selectedFile, selectedCompany);
                if (b){
                    DialogUtils.showCompletionDialog("Success" , "Company image update successful");
                }
                else {
                    DialogUtils.showErrorMessage("Error", "Please contact your I.T Department");
                }
            } catch (Exception e) {
                System.out.println("Failed to load logo: " + e.getMessage());
            }
        }
    }


}
