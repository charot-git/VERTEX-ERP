package com.vertex.vos;

import com.vertex.vos.Constructors.*;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
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
import org.apache.commons.lang3.RandomStringUtils;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
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

    private ObservableList<Map<String, String>> brandData;
    private ObservableList<Map<String, String>> classData = FXCollections.observableArrayList();
    private ObservableList<Map<String, String>> segmentData = FXCollections.observableArrayList();
    private ObservableList<Map<String, String>> categoryData = FXCollections.observableArrayList();
    private ObservableList<Map<String, String>> natureData = FXCollections.observableArrayList();
    private ObservableList<Map<String, String>> sectionData = FXCollections.observableArrayList();

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
                case "industry" -> loadIndustryTable();
                case "division" -> loadDivisionTable();
                case "department" -> loadDepartmentTable();
                case "category" -> loadCategoryTable();
                case "brand" -> loadBrandTable();
                case "segment" -> loadSegmentTable();
                case "delivery_terms" -> loadDeliveryTerms();
                case "payment_terms" -> loadPaymentTerms();
                case "discount_setup" -> loadDiscountSetUpTable();
                case "class" -> loadClassTable();
                case "nature" -> loadNatureTable();
                case "section" -> loadSectionTable();

                default -> tableHeader.setText("Unknown Type");
            }
            defaultTable.setVisible(true);
        });

        defaultTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Check for double-click
                handleTableDoubleClick(defaultTable.getSelectionModel().getSelectedItem());
            }
        });
    }

    @FXML
    private void addNew(MouseEvent mouseEvent) {
        switch (registrationType) {
            case "company" -> addNewCompany();
            case "branch" -> addNewBranch();
            case "employee" -> addNewEmployee();
            case "supplier" -> addNewSupplier();
            case "product" -> addNewProduct();
            case "system_employee" -> addNewSystemEmployeeTable();
            case "industry" -> addNewIndustry();
            case "division" -> addNewDivision();
            case "department" -> addNewDepartment();
            case "category" -> addNewCategory();
            case "brand" -> addNewBrand();
            case "segment" -> addNewSegment();
            case "nature" -> addNewNature();
            case "class" -> addNewClass();
            case "section" -> addNewSection();
            default -> tableHeader.setText("Unknown Type");
        }
    }

    private void addNewClass() {
        String natureName = EntryAlert.showEntryAlert("Nature Registration", "Please enter nature to be registered", "Nature : ");
        NatureDAO natureDAO = new NatureDAO();
        if (!natureName.isEmpty()) {
            boolean natureRegistered = natureDAO.createNature(natureName);
            if (natureRegistered) {
                DialogUtils.showConfirmationDialog("Nature Created", "Nature created successfully: " + natureName);
                // The nature was created successfully, perform additional actions if needed
            } else {
                DialogUtils.showErrorMessage("Nature Creation Failed", "Failed to create nature: " + natureName);
                // Handle the case where nature creation failed
            }
        } else {
            DialogUtils.showErrorMessage("Invalid Nature", "Nature name is empty or null. Nature creation canceled.");
            // Handle the case where the nature name is empty or null
        }

    }

    public void addNewSection() {
        String sectionName = EntryAlert.showEntryAlert("Section Registration", "Please enter section to be registered", "Section: ");
        SectionsDAO sectionsDAO = new SectionsDAO(); // Assuming you have a DAO class for handling sections

        if (sectionName != null && !sectionName.isEmpty()) {
            boolean sectionAdded = sectionsDAO.addSection(sectionName);
            if (sectionAdded) {
                DialogUtils.showConfirmationDialog("Section Created", "Section created successfully: " + sectionName);
                // Additional actions upon successful section creation
            } else {
                DialogUtils.showErrorMessage("Section Creation Failed", "Failed to create section: " + sectionName);
                // Handle the case where section creation failed
            }
        } else {
            DialogUtils.showErrorMessage("Invalid Section", "Section name is empty or null. Section creation canceled.");
            // Handle the case where the section name is empty or null
        }
        try {
            loadSectionData();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void addNewNature() {
        String natureName = EntryAlert.showEntryAlert("Nature Registration", "Please enter nature to be registered", "Nature : ");
        NatureDAO natureDAO = new NatureDAO();
        if (natureName != null && !natureName.isEmpty()) {
            boolean natureRegistered = natureDAO.createNature(natureName);
            if (natureRegistered) {
                DialogUtils.showConfirmationDialog("Nature Created", "Nature created successfully: " + natureName);
            } else {
                DialogUtils.showErrorMessage("Nature Creation Failed", "Failed to create nature: " + natureName);
            }
        } else {
            DialogUtils.showErrorMessage("Invalid Nature", "Nature name is empty or null. Nature creation canceled.");
        }

        try {
            loadNatureData();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private void addNewIndustry() {
        ToDoAlert.showToDoAlert();
    }

    private void addNewDivision() {
        ToDoAlert.showToDoAlert();
    }

    private void addNewDepartment() {
        ToDoAlert.showToDoAlert();
    }

    private void addNewCategory() {
        String productCategory = EntryAlert.showEntryAlert("Category Registration", "Please enter category to be registered", "Category : ");
        CategoriesDAO categoriesDAO = new CategoriesDAO();
        if (!productCategory.isEmpty()) {
            boolean categoryRegistered = categoriesDAO.createCategory(productCategory);
            if (categoryRegistered) {
                DialogUtils.showConfirmationDialog("Category Created", "Category created successfully: " + productCategory);
                // The category was created successfully, perform additional actions if needed
            } else {
                DialogUtils.showErrorMessage("Category Creation Failed", "Failed to create category: " + productCategory);
                // Handle the case where category creation failed
            }
        } else {
            DialogUtils.showErrorMessage("Invalid Category", "Category name is empty or null. Category creation canceled.");
        }
        try {
            loadCategoryData();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void addNewBrand() {
        String productBrand = EntryAlert.showEntryAlert("Brand Registration", "Please enter brand to be registered", "Brand : ");
        BrandDAO brandDAO = new BrandDAO();
        boolean brandRegistered = brandDAO.createBrand(productBrand);

        if (brandRegistered) {
            DialogUtils.showConfirmationDialog("Brand registration", productBrand + " successfully registered");
        } else {
            DialogUtils.showErrorMessage("Brand registration failed", "Registration of brand " + productBrand + " has failed, please try again later.");
        }

        loadBrandData();
    }

    private void addNewSegment() {
        String productSegment = EntryAlert.showEntryAlert("Segment Registration", "Please enter segment to be registered", "Segment : ");
        SegmentDAO segmentDAO = new SegmentDAO();
        boolean segmentRegistered = segmentDAO.createSegment(productSegment);
        if (segmentRegistered) {
            DialogUtils.showConfirmationDialog("Segment registration", productSegment + " successfully registered");
        } else {
            DialogUtils.showErrorMessage("Segment registration failed", "Registration of segment " + productSegment + " has failed, please try again later.");
        }

        try {
            loadSegmentData();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private void addNewSystemEmployeeTable() {
        User selectedEmployee = (User) defaultTable.getSelectionModel().getSelectedItem();
        if (selectedEmployee != null) {
            confirmationAlert confirmationAlert = new confirmationAlert("Add Employee to System?",
                    "Add " + selectedEmployee.getUser_fname() + " to the system?", "Add employee to system?");

            boolean userConfirmed = confirmationAlert.showAndWait();

            if (userConfirmed) {
                String generatedPassword = RandomStringUtils.randomAlphanumeric(8);
                selectedEmployee.setUser_password(generatedPassword);

                // Update the password in the database
                String updateQuery = "UPDATE user SET user_password = ? WHERE user_id = ?";
                try (Connection connection = dataSource.getConnection();
                     PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                    preparedStatement.setString(1, generatedPassword);
                    preparedStatement.setInt(2, selectedEmployee.getUser_id());
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace(); // Print the stack trace for debugging purposes
                    // You can also show an error message to the user if the update fails
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Generated Password");
                alert.setHeaderText("Password generated for " + selectedEmployee.getUser_fname());
                alert.setContentText("Generated Password: " + generatedPassword);
                alert.showAndWait();
            }
        } else {
            // Handle the case where no employee is selected
            System.out.println("No employee selected.");
        }
    }

    private void addNewProduct() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("registerProduct.fxml"));
            Parent content = loader.load();

            RegisterProductController controller = loader.getController();

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

    private void loadNatureTable() {
        tableHeader.setText("Nature");
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Product Nature.png"));
        tableImg.setImage(image);

        columnHeader1.setText("Nature Name");

        TableColumn<Map<String, String>, String> column1 = new TableColumn<>("Nature Name");
        column1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("nature_name")));

        try {
            loadNatureData(); // Load data into the 'natureData' ObservableList

            defaultTable.setItems(natureData); // Set items from 'natureData' to the table
        } catch (SQLException e) {
            e.printStackTrace();
        }

        defaultTable.getColumns().clear(); // Clear existing columns
        defaultTable.getColumns().add(column1);
    }

    private void loadNatureData() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM nature";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                natureData.clear(); // Clear existing data before loading new data

                while (resultSet.next()) {
                    Map<String, String> natureRow = new HashMap<>();
                    natureRow.put("nature_name", resultSet.getString("nature_name"));
                    natureData.add(natureRow);
                }
            }
        }
    }

    private void loadSectionTable() {
        tableHeader.setText("Section");
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/section.png"));
        tableImg.setImage(image);

        columnHeader1.setText("Section Name");

        TableColumn<Map<String, String>, String> column1 = new TableColumn<>("Section Name");
        column1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("section_name")));

        try {
            loadSectionData(); // Load data into the 'sectionData' ObservableList

            defaultTable.setItems(sectionData); // Set items from 'sectionData' to the table
        } catch (SQLException e) {
            e.printStackTrace();
        }

        defaultTable.getColumns().clear(); // Clear existing columns
        defaultTable.getColumns().add(column1);
    }

    private void loadSectionData() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM sections";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                sectionData.clear(); // Clear existing data before loading new data

                while (resultSet.next()) {
                    Map<String, String> sectionRow = new HashMap<>();
                    sectionRow.put("section_name", resultSet.getString("section_name"));
                    sectionData.add(sectionRow);
                }
            }
        }
    }

    private void loadClassTable() {
        tableHeader.setText("Class");
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Prduct Class.png"));
        tableImg.setImage(image);

        columnHeader1.setText("Class Name");

        TableColumn<Map<String, String>, String> column1 = new TableColumn<>("Class Name");
        column1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("class_name")));

        try {
            loadClassData(); // Load data into the 'classData' ObservableList

            defaultTable.setItems(classData); // Set items from 'classData' to the table
        } catch (SQLException e) {
            e.printStackTrace();
        }

        defaultTable.getColumns().clear(); // Clear existing columns
        defaultTable.getColumns().add(column1);
    }

    private void loadClassData() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM classes";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                classData.clear(); // Clear existing data before loading new data

                while (resultSet.next()) {
                    Map<String, String> classRow = new HashMap<>();
                    classRow.put("class_name", resultSet.getString("class_name"));
                    classData.add(classRow);
                }
            }
        }
    }

    private void loadDiscountSetUpTable() {
        ToDoAlert.showToDoAlert();
        tableHeader.setText("Discount Set Up");
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Discount.png"));
        tableImg.setImage(image);

        columnHeader1.setText("Discount Name");

        TableColumn<Map<String, String>, String> column1 = new TableColumn<>("Discount Name");
        column1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("segment_name")));

        try {
            ObservableList<Map<String, String>> segmentData = FXCollections.observableArrayList();

            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM segment";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                     ResultSet resultSet = preparedStatement.executeQuery()) {

                    while (resultSet.next()) {
                        Map<String, String> segmentRow = new HashMap<>();
                        segmentRow.put("segment_name", resultSet.getString("segment_name"));
                        segmentData.add(segmentRow);
                    }
                }
            }

            defaultTable.setItems(segmentData);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        defaultTable.getColumns().clear(); // Clear existing columns
        defaultTable.getColumns().add(column1);
    }

    private void loadPaymentTerms() {
        tableHeader.setText("Payment Terms");
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Card Payment.png"));
        tableImg.setImage(image);

        columnHeader1.setText("Payment Term Names");

        TableColumn<Map<String, String>, String> column1 = new TableColumn<>("Payment Names");
        column1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("payment_name")));

        try {
            ObservableList<Map<String, String>> paymentData = FXCollections.observableArrayList();

            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM payment_terms";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                     ResultSet resultSet = preparedStatement.executeQuery()) {

                    while (resultSet.next()) {
                        Map<String, String> paymentRow = new HashMap<>();
                        paymentRow.put("payment_name", resultSet.getString("payment_name"));
                        paymentData.add(paymentRow);
                    }
                }
            }

            defaultTable.setItems(paymentData);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        defaultTable.getColumns().clear(); // Clear existing columns
        defaultTable.getColumns().add(column1);

    }

    private void loadDeliveryTerms() {
        tableHeader.setText("Delivery Terms");
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Delivery.png"));
        tableImg.setImage(image);

        TableColumn<Map<String, String>, String> column1 = new TableColumn<>("Delivery Terms");
        column1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("delivery_name")));

        try {
            ObservableList<Map<String, String>> deliveryData = FXCollections.observableArrayList();

            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM delivery_terms";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                     ResultSet resultSet = preparedStatement.executeQuery()) {

                    while (resultSet.next()) {
                        Map<String, String> deliveryRow = new HashMap<>();
                        deliveryRow.put("delivery_terms", resultSet.getString("delivery_terms"));
                        deliveryData.add(deliveryRow);
                    }
                }
            }

            defaultTable.setItems(deliveryData);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        defaultTable.getColumns().clear(); // Clear existing columns
        defaultTable.getColumns().add(column1);

    }

    private void loadSegmentTable() {
        tableHeader.setText("Segment");
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Sorting Category.png"));
        tableImg.setImage(image);

        columnHeader1.setText("Segment Name");

        TableColumn<Map<String, String>, String> column1 = new TableColumn<>("Segment Name");
        column1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("segment_name")));

        try {
            loadSegmentData(); // Load data into the 'segmentData' ObservableList

            defaultTable.setItems(segmentData); // Set items from 'segmentData' to the table
        } catch (SQLException e) {
            e.printStackTrace();
        }

        defaultTable.getColumns().clear(); // Clear existing columns
        defaultTable.getColumns().add(column1);
    }

    private void loadSegmentData() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM segment";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                segmentData.clear(); // Clear existing data before loading new data

                while (resultSet.next()) {
                    Map<String, String> segmentRow = new HashMap<>();
                    segmentRow.put("segment_name", resultSet.getString("segment_name"));
                    segmentData.add(segmentRow);
                }
            }
        }
    }


    private void loadBrandTable() {
        tableHeader.setText("Brand");
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/brand.png"));
        tableImg.setImage(image);

        columnHeader1.setText("Brand Name");

        TableColumn<Map<String, String>, String> column1 = new TableColumn<>("Brand Name");
        column1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("brand_name")));

        brandData = FXCollections.observableArrayList();
        loadBrandData();

        defaultTable.setItems(brandData);
        defaultTable.getColumns().clear(); // Clear existing columns
        defaultTable.getColumns().add(column1);
    }

    public void loadBrandData() {
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM brand";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                brandData.clear(); // Clear existing data before loading new data

                while (resultSet.next()) {
                    Map<String, String> brandRow = new HashMap<>();
                    brandRow.put("brand_name", resultSet.getString("brand_name"));
                    brandData.add(brandRow);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle SQL exceptions here
        }
    }


    private void loadCategoryTable() {
        tableHeader.setText("Category");
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/categorization.png"));
        tableImg.setImage(image);

        columnHeader1.setText("Category Name");

        TableColumn<Map<String, String>, String> column1 = new TableColumn<>("Category Name");
        column1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("category_name")));

        try {
            loadCategoryData(); // Load data into the 'categoryData' ObservableList

            defaultTable.setItems(categoryData); // Set items from 'categoryData' to the table
        } catch (SQLException e) {
            e.printStackTrace();
        }

        defaultTable.getColumns().clear(); // Clear existing columns
        defaultTable.getColumns().add(column1);
    }

    private void loadCategoryData() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM categories";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                categoryData.clear(); // Clear existing data before loading new data

                while (resultSet.next()) {
                    Map<String, String> categoryRow = new HashMap<>();
                    categoryRow.put("category_name", resultSet.getString("category_name"));
                    categoryData.add(categoryRow);
                }
            }
        }
    }

    private void loadDepartmentTable() {
        tableHeader.setText("Department");
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Organization Chart People.png"));
        tableImg.setImage(image);

        defaultTable.getColumns().removeAll(column7, column8);

        columnHeader1.setText("Division");
        columnHeader2.setText("Department Name");
        columnHeader3.setText("Department Head");
        columnHeader4.setText("Department Description");
        columnHeader5.setText("Date Added");
        columnHeader6.setText("Tax ID");

        column1.setCellValueFactory(new PropertyValueFactory<>("parentDivision"));
        column2.setCellValueFactory(new PropertyValueFactory<>("departmentName"));
        column3.setCellValueFactory(new PropertyValueFactory<>("departmentHead"));
        column4.setCellValueFactory(new PropertyValueFactory<>("departmentDescription"));
        column5.setCellValueFactory(new PropertyValueFactory<>("dateAdded"));
        column6.setCellValueFactory(new PropertyValueFactory<>("taxId"));

        String query = "SELECT * FROM department";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            defaultTable.getItems().clear();
            while (resultSet.next()) {
                Department department = new Department(
                        resultSet.getInt("department_id"),
                        resultSet.getString("parent_division"),
                        resultSet.getString("department_name"),
                        resultSet.getString("department_head"),
                        resultSet.getString("department_description"),
                        resultSet.getDate("date_added"),
                        resultSet.getInt("tax_id")

                );
                defaultTable.getItems().add(department);

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadDivisionTable() {
        tableHeader.setText("Division");
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/division.png"));
        tableImg.setImage(image);

        defaultTable.getColumns().removeAll(column5, column6, column7, column8);

        columnHeader1.setText("Division Name");
        columnHeader2.setText("Division Head");
        columnHeader3.setText("Division Description");
        columnHeader4.setText("Date Added");
        column1.setCellValueFactory(new PropertyValueFactory<>("divisionName"));
        column2.setCellValueFactory(new PropertyValueFactory<>("divisionHead"));
        column3.setCellValueFactory(new PropertyValueFactory<>("divisionDescription"));
        column4.setCellValueFactory(new PropertyValueFactory<>("dateAdded"));

        String query = "SELECT * FROM division";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            defaultTable.getItems().clear();
            while (resultSet.next()) {
                Division division = new Division(
                        resultSet.getInt("division_id"),
                        resultSet.getString("division_name"),
                        resultSet.getString("division_head"),
                        resultSet.getString("division_description"),
                        resultSet.getString("division_code"),
                        resultSet.getDate("date_added")
                );
                defaultTable.getItems().add(division);

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadIndustryTable() {
        tableHeader.setText("Industries");
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Manufacturing.png"));
        tableImg.setImage(image);

        columnHeader1.setText("Industry Name");
        columnHeader2.setText("Industry Head");
        columnHeader3.setText("Industry Description");
        columnHeader4.setText("Date Added");
        columnHeader5.setText("Tax ID");

        defaultTable.getColumns().removeAll(column3, column6, column7, column8);

        column1.setCellValueFactory(new PropertyValueFactory<>("industryName"));
        column2.setCellValueFactory(new PropertyValueFactory<>("industryHead"));
        column3.setCellValueFactory(new PropertyValueFactory<>("industryDescription"));
        column4.setCellValueFactory(new PropertyValueFactory<>("dateAdded"));
        column5.setCellValueFactory(new PropertyValueFactory<>("taxId"));

        String query = "SELECT * FROM industry"; // Exclude users without passwords
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            // Clear existing items in the table
            defaultTable.getItems().clear();
            while (resultSet.next()) {
                Industry industry = new Industry(
                        resultSet.getInt("id"),
                        resultSet.getString("industry_name"),
                        resultSet.getString("industry_head"),
                        resultSet.getString("industry_description"),
                        resultSet.getDate("date_added"),
                        resultSet.getInt("tax_id")
                );
                defaultTable.getItems().add(industry);

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
                        defaultTable.setOnMouseClicked(event -> {
                            if (event.getClickCount() == 2) {
                                addNewSystemEmployeeTable();
                            }
                        });
                    } else {
                        setStyle(""); // Set default style for rows with non-empty password
                        defaultTable.setOnMouseClicked(event -> {
                            if (event.getClickCount() == 2) { // Check for double-click
                                handleTableDoubleClick(defaultTable.getSelectionModel().getSelectedItem());
                            }
                        });
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

    private void loadSupplierTable() {
        tableHeader.setText("Suppliers");
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Supplier Info.png"));
        tableImg.setImage(image);
        columnHeader1.setText("Supplier Name");
        columnHeader2.setText("Logo");
        columnHeader3.setText("Contact Person");
        columnHeader4.setText("Email Address");
        columnHeader5.setText("Phone Number");
        columnHeader6.setText("Address");
        columnHeader7.setText("City");
        columnHeader8.setText("Baranggay");

        column1.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        column2.setCellValueFactory(new PropertyValueFactory<>("supplierImage"));
        column3.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));
        column4.setCellValueFactory(new PropertyValueFactory<>("emailAddress"));
        column5.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        column6.setCellValueFactory(new PropertyValueFactory<>("address"));
        column7.setCellValueFactory(new PropertyValueFactory<>("city"));
        column8.setCellValueFactory(new PropertyValueFactory<>("brgy"));

        column2.setCellFactory(param -> new TableCell<Supplier, byte[]>() {
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


        columnHeader1.setText("Product Name");
        columnHeader2.setText("Product Code");
        columnHeader3.setText("Description");
        columnHeader4.setText("Supplier Name");
        columnHeader5.setText("Brand");
        columnHeader6.setText("Category");
        columnHeader7.setText("Segment");
        columnHeader8.setText("Section");


        SupplierDAO supplierDAO = new SupplierDAO();
        BrandDAO brandDAO = new BrandDAO();
        CategoriesDAO categoriesDAO = new CategoriesDAO();
        SegmentDAO segmentDAO = new SegmentDAO();
        SectionsDAO sectionsDAO = new SectionsDAO();


        column1.setCellValueFactory(new PropertyValueFactory<>("product_name"));
        column2.setCellValueFactory(new PropertyValueFactory<>("product_code"));
        column3.setCellValueFactory(new PropertyValueFactory<>("description"));
        column4.setCellValueFactory(new PropertyValueFactory<>("supplierFromId"));
        column5.setCellValueFactory(new PropertyValueFactory<>("brandFromId"));
        column6.setCellValueFactory(new PropertyValueFactory<>("categoryFromId"));
        column7.setCellValueFactory(new PropertyValueFactory<>("segmentFromId"));
        column8.setCellValueFactory(new PropertyValueFactory<>("sectionFromId"));

        String query = "SELECT * FROM products";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            defaultTable.getItems().clear();

            while (resultSet.next()) {
                Product product = new Product();


                int supplierId = resultSet.getInt("supplier_name");
                String supplierName = supplierDAO.getSupplierNameById(supplierId);
                int brandId = resultSet.getInt("product_brand");
                String brandName = brandDAO.getBrandNameById(brandId);
                int categoryId = resultSet.getInt("product_category");
                String categoryName = categoriesDAO.getCategoryNameById(categoryId);
                int segmentId = resultSet.getInt("product_segment");
                String segmentName = segmentDAO.getSegmentNameById(segmentId);
                int sectionId = resultSet.getInt("product_section");
                String sectionName = sectionsDAO.getSectionNameById(sectionId);

                product.setProduct_name(resultSet.getString("product_name"));
                product.setProduct_code(resultSet.getString("product_code"));
                product.setDescription(resultSet.getString("description"));
                product.setSupplierFromId(supplierName);
                product.setBrandFromId(brandName);
                product.setCategoryFromId(categoryName);
                product.setSegmentFromId(segmentName);
                product.setSectionFromId(sectionName);
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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("registerProduct.fxml"));
                Parent root = loader.load();

                // Pass the selected employee data to the controller of employeeDetails.fxml
                RegisterProductController controller = loader.getController();
                controller.initData(selectedProduct);

                Stage stage = new Stage();
                stage.setTitle("Product Details");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (selectedItem instanceof Supplier selectedSupplier) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("supplierInfoRegistration.fxml"));
                Parent root = loader.load();

                // Pass the selected supplier data to the controller of supplierDetails.fxml
                SupplierInfoRegistrationController controller = loader.getController();
                controller.initData(selectedSupplier);

                // Create a new stage (window) for supplier details
                Stage stage = new Stage();
                stage.setTitle("Supplier Details");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace(); // Handle the exception according to your needs
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

        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Franchise.png"));

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
