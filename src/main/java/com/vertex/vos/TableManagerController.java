package com.vertex.vos;

import com.zaxxer.hikari.HikariDataSource;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class TableManagerController implements Initializable {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    private String registrationType;
    @FXML
    private ProgressIndicator loadingSpinner;

    public void setRegistrationType(String registrationType) {
        this.registrationType = registrationType;
    }

    public void setContentPane(AnchorPane contentPane) {
        // Declare contentPane variable
    }

    private final HistoryManager historyManager = new HistoryManager();

    private final int currentNavigationId = -1; // Initialize to a default value

    @FXML
    private ImageView tableImg, addImage;
    @FXML
    private Label tableHeader;
    @FXML
    private TableView defaultTable;
    @FXML
    private TableColumn column1;
    @FXML
    private Label columnHeader1;
    @FXML
    private TableColumn column2;
    @FXML
    private Label columnHeader2;
    @FXML
    private TableColumn column3;
    @FXML
    private Label columnHeader3;
    @FXML
    private TableColumn column4;
    @FXML
    private Label columnHeader4;
    @FXML
    private TableColumn column5;
    @FXML
    private Label columnHeader5;
    @FXML
    private TableColumn column6;
    @FXML
    private Label columnHeader6;
    @FXML
    private TableColumn column7;
    @FXML
    private Label columnHeader7;
    @FXML
    private TableColumn column8;
    @FXML
    private Label columnHeader8;

    @FXML
    private void addNew(MouseEvent mouseEvent) {
        switch (registrationType) {
            case "company" -> addNewCompany();
            case "branch" -> addNewBranch();
            case "employee" -> addNewEmployee();
            case "supplier" -> addNewSupplier();
            case "product" -> addNewProduct();
            case "system_employee" -> addNewSystemEmployeeTable();
            default -> tableHeader.setText("Unknown Type");
        }
    }

    private void addNewSystemEmployeeTable() {

    }

    private void addNewProduct() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("registerProductTemplate.fxml"));
            Parent content = loader.load();

            RegisterProductTemplateController controller = loader.getController();

            // Create a new stage (window) for company registration
            Stage stage = new Stage();
            stage.setTitle("Product Registration"); // Set the title of the new stage
            stage.setScene(new Scene(content)); // Set the scene with the loaded content
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading companyRegistration.fxml: " + e.getMessage());
        }
    }

    private void addNewSupplier() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("supplierInfoRegistration.fxml"));
            Parent content = loader.load();

            SupplierInfoRegistrationController controller = loader.getController();

            // Create a new stage (window) for company registration
            Stage stage = new Stage();
            stage.setTitle("Supplier Registration"); // Set the title of the new stage
            stage.setScene(new Scene(content)); // Set the scene with the loaded content
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading companyRegistration.fxml: " + e.getMessage());
        }
    }

    private void addNewEmployee() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addNewEmployee.fxml"));
            Parent content = loader.load();

            AddNewEmployeeController controller = loader.getController();

            // Create a new stage (window) for company registration
            Stage stage = new Stage();
            stage.setTitle("Register new employee"); // Set the title of the new stage
            stage.setScene(new Scene(content)); // Set the scene with the loaded content
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading companyRegistration.fxml: " + e.getMessage());
        }
    }

    private void addNewBranch() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("branchRegistration.fxml"));
            Parent content = loader.load();

            BranchRegistrationController controller = loader.getController();

            // Create a new stage (window) for company registration
            Stage stage = new Stage();
            stage.setTitle("Supplier Registration"); // Set the title of the new stage
            stage.setScene(new Scene(content)); // Set the scene with the loaded content
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading companyRegistration.fxml: " + e.getMessage());
        }
    }

    private void addNewCompany() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("companyRegistration.fxml"));
            Parent content = loader.load();

            CompanyRegistrationController controller = loader.getController();

            // Create a new stage (window) for company registration
            Stage stage = new Stage();
            stage.setTitle("Company Registration"); // Set the title of the new stage
            stage.setScene(new Scene(content)); // Set the scene with the loaded content
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading companyRegistration.fxml: " + e.getMessage());
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        defaultTable.setVisible(false);
        Platform.runLater(() -> {
            System.out.println("Registration Type: " + registrationType); // Debug statement
            switch (registrationType) {
                case "company" -> loadCompanyTable();
                case "branch" -> loadBranchTable();
                case "employee" -> loadEmployeeTable();
                case "product" -> loadProductTable();
                case "supplier" -> loadSupplierTable();
                case "system_employee" -> loadSystemEmployeeTable();
                default -> tableHeader.setText("Unknown Type");
            }
            defaultTable.setVisible(true);
        });

        defaultTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Check for double-click
                handleTableDoubleClick(defaultTable.getSelectionModel().getSelectedItem());
            }
        });
        Duration duration = Duration.seconds(5);
        KeyFrame keyFrame = new KeyFrame(duration, event -> {
            // Call your method to refresh data periodically
            if (registrationType != null) {
                refreshData();
            }
        });
        Timeline timeline = new Timeline(keyFrame);
        timeline.setCycleCount(Timeline.INDEFINITE); // Run the timeline indefinitely
        timeline.play();
    }

    private void loadSystemEmployeeTable() {
        tableHeader.setText("System Employees");

        // Set column headers
        columnHeader1.setText("Employee ID");
        columnHeader2.setText("First Name");
        columnHeader3.setText("Middle Name");
        columnHeader4.setText("Last Name");
        columnHeader5.setText("Email");
        columnHeader6.setText("Contact");
        columnHeader7.setText("Position");
        columnHeader8.setText("Department");

        // Set cell value factories for table columns
        column1.setCellValueFactory(new PropertyValueFactory<>("user_id"));
        column2.setCellValueFactory(new PropertyValueFactory<>("user_fname"));
        column3.setCellValueFactory(new PropertyValueFactory<>("user_mname"));
        column4.setCellValueFactory(new PropertyValueFactory<>("user_lname"));
        column5.setCellValueFactory(new PropertyValueFactory<>("user_email"));
        column6.setCellValueFactory(new PropertyValueFactory<>("user_contact"));
        column7.setCellValueFactory(new PropertyValueFactory<>("user_position"));
        column8.setCellValueFactory(new PropertyValueFactory<>("user_department"));

        defaultTable.setRowFactory(tv -> new TableRow<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle(""); // Set default style for empty rows
                } else {
                    String password = item.getUser_password(); // Replace with the appropriate method to get user_password
                    if (password == null || password.isEmpty()) {
                        setStyle("-fx-background-color: orange;"); // Set orange background for rows with null or empty password
                    } else {
                        setStyle(""); // Set default style for rows with non-empty password
                    }
                }
            }
        });
        String query = "SELECT * FROM user"; // Exclude users without passwords
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            // Clear existing items in the table
            defaultTable.getItems().clear();
            while (resultSet.next()) {
                User employee = new User(
                        resultSet.getInt("user_id"),
                        resultSet.getString("user_email"),
                        resultSet.getString("user_password"),
                        resultSet.getString("user_fname"),
                        resultSet.getString("user_mname"),
                        resultSet.getString("user_lname"),
                        resultSet.getString("user_contact"),
                        resultSet.getString("user_province"),
                        resultSet.getString("user_city"),
                        resultSet.getString("user_brgy"),
                        resultSet.getString("user_sss"),
                        resultSet.getString("user_philhealth"),
                        resultSet.getString("user_tin"),
                        resultSet.getString("user_position"),
                        resultSet.getString("user_department"),
                        resultSet.getDate("user_dateOfHire"),
                        resultSet.getString("user_tags"),
                        resultSet.getDate("user_bday"),
                        resultSet.getInt("role_id"),
                        resultSet.getBytes("user_image")
                );
                defaultTable.getItems().add(employee);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private void refreshData() {
        defaultTable.getItems().clear();
        switch (registrationType) {
            case "company" -> loadCompanyTable();
            case "branch" -> loadBranchTable();
            case "employee" -> loadEmployeeTable();
            case "product" -> loadProductTable();
            case "supplier" -> loadSupplierTable();
            case "system_employee" -> loadSystemEmployeeTable();
            default -> tableHeader.setText("Unknown Type");
        }
    }

    private void loadSupplierTable() {
        tableHeader.setText("Suppliers");
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/price-tag.png"));
        tableImg.setImage(image);
        columnHeader1.setText("Supplier ID");
        columnHeader2.setText("Supplier Name");
        columnHeader3.setText("Logo");
        columnHeader4.setText("Email Address");
        columnHeader5.setText("Phone Number");
        columnHeader6.setText("Address");
        columnHeader7.setText("City");
        columnHeader8.setText("Baranggay");

        // Set cell value factories for table columns
        column1.setCellValueFactory(new PropertyValueFactory<>("id"));
        column2.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        column3.setCellValueFactory(new PropertyValueFactory<>("supplierImage"));
        column4.setCellValueFactory(new PropertyValueFactory<>("emailAddress"));
        column5.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        column6.setCellValueFactory(new PropertyValueFactory<>("address"));
        column7.setCellValueFactory(new PropertyValueFactory<>("city"));
        column8.setCellValueFactory(new PropertyValueFactory<>("brgy"));

        column3.setCellFactory(param -> new TableCell<Supplier, byte[]>() {
            private final ImageView imageView = new ImageView();

            {
                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(byte[] logo, boolean empty) {
                super.updateItem(logo, empty);
                if (empty || logo == null) {
                    setGraphic(null);
                } else {
                    Image image = new Image(new ByteArrayInputStream(logo));
                    imageView.setImage(image);
                    imageView.setFitWidth(50);
                    imageView.setFitHeight(50);
                    setGraphic(imageView);
                }
            }
        });

        // Execute a database query to fetch supplier data
        String query = "SELECT * FROM suppliers";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            defaultTable.getItems().clear();

            while (resultSet.next()) {
                Supplier supplier = new Supplier(
                        resultSet.getInt("id"),
                        resultSet.getString("supplier_name"),
                        resultSet.getString("contact_person"),
                        resultSet.getString("email_address"),
                        resultSet.getString("phone_number"),
                        resultSet.getString("address"),
                        resultSet.getString("city"),
                        resultSet.getString("brgy"),
                        resultSet.getString("state_province"),
                        resultSet.getString("postal_code"),
                        resultSet.getString("country"),
                        resultSet.getString("supplier_type"),
                        resultSet.getString("tin_number"),
                        resultSet.getString("bank_details"),
                        resultSet.getString("products_or_services"),
                        resultSet.getString("payment_terms"),
                        resultSet.getString("delivery_terms"),
                        resultSet.getString("agreement_or_contract"),
                        resultSet.getString("preferred_communication_method"),
                        resultSet.getString("notes_or_comments"),
                        resultSet.getDate("date_added"),
                        resultSet.getBytes("supplier_image")
                );

                // Add the supplier to the table
                defaultTable.getItems().add(supplier);

            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception according to your needs
        }


    }


    private void loadProductTable() {
        tableHeader.setText("Products");
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/package.png"));

        tableImg.setImage(image);

        // Set column headers
        columnHeader1.setText("Product ID");
        columnHeader2.setText("Product Name");
        columnHeader3.setText("Product Code");
        columnHeader4.setText("Cost per Unit");
        columnHeader5.setText("Price per Unit");
        columnHeader6.setText("Discount");
        columnHeader7.setText("Quantity Available");
        columnHeader8.setText("Supplier Name");

        column1.setCellValueFactory(new PropertyValueFactory<>("productId"));
        column2.setCellValueFactory(new PropertyValueFactory<>("productName"));
        column3.setCellValueFactory(new PropertyValueFactory<>("productCode"));
        column4.setCellValueFactory(new PropertyValueFactory<>("costPerUnit"));
        column5.setCellValueFactory(new PropertyValueFactory<>("pricePerUnit"));
        column6.setCellValueFactory(new PropertyValueFactory<>("productDiscount"));
        column7.setCellValueFactory(new PropertyValueFactory<>("quantityAvailable"));
        column8.setCellValueFactory(new PropertyValueFactory<>("supplierName"));

        String query = "SELECT * FROM products";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            defaultTable.getItems().clear();

            while (resultSet.next()) {
                Product product = new Product(
                        resultSet.getInt("product_id"),
                        resultSet.getString("product_name"),
                        resultSet.getString("product_code"),
                        resultSet.getDouble("cost_per_unit"),
                        resultSet.getDouble("price_per_unit"),
                        resultSet.getDouble("product_discount"),
                        resultSet.getInt("quantity_available"),
                        resultSet.getString("description"),
                        resultSet.getString("supplier_name"),
                        resultSet.getDate("date_added"),
                        resultSet.getTimestamp("last_updated"),
                        resultSet.getDouble("priceA"),
                        resultSet.getDouble("priceB"),
                        resultSet.getDouble("priceC")
                );
                defaultTable.getItems().add(product);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception according to your needs
        }
    }


    private void handleTableDoubleClick(Object selectedItem) {
        if (selectedItem instanceof User selectedEmployee) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("employeeDetails.fxml"));
                Parent root = loader.load();

                EmployeeDetailsController controller = loader.getController();
                controller.initData(selectedEmployee);

                // Create a new stage (window) for employee details
                Stage stage = new Stage();
                stage.setTitle("Employee Details");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace(); // Handle the exception according to your needs
            }
        } else if (selectedItem instanceof Product selectedProduct) {

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("registerProductTemplate.fxml"));
                Parent root = loader.load();

                // Pass the selected employee data to the controller of employeeDetails.fxml
                RegisterProductTemplateController controller = loader.getController();
                controller.initData(selectedProduct);

                Stage stage = new Stage();
                stage.setTitle("Product Details");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        }
    }


    private void loadEmployeeTable() {
        tableHeader.setText("Employees");

        // Set column headers
        columnHeader1.setText("Employee ID");
        columnHeader2.setText("First Name");
        columnHeader3.setText("Middle Name");
        columnHeader4.setText("Last Name");
        columnHeader5.setText("Email");
        columnHeader6.setText("Contact");
        columnHeader7.setText("Position");
        columnHeader8.setText("Department");

        column1.setCellValueFactory(new PropertyValueFactory<>("user_id"));
        column2.setCellValueFactory(new PropertyValueFactory<>("user_fname"));
        column3.setCellValueFactory(new PropertyValueFactory<>("user_mname"));
        column4.setCellValueFactory(new PropertyValueFactory<>("user_lname"));
        column5.setCellValueFactory(new PropertyValueFactory<>("user_email"));
        column6.setCellValueFactory(new PropertyValueFactory<>("user_contact"));
        column7.setCellValueFactory(new PropertyValueFactory<>("user_position"));
        column8.setCellValueFactory(new PropertyValueFactory<>("user_department"));

        String query = "SELECT * FROM user"; // Assuming employees have a specific role ID
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            // Clear existing items in the table
            defaultTable.getItems().clear();
            // Iterate through the result set and populate the table with employee data
            while (resultSet.next()) {
                User employee = new User(
                        resultSet.getInt("user_id"),
                        resultSet.getString("user_email"),
                        resultSet.getString("user_password"),
                        resultSet.getString("user_fname"),
                        resultSet.getString("user_mname"),
                        resultSet.getString("user_lname"),
                        resultSet.getString("user_contact"),
                        resultSet.getString("user_province"),
                        resultSet.getString("user_city"),
                        resultSet.getString("user_brgy"),
                        resultSet.getString("user_sss"),
                        resultSet.getString("user_philhealth"),
                        resultSet.getString("user_tin"),
                        resultSet.getString("user_position"),
                        resultSet.getString("user_department"),
                        resultSet.getDate("user_dateOfHire"),
                        resultSet.getString("user_tags"),
                        resultSet.getDate("user_bday"),
                        resultSet.getInt("role_id"),
                        resultSet.getBytes("user_image")
                );
                defaultTable.getItems().add(employee);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }


    private void loadBranchTable() {
        tableHeader.setText("Branches");

        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/division.png"));

        tableImg.setImage(image);

        // Set column headers
        columnHeader1.setText("Branch ID");
        columnHeader2.setText("Description");
        columnHeader3.setText("Branch Name");
        columnHeader4.setText("Branch Head");
        columnHeader5.setText("Branch Code");
        columnHeader6.setText("State/Province");
        columnHeader7.setText("City");
        columnHeader8.setText("Barangay");

        // Set cell value factories for table columns
        column1.setCellValueFactory(new PropertyValueFactory<>("id"));
        column2.setCellValueFactory(new PropertyValueFactory<>("branchDescription"));
        column3.setCellValueFactory(new PropertyValueFactory<>("branchName"));
        column4.setCellValueFactory(new PropertyValueFactory<>("branchHead"));
        column5.setCellValueFactory(new PropertyValueFactory<>("branchCode"));
        column6.setCellValueFactory(new PropertyValueFactory<>("stateProvince"));
        column7.setCellValueFactory(new PropertyValueFactory<>("city"));
        column8.setCellValueFactory(new PropertyValueFactory<>("brgy"));

        // Execute a database query to fetch branch data
        String query = "SELECT * FROM branches";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            // Clear existing items in the table
            defaultTable.getItems().clear();

            // Iterate through the result set and populate the table
            while (resultSet.next()) {
                Branch branch = new Branch(
                        resultSet.getInt("id"),
                        resultSet.getString("branch_description"),
                        resultSet.getString("branch_name"),
                        resultSet.getString("branch_head"),
                        resultSet.getString("branch_code"),
                        resultSet.getString("state_province"),
                        resultSet.getString("city"),
                        resultSet.getString("brgy"),
                        resultSet.getString("phone_number"),
                        resultSet.getString("postal_code"),
                        resultSet.getDate("date_added")
                );

                // Add the branch to the table
                defaultTable.getItems().add(branch);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception according to your needs
        }
    }


    private void loadCompanyTable() {
        ObservableList<Company> companies = FXCollections.observableArrayList();

        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/business-and-trade.png"));

        tableImg.setImage(image);

        tableHeader.setText("Companies");
        columnHeader1.setText("Company ID");
        columnHeader2.setText("Company Name");
        columnHeader3.setText("Logo");
        columnHeader4.setText("Company Code");
        columnHeader5.setText("Company Type");
        columnHeader6.setText("First Address");
        columnHeader7.setText("Registration Number");
        columnHeader8.setText("TIN");
        // Set cell value factories for table columns
        column1.setCellValueFactory(new PropertyValueFactory<>("companyId"));
        column2.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        column3.setCellValueFactory(new PropertyValueFactory<>("companyLogo")); // Using PropertyValueFactory for demonstration purposes
        column4.setCellValueFactory(new PropertyValueFactory<>("companyCode"));
        column5.setCellValueFactory(new PropertyValueFactory<>("companyType"));
        column6.setCellValueFactory(new PropertyValueFactory<>("companyFirstAddress"));
        column7.setCellValueFactory(new PropertyValueFactory<>("companyRegistrationNumber"));
        column8.setCellValueFactory(new PropertyValueFactory<>("companyTIN"));


        column3.setCellFactory(param -> new TableCell<Company, byte[]>() {
            private final ImageView imageView = new ImageView();

            {
                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(byte[] logo, boolean empty) {
                super.updateItem(logo, empty);
                if (empty || logo == null) {
                    setGraphic(null);
                } else {
                    // Assuming logo is a byte array containing image data
                    Image image = new Image(new ByteArrayInputStream(logo));
                    imageView.setImage(image);
                    imageView.setFitWidth(50);  // Set the width of the displayed image
                    imageView.setFitHeight(50); // Set the height of the displayed image
                    setGraphic(imageView);
                }
            }
        });
        // Execute a database query to fetch company data
        String query = "SELECT * FROM company";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            // Clear existing items in the table
            defaultTable.getItems().clear();
            // Iterate through the result set and populate the table
            while (resultSet.next()) {
                Company company = new Company(
                        resultSet.getInt("company_id"),
                        resultSet.getString("company_name"),
                        resultSet.getString("company_type"),
                        resultSet.getString("company_code"),
                        resultSet.getString("company_firstAddress"),
                        resultSet.getString("company_secondAddress"),
                        resultSet.getString("company_registrationNumber"),
                        resultSet.getString("company_tin"),
                        resultSet.getDate("company_dateAdmitted"),
                        resultSet.getString("company_contact"),
                        resultSet.getString("company_email"),
                        resultSet.getString("company_department"),
                        resultSet.getBytes("company_logo"),
                        resultSet.getString("company_tags")

                );
                companies.add(company);
                defaultTable.setItems(companies);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
