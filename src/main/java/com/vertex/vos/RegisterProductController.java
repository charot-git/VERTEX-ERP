package com.vertex.vos;

import com.vertex.vos.Constructors.*;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class RegisterProductController implements Initializable, DateSelectedCallback {

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
    private TableColumn barcodeColumn;
    @FXML
    private TableColumn copColumn;
    @FXML
    private TableColumn descriptionColumn;
    @FXML
    private TableColumn descriptionColumnPricing;
    @FXML
    private TableColumn eecColumn;
    @FXML
    private TableColumn eucColumn;
    @FXML
    private TableColumn ppuColumn;
    @FXML
    private TableColumn priceAColumn;
    @FXML
    private TableColumn priceBColumn;
    @FXML
    private TableColumn priceCColumn;
    @FXML
    private TableColumn priceDColumn;
    @FXML
    private TableColumn priceEColumn;
    @FXML
    private TableColumn quantityColumn;
    @FXML
    private TableColumn shortDescriptionColumn;
    @FXML
    private TableColumn unitCountColumn;
    @FXML
    private TableColumn unitOfMeasurementColumn;

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
    private ComboBox<String> natureComboBox;
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
    private TextField dateAddedTextField;
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


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configLabel.setText("Add Product Details");

        populateComboBox();

        //restrictions
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
        dateAddedTextField.setPromptText(LocalDate.now().toString());
        dateAddedTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                openCalendarView();
            }
        });
        confirmButton.setOnMouseClicked(mouseEvent -> registerProductDetails());
    }

    private String getNewBarcode() {
        int barcode = productDAO.getNextBarcodeNumber();
        productBarcodeTextField.setText(String.valueOf(barcode));
        return String.valueOf(barcode);
    }


    private void populateComboBox() {
        //setup
        TextFieldUtils.setComboBoxBehavior(baseUnitComboBox);
        TextFieldUtils.setComboBoxBehavior(brandComboBox);
        TextFieldUtils.setComboBoxBehavior(categoryComboBox);
        TextFieldUtils.setComboBoxBehavior(classComboBox);
        TextFieldUtils.setComboBoxBehavior(natureComboBox);
        TextFieldUtils.setComboBoxBehavior(sectionComboBox);
        TextFieldUtils.setComboBoxBehavior(segmentComboBox);

        //brands
        BrandDAO brandDAO = new BrandDAO();
        ObservableList<Brands> brandsList = brandDAO.getBrandDetails();
        ObservableList<String> brandNames = FXCollections.observableArrayList();
        for (Brands brands : brandsList) {
            int brand_id = brands.getBrand_id();
            String brand_name = brands.getBrand_name();
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
            int category_id = category.getCategory_id();
            String category_name = category.getCategory_name();
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

        //nature
        NatureDAO natureDAO = new NatureDAO();
        ObservableList<Nature> natureObservableList = natureDAO.getNatureDetails();
        ObservableList<String> natureNames = FXCollections.observableArrayList();
        for (Nature nature : natureObservableList) {
            int nature_id = nature.getNatureId();
            String nature_name = nature.getNatureName();
            natureNames.add(nature_name);
        }
        natureComboBox.setItems(natureNames);
        ComboBoxFilterUtil.setupComboBoxFilter(natureComboBox, natureNames);

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
        boolean userConfirmed = userConfirmationDetails();
        if (userConfirmed) {
            int productId = productParentRegistered();
            if (productId != -1) {
                registrationVBox.getChildren().add(productTabPane);
                VBox confirmationBox = (VBox) confirmButton.getParent();
                registrationVBox.getChildren().remove(confirmationBox);
                productTabPane.getTabs().removeAll(priceControlTab, productPricingTab);
                addConfiguration.setOnMouseClicked(mouseEvent -> addNewConfigSetup(productId));
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.show();
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
                stage.setTitle("Add breakdown for " + productDescriptionTextField.getText());
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

                Stage stage = new Stage();
                stage.setTitle("Register Product Configuration");
                stage.setScene(new Scene(content));
                stage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace(); // Handle the exception according to your needs
                System.err.println("Error loading companyRegistration.fxml: " + e.getMessage());
            }
        }
    }

    private void initializeConfigurationRegistration(int productId) {
        BrandDAO brandDAO = new BrandDAO();
        CategoriesDAO categoriesDAO = new CategoriesDAO();
        SegmentDAO segmentDAO = new SegmentDAO();
        SectionsDAO sectionsDAO = new SectionsDAO();
        UnitDAO unitDAO = new UnitDAO();
        ProductClassDAO productClassDAO = new ProductClassDAO();
        NatureDAO natureDAO = new NatureDAO();

        productNameTextField.setDisable(true);
        baseUnitComboBox.setDisable(false);
        unitCountTextField.setDisable(false);

        Product productGetter = productDAO.getProductById(productId);

        String productName = productGetter.getProductName();

        Product selectedProduct = productDAO.getProductDetails(productId);


        //setting default values
        HeaderText.setText("Configuration of : " + selectedProduct.getProductName());
        productNameTextField.setText(productName);
        brandComboBox.setValue(brandDAO.getBrandNameById(selectedProduct.getProductBrand()));
        categoryComboBox.setValue(categoriesDAO.getCategoryNameById(selectedProduct.getProductCategory()));
        segmentComboBox.setValue(segmentDAO.getSegmentNameById(selectedProduct.getProductSegment()));
        sectionComboBox.setValue(sectionsDAO.getSectionNameById(selectedProduct.getProductSection()));
        classComboBox.setValue(productClassDAO.getProductClassNameById(selectedProduct.getProductClass()));
        natureComboBox.setValue(natureDAO.getNatureNameById(selectedProduct.getProductNature()));

        confirmButton.setOnMouseClicked(mouseEvent -> userConfirmationConfig(productName));
    }

    private void userConfirmationConfig(String productName) {
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Register Product Configuration? ", "Add product configuration for : " + productNameTextField.getText(), "Please verify");
        boolean userConfirmed = confirmationAlert.showAndWait();

        ProductDAO productDAO = new ProductDAO();

        int productId = productDAO.getProductIdByName(productName);

        int configId;

        if (userConfirmed) {
            configId = addNewConfig(productId);
            if (configId != -1) {
                DialogUtils.showConfirmationDialog("Registration Successful", "New configuration for " + productName + " has been registered");
                Product productConfig = productDAO.getProductDetails(configId);
                if (productConfig != null) {
                    // Add the newly fetched product to the productList
                    productConfigurationList.add(productConfig);

                    // Close the stage or perform other actions
                    stage = (Stage) HeaderText.getScene().getWindow();
                    stage.close();
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
        NatureDAO natureDAO = new NatureDAO();

        Product ConfigProduct = new Product();

        // Example values retrieved from UI components (ComboBoxes, TextFields, etc.)
        ConfigProduct.setIsActive(1);
        ConfigProduct.setParentId(productId);
        ConfigProduct.setProductName(productNameTextField.getText());
        // Setting other properties (Replace these with actual values from your UI)
        ConfigProduct.setBarcode(productBarcodeTextField.getText());
        ConfigProduct.setProductCode(productCodeTextField.getText());
        ConfigProduct.setProductImage("todo");
        ConfigProduct.setDescription(productDescriptionTextField.getText());
        ConfigProduct.setShortDescription(shortDescriptionTextField.getText());
        ConfigProduct.setDateAdded(Date.valueOf(dateAddedTextField.getText()));
        ConfigProduct.setLastUpdated(new Timestamp(System.currentTimeMillis()));
        ConfigProduct.setProductBrand(brandDAO.getBrandIdByName(brandComboBox.getSelectionModel().getSelectedItem()));
        ConfigProduct.setProductCategory(categoriesDAO.getCategoryIdByName(categoryComboBox.getSelectionModel().getSelectedItem()));
        ConfigProduct.setProductClass(productClassDAO.getProductClassIdByName(classComboBox.getSelectionModel().getSelectedItem()));
        ConfigProduct.setProductSegment(segmentDAO.getSegmentIdByName(segmentComboBox.getSelectionModel().getSelectedItem()));
        ConfigProduct.setProductNature(natureDAO.getNatureIdByName(natureComboBox.getSelectionModel().getSelectedItem()));
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
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Register Product Details? ", "Add product " + productNameTextField.getText(), "Please verify");
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
        NatureDAO natureDAO = new NatureDAO();
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

        String selectedNature = natureComboBox.getSelectionModel().getSelectedItem();
        int natureId = selectedNature != null ? natureDAO.getNatureIdByName(selectedNature) : -1;

        String selectedSection = sectionComboBox.getSelectionModel().getSelectedItem();
        int sectionId = selectedSection != null ? sectionsDAO.getSectionIdByName(selectedSection) : -1;

        // Set other properties
        product.setIsActive(1);
        product.setParentId(0);
        product.setProductName(productNameTextField.getText());
        product.setBarcode(productBarcodeTextField.getText());
        product.setProductCode(productCodeTextField.getText());
        product.setProductImage("todo");
        product.setDescription(productDescriptionTextField.getText());
        product.setShortDescription(shortDescriptionTextField.getText());
        product.setDateAdded(Date.valueOf(dateAddedTextField.getText()));
        product.setLastUpdated(new Timestamp(System.currentTimeMillis()));
        product.setProductBrand(brandId);
        product.setProductCategory(categoryId);
        product.setProductClass(classId);
        product.setProductSegment(segmentId);
        product.setProductNature(natureId);
        product.setProductSection(sectionId);
        product.setProductShelfLife(Integer.parseInt(productShelfLifeTextField.getText()));
        product.setProductWeight(Double.parseDouble(baseWeightTextField.getText()));
        product.setMaintainingQuantity(Integer.parseInt(maintainingBaseQtyTextField.getText()));
        product.setQuantity(0);
        product.setUnitOfMeasurement(unitDAO.getUnitIdByName(baseUnitComboBox.getSelectionModel().getSelectedItem()));
        product.setUnitOfMeasurementCount(Integer.parseInt(unitCountTextField.getText()));

        return productDAO.addProduct(product);
    }


    @Override
    public void onDateSelected(LocalDate selectedDate) {
        dateAddedTextField.setText(selectedDate.toString());
    }

    @FXML
    private void openCalendarView() {
        // Create a new instance of CalendarView
        CalendarView calendarView = new CalendarView(this);
        Stage stage = new Stage();
        calendarView.start(stage);
    }

    @FXML
    private void openCalendarViewOnClick(MouseEvent mouseEvent) {
        openCalendarView();
    }

    public void initData(int productName) {
        ProductDAO productDAO = new ProductDAO();
        UnitDAO unitDAO = new UnitDAO();
        BrandDAO brandDAO = new BrandDAO();
        CategoriesDAO categoriesDAO = new CategoriesDAO();
        SegmentDAO segmentDAO = new SegmentDAO();
        SectionsDAO sectionsDAO = new SectionsDAO();
        ProductClassDAO productClassDAO = new ProductClassDAO();
        NatureDAO natureDAO = new NatureDAO();
        Product product;
        product = productDAO.getProductDetails(productName);

        confirmButton.setText("Update " + product.getDescription());
        productNameTextField.setText(product.getProductName());
        productCodeTextField.setText(product.getProductCode());
        productBarcodeTextField.setText(product.getBarcode());
        Date dateAdded = product.getDateAdded();
        if (dateAdded != null) {
            dateAddedTextField.setText(dateAdded.toString());
        } else {
            dateAddedTextField.setText("N/A"); // Or any default value you prefer
        }
        generateBarcode.setOnMouseClicked(mouseEvent -> {
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

            WritableImage barcodeImage = BarcodePrinter.generateBarcodeImage(barcodeText);

            if (barcodeImage != null) {
                Stage barcodeStage = new Stage();
                barcodeStage.setTitle("Product Barcode");

                // Create a VBox with padding
                VBox barcodeVBox = new VBox();
                barcodeVBox.setPadding(new javafx.geometry.Insets(10));  // Adjust padding as needed
                barcodeVBox.setStyle("-fx-alignment: center;");  // Center content within VBox

                // Create an ImageView
                ImageView barcodeImageView = new ImageView(barcodeImage);

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

                // Add the ImageView and button to the VBox
                barcodeVBox.getChildren().addAll(barcodeImageView, copyButton);

                // Set the VBox as the scene's root
                barcodeStage.setScene(new Scene(barcodeVBox));
                barcodeStage.show();
            } else {
                System.out.println("Error generating barcode image!");
            }
        });



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
        natureComboBox.setValue(natureDAO.getNatureNameById(product.getProductNature()));
        eucTextField.setText(String.valueOf(product.getEstimatedUnitCost()));
        eeucTextField.setText(String.valueOf(product.getEstimatedExtendedCost()));
        copTextField1.setText(String.valueOf(product.getCostPerUnit()));
        ppuTextField.setText(String.valueOf(product.getPricePerUnit()));
        priceATextField.setText(String.valueOf(product.getPriceA()));
        priceBTextField.setText(String.valueOf(product.getPriceB()));
        priceCTextField.setText(String.valueOf(product.getPriceC()));
        priceDTextField.setText(String.valueOf(product.getPriceD()));
        priceETextField.setText(String.valueOf(product.getPriceE()));
        String imageUrl = product.getProductImage();
        if (imageUrl != null) {
            Image image = new Image(new File(imageUrl).toURI().toString());
            productPic.setImage(image);
        }


        Product finalProduct = product;
        addConfiguration.setOnMouseClicked(mouseEvent -> addNewConfigSetup(finalProduct.getProductId()));

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
        }

        initializeTableView(finalProduct.getProductId());

        confirmButtonPriceControl.setOnMouseClicked(mouseEvent -> updateProductPricing(productName));

        confirmButton.setOnMouseClicked(mouseEvent -> initiateUpdateDetails(productName));

        changePicButton.setOnMouseClicked(mouseEvent -> updateProductPicture(productName));
    }

    private void updateProductPicture(int productName) {
        Stage fileChooserStage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Product Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("JPEG", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("GIF", "*.gif"),
                new FileChooser.ExtensionFilter("Bitmap", "*.bmp")
        );
        File selectedFile = fileChooser.showOpenDialog(fileChooserStage);

        if (selectedFile != null) {
            boolean success = ServerUtility.uploadProductImageAndStoreInDB(selectedFile, productName);
            if (success) {
                DialogUtils.showConfirmationDialog("Product Image Updated", "User image update successful");
            } else {
                DialogUtils.showErrorMessage("Product Image Error", "There has been an error in updating your profile image.");
            }

        }
    }

    private void initiateUpdateDetails(int productId) {
        ConfirmationAlert confirmationDialog = new ConfirmationAlert("Update Product Details?", "Please double check values", "");
        boolean userConfirmed = confirmationDialog.showAndWait();

        if (userConfirmed) {
            int productUpdated = updateProductDetails(productId);

            if (productUpdated > 0) {
                DialogUtils.showConfirmationDialog("Success", "Product details updated successfully!");
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
        NatureDAO natureDAO = new NatureDAO();

        if (existingProduct != null) {
            existingProduct.setProductName(productNameTextField.getText());
            existingProduct.setBarcode(productBarcodeTextField.getText());
            existingProduct.setProductCode(productCodeTextField.getText());
            existingProduct.setProductImage("todo");
            existingProduct.setDescription(productDescriptionTextField.getText());
            existingProduct.setShortDescription(shortDescriptionTextField.getText());
            existingProduct.setDateAdded(Date.valueOf(dateAddedTextField.getText()));
            existingProduct.setLastUpdated(new Timestamp(System.currentTimeMillis()));
            existingProduct.setProductBrand(brandDAO.getBrandIdByName(brandComboBox.getSelectionModel().getSelectedItem()));
            existingProduct.setProductCategory(categoriesDAO.getCategoryIdByName(categoryComboBox.getSelectionModel().getSelectedItem()));
            existingProduct.setProductClass(productClassDAO.getProductClassIdByName(classComboBox.getSelectionModel().getSelectedItem()));
            existingProduct.setProductSegment(segmentDAO.getSegmentIdByName(segmentComboBox.getSelectionModel().getSelectedItem()));
            existingProduct.setProductNature(natureDAO.getNatureIdByName(natureComboBox.getSelectionModel().getSelectedItem()));
            existingProduct.setProductSection(sectionsDAO.getSectionIdByName(sectionComboBox.getSelectionModel().getSelectedItem()));
            existingProduct.setProductShelfLife(Integer.parseInt(productShelfLifeTextField.getText()));
            existingProduct.setProductWeight(Double.parseDouble(baseWeightTextField.getText()));
            existingProduct.setMaintainingQuantity(Integer.parseInt(maintainingBaseQtyTextField.getText()));
            existingProduct.setUnitOfMeasurement(unitDAO.getUnitIdByName(baseUnitComboBox.getSelectionModel().getSelectedItem()));
            existingProduct.setUnitOfMeasurementCount(Integer.parseInt(unitCountTextField.getText()));
            return productDAO.updateProduct(existingProduct);
        } else {
            return -1; // Return an appropriate error code or handle accordingly
        }
    }


    private void updateProductPricing(int productName) {
        ProductDAO productDAO = new ProductDAO();
        double euc = Double.parseDouble(eucTextField.getText());
        double eeuc = Double.parseDouble(eeucTextField.getText());
        double ppu = Double.parseDouble(ppuTextField.getText());
        double cpu = Double.parseDouble(copTextField1.getText());
        double priceA = Double.parseDouble(priceATextField.getText());
        double priceB = Double.parseDouble(priceBTextField.getText());
        double priceC = Double.parseDouble(priceCTextField.getText());
        double priceD = Double.parseDouble(priceDTextField.getText());
        double priceE = Double.parseDouble(priceETextField.getText());

        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Update price?", "Please double check before proceeding", "");

        boolean userConfirmed = confirmationAlert.showAndWait();

        if (userConfirmed) {
            int priceUpdate = productDAO.updateProductPrices(productName, euc, eeuc, ppu, cpu, priceA, priceB, priceC, priceD, priceE);
            if (priceUpdate > 0) {
                DialogUtils.showConfirmationDialog("Success", "Price update success!");
            } else {
                DialogUtils.showErrorMessage("Error", "Price update failed!");

            }
        } else {
            DialogUtils.showErrorMessage("Cancelled", "You have cancelled updating the price for this item");
        }
    }

    private void initializeTableView(int productId) {
        ProductDAO productDAO = new ProductDAO();
        List<Product> productConfigurations = productDAO.getAllProductConfigs(productId);

        productConfigurationList.addAll(productConfigurations);

        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        shortDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("shortDescription"));
        unitOfMeasurementColumn.setCellValueFactory(new PropertyValueFactory<>("unitOfMeasurementString"));
        unitCountColumn.setCellValueFactory(new PropertyValueFactory<>("unitOfMeasurementCount"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));

        productConfigurationTable.setItems(productConfigurationList);

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

        productPricing.setItems(productConfigurationList);
    }

    void addNewParentProduct() {
        UnitDAO unitDAO = new UnitDAO();

        baseUnitComboBox.setValue(unitDAO.getUnitNameById(1));
        baseUnitComboBox.setDisable(true);
        unitCountTextField.setText("1");
        unitCountTextField.setDisable(true);
    }

    void isParent(int parentId) {
        ProductDAO productDAO = new ProductDAO();
        Product product = productDAO.getProductById(parentId);

        String parentName = product.getProductName();

        if (parentId > 0) {
            HeaderText.setText("Configuration of " + parentName);
            baseUnitComboBox.setDisable(false);
            unitCountTextField.setDisable(false);
            productTabPane.getTabs().removeAll(productConfigTab, productPricingTab);
        } else if (parentId == 0) {
            HeaderText.setText("Product Details");
            baseUnitComboBox.setDisable(true);
            unitCountTextField.setDisable(true);
        }
    }
}



