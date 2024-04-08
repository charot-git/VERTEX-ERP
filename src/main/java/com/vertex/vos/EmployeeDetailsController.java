package com.vertex.vos;

import com.vertex.vos.Constructors.User;
import com.vertex.vos.Utilities.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;


import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class EmployeeDetailsController implements Initializable {
    public TextField fname, mname, lname;
    public DatePicker birthDay, hiredDay;
    public TextField sss;
    public TextField tin;

    public TextField contact;
    public TextField email;
    public ComboBox<String> province, city, brgy;

    public ComboBox<String> department;
    public Label fullName, position;
    public Label roles;
    public ImageView profilePic;
    public ImageView deleteButton, editButton;
    public TextField philHealth;
    @FXML
    private Button confirm;
    @FXML
    private DatePicker birthday;
    @FXML
    private DatePicker dateHired;

    EmployeeDAO employeeDAO = new EmployeeDAO();
    @FXML
    private TextField positionTextField;

    public void initData(User selectedUser) {
        Platform.runLater(() -> {
            setDetails(selectedUser);
        });
    }

    private void setDetails(User selectedUser) {
        String completeName = selectedUser.getUser_fname()
                + " " + selectedUser.getUser_mname()
                + " " + selectedUser.getUser_lname();
        fullName.setText(completeName);
        position.setText(selectedUser.getUser_position());
        roles.setText(selectedUser.getUser_tags());
        fname.setText(selectedUser.getUser_fname());
        mname.setText(selectedUser.getUser_mname());
        lname.setText(selectedUser.getUser_lname());
        province.setValue(selectedUser.getUser_province());
        city.setValue(selectedUser.getUser_city());
        brgy.setValue(selectedUser.getUser_brgy());
        contact.setText(selectedUser.getUser_contact());
        email.setText(selectedUser.getUser_email());
        department.setValue(departmentDAO.getDepartmentNameById(selectedUser.getUser_department()));
        tin.setText(selectedUser.getUser_tin());
        sss.setText(selectedUser.getUser_sss());
        philHealth.setText(selectedUser.getUser_philhealth());
        Label bday = new Label();
        Label dateOfHire = new Label();
        bday.setText(selectedUser.getUser_bday().toString());
        dateOfHire.setText(selectedUser.getUser_dateOfHire().toString());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        deleteButton.setOnMouseClicked(mouseEvent -> {
            // Handle the click event here
            System.out.println("Delete button clicked!");
            // Add your logic for handling the click event
        });

        editButton.setOnMouseClicked(mouseEvent -> {
            editUserDetails();
        });

    }

    private void editUserDetails() {
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("User editing", "Edit user", fullName.getText());

        boolean result = confirmationAlert.showAndWait();
        Boolean isEditable = result;
        if (isEditable) {
            makeFieldsEditable(isEditable);
        }
    }

    private void makeFieldsEditable(Boolean isEditable) {
        fname.setEditable(isEditable);
        mname.setEditable(isEditable);
        lname.setEditable(isEditable);
        sss.setEditable(isEditable);
        tin.setEditable(isEditable);
        contact.setEditable(isEditable);
        email.setEditable(isEditable);
        province.setEditable(isEditable);
        city.setEditable(isEditable);
        brgy.setEditable(isEditable);
        department.setEditable(isEditable);
        philHealth.setEditable(isEditable);
        positionTextField.setEditable(isEditable);
    }

    DepartmentDAO departmentDAO = new DepartmentDAO();

    void registerNewEmployee() {
        makeFieldsEditable(true);

        department.setItems(departmentDAO.getAllDepartmentNames());
        initializeAddress();

        confirm.setOnMouseClicked(event -> registerEmployeeToDatabase());
    }

    private void registerEmployeeToDatabase() {
        User user = new User();
        user.setUser_fname(fname.getText());
        user.setUser_mname(mname.getText());
        user.setUser_lname(lname.getText());
        user.setUser_contact(contact.getText());
        user.setUser_email(email.getText());
        user.setUser_province(province.getValue());
        user.setUser_city(city.getValue());
        user.setUser_brgy(brgy.getValue());
        user.setUser_department(departmentDAO.getDepartmentIdByName(department.getValue())); // Assuming getDepartmentIdByName method is implemented in DepartmentDAO
        user.setUser_tin(tin.getText());
        user.setUser_sss(sss.getText());
        user.setUser_philhealth(philHealth.getText());
        user.setUser_bday(java.sql.Date.valueOf(birthday.getValue()));
        user.setUser_dateOfHire(java.sql.Date.valueOf(dateHired.getValue()));
        user.setUser_position(positionTextField.getText());

        boolean register = employeeDAO.initialEmployeeRegistration(user);
        if (register) {
            DialogUtils.showConfirmationDialog("Success", fname.getText() + " " + lname.getText() + " has been registered successfully.");
        } else {
            DialogUtils.showErrorMessage("Error", "Use registration failed");
        }
    }

    private void initializeAddress() {
        Map<String, String> provinceData = LocationCache.getProvinceData();
        Map<String, String> cityData = LocationCache.getCityData();
        Map<String, String> barangayData = LocationCache.getBarangayData();

        province.setItems(FXCollections.observableArrayList(provinceData.values()));

        province.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            String selectedProvinceCode = getKeyFromValue(provinceData, newValue);
            List<String> citiesInProvince = filterLocationsByParentCode(cityData, selectedProvinceCode);
            city.setItems(FXCollections.observableArrayList(citiesInProvince));
        });

        city.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            String selectedCityCode = getKeyFromValue(cityData, newValue);
            List<String> barangaysInCity = filterLocationsByParentCode(barangayData, selectedCityCode);
            brgy.setItems(FXCollections.observableArrayList(barangaysInCity));
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
}
