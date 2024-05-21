package com.vertex.vos;

import com.vertex.vos.Constructors.ComboBoxFilterUtil;
import com.vertex.vos.Constructors.Product;
import com.vertex.vos.Constructors.ProductsInTransact;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

public class ProductSelectionForGeneralReceiveController {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    private final BrandDAO brandDAO = new BrandDAO();
    private final CategoriesDAO categoriesDAO = new CategoriesDAO();
    private final SegmentDAO segmentDAO = new SegmentDAO();
    private final SectionsDAO sectionsDAO = new SectionsDAO();
    private final ProductsPerSupplierDAO productsPerSupplierDAO = new ProductsPerSupplierDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final UnitDAO unitDAO = new UnitDAO();

    @FXML
    private Label businessTypeLabel;

    @FXML
    private TableView<Product> productsPerSupplier;

    @FXML
    private ComboBox<String> supplier;

    @FXML
    private VBox supplierBox;

    @FXML
    private Label supplierErr;

    private ObservableList<Product> fetchProductsForSupplier(int supplierId) {
        ObservableList<Product> products = FXCollections.observableArrayList();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            List<Integer> supplierProducts = productsPerSupplierDAO.getProductsForSupplier(supplierId);

            if (supplierProducts.isEmpty()) {
                return products; // return empty list if no products found
            }

            String productIds = supplierProducts.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(","));

            String query = "SELECT * FROM products WHERE parent_id IN (" + productIds + ") OR product_id IN (" + productIds + ")";

            try (ResultSet resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    Product product = new Product();
                    product.setProductName(resultSet.getString("product_name"));
                    product.setProductCode(resultSet.getString("product_code"));
                    product.setDescription(resultSet.getString("description"));
                    product.setProductImage(resultSet.getString("product_image"));
                    product.setProductBrandString(brandDAO.getBrandNameById(resultSet.getInt("product_brand")));
                    product.setProductCategoryString(categoriesDAO.getCategoryNameById(resultSet.getInt("product_category")));
                    product.setProductSegmentString(segmentDAO.getSegmentNameById(resultSet.getInt("product_segment")));
                    product.setProductSectionString(sectionsDAO.getSectionNameById(resultSet.getInt("product_section")));
                    product.setUnitOfMeasurementString(unitDAO.getUnitNameById(resultSet.getInt("unit_of_measurement")));
                    product.setParentId(resultSet.getInt("parent_id"));
                    product.setProductId(resultSet.getInt("product_id"));
                    products.add(product);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }

    public void addProductToTable(String selectedItem) {
        ObservableList <String> allSupplierNames = supplierDAO.getAllSupplierNames();
        TextFieldUtils.setComboBoxBehavior(supplier);
        supplier.setItems(allSupplierNames);
        ComboBoxFilterUtil.setupComboBoxFilter(supplier, allSupplierNames);

        supplier.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                int supplierId = supplierDAO.getSupplierIdByName(newValue);
                loadProductsPerSupplier(supplierId);
            }
        });

        productsPerSupplier.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Product selectedProduct = row.getItem();
                    addSelectedProduct(selectedProduct);
                }
            });
            return row;
        });

        // Set up key press event handler for the table view
        productsPerSupplier.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                Product selectedProduct = productsPerSupplier.getSelectionModel().getSelectedItem();
                addSelectedProduct(selectedProduct);
            }
        });

        createTableColumns();
    }
    private void addSelectedProduct(Product selectedProduct) {
        if (selectedProduct != null) {
            ConfirmationAlert confirmationAlert = new ConfirmationAlert("Confirmation", "Add " + selectedProduct.getDescription(), "Are you sure you want to add this product?", false);
            boolean confirmed = confirmationAlert.showAndWait();
            if (confirmed) {
                // Convert Product to ProductsInTransact
                ProductsInTransact productsInTransact = new ProductsInTransact();
                productsInTransact.setDescription(selectedProduct.getDescription());
                productsInTransact.setUnit(selectedProduct.getUnitOfMeasurementString());
                receivingIOperationsController.addProductToReceivingTable(productsInTransact);
                productsPerSupplier.getItems().remove(selectedProduct);
            }
        }
    }



    private void loadProductsPerSupplier(int supplierId) {
        ObservableList<Product> products = fetchProductsForSupplier(supplierId);
        productsPerSupplier.setItems(products);
    }

    private void createTableColumns() {
        TableColumn<Product, String> productDescriptionColumn = new TableColumn<>("Product Description");
        productDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Product, String> productUnitColumn = new TableColumn<>("Product Unit");
        productUnitColumn.setCellValueFactory(new PropertyValueFactory<>("unitOfMeasurementString"));

        productsPerSupplier.getColumns().addAll(productDescriptionColumn, productUnitColumn);
    }

    ReceivingIOperationsController receivingIOperationsController;
    void setTargetController(ReceivingIOperationsController receivingIOperationsController) {
        this.receivingIOperationsController = receivingIOperationsController;
    }
}
