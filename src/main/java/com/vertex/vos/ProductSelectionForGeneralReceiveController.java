package com.vertex.vos;

import com.vertex.vos.Constructors.ComboBoxFilterUtil;
import com.vertex.vos.Constructors.Product;
import com.vertex.vos.Constructors.ProductsInTransact;
import com.vertex.vos.Constructors.PurchaseOrder;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ProductSelectionForGeneralReceiveController {
    private static final Logger LOGGER = Logger.getLogger(ProductSelectionForGeneralReceiveController.class.getName());
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    private final BrandDAO brandDAO = new BrandDAO();
    private final CategoriesDAO categoriesDAO = new CategoriesDAO();
    private final SegmentDAO segmentDAO = new SegmentDAO();
    private final SectionsDAO sectionsDAO = new SectionsDAO();
    private final ProductsPerSupplierDAO productsPerSupplierDAO = new ProductsPerSupplierDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final UnitDAO unitDAO = new UnitDAO();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

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

    private Task<ObservableList<Product>> createFetchProductsTask(int supplierId) {
        return new Task<ObservableList<Product>>() {
            @Override
            protected ObservableList<Product> call() throws Exception {
                ObservableList<Product> products = FXCollections.observableArrayList();
                String productIds = productsPerSupplierDAO.getProductsForSupplier(supplierId).stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(","));

                if (productIds.isEmpty()) {
                    return products;
                }

                String query = "SELECT * FROM products WHERE parent_id IN (" + productIds + ") OR product_id IN (" + productIds + ")";

                try (Connection connection = dataSource.getConnection();
                     Statement statement = connection.createStatement();
                     ResultSet resultSet = statement.executeQuery(query)) {

                    while (resultSet.next()) {
                        Product product = createProductFromResultSet(resultSet);
                        products.add(product);
                    }
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Failed to fetch products for supplier", e);
                    throw e;
                }

                return products;
            }
        };
    }

    private Product createProductFromResultSet(ResultSet resultSet) throws SQLException {
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
        product.setPricePerUnit(resultSet.getDouble("price_per_unit"));
        product.setParentId(resultSet.getInt("parent_id"));
        product.setProductId(resultSet.getInt("product_id"));
        return product;
    }


    public void addProductToTable(String PO_NO, PurchaseOrder generalReceivePO) {
        ObservableList<String> allSupplierNames = supplierDAO.getAllSupplierNames();
        TextFieldUtils.setComboBoxBehavior(supplier);
        supplier.setItems(allSupplierNames);
        ComboBoxFilterUtil.setupComboBoxFilter(supplier, allSupplierNames);

        supplier.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                int supplierId = supplierDAO.getSupplierIdByName(newValue);
                loadProductsPerSupplier(supplierId);
                generalReceivePO.setSupplierName(supplierId);
            }
        });

        productsPerSupplier.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Product selectedProduct = row.getItem();
                    addSelectedProduct(selectedProduct, PO_NO);
                }
            });
            return row;
        });

        productsPerSupplier.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                Product selectedProduct = productsPerSupplier.getSelectionModel().getSelectedItem();
                addSelectedProduct(selectedProduct, PO_NO);
            }
        });

        createTableColumns();
    }

    private void addSelectedProduct(Product selectedProduct, String PO_NO) {
        if (selectedProduct != null) {
            ConfirmationAlert confirmationAlert = new ConfirmationAlert("Confirmation", "Add " + selectedProduct.getDescription(), "Are you sure you want to add this product?", false);
            boolean confirmed = confirmationAlert.showAndWait();
            if (confirmed) {
                ProductsInTransact productsInTransact = setProductProfile(selectedProduct, PO_NO);
                receivingIOperationsController.addProductToReceivingTable(productsInTransact);
                productsPerSupplier.getItems().remove(selectedProduct);
            }
        }
    }

    private static ProductsInTransact setProductProfile(Product selectedProduct, String PO_NO) {
        ProductsInTransact productsInTransact = new ProductsInTransact();
        productsInTransact.setProductId(selectedProduct.getProductId());
        productsInTransact.setDescription(selectedProduct.getDescription());
        productsInTransact.setUnit(selectedProduct.getUnitOfMeasurementString());
        productsInTransact.setUnitPrice(selectedProduct.getPricePerUnit());
        productsInTransact.setPurchaseOrderId(Integer.parseInt(PO_NO));
        return productsInTransact;
    }

    private void loadProductsPerSupplier(int supplierId) {
        Task<ObservableList<Product>> fetchProductsTask = createFetchProductsTask(supplierId);

        fetchProductsTask.setOnSucceeded(event -> productsPerSupplier.setItems(fetchProductsTask.getValue()));
        fetchProductsTask.setOnFailed(event -> LOGGER.log(Level.SEVERE, "Failed to load products for supplier", fetchProductsTask.getException()));

        executorService.submit(fetchProductsTask);
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
