package com.vertex.vos;

import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.*;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ProductSelectionPerSupplier implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(ProductSelectionPerSupplier.class.getName());
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    private final BrandDAO brandDAO = new BrandDAO();
    private final CategoriesDAO categoriesDAO = new CategoriesDAO();
    private final SegmentDAO segmentDAO = new SegmentDAO();
    private final SectionsDAO sectionsDAO = new SectionsDAO();
    private final ProductsPerSupplierDAO productsPerSupplierDAO = new ProductsPerSupplierDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final UnitDAO unitDAO = new UnitDAO();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    public VBox branchBox;
    public ComboBox<String> branch;

    @FXML
    private Label businessTypeLabel;
    @FXML
    private TableView<Product> productsPerSupplier;
    @FXML
    public ComboBox<String> supplier;
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

                String query = "SELECT * FROM products " +
                        "WHERE (parent_id IN (" + productIds + ") OR product_id IN (" + productIds + ")) " +
                        "AND isActive = 1"; // Added check for 'is_active'

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

    ObservableList<String> allSupplierNames = supplierDAO.getAllSupplierNames();

    public void addProductToTableForGeneralReceive(String PO_NO, PurchaseOrder generalReceivePO) {
        String supplierName = supplierDAO.getSupplierNameById(generalReceivePO.getSupplierName());

        if (supplierName == null) {
            TextFieldUtils.setComboBoxBehavior(supplier);

            supplier.setItems(allSupplierNames);
            supplier.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    int supplierId = supplierDAO.getSupplierIdByName(newValue);
                    loadProductsPerSupplier(supplierId);
                    generalReceivePO.setSupplierName(supplierId);
                }
            });
            ComboBoxFilterUtil.setupComboBoxFilter(supplier, allSupplierNames);
        } else {
            supplier.setDisable(true);
            supplier.setValue(supplierName);
            loadProductsPerSupplier(generalReceivePO.getSupplierName());
        }

        productsPerSupplier.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Product selectedProduct = row.getItem();
                    addSelectedProductToGeneralReceive(selectedProduct, PO_NO);
                }
            });
            return row;
        });

        productsPerSupplier.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                Product selectedProduct = productsPerSupplier.getSelectionModel().getSelectedItem();
                addSelectedProductToGeneralReceive(selectedProduct, PO_NO);
            }
        });

    }

    private void addSelectedProductToGeneralReceive(Product selectedProduct, String PO_NO) {
        if (selectedProduct != null) {
            ConfirmationAlert confirmationAlert = new ConfirmationAlert("Confirmation", "Add " + selectedProduct.getDescription(), "Are you sure you want to add this product?", false);
            boolean confirmed = confirmationAlert.showAndWait();
            if (confirmed) {
                ProductsInTransact productsInTransact = setProductProfileForGeneralReceive(selectedProduct, PO_NO);
                receivingIOperationsController.addProductToReceivingTable(productsInTransact);
                productsPerSupplier.getItems().remove(selectedProduct);
            }
        }
    }

    private static ProductsInTransact setProductProfileForGeneralReceive(Product selectedProduct, String PO_NO) {
        ProductsInTransact productsInTransact = new ProductsInTransact();
        productsInTransact.setProductId(selectedProduct.getProductId());
        productsInTransact.setDescription(selectedProduct.getDescription());
        productsInTransact.setUnit(selectedProduct.getUnitOfMeasurementString());
        productsInTransact.setUnitPrice(selectedProduct.getPricePerUnit());
        productsInTransact.setOrderId(Integer.parseInt(PO_NO));
        return productsInTransact;
    }

    private void loadProductsAndSetInventoryQuantities(SalesOrder salesOrder) {
        int supplierId = salesOrder.getSupplierId();
        Task<ObservableList<Product>> fetchProductsTask = createFetchProductsTask(supplierId);

        fetchProductsTask.setOnSucceeded(event -> {
            ObservableList<Product> products = fetchProductsTask.getValue();
            productsPerSupplier.setItems(products);
            setInventoryQuantitiesOfProductsPerSupplier(salesOrder);
        });

        fetchProductsTask.setOnFailed(event -> {
            LOGGER.log(Level.SEVERE, "Failed to load products for supplier", fetchProductsTask.getException());
        });

        // Start the task on a background thread
        new Thread(fetchProductsTask).start();
    }


    private void loadProductsPerSupplier(int supplierId) {
        Task<ObservableList<Product>> fetchProductsTask = createFetchProductsTask(supplierId);

        // Create a ProgressIndicator and set it as the placeholder
        ProgressIndicator progressIndicator = new ProgressIndicator();
        productsPerSupplier.setPlaceholder(progressIndicator);

        fetchProductsTask.setOnSucceeded(event -> {
            ObservableList<Product> products = fetchProductsTask.getValue();
            productsPerSupplier.setItems(products);
            if (products.isEmpty()) {
                productsPerSupplier.setPlaceholder(new Label("No products found."));
            }
        });

        fetchProductsTask.setOnFailed(event -> {
            LOGGER.log(Level.SEVERE, "Failed to load products for supplier", fetchProductsTask.getException());
            productsPerSupplier.setPlaceholder(new Label("Failed to load products."));
        });

        // Start the task on a background thread
        new Thread(fetchProductsTask).start();
    }


    ReceivingIOperationsController receivingIOperationsController;

    void setTargetController(ReceivingIOperationsController receivingIOperationsController) {
        this.receivingIOperationsController = receivingIOperationsController;
    }

    SalesOrderEntryController salesOrderIOperationsController;

    public void setSalesController(SalesOrderEntryController salesOrderIOperationsController) {
        this.salesOrderIOperationsController = salesOrderIOperationsController;
    }

    InventoryDAO inventoryDAO = new InventoryDAO();
    BranchDAO branchDAO = new BranchDAO();

    public void addProductToTableForSalesOrder(SalesOrder salesOrder) {
        String supplierName = supplierDAO.getSupplierNameById(salesOrder.getSupplierId());
        String branchName = branchDAO.getBranchNameById(salesOrder.getSourceBranchId());
        branchBox.setVisible(true);

        ObservableList<String> branches = inventoryDAO.getBranchNamesWithInventory();
        branch.setItems(branches);

        TableColumn<Product, Integer> productQuantityColumn = new TableColumn<>("Quantity");
        productQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<Product, Integer> productReservedQuantityColumn = new TableColumn<>("Reserved Quantity");
        productReservedQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("reservedQuantity"));

        productsPerSupplier.getColumns().addAll(productQuantityColumn, productReservedQuantityColumn);

        if (supplierName == null) {
            supplier.setItems(allSupplierNames);
            supplier.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    int supplierId = supplierDAO.getSupplierIdByName(newValue);
                    salesOrder.setSupplierId(supplierId);
                    loadProductsAndSetInventoryQuantities(salesOrder);
                }
            });
            ComboBoxFilterUtil.setupComboBoxFilter(supplier, allSupplierNames);
        } else {
            supplier.setDisable(true);
            supplier.setValue(supplierName);
            Platform.runLater(() -> loadProductsAndSetInventoryQuantities(salesOrder));
        }

        if (branchName == null) {
            branch.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    int branchId = branchDAO.getBranchIdByName(newValue);
                    salesOrder.setSourceBranchId(branchId);
                    setInventoryQuantitiesOfProductsPerSupplier(salesOrder);
                }
            });
            ComboBoxFilterUtil.setupComboBoxFilter(branch, branches);
        } else {
            branch.setDisable(true);
            branch.setValue(branchName);
            setInventoryQuantitiesOfProductsPerSupplier(salesOrder);
        }
        productsPerSupplier.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Product selectedProduct = row.getItem();
                    addSelectedProductToSalesOrder(selectedProduct, salesOrder);
                }
            });
            return row;
        });
        productsPerSupplier.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                Product selectedProduct = productsPerSupplier.getSelectionModel().getSelectedItem();
                addSelectedProductToSalesOrder(selectedProduct, salesOrder);
            }
        });
    }

    private void setInventoryQuantitiesOfProductsPerSupplier(SalesOrder salesOrder) {
        int branchId = salesOrder.getSourceBranchId();
        ObservableList<Product> products = productsPerSupplier.getItems();

        products.forEach(product -> {
            product.setQuantity(0);
            product.setReservedQuantity(0);
        });

        List<CompletableFuture<Void>> futures = products.stream().map(product ->
                CompletableFuture.runAsync(() -> {
                    int productId = product.getProductId();
                    String inventoryQuery = "SELECT * FROM inventory WHERE product_id = ? AND branch_id = ?";

                    try (Connection connection = dataSource.getConnection();
                         PreparedStatement inventoryStatement = connection.prepareStatement(inventoryQuery)) {

                        inventoryStatement.setInt(1, productId);
                        inventoryStatement.setInt(2, branchId);

                        try (ResultSet inventoryResultSet = inventoryStatement.executeQuery()) {
                            if (inventoryResultSet.next()) {
                                int quantity = inventoryResultSet.getInt("quantity");
                                int reservedQuantity = inventoryResultSet.getInt("reserved_quantity");
                                product.setQuantity(quantity);
                                product.setReservedQuantity(reservedQuantity);
                            }
                        }
                    } catch (SQLException e) {
                        LOGGER.log(Level.SEVERE, "Failed to fetch inventory for product " + productId, e);
                    }
                }, executorService)
        ).toList();

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        allFutures.thenRun(() -> {
            products.sort(Comparator.comparingInt(Product::getQuantity).reversed());

            Platform.runLater(() -> productsPerSupplier.refresh());
        }).exceptionally(ex -> {
            // Log the exception if any of the tasks fail
            LOGGER.log(Level.SEVERE, "Failed to fetch inventory quantities", ex);
            return null;
        });
    }




    private void addSelectedProductToSalesOrder(Product selectedProduct, SalesOrder salesOrder) {
        if (selectedProduct != null) {
            ConfirmationAlert confirmationAlert = new ConfirmationAlert("Confirmation", "Add " + selectedProduct.getDescription(), "Are you sure you want to add this product?", false);
            boolean confirmed = confirmationAlert.showAndWait();
            if (confirmed) {
                ProductsInTransact productsInTransact = setProductProfileForSalesOrder(selectedProduct, salesOrder);
                salesOrderIOperationsController.addProductToSalesOrderTable(productsInTransact);
                productsPerSupplier.getItems().remove(selectedProduct);
            }
        }
    }

    private static ProductsInTransact setProductProfileForSalesOrder(Product selectedProduct, SalesOrder salesOrder) {
        ProductsInTransact productsInTransact = new ProductsInTransact();
        productsInTransact.setProductId(selectedProduct.getProductId());
        productsInTransact.setDescription(selectedProduct.getDescription());
        productsInTransact.setUnit(selectedProduct.getUnitOfMeasurementString());
        productsInTransact.setUnitPrice(selectedProduct.getPricePerUnit());
        productsInTransact.setOrderId(Integer.parseInt(salesOrder.getOrderID()));
        productsInTransact.setInventoryQuantity(selectedProduct.getQuantity());
        productsInTransact.setReservedQuantity(selectedProduct.getReservedQuantity());
        return productsInTransact;
    }

    private void createTableColumns() {
        TableColumn<Product, String> productDescriptionColumn = new TableColumn<>("Product Description");
        productDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Product, String> productUnitColumn = new TableColumn<>("Product Unit");
        productUnitColumn.setCellValueFactory(new PropertyValueFactory<>("unitOfMeasurementString"));

        productsPerSupplier.getColumns().addAll(productDescriptionColumn, productUnitColumn);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        productsPerSupplier.setPlaceholder(progressIndicator);
        createTableColumns();
    }
}
