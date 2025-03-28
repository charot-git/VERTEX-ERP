package com.vertex.vos;

import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
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
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.IllegalFormatCodePointException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class RegisterProductController implements Initializable {

    public DatePicker dateAdded;
    public Button deleteButton;
    Stage stage;
    @FXML
    private ImageView HeaderLogo;
    @FXML
    private ImageView productPic;
    @FXML
    private Label HeaderText;
    @FXML
    private Label baseUnitErr;
    @FXML
    private Label baseUnitLabel;
    @FXML
    private Label brandErr;
    @FXML
    private Label businessTypeLabel;
    @FXML
    private Label categoryErr;
    @FXML
    private Label classErr;
    @FXML
    private Label companyNameLabel;
    @FXML
    private Label companyNameLabel1;
    @FXML
    private Label companyNameLabel111;
    @FXML
    private Label companyNameLabel112;
    @FXML
    private Label companyNameLabel113;
    @FXML
    private Label configLabel;
    @FXML
    private Label confirmationLabel;
    @FXML
    private Label copErr1;
    @FXML
    private Label dateAddedErr;
    @FXML
    private Label dateOfFormationLabel;
    @FXML
    private Label eeucErr;
    @FXML
    private Label eucErr;
    @FXML
    private Label maintainingBaseQtyErr;
    @FXML
    private Label natureErr;
    @FXML
    private Label ppuErr;
    @FXML
    private Label priceAErr;
    @FXML
    private Label priceBErr;
    @FXML
    private Label priceCErr;
    @FXML
    private Label priceDErr;
    @FXML
    private Label priceEErr;
    @FXML
    private Label productCodeErr;
    @FXML
    private Label productDescriptionErr;
    @FXML
    private Label productNameErr;
    @FXML
    private Label sectionErr;
    @FXML
    private Label segmentErr;
    @FXML
    private Label shortDescriptionErr;
    @FXML
    private Label productBarcodeErr;
    @FXML
    private Label unitCountErr;
    @FXML
    private VBox addConfiguration;
    @FXML
    private VBox baseWeightBox;
    @FXML
    private TableColumn<Product, String> barcodeColumn;
    @FXML
    private TableColumn<Product, Double> copColumn;
    @FXML
    private TableColumn<Product, String> descriptionColumn;
    @FXML
    private TableColumn<Product, String> descriptionColumnPricing;
    @FXML
    private TableColumn<Product, Double> eecColumn;
    @FXML
    private TableColumn<Product, Double> eucColumn;
    @FXML
    private TableColumn<Product, Double> ppuColumn;
    @FXML
    private TableColumn<Product, Double> priceAColumn;
    @FXML
    private TableColumn<Product, Double> priceBColumn;
    @FXML
    private TableColumn<Product, Double> priceCColumn;
    @FXML
    private TableColumn<Product, Double> priceDColumn;
    @FXML
    private TableColumn<Product, Double> priceEColumn;
    @FXML
    private TableColumn<Product, String> shortDescriptionColumn;
    @FXML
    private TableColumn<Product, Integer> unitCountColumn;
    @FXML
    private TableColumn<Product, String> unitOfMeasurementColumn;

    // ComboBox
    @FXML
    private ComboBox<String> baseUnitComboBox;
    @FXML
    private ComboBox<String> brandComboBox;
    @FXML
    private ComboBox<String> categoryComboBox;
    @FXML
    private ComboBox<String> classComboBox;
    @FXML
    private ComboBox<String> sectionComboBox;
    @FXML
    private ComboBox<String> segmentComboBox;

    private final ObservableList<Product> productConfigurationList = FXCollections.observableArrayList();
    // HBox
    @FXML
    private HBox changePicButton;

    // TextField
    @FXML
    private TextField baseWeightTextField;
    @FXML
    private TextField copTextField1;
    @FXML
    private TextField eeucTextField;
    @FXML
    private TextField eucTextField;
    @FXML
    private TextField maintainingBaseQtyTextField;
    @FXML
    private TextField ppuTextField;
    @FXML
    private TextField priceATextField;
    @FXML
    private TextField priceBTextField;
    @FXML
    private TextField priceCTextField;
    @FXML
    private TextField priceDTextField;
    @FXML
    private TextField priceETextField;
    @FXML
    private TextField productCodeTextField;
    @FXML
    private TextField productDescriptionTextField;
    @FXML
    private TextField productNameTextField;
    @FXML
    private TextField productShelfLifeTextField;
    @FXML
    private TextField productBarcodeTextField;
    @FXML
    private TextField shortDescriptionTextField;
    @FXML
    private TextField unitCountTextField;

    // TableView
    @FXML
    private TableView<Product> productConfigurationTable;
    @FXML
    private TableView<Product> productPricing;

    // TabPane/Tab
    @FXML
    private Tab priceControlTab;
    @FXML
    private Tab productConfigTab;
    @FXML
    private Tab productPricingTab;
    @FXML
    private TabPane productTabPane;

    @FXML
    private VBox registrationVBox;
    @FXML
    private VBox generateBarcode;

    @FXML
    private Button confirmButton;
    @FXML
    private Button confirmButtonPriceControl;

    ProductDAO productDAO = new ProductDAO();

    // HikariDataSource
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    @FXML
    private CheckBox active;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configLabel.setText("Add Product Details");

        populateComboBox();

        TextFieldUtils.addDoubleInputRestriction(baseWeightTextField);
        TextFieldUtils.addNumericInputRestriction(unitCountTextField);
        TextFieldUtils.addNumericInputRestriction(maintainingBaseQtyTextField);
        TextFieldUtils.addNumericInputRestriction(productShelfLifeTextField);
        TextFieldUtils.addDoubleInputRestriction(copTextField1);
        TextFieldUtils.addDoubleInputRestriction(ppuTextField);
        TextFieldUtils.addDoubleInputRestriction(eucTextField);
        TextFieldUtils.addDoubleInputRestriction(eeucTextField);
        TextFieldUtils.addDoubleInputRestriction(priceATextField);
        TextFieldUtils.addDoubleInputRestriction(priceBTextField);
        TextFieldUtils.addDoubleInputRestriction(priceCTextField);
        TextFieldUtils.addDoubleInputRestriction(priceDTextField);
        TextFieldUtils.addDoubleInputRestriction(priceETextField);
        registrationVBox.getChildren().remove(productTabPane);
        dateAdded.setValue(LocalDate.now());

        registerDoubleClickEventForTable(productConfigurationTable);
        registerDoubleClickEventForTable(productPricing);

        confirmButton.setOnMouseClicked(mouseEvent -> registerProductDetails());
    }

    private String getNewBarcode() {
        int barcode = Integer.parseInt(productDAO.getNextBarcodeNumber());
        productBarcodeTextField.setText(String.valueOf(barcode));
        return String.valueOf(barcode);
    }


    private void populateComboBox() {
        TextFieldUtils.setComboBoxBehavior(baseUnitComboBox);
        TextFieldUtils.setComboBoxBehavior(brandComboBox);
        TextFieldUtils.setComboBoxBehavior(categoryComboBox);
        TextFieldUtils.setComboBoxBehavior(classComboBox);
        TextFieldUtils.setComboBoxBehavior(sectionComboBox);
        TextFieldUtils.setComboBoxBehavior(segmentComboBox);

        BrandDAO brandDAO = new BrandDAO();
        ObservableList<Brand> brandList = brandDAO.getBrandDetails();
        ObservableList<String> brandNames = FXCollections.observableArrayList();
        for (Brand brand : brandList) {
            int brand_id;
            brand_id = brand.getBrand_id();
            String brand_name = brand.getBrand_name();
            brandNames.add(brand_name);
        }
        brandComboBox.setItems(brandNames);
        ComboBoxFilterUtil.setupComboBoxFilter(brandComboBox, brandNames);

        //segments
        SegmentDAO segmentDAO = new SegmentDAO();
        ObservableList<Segment> segmentObservableList = segmentDAO.getSegmentDetails();
        ObservableList<String> segmentNames = FXCollections.observableArrayList();
        for (Segment segment : segmentObservableList) {
            int segment_id = segment.getSegment_id();
            String segment_name = segment.getSegment_name();
            segmentNames.add(segment_name);
        }
        segmentComboBox.setItems(segmentNames);
        ComboBoxFilterUtil.setupComboBoxFilter(segmentComboBox, segmentNames);

        //categories
        CategoriesDAO categoriesDAO = new CategoriesDAO();
        ObservableList<Category> categoryObservableList = categoriesDAO.getCategoryDetails();
        ObservableList<String> categoryNames = FXCollections.observableArrayList();
        for (Category category : categoryObservableList) {
            int category_id = category.getCategoryId();
            String category_name = category.getCategoryName();
            categoryNames.add(category_name);
        }
        categoryComboBox.setItems(categoryNames);
        ComboBoxFilterUtil.setupComboBoxFilter(categoryComboBox, categoryNames);

        //sections
        SectionsDAO sectionsDAO = new SectionsDAO();
        ObservableList<Section> sectionObservableList = sectionsDAO.getSectionDetails();
        ObservableList<String> sectionNames = FXCollections.observableArrayList();
        for (Section section : sectionObservableList) {
            int section_id = section.getSection_id();
            String section_name = section.getSection_name();
            sectionNames.add(section_name);
        }
        sectionComboBox.setItems(sectionNames);
        ComboBoxFilterUtil.setupComboBoxFilter(sectionComboBox, sectionNames);


        //class
        ProductClassDAO productClassDAO = new ProductClassDAO();
        ObservableList<ProductClass> productClassList = productClassDAO.getProductClassDetails();
        ObservableList<String> productClassNames = FXCollections.observableArrayList();

        for (ProductClass productClass : productClassList) {
            int classId = productClass.getClassId();
            String className = productClass.getClassName();
            productClassNames.add(className);
        }

        classComboBox.setItems(productClassNames);
        ComboBoxFilterUtil.setupComboBoxFilter(classComboBox, productClassNames);


        //base unit
        UnitDAO unitDAO = new UnitDAO();
        ObservableList<Unit> unitObservableList = unitDAO.getUnitDetails();
        ObservableList<String> unitNames = FXCollections.observableArrayList();

        for (Unit unit : unitObservableList) {
            int unit_id = unit.getUnit_id();
            String unit_name = unit.getUnit_name();
            unitNames.add(unit_name);
        }
        baseUnitComboBox.setItems(unitNames);
        ComboBoxFilterUtil.setupComboBoxFilter(baseUnitComboBox, unitNames);
    }

    private void registerProductDetails() {
        String validateFields = validateFields();
        boolean userConfirmed = false;
        if (!validateFields.isEmpty()) {
            DialogUtils.showErrorMessageForValidation("Error", "Please correct the following fields", validateFields);
        } else {
            userConfirmed = userConfirmationDetails();
        }
        if (userConfirmed) {
            int productId = productParentRegistered();
            if (productId != -1) {
                registrationVBox.getChildren().add(productTabPane);
                VBox confirmationBox = (VBox) confirmButton.getParent();
                registrationVBox.getChildren().remove(confirmationBox);
                productTabPane.getTabs().removeAll(priceControlTab, productPricingTab);
                addConfiguration.setOnMouseClicked(mouseEvent -> addNewConfigSetup(productId));
            } else {
                DialogUtils.showErrorMessage("Error", "Please contact your system administrator");
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.show();
        }
    }

    private void addNewConfigSetup(int productId) {
        if (UserSession.getInstance().getUserPosition().equals("Intern")) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("initialProductRegistration.fxml"));
                Parent root = loader.load();
                InitialProductRegistrationController controller = loader.getController();
                controller.initializeProductforNonBarcode(productId);
                Stage stage = new Stage();
                stage.setTitle("Add breakdown for " + productNameTextField.getText());
                stage.setMaximized(true);
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("registerProduct.fxml"));
                Parent content = loader.load();

                RegisterProductController controller = loader.getController();
                controller.initializeConfigurationRegistration(productId);
                controller.setRegisterProductController(this);

                Stage stage = new Stage();
                stage.setTitle("Register Product Configuration");
                stage.setMaximized(true);
                stage.setScene(new Scene(content));
                stage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace(); // Handle the exception according to your needs
                System.err.println("Error loading companyRegistration.fxml: " + e.getMessage());
            }
        }
    }

    @Setter
    RegisterProductController registerProductController;


    private void initializeConfigurationRegistration(int productId) {
        BrandDAO brandDAO = new BrandDAO();
        CategoriesDAO categoriesDAO = new CategoriesDAO();
        SegmentDAO segmentDAO = new SegmentDAO();
        SectionsDAO sectionsDAO = new SectionsDAO();
        UnitDAO unitDAO = new UnitDAO();
        ProductClassDAO productClassDAO = new ProductClassDAO();

        productNameTextField.setDisable(true);
        baseUnitComboBox.setDisable(false);
        unitCountTextField.setDisable(false);

        Product productGetter = productDAO.getProductById(productId);

        String productName = productGetter.getProductName();

        Product selectedProduct = productDAO.getProductDetails(productId);


        generateBarcode.setOnMouseClicked(mouseEvent -> checkIfBarcoded());

        HeaderText.setText("Configuration of : " + selectedProduct.getProductName());
        productNameTextField.setText(productName);
        brandComboBox.setValue(brandDAO.getBrandNameById(selectedProduct.getProductBrand()));
        categoryComboBox.setValue(categoriesDAO.getCategoryNameById(selectedProduct.getProductCategory()));
        segmentComboBox.setValue(segmentDAO.getSegmentNameById(selectedProduct.getProductSegment()));
        sectionComboBox.setValue(sectionsDAO.getSectionNameById(selectedProduct.getProductSection()));
        classComboBox.setValue(productClassDAO.getProductClassNameById(selectedProduct.getProductClass()));
        productDescriptionTextField.setText(selectedProduct.getProductName());
        confirmButton.setOnMouseClicked(mouseEvent -> userConfirmationConfig(productName));
    }

    private void userConfirmationConfig(String productName) {
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Register Product Configuration? ", "Add product configuration for : " + productNameTextField.getText(), "Please verify", false);
        String validateFields = validateFields();
        boolean userConfirmed = false;
        if (!validateFields.isEmpty()) {
            DialogUtils.showErrorMessageForValidation("Error", "Please correct the following fields", validateFields);
        } else {
            userConfirmed = confirmationAlert.showAndWait();
        }
        ProductDAO productDAO = new ProductDAO();

        int productId = productDAO.getProductIdByName(productName);

        int configId;

        if (userConfirmed) {
            configId = addNewConfig(productId);
            if (configId != -1) {
                DialogUtils.showCompletionDialog("Registration Successful", "New configuration for " + productName + " has been registered");
                Product productConfig = productDAO.getProductDetails(configId);
                if (productConfig != null) {
                    productConfigurationList.add(productConfig);
                    stage = (Stage) HeaderText.getScene().getWindow();
                    stage.close();
                    registerProductController.initializeTableView(productId);

                } else {
                    DialogUtils.showErrorMessage("Fetching Product Details Failed", "Failed to fetch the newly registered product details.");
                }
            } else {
                DialogUtils.showErrorMessage("Registration Failed", "An error occurred.");
            }
        } else {
            DialogUtils.showErrorMessage("Registration Failed", "An error occurred.");
        }
    }

    private int addNewConfig(int productId) {
        ProductDAO productDAO = new ProductDAO();
        BrandDAO brandDAO = new BrandDAO();
        CategoriesDAO categoriesDAO = new CategoriesDAO();
        SegmentDAO segmentDAO = new SegmentDAO();
        SectionsDAO sectionsDAO = new SectionsDAO();
        UnitDAO unitDAO = new UnitDAO();
        ProductClassDAO productClassDAO = new ProductClassDAO();

        Product ConfigProduct = new Product();
        ConfigProduct.setIsActive(1);
        ConfigProduct.setParentId(productId);
        ConfigProduct.setProductName(productNameTextField.getText());
        // Setting other properties (Replace these with actual values from your UI)
        ConfigProduct.setBarcode(productBarcodeTextField.getText());
        ConfigProduct.setProductCode(productCodeTextField.getText());
        ConfigProduct.setProductImage("");
        ConfigProduct.setDescription(productDescriptionTextField.getText());
        ConfigProduct.setShortDescription(shortDescriptionTextField.getText());
        ConfigProduct.setDateAdded(Date.valueOf(dateAdded.getValue()));
        ConfigProduct.setLastUpdated(new Timestamp(System.currentTimeMillis()));
        ConfigProduct.setProductBrand(brandDAO.getBrandIdByName(brandComboBox.getSelectionModel().getSelectedItem()));
        ConfigProduct.setProductCategory(categoriesDAO.getCategoryIdByName(categoryComboBox.getSelectionModel().getSelectedItem()));
        ConfigProduct.setProductClass(productClassDAO.getProductClassIdByName(classComboBox.getSelectionModel().getSelectedItem()));
        ConfigProduct.setProductSegment(segmentDAO.getSegmentIdByName(segmentComboBox.getSelectionModel().getSelectedItem()));
        ConfigProduct.setProductSection(sectionsDAO.getSectionIdByName(sectionComboBox.getSelectionModel().getSelectedItem()));
        ConfigProduct.setProductShelfLife(Integer.parseInt(productShelfLifeTextField.getText()));
        ConfigProduct.setProductWeight(Double.parseDouble(baseWeightTextField.getText()));
        ConfigProduct.setMaintainingQuantity(Integer.parseInt(maintainingBaseQtyTextField.getText()));
        ConfigProduct.setQuantity(0);
        ConfigProduct.setUnitOfMeasurement(unitDAO.getUnitIdByName(baseUnitComboBox.getSelectionModel().getSelectedItem()));
        ConfigProduct.setUnitOfMeasurementCount(Integer.parseInt(unitCountTextField.getText()));
        return productDAO.addProduct(ConfigProduct);
    }

    private boolean userConfirmationDetails() {
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Register Product Details? ", "Add product " + productNameTextField.getText(), "Please verify", false);
        return confirmationAlert.showAndWait();
    }

    private int productParentRegistered() {
        Product product = new Product();
        BrandDAO brandDAO = new BrandDAO();
        CategoriesDAO categoriesDAO = new CategoriesDAO();
        SegmentDAO segmentDAO = new SegmentDAO();
        SectionsDAO sectionsDAO = new SectionsDAO();
        UnitDAO unitDAO = new UnitDAO();
        ProductClassDAO productClassDAO = new ProductClassDAO();
        ProductDAO productDAO = new ProductDAO();

        // Set default value for combo boxes if no value is selected
        String selectedBrand = brandComboBox.getSelectionModel().getSelectedItem();
        int brandId = selectedBrand != null ? brandDAO.getBrandIdByName(selectedBrand) : -1;

        String selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
        int categoryId = selectedCategory != null ? categoriesDAO.getCategoryIdByName(selectedCategory) : -1;

        String selectedClass = classComboBox.getSelectionModel().getSelectedItem();
        int classId = selectedClass != null ? productClassDAO.getProductClassIdByName(selectedClass) : -1;

        String selectedSegment = segmentComboBox.getSelectionModel().getSelectedItem();
        int segmentId = selectedSegment != null ? segmentDAO.getSegmentIdByName(selectedSegment) : -1;
        String selectedSection = sectionComboBox.getSelectionModel().getSelectedItem();
        int sectionId = selectedSection != null ? sectionsDAO.getSectionIdByName(selectedSection) : -1;

        // Set other properties
        product.setIsActive(1);
        product.setParentId(0);
        product.setProductName(productNameTextField.getText());
        product.setBarcode(productBarcodeTextField.getText());
        product.setProductCode(productCodeTextField.getText());
        product.setProductImage("");
        product.setDescription(productDescriptionTextField.getText());
        product.setShortDescription(shortDescriptionTextField.getText());
        product.setDateAdded(Date.valueOf(dateAdded.getValue()));
        product.setLastUpdated(new Timestamp(System.currentTimeMillis()));
        product.setProductBrand(brandId);
        product.setProductCategory(categoryId);
        product.setProductClass(classId);
        product.setProductSegment(segmentId);
        product.setProductSection(sectionId);
        product.setProductShelfLife(Integer.parseInt(productShelfLifeTextField.getText()));
        product.setProductWeight(Double.parseDouble(baseWeightTextField.getText()));
        product.setMaintainingQuantity(Integer.parseInt(maintainingBaseQtyTextField.getText()));
        product.setQuantity(0);
        product.setUnitOfMeasurement(unitDAO.getUnitIdByName(baseUnitComboBox.getSelectionModel().getSelectedItem()));
        product.setUnitOfMeasurementCount(Integer.parseInt(unitCountTextField.getText()));

        return productDAO.addProduct(product);
    }

    private String validateFields() {
        StringBuilder errorMessage = new StringBuilder();

        // Validate product name
        String productName = productNameTextField.getText().trim();
        if (productName.isEmpty()) {
            errorMessage.append("Product name is required.\n");
        }

        String description = productDescriptionTextField.getText().trim();
        if (description.isEmpty()) {
            errorMessage.append("Product description is required.\n");
        }
        String dateAddedString = dateAdded.getValue().toString();
        if (dateAddedString.isEmpty()) {
            errorMessage.append("Date added is required.\n");
        } else {
            try {
                Date.valueOf(dateAddedString); // Check if the date format is valid
            } catch (IllegalArgumentException e) {
                errorMessage.append("Invalid date format for date added. Use yyyy-mm-dd.\n");
            }
        }

        String shelfLife = productShelfLifeTextField.getText().trim();
        if (shelfLife.isEmpty()) {
            errorMessage.append("Product shelf life is required.\n");
        } else {
            try {
                Integer.parseInt(shelfLife);
            } catch (NumberFormatException e) {
                errorMessage.append("Product shelf life must be a valid integer.\n");
            }
        }

        String weight = baseWeightTextField.getText().trim();
        if (weight.isEmpty()) {
            errorMessage.append("Product weight is required.\n");
        } else {
            try {
                Double.parseDouble(weight);
            } catch (NumberFormatException e) {
                errorMessage.append("Product weight must be a valid number.\n");
            }
        }

        String maintainingQuantity = maintainingBaseQtyTextField.getText().trim();
        if (maintainingQuantity.isEmpty()) {
            errorMessage.append("Maintaining quantity is required.\n");
        } else {
            try {
                Integer.parseInt(maintainingQuantity);
            } catch (NumberFormatException e) {
                errorMessage.append("Maintaining quantity must be a valid integer.\n");
            }
        }

        String unitCount = unitCountTextField.getText().trim();
        if (unitCount.isEmpty()) {
            errorMessage.append("Unit count is required.\n");
        } else {
            try {
                Integer.parseInt(unitCount);
            } catch (NumberFormatException e) {
                errorMessage.append("Unit count must be a valid integer.\n");
            }
        }

        return errorMessage.toString();
    }


    public void initData(int productId) {
        ProductDAO productDAO = new ProductDAO();
        UnitDAO unitDAO = new UnitDAO();
        BrandDAO brandDAO = new BrandDAO();
        CategoriesDAO categoriesDAO = new CategoriesDAO();
        SegmentDAO segmentDAO = new SegmentDAO();
        SectionsDAO sectionsDAO = new SectionsDAO();
        ProductClassDAO productClassDAO = new ProductClassDAO();

        Product product = productDAO.getProductDetails(productId);

        if (product.getParentId() > 0) {
            productTabPane.getTabs().removeAll(productConfigTab, productPricingTab);
        }

        HeaderText.setText(product.getProductName());
        confirmButton.setText("Update " + product.getDescription());
        productNameTextField.setText(product.getProductName());
        productCodeTextField.setText(product.getProductCode());
        productBarcodeTextField.setText(product.getBarcode());
        Date dateAddedDate = product.getDateAdded();
        if (dateAddedDate != null) {
            dateAdded.setValue(dateAddedDate.toLocalDate());
        } else {
            dateAdded.setPromptText("N/A");
        }
        active.setSelected(product.getIsActive() == 1);
        baseWeightTextField.setText(String.valueOf(product.getProductWeight()));
        unitCountTextField.setText(String.valueOf(product.getUnitOfMeasurementCount()));
        maintainingBaseQtyTextField.setText(String.valueOf(product.getMaintainingQuantity()));
        productDescriptionTextField.setText(product.getDescription());
        shortDescriptionTextField.setText(product.getShortDescription());
        productShelfLifeTextField.setText(String.valueOf(product.getProductShelfLife()));
        baseUnitComboBox.setValue(unitDAO.getUnitNameById(product.getUnitOfMeasurement()));
        brandComboBox.setValue(brandDAO.getBrandNameById(product.getProductBrand()));
        categoryComboBox.setValue(categoriesDAO.getCategoryNameById(product.getProductCategory()));
        segmentComboBox.setValue(segmentDAO.getSegmentNameById(product.getProductSegment()));
        sectionComboBox.setValue(sectionsDAO.getSectionNameById(product.getProductSection()));
        classComboBox.setValue(productClassDAO.getProductClassNameById(product.getProductClass()));

        loadImage(product);

        copTextField1.setText(String.valueOf(product.getCostPerUnit()));
        ppuTextField.setText(String.valueOf(product.getPricePerUnit()));
        priceATextField.setText(String.valueOf(product.getPriceA()));
        priceBTextField.setText(String.valueOf(product.getPriceB()));
        priceCTextField.setText(String.valueOf(product.getPriceC()));
        priceDTextField.setText(String.valueOf(product.getPriceD()));
        priceETextField.setText(String.valueOf(product.getPriceE()));

        addConfiguration.setOnMouseClicked(mouseEvent -> addNewConfigSetup(product.getProductId()));

        if (UserSession.getInstance().getUserPosition().equals("System Developer")) {
            registrationVBox.getChildren().add(productTabPane);
        } else if (UserSession.getInstance().getUserPosition().equals("Controller")) {
            registrationVBox.getChildren().add(productTabPane);
            productTabPane.getTabs().removeAll(productConfigTab);
        } else if (UserSession.getInstance().getUserPosition().equals("Administrator")) {
            registrationVBox.getChildren().add(productTabPane);
            productTabPane.getTabs().removeAll(priceControlTab, productPricingTab);
        } else if (UserSession.getInstance().getUserPosition().equals("Intern")) {
            registrationVBox.getChildren().add(productTabPane);
            productTabPane.getTabs().removeAll(priceControlTab, productPricingTab);
        } else if (UserSession.getInstance().getUserPosition().equals("Store Head")) {
            registrationVBox.getChildren().add(productTabPane);
            productTabPane.getTabs().removeAll(productConfigTab);
        }
        initializeTableView(product.getProductId());

        confirmButtonPriceControl.setOnMouseClicked(mouseEvent -> updateProductPricing(productId));

        confirmButton.setOnMouseClicked(mouseEvent -> initiateUpdateDetails(product));

        changePicButton.setOnMouseClicked(mouseEvent -> updateProductPicture(product));

        deleteButton.setOnAction(actionEvent -> deleteProduct(product));
    }

    private void deleteProduct(Product product) {
        TextInputDialog dialog = new TextInputDialog(product.getProductName());
        dialog.setTitle("Delete Product");
        dialog.setHeaderText("Please enter the product code to confirm deletion:");
        dialog.setContentText("Product Code:");
        Optional<String> result = dialog.showAndWait();

        if (result.isPresent() && result.get().equals(product.getProductCode())) {
            ConfirmationAlert confirmationAlert = new ConfirmationAlert("Delete Product", "Are you sure you want to delete this product?", product.getProductName(), true);
            if (confirmationAlert.showAndWait()) {
                boolean isProductDeleted = productDAO.deleteProduct(product);
                Alert alert;
                if (isProductDeleted) {
                    alert = new Alert(Alert.AlertType.INFORMATION, "Product deleted successfully.");
                    productDetailsStage = null;

                    // Ensure table is refreshed properly
                    Platform.runLater(() -> {
                        registerProductController.initializeTableView(product.getParentId());
                    });

                    Stage currentStage = (Stage) confirmButton.getScene().getWindow();
                    currentStage.close();
                } else {
                    alert = new Alert(Alert.AlertType.ERROR, "Failed to delete product.");
                }
                alert.setTitle(isProductDeleted ? "Success" : "Error");
                alert.setHeaderText(null);
                alert.showAndWait();
            } else {
                DialogUtils.showConfirmationDialog("Cancelled", "Product deletion cancelled.");
            }
        } else {
            DialogUtils.showConfirmationDialog("Cancelled", "Product deletion cancelled. Product code does not match.");
        }
    }


    private void loadImage(Product product) {
        String imageUrl = product.getProductImage();
        if (imageUrl != null) {
            Image image = new Image(new File(imageUrl).toURI().toString());
            productPic.setImage(image);
        }
    }

    private void getBarcodeImage(Product product) {
        String barcodeText = product.getBarcode();
        if (barcodeText == null || barcodeText.isEmpty()) {
            barcodeText = getNewBarcode();
            if (!barcodeText.isEmpty()) {
                product.setBarcode(barcodeText);
            } else {
                System.out.println("Error: Failed to generate a new barcode.");
                return; // Exit the method if failed to generate a barcode
            }
        }

        WritableImage barcodeImage = BarcodePrinter.generateBarcodeEAN(barcodeText);

        if (barcodeImage != null) {
            Stage barcodeStage = new Stage();
            barcodeStage.setTitle("Product Barcode");

            // Create a VBox with padding
            VBox barcodeVBox = new VBox();
            ImageView barcodeImageView = new ImageView(barcodeImage);

            Button copyButton = getButton(barcodeImage);

            // Add the ImageView and button to the VBox
            barcodeVBox.getChildren().addAll(barcodeImageView, copyButton);
            barcodeVBox.setAlignment(Pos.CENTER);

            barcodeVBox.setSpacing(5);
            barcodeStage.setScene(new Scene(barcodeVBox));
            barcodeStage.setResizable(false);
            barcodeStage.show();
        } else {
            DialogUtils.showErrorMessage("Error", "Failed to generate barcode image.");
        }

    }

    private static Button getButton(WritableImage barcodeImage) {
        Button copyButton = new Button("Copy Barcode Image");
        copyButton.setOnAction(copyEvent -> {
            // Convert WritableImage to byte array (assuming PNG format)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                PixelReader pixelReader = barcodeImage.getPixelReader();
                int width = (int) barcodeImage.getWidth();
                int height = (int) barcodeImage.getHeight();
                BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int argb = pixelReader.getArgb(x, y);
                        bufferedImage.setRGB(x, y, argb);
                    }
                }
                ImageIO.write(bufferedImage, "png", baos);
                baos.close();
            } catch (IOException e) {
                System.err.println("Error converting image to byte array: " + e.getMessage());
            }

            // Create a ByteArrayInputStream from the byte array
            byte[] imageBytes = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);

            // Create an Image object from the input stream
            Image clipboardImage = new Image(bais);

            // Create a Clipboard object and set the image content
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putImage(clipboardImage);
            clipboard.setContent(content);

            System.out.println("Barcode image copied to clipboard!");
        });
        return copyButton;
    }

    private void updateProductPicture(Product product) {
        FileChooser fileChooser = createFileChooser();
        File selectedFile = showFileChooser(fileChooser);

        if (selectedFile != null) {
            String success = uploadAndStoreProductImage(selectedFile, product);
            handleImageUpdateResult(success, product);
        }
    }

    private FileChooser createFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Product Image");
        fileChooser.getExtensionFilters().addAll(
                getImageExtensionFilters()
        );
        return fileChooser;
    }

    private File showFileChooser(FileChooser fileChooser) {
        Stage fileChooserStage = new Stage();
        return fileChooser.showOpenDialog(fileChooserStage);
    }

    private ObservableList<FileChooser.ExtensionFilter> getImageExtensionFilters() {
        return FXCollections.observableArrayList(
                new FileChooser.ExtensionFilter("All Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("JPEG", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("GIF", "*.gif"),
                new FileChooser.ExtensionFilter("Bitmap", "*.bmp")
        );
    }

    private String uploadAndStoreProductImage(File selectedFile, Product product) {
        return ServerUtility.uploadProductImageAndStoreInDB(selectedFile, product.getProductId());
    }

    private void handleImageUpdateResult(String success, Product product) {
        if (success != null) {
            DialogUtils.showCompletionDialog("Product Image Updated", "User image update successful");
            product.setProductImage(success);
            loadImage(product);
        } else {
            DialogUtils.showErrorMessage("Product Image Error", "There has been an error in updating your profile image.");
        }
    }

    private void initiateUpdateDetails(Product product) {
        ConfirmationAlert confirmationDialog = new ConfirmationAlert("Update Product Details?", "Please double check values", "", false);
        boolean userConfirmed = confirmationDialog.showAndWait();

        if (userConfirmed) {

            int productUpdated = updateProductDetails(product.getProductId());

            if (productUpdated > 0) {
                DialogUtils.showCompletionDialog("Success", "Product details updated successfully!");
                registerProductController.initializeTableView(product.getParentId());

                if (productListController!= null){
                    productListController.loadMoreProducts();
                }

            } else if (productUpdated == -2) {
                DialogUtils.showErrorMessage("Cancelled", "Update canceled by the user.");
            } else {
                DialogUtils.showErrorMessage("Failed", "Failed to update product details.");
            }
        } else {
            DialogUtils.showErrorMessage("Cancelled", "Update canceled by the user.");
        }


    }

    private int updateProductDetails(int productId) {
        ProductDAO productDAO = new ProductDAO();
        Product existingProduct = productDAO.getProductDetails(productId);
        UnitDAO unitDAO = new UnitDAO();
        BrandDAO brandDAO = new BrandDAO();
        CategoriesDAO categoriesDAO = new CategoriesDAO();
        SegmentDAO segmentDAO = new SegmentDAO();
        SectionsDAO sectionsDAO = new SectionsDAO();
        ProductClassDAO productClassDAO = new ProductClassDAO();

        if (existingProduct != null) {
            existingProduct.setProductName(productNameTextField.getText());
            existingProduct.setBarcode(productBarcodeTextField.getText());
            existingProduct.setProductCode(productCodeTextField.getText());
            existingProduct.setDescription(productDescriptionTextField.getText());
            existingProduct.setShortDescription(shortDescriptionTextField.getText());
            if (dateAdded.getValue() == null) {
                existingProduct.setDateAdded(null);
            } else {
                existingProduct.setDateAdded(Date.valueOf(dateAdded.getValue()));
            }
            existingProduct.setLastUpdated(new Timestamp(System.currentTimeMillis()));
            existingProduct.setProductBrand(brandDAO.getBrandIdByName(brandComboBox.getSelectionModel().getSelectedItem()));
            existingProduct.setProductCategory(categoriesDAO.getCategoryIdByName(categoryComboBox.getSelectionModel().getSelectedItem()));
            existingProduct.setProductClass(productClassDAO.getProductClassIdByName(classComboBox.getSelectionModel().getSelectedItem()));
            existingProduct.setProductSegment(segmentDAO.getSegmentIdByName(segmentComboBox.getSelectionModel().getSelectedItem()));
            existingProduct.setProductSection(sectionsDAO.getSectionIdByName(sectionComboBox.getSelectionModel().getSelectedItem()));
            existingProduct.setProductShelfLife(Integer.parseInt(productShelfLifeTextField.getText()));
            existingProduct.setProductWeight(Double.parseDouble(baseWeightTextField.getText()));
            existingProduct.setMaintainingQuantity(Integer.parseInt(maintainingBaseQtyTextField.getText()));
            existingProduct.setUnitOfMeasurement(unitDAO.getUnitIdByName(baseUnitComboBox.getSelectionModel().getSelectedItem()));
            existingProduct.setUnitOfMeasurementCount(Integer.parseInt(unitCountTextField.getText()));
            existingProduct.setIsActive(active.isSelected() ? 1 : 0);
            return productDAO.updateProduct(existingProduct);
        } else {
            return -1; // Return an appropriate error code or handle accordingly
        }
    }


    private void updateProductPricing(int productName) {
        ProductDAO productDAO = new ProductDAO();
        double euc = parseDoubleOrDefault(eucTextField.getText());
        double eeuc = parseDoubleOrDefault(eeucTextField.getText());
        double ppu = parseDoubleOrDefault(ppuTextField.getText());
        double cpu = parseDoubleOrDefault(copTextField1.getText());
        double priceA = parseDoubleOrDefault(priceATextField.getText());
        double priceB = parseDoubleOrDefault(priceBTextField.getText());
        double priceC = parseDoubleOrDefault(priceCTextField.getText());
        double priceD = parseDoubleOrDefault(priceDTextField.getText());
        double priceE = parseDoubleOrDefault(priceETextField.getText());

        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Update price?",
                "Please double check before proceeding", "", false);

        boolean userConfirmed = confirmationAlert.showAndWait();

        if (userConfirmed) {
            int priceUpdate = productDAO.updateProductPrices(productName, euc, eeuc, ppu, cpu, priceA, priceB, priceC, priceD, priceE);
            if (priceUpdate > 0) {
                DialogUtils.showCompletionDialog("Success", "Price update success!");
            } else {
                DialogUtils.showErrorMessage("Error", "Price update failed!");
            }
        } else {
            DialogUtils.showErrorMessage("Cancelled", "You have cancelled updating the price for this item");
        }
    }

    private double parseDoubleOrDefault(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }


    private void initializeTableView(int productId) {
        CompletableFuture<List<Product>> productConfigurationsFuture = CompletableFuture.supplyAsync(
                () -> new ProductDAO().getAllProductConfigs(productId)
        );

        productConfigurationsFuture.thenAcceptAsync(productConfigurations -> {
            Platform.runLater(() -> { // Ensure updates happen in the JavaFX UI thread
                productConfigurationList.setAll(productConfigurations);

                initializeProductConfigTableColumns();
                initializeProductPricingTableColumns();

                productConfigurationTable.setItems(productConfigurationList);
                productPricing.setItems(productConfigurationList);
            });
        });
    }


    private void initializeProductConfigTableColumns() {
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        shortDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("shortDescription"));
        unitOfMeasurementColumn.setCellValueFactory(new PropertyValueFactory<>("unitOfMeasurementString"));
        unitCountColumn.setCellValueFactory(new PropertyValueFactory<>("unitOfMeasurementCount"));
        barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));
    }

    private void initializeProductPricingTableColumns() {
        descriptionColumnPricing.setCellValueFactory(new PropertyValueFactory<>("description"));
        copColumn.setCellValueFactory(new PropertyValueFactory<>("costPerUnit"));
        ppuColumn.setCellValueFactory(new PropertyValueFactory<>("pricePerUnit"));
        priceAColumn.setCellValueFactory(new PropertyValueFactory<>("priceA"));
        priceBColumn.setCellValueFactory(new PropertyValueFactory<>("priceB"));
        priceCColumn.setCellValueFactory(new PropertyValueFactory<>("priceC"));
        priceDColumn.setCellValueFactory(new PropertyValueFactory<>("priceD"));
        priceEColumn.setCellValueFactory(new PropertyValueFactory<>("priceE"));
        eucColumn.setCellValueFactory(new PropertyValueFactory<>("estimatedUnitCost"));
        eecColumn.setCellValueFactory(new PropertyValueFactory<>("estimatedExtendedCost"));
    }

    private static final Logger logger = LoggerFactory.getLogger(RegisterProductController.class);

    private void registerDoubleClickEventForTable(TableView<Product> tableView) {
        tableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Product selectedProduct = tableView.getSelectionModel().getSelectedItem();
                if (selectedProduct != null) {
                    openProductDetails(selectedProduct);
                }
            }
        });
    }

    private Stage productDetailsStage = null;
    ErrorUtilities errorUtilities = new ErrorUtilities();


    private void openProductDetails(Product product) {
        Platform.runLater(() -> {
            if (productDetailsStage != null) {
                productDetailsStage.close(); // Close any existing window before opening a new one
                productDetailsStage = null;
            }

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("registerProduct.fxml"));
                Parent root = loader.load();
                RegisterProductController controller = loader.getController();
                controller.initData(product.getProductId());
                controller.setRegisterProductController(this);

                productDetailsStage = new Stage();
                productDetailsStage.setMaximized(true);
                productDetailsStage.setTitle("Product Details");
                productDetailsStage.setScene(new Scene(root));
                productDetailsStage.setOnCloseRequest(event -> productDetailsStage = null);
                productDetailsStage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }


    void addNewParentProduct() {
        UnitDAO unitDAO = new UnitDAO();

        baseUnitComboBox.setValue(unitDAO.getUnitNameById(1));
        baseUnitComboBox.setDisable(true);
        unitCountTextField.setText("1");
        unitCountTextField.setDisable(true);

        generateBarcode.setOnMouseClicked(mouseEvent -> checkIfBarcoded());
    }

    private void checkIfBarcoded() {
        if (productBarcodeTextField.getText().isEmpty() || productBarcodeTextField.getText().equals("")) {
            productBarcodeTextField.setText(getNewBarcode());
        } else {
            Product product = new Product();
            product.setBarcode(productBarcodeTextField.getText());
            getBarcodeImage(product);
        }
    }


    @Setter
    ProductListController productListController;
}



