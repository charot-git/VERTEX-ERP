package com.vertex.vos;

import com.vertex.vos.Constructors.Branch;
import com.vertex.vos.Constructors.ComboBoxFilterUtil;
import com.vertex.vos.Constructors.UserSession;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static com.vertex.vos.Utilities.TextFieldUtils.addNumericInputRestriction;

public class BranchRegistrationController implements DateSelectedCallback {

    public CheckBox isMovingCheckBox;
    private AnchorPane contentPane;
    @FXML
    private ComboBox<String> province;
    @FXML
    private ComboBox<String> city;
    @FXML
    private ComboBox<String> barangay;
    @FXML
    private Label branchCodeLabel;

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    private final HistoryManager historyManager = new HistoryManager();

    private final int currentNavigationId = -1; // Initialize to a default value


    @FXML
    private TextField dateOfFormation;
    @FXML
    private Label branchNameHeaderLabel;
    @FXML
    private TextField branchNameTextField;
    @FXML
    private Label branchNameErr;
    @FXML
    private TextField branchCodeTextField;
    @FXML
    private Label branchCodeErr;
    @FXML
    private ComboBox<String> branchHeadComboBox;
    @FXML
    private Label branchHeadErr;
    @FXML
    private TextField branchDescriptionTextField;
    @FXML
    private Label branchDescriptionLabelErr;
    @FXML
    private TextField branchContactNoTextField;
    @FXML
    private Label branchContactNoErr;
    @FXML
    private Label dateOfFormationErr;
    @FXML
    private Label provinceErr;
    @FXML
    private Label cityErr;
    @FXML
    private Label baranggayErr;
    @FXML
    private TextField postalCodeTextField;
    @FXML
    private Label postalCodeErr;
    @FXML
    private Button confirmButton;
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    private final HikariDataSource auditTrailSource = AuditTrailDatabaseConnectionPool.getDataSource();

    @FXML
    private Label confirmationLabel;

    private void initiateRegistration() {
        String errorMessage = validateFields();
        if (errorMessage.isEmpty()) {
            ConfirmationAlert confirmationAlert = new ConfirmationAlert("Registration Confirmation", "Register " + branchNameTextField.getText() + " ?", "todo", false);
            boolean userConfirmed = confirmationAlert.showAndWait();
            if (userConfirmed) {
                logAuditTrailEntry("REGISTRATION_INITIATION", "Branch registration initiated for branch: " + branchNameTextField.getText(), 0);
                registerBranch();
            }
        } else {
            System.out.println("Validation Errors:\n" + errorMessage);
        }

    }


    private String validateFields() {
        clearErrorLabels();
        StringBuilder errorMessage = new StringBuilder();

        String branchName = branchNameTextField.getText();
        String branchCode = branchCodeTextField.getText();
        String branchHead = branchHeadComboBox.getSelectionModel().getSelectedItem();
        String branchDescription = branchDescriptionTextField.getText();
        String branchContactNo = branchContactNoTextField.getText();
        String date = dateOfFormation.getText();
        String provinceText = province.getSelectionModel().getSelectedItem();
        String cityText = city.getSelectionModel().getSelectedItem();
        String barangayText = barangay.getSelectionModel().getSelectedItem();
        String postalCode = postalCodeTextField.getText();

        if (branchName.isEmpty()) {
            branchNameErr.setText("Branch name is required");
            errorMessage.append("Branch name is required.\n");
        }

        if (branchCode.isEmpty()) {
            branchCodeErr.setText("Branch code is required");
            errorMessage.append("Branch code is required.\n");
        }

        if (branchHead == null) {
            branchHeadErr.setText("Branch head is required");
            errorMessage.append("Branch head is required.\n");
        } else if (branchHead.isEmpty()) {
            branchHeadErr.setText("Branch head is required");
            errorMessage.append("Branch head is required.\n");
        }

        if (branchDescription.isEmpty()) {
            branchDescriptionLabelErr.setText("Branch description is required");
            errorMessage.append("Branch description is required.\n");
        }

        if (branchContactNo.isEmpty()) {
            branchContactNoErr.setText("Branch contact no. is required");
            errorMessage.append("Branch contact no. is required.\n");
        }
        if (date.isEmpty()) {
            dateOfFormationErr.setText("Date is required");
            errorMessage.append("Date is required.\n");
        }
        if (provinceText.isEmpty()) {
            provinceErr.setText("Province is required");
            errorMessage.append("Province is required.\n");
        }
        if (cityText.isEmpty()) {
            cityErr.setText("City is required");
            errorMessage.append("City is required.\n");
        }
        if (barangayText.isEmpty()) {
            baranggayErr.setText("Baranggay is required");
            errorMessage.append("Baranggay is required.\n");
        }
        if (postalCode.isEmpty()) {
            postalCodeErr.setText("Postal Code is required");
            errorMessage.append("Postal Code is required.\n");
        }

        // Add more specific validations for other fields as needed

        return errorMessage.toString();
    }


