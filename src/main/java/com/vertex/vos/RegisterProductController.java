package com.vertex.vos;

import com.vertex.vos.Constructors.*;
import com.vertex.vos.Utilities.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class RegisterProductController implements Initializable, DateSelectedCallback {
    @FXML
    private TextField productNameTextField;
    @FXML
    private Label productNameErr;
    @FXML
    private TextField productCodeTextField;
    @FXML
    private Label productCodeErr;
    @FXML
    private ComboBox<String> supplierNameComboBox;
    @FXML
    private Label supplierNameErr;
    @FXML
    private TextField productDescriptionTextField;
    @FXML
    private Label productDescriptionErr;
    @FXML
    private TextField dateAddedTextField;
    @FXML
    private ImageView datePickerButton;
    @FXML
    private Label dateAddedErr;
    @FXML
    private ComboBox<String> brandComboBox;
    @FXML
    private Label brandErr;
    @FXML
    private ComboBox<String> categoryComboBox;
    @FXML
    private Label categoryErr;
    @FXML
    private ComboBox<String> segmentComboBox;
    @FXML
    private Label segmentErr;
    @FXML
    private ComboBox<String> sectionComboBox;
    @FXML
    private Label sectionErr;
    @FXML
    private Button confirmButton;
    @FXML
    private Label confirmationLabel;
    @FXML
    private TableView productConfigurationTable;
    @FXML
    private TableColumn descriptionColumn;
    @FXML
    private TableColumn unitOfMeasurementColumn;
    @FXML
    private TableColumn unitCountColumn;
    @FXML
    private TableColumn quantityColumn;
    @FXML
    private TableColumn priceColumn;
    @FXML
    private TableColumn lastUpdateColumn;
    @FXML
    private TableColumn barcodeColumn;
    @FXML
    private VBox addConfiguration;
    @FXML
    private Label configLabel;
    private Product selectedProduct;
    @FXML
    private ImageView productPic;
    @FXML
    private HBox changePicButton;
    @FXML
    private TextField baseWeightTextField;
    @FXML
    private Label baseWeightErr;
    @FXML
    private ComboBox<String> baseUnitComboBox;
    @FXML
    private Label baseUnitErr;
    @FXML
    private TextField maintainingBaseQtyTextField;
    @FXML
    private Label maintainingBaseQtyErr;
    @FXML
    private TextField productShelfLifeTextField;
    @FXML
    private Label productShelfLifeErr;
    @FXML
    private ComboBox<String> classComboBox;
    @FXML
    private Label classErr;
    @FXML
    private ComboBox<String> natureComboBox;
    @FXML
    private Label natureErr;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        populateComboBox();

        configLabel.setText("Add Product Details");

        dateAddedTextField.setPromptText(LocalDate.now().toString());
        dateAddedTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                openCalendarView();
            }
        });

        addConfiguration.setOnMouseEntered(event -> {
            configLabel.setVisible(true);
        });

        addConfiguration.setOnMouseExited(event -> {
            configLabel.setVisible(false);
        });

        addConfiguration.setOnMouseClicked(mouseEvent -> {
            try {
                openAddConfig("addDetails");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void populateComboBox() {
        //setup
        TextFieldUtils.setComboBoxBehavior(supplierNameComboBox);
        TextFieldUtils.setComboBoxBehavior(brandComboBox);
        TextFieldUtils.setComboBoxBehavior(segmentComboBox);
        TextFieldUtils.setComboBoxBehavior(sectionComboBox);
        TextFieldUtils.setComboBoxBehavior(categoryComboBox);
        TextFieldUtils.setComboBoxBehavior(natureComboBox);
        TextFieldUtils.setComboBoxBehavior(classComboBox);
        TextFieldUtils.setComboBoxBehavior(baseUnitComboBox);


        //supplier
        SupplierDAO suppliersDAO = new SupplierDAO();
        ObservableList<Supplier> suppliersList = suppliersDAO.getAllSuppliers();
        ObservableList<String> supplierNames = FXCollections.observableArrayList();
        for (Supplier supplier : suppliersList) {
            String supplierName = supplier.getSupplierName();
            supplierNames.add(supplierName);
        }
        supplierNameComboBox.setItems(supplierNames);
        ComboBoxFilterUtil.setupComboBoxFilter(supplierNameComboBox, supplierNames);

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

    private void openAddConfig(String config) throws IOException {

        if (config.equals("addConfig")) {
            registerProductConfig();
        } else if (config.equals("addDetails")) {
            registerProductDetails();
        } else {
            ToDoAlert.showToDoAlert();
        }
    }

    private void registerProductConfig() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("registerProductConfiguration.fxml"));
            Parent root = loader.load();

            // Pass the selected employee data to the controller of employeeDetails.fxml
            RegisterProductConfigurationController controller = loader.getController();
            controller.initData(productNameTextField.getText().toString());

            Stage stage = new Stage();
            stage.setTitle("Product Configuration");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerProductDetails() {
        String supplier_name = supplierNameComboBox.getSelectionModel().getSelectedItem();
        String brand_name = brandComboBox.getSelectionModel().getSelectedItem();
        String category_name = categoryComboBox.getSelectionModel().getSelectedItem();
        String segment_name = segmentComboBox.getSelectionModel().getSelectedItem();
        String section_name = sectionComboBox.getSelectionModel().getSelectedItem();
        String class_name = classComboBox.getSelectionModel().getSelectedItem();
        String nature_name = natureComboBox.getSelectionModel().getSelectedItem();
        String base_unit = baseUnitComboBox.getSelectionModel().getSelectedItem();


        confirmationAlert confirmationAlert = new confirmationAlert("Register Product Details? ", "Add product " + productNameTextField.getText(), "Supplier to add to : " + supplier_name);

        boolean userConfirmed = confirmationAlert.showAndWait();

        if (userConfirmed) {
            Product product = new Product();
            SupplierDAO supplierDAO = new SupplierDAO();
            BrandDAO brandDAO = new BrandDAO();
            CategoriesDAO categoriesDAO = new CategoriesDAO();
            SegmentDAO segmentDAO = new SegmentDAO();
            SectionsDAO sectionsDAO = new SectionsDAO();
            UnitDAO unitDAO = new UnitDAO();
            ProductClassDAO productClassDAO = new ProductClassDAO();
            NatureDAO natureDAO = new NatureDAO();

            product.setProduct_name(productNameTextField.getText());
            product.setProduct_code(productCodeTextField.getText());
            product.setDescription(productDescriptionTextField.getText());
            product.setDate_added(Date.valueOf(dateAddedTextField.getText()));
            product.setSupplier_name(supplierDAO.getSupplierIdByName(supplier_name));
            product.setProduct_brand(brandDAO.getBrandIdByName(brand_name));
            product.setProduct_category(categoriesDAO.getCategoryIdByName(category_name));
            product.setProduct_segment(segmentDAO.getSegmentIdByName(segment_name));
            product.setProduct_section(sectionsDAO.getSectionIdByName(section_name));
            product.setBase_unit(unitDAO.getUnitIdByName(base_unit));
            product.setProduct_nature(natureDAO.getNatureIdByName(nature_name));
            product.setProduct_class(productClassDAO.getProductClassIdByName(class_name));
            product.setProduct_shelf_life(Integer.parseInt(productShelfLifeTextField.getText()));
            product.setMaintaining_base_quantity(Integer.parseInt(maintainingBaseQtyTextField.getText()));
            product.setProduct_base_weight(Double.valueOf(baseWeightTextField.getText()));

            ProductDAO productDAO = new ProductDAO();

            boolean isProductRegistered = productDAO.addProduct(product);
            if (isProductRegistered) {
                configLabel.setText("Add Product Configuration");
                String config = "addConfig";
                addConfiguration.setOnMouseClicked(mouseEvent -> {
                    try {
                        openAddConfig(config);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.show();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.show();
        }

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

    public void initData(Product selectedProduct) {
        this.selectedProduct = selectedProduct;

        if (selectedProduct != null) {
            loadSelectedProduct(selectedProduct);
            confirmButton.setText("Update");
        }
    }

    private void loadSelectedProduct(Product selectedProduct) {
        ProductDAO productDAO = new ProductDAO(); // Assuming you have a ProductDAO instance

        String productCode = selectedProduct.getProduct_code();
        Product freshProductData = productDAO.getProductByCode(productCode);

        if (freshProductData != null) {
            UnitDAO unitDAO = new UnitDAO();
            SupplierDAO supplierDAO = new SupplierDAO();
            BrandDAO brandDAO = new BrandDAO();
            CategoriesDAO categoriesDAO = new CategoriesDAO();
            SegmentDAO segmentDAO = new SegmentDAO();
            SectionsDAO sectionsDAO = new SectionsDAO();
            ProductClassDAO productClassDAO = new ProductClassDAO();
            NatureDAO natureDAO = new NatureDAO();

            String supplierName = supplierDAO.getSupplierNameById(freshProductData.getSupplier_name());
            supplierNameComboBox.setValue(supplierName);
            productNameTextField.setText(freshProductData.getProduct_name());
            dateAddedTextField.setText(String.valueOf(freshProductData.getDate_added()));
            baseWeightTextField.setText(String.valueOf(freshProductData.getProduct_base_weight()));
            String baseUnitName = unitDAO.getUnitNameById(freshProductData.getBase_unit());
            baseUnitComboBox.setValue(baseUnitName);
            maintainingBaseQtyTextField.setText(String.valueOf(freshProductData.getMaintaining_base_quantity()));
            productDescriptionTextField.setText(freshProductData.getDescription());
            productCodeTextField.setText(freshProductData.getProduct_code());
            productShelfLifeTextField.setText(String.valueOf(freshProductData.getProduct_shelf_life()));
            String brandName = brandDAO.getBrandNameById(freshProductData.getProduct_brand());
            brandComboBox.setValue(brandName);
            String categoryName = categoriesDAO.getCategoryNameById(freshProductData.getProduct_category());
            categoryComboBox.setValue(categoryName);
            String segmentName = segmentDAO.getSegmentNameById(freshProductData.getProduct_segment());
            segmentComboBox.setValue(segmentName);
            String sectionName = sectionsDAO.getSectionNameById(freshProductData.getProduct_section());
            sectionComboBox.setValue(sectionName);
            String className = productClassDAO.getProductClassNameById(freshProductData.getProduct_class());
            classComboBox.setValue(className);
            String natureName = natureDAO.getNatureNameById(freshProductData.getProduct_nature());
            natureComboBox.setValue(natureName);

            configLabel.setText("Add Product Configuration");
            String config = "addConfig";
            addConfiguration.setOnMouseClicked(mouseEvent -> {
                try {
                    openAddConfig(config);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        } else {
            ToDoAlert.showToDoAlert();
        }
    }

}



