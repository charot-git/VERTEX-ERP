package com.example.vos;

import com.zaxxer.hikari.HikariDataSource;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.w3c.dom.Text;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class EmployeeManagementController implements Initializable {
    public AnchorPane anchorPane;
    private ObservableList<User> userList;
    private FilteredList<User> filteredUserList;
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, Integer> userIdColumn;

    @FXML
    private TableColumn<User, String> firstNameColumn;

    @FXML
    private TableColumn<User, String> middleNameColumn;
    @FXML
    private TableColumn<User, String> lastNameColumn;
    @FXML
    private TableColumn<User, String> provinceColumn;
    @FXML
    private TableColumn<User, String> cityColumn;
    @FXML
    private TableColumn<User, String> brgyColumn;


    private List<User> fetchDataFromDatabase() {
        List<User> userList = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT * FROM user";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        User user = new User();
                        user.setUser_id(resultSet.getInt("user_id"));
                        user.setUser_fname(resultSet.getString("user_fname"));
                        user.setUser_mname(resultSet.getString("user_mname"));
                        user.setUser_lname(resultSet.getString("user_lname"));
                        user.setUser_bday(resultSet.getDate("user_bday"));
                        user.setUser_brgy(resultSet.getString("user_brgy"));
                        user.setUser_city(resultSet.getString("user_city"));
                        user.setUser_province(resultSet.getString("user_province"));
                        user.setUser_contact(resultSet.getString("user_contact"));
                        user.setUser_department(resultSet.getString("user_department"));
                        user.setUser_email(resultSet.getString("user_email"));
                        user.setUser_tin(resultSet.getString("user_tin"));
                        user.setUser_philhealth(resultSet.getString("user_philhealth"));
                        user.setUser_sss(resultSet.getString("user_sss"));
                        user.setUser_tags(resultSet.getString("user_tags"));
                        user.setUser_dateOfHire(resultSet.getDate("user_dateOfHire"));
                        user.setUser_position(resultSet.getString("user_position"));
                        userList.add(user);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userList;
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Initialize the columns
        userIdColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getUser_id()).asObject());
        firstNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUser_fname()));
        middleNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUser_mname()));
        lastNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUser_lname()));
        provinceColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUser_province()));
        cityColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUser_city()));
        brgyColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUser_brgy()));


        FetchDataFromDatabaseTask fetchDataTask = new FetchDataFromDatabaseTask(dataSource);

        // Set up progress and message bindings
        ProgressBar progressBar = new ProgressBar();
        progressBar.progressProperty().bind(fetchDataTask.progressProperty());
        Label statusLabel = new Label();
        statusLabel.textProperty().bind(fetchDataTask.messageProperty());

        // Add progress bar and status label to the AnchorPane
        AnchorPane.setTopAnchor(progressBar, 10.0);
        AnchorPane.setLeftAnchor(progressBar, 10.0);
        AnchorPane.setTopAnchor(statusLabel, 30.0);
        AnchorPane.setLeftAnchor(statusLabel, 10.0);

        anchorPane.getChildren().addAll(progressBar, statusLabel);

        // Execute the task in a background thread
        Thread thread = new Thread(fetchDataTask);
        thread.setDaemon(true);
        thread.start();

        // Update UI when the task completes successfully
        fetchDataTask.setOnSucceeded(succeededEvent  -> {
            List<User> fetchedUsers = fetchDataTask.getValue();
            userList = FXCollections.observableArrayList(fetchedUsers);
            filteredUserList = new FilteredList<>(userList, p -> true);
            userTable.setItems(filteredUserList);

            userTable.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1) {
                    // Get the selected user from the clicked row
                    User selectedUser = userTable.getSelectionModel().getSelectedItem();
                    if (selectedUser != null) {
                        try {
                            // Load employeeDetails.fxml file
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("employeeDetails.fxml"));
                            Parent employeeDetails = loader.load();

                            EmployeeDetailsController controller = loader.getController();

                            controller.initData(selectedUser);

                            // Set the loaded content as the content of the contentPane
                            AnchorPane contentPane = (AnchorPane) ((Node) event.getSource()).getScene().getRoot().lookup("#contentPane");
                            contentPane.getChildren().clear(); // Clear existing content
                            contentPane.getChildren().add(employeeDetails);

                            // Set maximum dimensions for the contentPane (parent of employeeDetails)
                            double maxDetailsWidth = contentPane.getWidth() - 20; // Adjust as needed
                            double maxDetailsHeight = contentPane.getHeight() - 20; // Adjust as needed
                            contentPane.setMaxSize(maxDetailsWidth, maxDetailsHeight);
                            contentPane.setMinSize(0, 0); // Allow contentPane to shrink smaller if necessary

                            // Make employeeDetails fit inside contentPane while preserving aspect ratio
                            AnchorPane.setTopAnchor(employeeDetails, 0.0);
                            AnchorPane.setRightAnchor(employeeDetails, 0.0);
                            AnchorPane.setBottomAnchor(employeeDetails, 0.0);
                            AnchorPane.setLeftAnchor(employeeDetails, 0.0);

                        } catch (IOException e) {
                            e.printStackTrace(); // Handle the exception according to your needs
                            System.err.println("Error loading employeeDetails.fxml: " + e.getMessage());
                        }
                    }
                }
            });

            // Remove progress and status labels from UI
            anchorPane.getChildren().removeAll(progressBar, statusLabel);
        });

        // Handle task failure
        fetchDataTask.setOnFailed(event -> {
            // Handle task failure here
            Exception ex = (Exception) fetchDataTask.getException();
            ex.printStackTrace();
        });

        TextField searchBar = SharedFunctions.getSearchBar();
        searchBar.setPromptText("Search employee");

        // Add listener to the search bar text property
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredUserList.setPredicate(user -> {
                // If the search bar is empty, show all employees
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                // Convert search input to lowercase for case-insensitive search
                String lowerCaseFilter = newValue.toLowerCase();

                // Check if any of the user properties contain the search input
                return user.getUser_fname().toLowerCase().contains(lowerCaseFilter) ||
                        user.getUser_mname().toLowerCase().contains(lowerCaseFilter) ||
                        user.getUser_lname().toLowerCase().contains(lowerCaseFilter) ||
                        user.getUser_province().toLowerCase().contains(lowerCaseFilter) ||
                        user.getUser_city().toLowerCase().contains(lowerCaseFilter) ||
                        user.getUser_brgy().toLowerCase().contains(lowerCaseFilter); // Show user if any property matches the search input
// Don't show user if no property matches the search input
            });
        });

        userList = FXCollections.observableArrayList(fetchDataFromDatabase());

        filteredUserList = new FilteredList<>(userList, p -> true);

        userTable.setItems(filteredUserList);

    }

    public void addNewEmployee(MouseEvent mouseEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addNewEmployee.fxml"));
            Parent addNewEmployee = loader.load();
            AddNewEmployeeController addNewEmployeeController = loader.getController();

            // Set the loaded content as the content of the contentPane
            AnchorPane contentPane = (AnchorPane) ((Node) mouseEvent.getSource()).getScene().getRoot().lookup("#contentPane");
            contentPane.getChildren().clear(); // Clear existing content
            contentPane.getChildren().add(addNewEmployee);

            // Set maximum dimensions for the contentPane (parent of addNewEmployee)
            double maxEmployeeWidth = contentPane.getWidth() - 20; // Adjust as needed
            double maxEmployeeHeight = contentPane.getHeight() - 20; // Adjust as needed
            contentPane.setMaxSize(maxEmployeeWidth, maxEmployeeHeight);
            contentPane.setMinSize(0, 0); // Allow contentPane to shrink smaller if necessary

            // Make addNewEmployee fit inside contentPane while preserving aspect ratio
            AnchorPane.setTopAnchor(addNewEmployee, 0.0);
            AnchorPane.setRightAnchor(addNewEmployee, 0.0);
            AnchorPane.setBottomAnchor(addNewEmployee, 0.0);
            AnchorPane.setLeftAnchor(addNewEmployee, 0.0);
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading addNewEmployee.fxml: " + e.getMessage());
        }

    }
}
