package com.vertex.vos;

import com.vertex.vos.Objects.Module;
import com.vertex.vos.Objects.Taskbar;
import com.vertex.vos.Objects.User;
import com.vertex.vos.Utilities.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class EmployeeDetailsController implements Initializable {
    public ListView <String> userModules, availableModules;
    @FXML
    private TextField fname, mname, lname, sss, tin, contact, email, philHealth, positionTextField;
    @FXML
    private ComboBox<String> province, city, brgy, department;
    @FXML
    private Label fullName, position, roles;
    @FXML
    private ImageView profilePic, deleteButton, editButton;
    @FXML
    private ListView<String> userTaskbar, availableTaskBars;
    @FXML
    private Button confirm;
    @FXML
    private DatePicker birthday, dateHired;

    private EmployeeDAO employeeDAO = new EmployeeDAO();
    private TaskbarDAO taskbarDAO = new TaskbarDAO();
    private DepartmentDAO departmentDAO = new DepartmentDAO();
    private ModuleDAO moduleDAO = new ModuleDAO();

    private ObservableList<String> userTaskbarNames = FXCollections.observableArrayList();
    private ObservableList<String> availableTaskBarNames = FXCollections.observableArrayList();
    private ObservableList<String> userModuleNames = FXCollections.observableArrayList(); // New list for modules
    private ObservableList<String> availableModuleNames = FXCollections.observableArrayList(); // New list for modules
    private TableManagerController tableManagerController;

    public void initData(User selectedUser) {
        confirm.setDisable(true);
        Platform.runLater(() -> {
            setDetails(selectedUser);
            setUserAccess(selectedUser);
            setUserModules(selectedUser);
        });
        deleteButton.setOnMouseClicked(mouseEvent -> {
            ConfirmationAlert confirmationAlert = new ConfirmationAlert("Delete user", "Delete user", fullName.getText(), true);
            boolean confirmed = confirmationAlert.showAndWait();
            if (confirmed) {
                employeeDAO.deleteEmployee(selectedUser.getUser_id());
            }
        });
    }

    private void setUserModules(User selectedUser) {
        List<Integer> moduleAccess = moduleDAO.getModulesForUser(selectedUser.getUser_id());
        ObservableList<Module> allModules = moduleDAO.getAllModules();

        // Populate userModules and availableModules lists
        ObservableList<String> userModulesList = FXCollections.observableArrayList();
        ObservableList<String> availableModulesList = FXCollections.observableArrayList();

        for (int moduleId : moduleAccess) {
            Module userModule = moduleDAO.getModuleById(moduleId);
            userModulesList.add(userModule.getModuleLabel());
        }

        allModules.removeIf(module -> userModulesList.contains(module.getModuleLabel()));

        for (Module module : allModules) {
            availableModulesList.add(module.getModuleLabel());
        }

        userModules.setItems(userModulesList);
        availableModules.setItems(availableModulesList);

        userModules.setOnMouseClicked(event -> {
            String selectedModuleLabel = userModules.getSelectionModel().getSelectedItem();
            if (selectedModuleLabel != null) {
                Module selectedModule = moduleDAO.getModuleByLabel(selectedModuleLabel);
                if (selectedModule != null) {
                    moduleDAO.removeModuleFromUser(selectedUser.getUser_id(), selectedModule.getId());
                    availableModulesList.add(selectedModule.getModuleLabel());
                    userModulesList.remove(selectedModule.getModuleLabel());
                    updateModuleLists(userModulesList, availableModulesList);
                }
            }
        });

        availableModules.setOnMouseClicked(event -> {
            String selectedModuleLabel = availableModules.getSelectionModel().getSelectedItem();
            if (selectedModuleLabel != null) {
                Module selectedModule = moduleDAO.getModuleByLabel(selectedModuleLabel);
                if (selectedModule != null) {
                    moduleDAO.assignModuleToUser(selectedUser.getUser_id(), selectedModule.getId());
                    userModulesList.add(selectedModule.getModuleLabel());
                    availableModulesList.remove(selectedModule.getModuleLabel());
                    updateModuleLists(userModulesList, availableModulesList);
                }
            }
        });
    }

    private void updateModuleLists(ObservableList<String> userModulesList, ObservableList<String> availableModulesList) {
        userModules.setItems(userModulesList);
        availableModules.setItems(availableModulesList);
    }

    private void setUserAccess(User selectedUser) {
        List<Integer> taskbarAccess = taskbarDAO.getTaskbarsForUser(selectedUser.getUser_id());
        ObservableList<Taskbar> allTaskbars = FXCollections.observableArrayList(taskbarDAO.getAllTaskbars());

        ObservableList<String> userTaskbarsList = FXCollections.observableArrayList();
        ObservableList<String> availableTaskbarsList = FXCollections.observableArrayList();

        // Populate userTaskbars and availableTaskbars lists
        for (int taskbarId : taskbarAccess) {
            Taskbar userTaskbar = taskbarDAO.getTaskbarById(taskbarId);
            userTaskbarsList.add(userTaskbar.getTaskbarLabel());
        }

        allTaskbars.removeIf(taskbar -> userTaskbarsList.contains(taskbar.getTaskbarLabel()));

        for (Taskbar taskbar : allTaskbars) {
            availableTaskbarsList.add(taskbar.getTaskbarLabel());
        }

        userTaskbar.setItems(userTaskbarsList);
        availableTaskBars.setItems(availableTaskbarsList);

        userTaskbar.setOnMouseClicked(event -> {
            String selectedTaskbarLabel = userTaskbar.getSelectionModel().getSelectedItem();
            if (selectedTaskbarLabel != null) {
                Taskbar selectedTaskbar = taskbarDAO.getTaskbarByLabel(selectedTaskbarLabel);
                if (selectedTaskbar != null) {
                    taskbarDAO.removeTaskbarFromUser(selectedUser.getUser_id(), selectedTaskbar.getId());
                    availableTaskbarsList.add(selectedTaskbar.getTaskbarLabel());
                    userTaskbarsList.remove(selectedTaskbar.getTaskbarLabel());
                    updateTaskbarLists(userTaskbarsList, availableTaskbarsList);
                }
            }
        });

        availableTaskBars.setOnMouseClicked(event -> {
            String selectedTaskbarLabel = availableTaskBars.getSelectionModel().getSelectedItem();
            if (selectedTaskbarLabel != null) {
                Taskbar selectedTaskbar = taskbarDAO.getTaskbarByLabel(selectedTaskbarLabel);
                if (selectedTaskbar != null) {
                    taskbarDAO.assignTaskbarToUser(selectedUser.getUser_id(), selectedTaskbar.getId());
                    userTaskbarsList.add(selectedTaskbar.getTaskbarLabel());
                    availableTaskbarsList.remove(selectedTaskbar.getTaskbarLabel());
                    updateTaskbarLists(userTaskbarsList, availableTaskbarsList);
                }
            }
        });
    }

    private void updateTaskbarLists(ObservableList<String> userTaskbarsList, ObservableList<String> availableTaskbarsList) {
        userTaskbar.setItems(userTaskbarsList);
        availableTaskBars.setItems(availableTaskbarsList);
    }

    private void setDetails(User selectedUser) {
        String completeName = selectedUser.getUser_fname() + " " + selectedUser.getUser_mname() + " " + selectedUser.getUser_lname();
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
        birthday.setValue(selectedUser.getUser_bday().toLocalDate());
        dateHired.setValue(selectedUser.getUser_dateOfHire().toLocalDate());

        confirm.setText("Update");
        confirm.setOnMouseClicked(mouseEvent -> updateUser());
    }

    private void updateUser() {
        User updatedUser = new User();
        updatedUser.setUser_fname(fname.getText());
        updatedUser.setUser_mname(mname.getText());
        updatedUser.setUser_lname(lname.getText());
        updatedUser.setUser_contact(contact.getText());
        updatedUser.setUser_email(email.getText());
        updatedUser.setUser_province(province.getValue());
        updatedUser.setUser_city(city.getValue());
        updatedUser.setUser_brgy(brgy.getValue());
        updatedUser.setUser_department(departmentDAO.getDepartmentIdByName(department.getValue()));
        updatedUser.setUser_tin(tin.getText());
        updatedUser.setUser_sss(sss.getText());
        updatedUser.setUser_philhealth(philHealth.getText());
        updatedUser.setUser_bday(java.sql.Date.valueOf(birthday.getValue()));
        updatedUser.setUser_dateOfHire(java.sql.Date.valueOf(dateHired.getValue()));
        updatedUser.setUser_position(positionTextField.getText());

        boolean success = employeeDAO.updateUser(updatedUser);
        if (success) {
            DialogUtils.showConfirmationDialog("Success", "User details updated successfully.");
        } else {
            DialogUtils.showErrorMessage("Error", "Failed to update user details.");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        editButton.setOnMouseClicked(mouseEvent -> editUserDetails());
        ObservableList<Taskbar> taskbars = FXCollections.observableArrayList(taskbarDAO.getAllTaskbars());
    }

    private void editUserDetails() {
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("User editing", "Edit user", fullName.getText(), false);
        boolean confirmed = confirmationAlert.showAndWait();
        if (confirmed) {
            makeFieldsEditable(true);
            confirm.setDisable(false);
            department.setItems(departmentDAO.getAllDepartmentNames());
        } else {
            makeFieldsEditable(false);
            confirm.setDisable(true);
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
        initializeAddress();
    }

    void registerNewEmployee() {
        makeFieldsEditable(true);
        birthday.setValue(LocalDate.now());
        dateHired.setValue(LocalDate.now());
        department.setItems(departmentDAO.getAllDepartmentNames());
        initializeAddress();
        confirm.setOnMouseClicked(event -> registerEmployeeToDatabase());
    }

    private void initializeAddress() {
        LocationComboBoxUtil locationComboBoxUtil = new LocationComboBoxUtil(province, city, brgy);
        locationComboBoxUtil.initializeComboBoxes();
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
        user.setUser_department(departmentDAO.getDepartmentIdByName(department.getValue()));
        user.setUser_tin(tin.getText());
        user.setUser_sss(sss.getText());
        user.setUser_philhealth(philHealth.getText());
        user.setUser_bday(java.sql.Date.valueOf(birthday.getValue()));
        user.setUser_dateOfHire(java.sql.Date.valueOf(dateHired.getValue()));
        user.setUser_position(positionTextField.getText());

        boolean success = employeeDAO.initialEmployeeRegistration(user);
        if (success) {
            DialogUtils.showConfirmationDialog("Success", "Employee added successfully.");
        } else {
            DialogUtils.showErrorMessage("Error", "Failed to add employee.");
        }
    }

    public void setTableManager(TableManagerController tableManagerController) {
        this.tableManagerController = tableManagerController;
    }
}