    private void clearErrorLabels() {
        branchNameErr.setText("");
        branchCodeErr.setText("");
        branchHeadErr.setText("");
        branchDescriptionLabelErr.setText("");
        branchContactNoErr.setText("");
        dateOfFormationErr.setText("");
        provinceErr.setText("");
        cityErr.setText("");
        baranggayErr.setText("");
        postalCodeErr.setText("");
    }


    private void registerBranch() {
        String insertQuery = "INSERT INTO branches (branch_description, branch_name, branch_head, branch_code, state_province, city, brgy, phone_number, postal_code, date_added, isMoving) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        EmployeeDAO employeeDAO = new EmployeeDAO();
        int branchId = employeeDAO.getUserIdByFullName(branchHeadComboBox.getSelectionModel().getSelectedItem());
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, branchDescriptionTextField.getText());
            preparedStatement.setString(2, branchNameTextField.getText());
            preparedStatement.setInt(3, branchId);
            preparedStatement.setString(4, branchCodeTextField.getText());
            preparedStatement.setString(5, province.getSelectionModel().getSelectedItem());
            preparedStatement.setString(6, city.getSelectionModel().getSelectedItem());
            preparedStatement.setString(7, barangay.getSelectionModel().getSelectedItem());
            preparedStatement.setString(8, branchContactNoTextField.getText());
            preparedStatement.setString(9, postalCodeTextField.getText());
            preparedStatement.setDate(10, java.sql.Date.valueOf(LocalDate.parse(dateOfFormation.getText())));
            preparedStatement.setBoolean(11, isMovingCheckBox.isSelected());
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int generatedBranchId = generatedKeys.getInt(1); // Get the generated branch_id
                    tableManagerController.loadBranchTable();
                    logAuditTrailEntry("REGISTRATION_SUCCESS", "Branch registered successfully with ID: " + generatedBranchId +
                            ", Branch Name: " + branchNameTextField.getText(), generatedBranchId);
                    confirmationLabel.setText("Branch registered successfully with ID: " + generatedBranchId);
                    confirmationLabel.setTextFill(Color.GREEN); // Set text color to green for success
                    Stage stage = (Stage) confirmationLabel.getScene().getWindow();
                    stage.close();
                }
            } else {
                logAuditTrailEntry("REGISTRATION_FAILURE", "Failed to register branch: " + branchNameTextField.getText(), 0);

                confirmationLabel.setText("Failed to register branch. Please try again.");
                confirmationLabel.setTextFill(Color.RED); // Set text color to red for failure
            }

        } catch (SQLException e) {
            e.printStackTrace();
            confirmationLabel.setText("Error occurred while registering branch.");
        }
    }


    private void populateComboBoxes() {
        String sqlQuery = "SELECT CONCAT(user_fname, ' ', COALESCE(user_mname, ''), ' ', user_lname) AS full_name FROM user";
        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            ObservableList<String> fullNames = FXCollections.observableArrayList();
            while (resultSet.next()) {
                String fullName = resultSet.getString("full_name");
                fullNames.add(fullName);
            }
            branchHeadComboBox.setItems(fullNames);
            TextFieldUtils.setComboBoxBehavior(branchHeadComboBox);
            ComboBoxFilterUtil.setupComboBoxFilter(branchHeadComboBox, fullNames);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        initializeAddress();

    }

    BranchDAO branchDAO = new BranchDAO();

    private void updateBranch(int id) {
        int branchHeadId = employeeDAO.getUserIdByFullName(branchHeadComboBox.getSelectionModel().getSelectedItem());
        System.out.println(branchHeadId);
        Branch branch = new Branch();
        branch.setId(id);
        branch.setBranchDescription(branchDescriptionTextField.getText());
        branch.setBranchName(branchNameTextField.getText());
        branch.setBranchHeadId(branchHeadId);
        branch.setBranchCode(branchCodeTextField.getText());
        branch.setStateProvince(province.getSelectionModel().getSelectedItem());
        branch.setCity(city.getSelectionModel().getSelectedItem());
        branch.setBrgy(barangay.getSelectionModel().getSelectedItem());
        branch.setPhoneNumber(branchContactNoTextField.getText());
        branch.setPostalCode(postalCodeTextField.getText());
        branch.setDateAdded(Date.valueOf(dateOfFormation.getText()));
        branch.setMoving(isMovingCheckBox.isSelected()); // Set the isMoving property
        boolean isUpdated = branchDAO.updateBranch(branch);

        if (isUpdated) {
            logAuditTrailEntry("UPDATE_SUCCESS", "Branch updated successfully with Name: " + branch.getBranchName(), branch.getId());

            confirmationLabel.setText("Branch updated successfully");
            confirmationLabel.setTextFill(Color.GREEN); // Set text color to green for success
            Stage stage = (Stage) confirmationLabel.getScene().getWindow();
            stage.close();
            tableManagerController.loadBranchTable();
            DialogUtils.showConfirmationDialog("Update Successful", "Success");
        } else {
            logAuditTrailEntry("UPDATE_FAILURE", "Failed to update branch: " + branch.getBranchName(), branch.getId());
            confirmationLabel.setText("Failed to update branch. Please try again.");
            confirmationLabel.setTextFill(Color.RED);
        }
    }


    private void initializeAddress() {
        Map<String, String> provinceData = LocationCache.getProvinceData();
        Map<String, String> cityData = LocationCache.getCityData();
        Map<String, String> barangayData = LocationCache.getBarangayData();
        ObservableList<String> provinceItems = FXCollections.observableArrayList(provinceData.values());
        province.setItems(provinceItems);


        ComboBoxFilterUtil.setupComboBoxFilter(province, provinceItems);

        province.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            String selectedProvinceCode = getKeyFromValue(provinceData, newValue);
            List<String> citiesInProvince = filterLocationsByParentCode(cityData, selectedProvinceCode);
            ObservableList<String> cityItems = FXCollections.observableArrayList(citiesInProvince);
            city.setItems(FXCollections.observableArrayList(citiesInProvince));
            ComboBoxFilterUtil.setupComboBoxFilter(city, cityItems);
        });

        city.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            String selectedCityCode = getKeyFromValue(cityData, newValue);
            List<String> barangaysInCity = filterLocationsByParentCode(barangayData, selectedCityCode);
            ObservableList<String> barangayItems = FXCollections.observableArrayList(barangaysInCity);
            barangay.setItems(FXCollections.observableArrayList(barangaysInCity));
            ComboBoxFilterUtil.setupComboBoxFilter(barangay, barangayItems);
        });
    }

    private String getKeyFromValue(Map<String, String> map, String value) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }

    // Helper method to filter locations based on the parent code
    private List<String> filterLocationsByParentCode(Map<String, String> locationData, String parentCode) {
        List<String> filteredLocations = new ArrayList<>();
        for (Map.Entry<String, String> entry : locationData.entrySet()) {
            String code = entry.getKey();
            String parentCodeOfLocation = getParentCode(code);
            if (parentCodeOfLocation.equals(parentCode)) {
                filteredLocations.add(entry.getValue());
            }
        }
        return filteredLocations;
    }

    // Helper method to extract the parent code from the location code (assuming a specific format)
    private String getParentCode(String code) {
        if (code.length() == 9) {
            return code.substring(0, 6);
        } else if (code.length() == 6) {
            return code.substring(0, 4);
        } else {
            return null;
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

    private void logAuditTrailEntry(String action, String description, int branchId) {
        try (Connection connection = auditTrailSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO audit_trail_table (timestamp, user_id, action, table_name, record_id, field_name, old_value, new_value) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
             )) {

            preparedStatement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            preparedStatement.setInt(2, UserSession.getInstance().getUserId());
            preparedStatement.setString(3, action);
            preparedStatement.setString(4, "branch"); // Table name or action-specific identifier
            preparedStatement.setInt(5, branchId); // Use the actual branch_id here
            preparedStatement.setString(6, "description");
            preparedStatement.setString(7, "N/A");
            preparedStatement.setString(8, description);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception appropriately, e.g., log it or show an error message
        }
    }

    private TableManagerController tableManagerController;

    void tableManagerController(TableManagerController tableManagerController) {
        this.tableManagerController = tableManagerController;
    }

    EmployeeDAO employeeDAO = new EmployeeDAO();

    public void initData(int id) {
        BranchDAO branchDAO = new BranchDAO();
        Branch branch = branchDAO.getBranchById(id);
        populateComboBoxes();
        if (branch != null) {
            branchNameTextField.setText(branch.getBranchName());
            branchDescriptionTextField.setText(branch.getBranchDescription());
            branchCodeTextField.setText(branch.getBranchCode());
            branchContactNoTextField.setText(branch.getPhoneNumber());
            postalCodeTextField.setText(branch.getPostalCode());
            province.getSelectionModel().select(branch.getStateProvince());
            city.getSelectionModel().select(branch.getCity());
            barangay.getSelectionModel().select(branch.getBrgy());
            dateOfFormation.setText(branch.getDateAdded().toString());
            String branchHeadName = employeeDAO.getFullNameById(branch.getBranchHeadId());
            branchHeadComboBox.setValue(branchHeadName);
            isMovingCheckBox.setSelected(branch.isMoving());
            if (branch.isMoving()){
                branchCodeLabel.setText("Truck Plate");
            }
            else {
                branchCodeLabel.setText("Branch Code");
            }
            branchHeadComboBox.setItems(employeeDAO.getAllUserNames());
            confirmButton.setOnMouseClicked(event -> updateBranch(id));
        } else {
            DialogUtils.showErrorMessage("Error", "Failed to retrieve branch details.");
        }
    }

    public void addNewBranch() {
        populateComboBoxes();
        addNumericInputRestriction(branchContactNoTextField);
        addNumericInputRestriction(postalCodeTextField);
        dateOfFormation.setPromptText(LocalDate.now().toString());
        branchNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            branchNameHeaderLabel.setText(newValue);
        });
        confirmButton.setOnMouseClicked(event -> {
            initiateRegistration();
        });
    }

    public void setTableManager(TableManagerController tableManagerController) {
        this.tableManagerController = tableManagerController;
    }
}
