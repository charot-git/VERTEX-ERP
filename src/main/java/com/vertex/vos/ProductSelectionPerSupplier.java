package com.vertex.vos;

import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
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
    public TextField productDescriptionTextField;
    public HBox selectionBox;

    @FXML
    private Label businessTypeLabel;
    @FXML
    private TableView<Product> productTableView;
    @FXML
    public ComboBox<String> supplier;
    @FXML
    private VBox supplierBox;
    @FXML
    private Label supplierErr;

    private Task<ObservableList<Product>> createFetchProductsTaskPerSupplier(int supplierId) {
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

    InventoryDAO inventoryDAO = new InventoryDAO();


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

        productTableView.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Product selectedProduct = row.getItem();
                    addSelectedProductToGeneralReceive(selectedProduct, PO_NO);
                }
            });
            return row;
        });

        productTableView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                Product selectedProduct = productTableView.getSelectionModel().getSelectedItem();
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
                productTableView.getItems().remove(selectedProduct);
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





    private void loadProductsPerSupplier(int supplierId) {
        Task<ObservableList<Product>> fetchProductsTask = createFetchProductsTaskPerSupplier(supplierId);

        // Create a ProgressIndicator and set it as the placeholder
        ProgressIndicator progressIndicator = new ProgressIndicator();
        productTableView.setPlaceholder(progressIndicator);

        fetchProductsTask.setOnSucceeded(event -> {
            ObservableList<Product> products = fetchProductsTask.getValue();
            productTableView.setItems(products);
            if (products.isEmpty()) {
                productTableView.setPlaceholder(new Label("No products found."));
            }
        });

        fetchProductsTask.setOnFailed(event -> {
            LOGGER.log(Level.SEVERE, "Failed to load products for supplier", fetchProductsTask.getException());
            productTableView.setPlaceholder(new Label("Failed to load products."));
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

    BranchDAO branchDAO = new BranchDAO();

    public void addProductToTableForSalesOrder(SalesOrder salesOrder, ObservableList<ProductsInTransact> existingProducts) {
        selectionBox.getChildren().remove(supplierBox);

        String branchName = branchDAO.getBranchNameById(salesOrder.getSourceBranchId());
        branchBox.setVisible(true);

        ObservableList<String> branches = inventoryDAO.getBranchNamesWithInventory();
        branch.setItems(branches);

        TableColumn<Product, Integer> productQuantityColumn = new TableColumn<>("Quantity");
        productQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<Product, Integer> productReservedQuantityColumn = new TableColumn<>("Reserved Quantity");
        productReservedQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("reservedQuantity"));

        productTableView.getColumns().addAll(productQuantityColumn, productReservedQuantityColumn);

        if (branchName == null) {
            branch.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    int branchId = branchDAO.getBranchIdByName(newValue);
                    salesOrder.setSourceBranchId(branchId);
                    loadProductsPerBranch(salesOrder, existingProducts);
                }
            });
            ComboBoxFilterUtil.setupComboBoxFilter(branch, branches);
        } else {
            branch.setDisable(true);
            branch.setValue(branchName);
            loadProductsPerBranch(salesOrder, existingProducts);
        }

        productTableView.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Product selectedProduct = row.getItem();
                    addSelectedProductToSalesOrder(selectedProduct, salesOrder);
                }
            });
            return row;
        });

        productTableView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                Product selectedProduct = productTableView.getSelectionModel().getSelectedItem();
                addSelectedProductToSalesOrder(selectedProduct, salesOrder);
            }
        });
    }

    private void loadProductsPerBranch(SalesOrder salesOrder, ObservableList<ProductsInTransact> existingProducts) {
        // Show a loading placeholder while data is being fetched
        productTableView.setPlaceholder(new ProgressIndicator());

        // Create a task for loading products in the background
        Task<ObservableList<Product>> task = new Task<>() {
            @Override
            protected ObservableList<Product> call() throws Exception {
                // Load all inventory items by branch in a single query
                ObservableList<Inventory> itemsByBranch = inventoryDAO.getInventoryItemsByBranch(salesOrder.getSourceBranchId());

                // Filter out products that are already in the existingProducts list
                Set<Integer> existingProductIds = existingProducts.stream()
                        .map(ProductsInTransact::getProductId)
                        .collect(Collectors.toSet());
                itemsByBranch.removeIf(item -> existingProductIds.contains(item.getProductId()));

                // If there are no items left after filtering, return an empty list early
                if (itemsByBranch.isEmpty()) {
                    return FXCollections.emptyObservableList();
                }

                // Fetch products in a single query for all remaining inventory items
                List<Integer> productIds = itemsByBranch.stream()
                        .map(Inventory::getProductId)
                        .collect(Collectors.toList());

                ProductDAO productDAO = new ProductDAO();
                ObservableList<Product> products = productDAO.getProductsByIds(productIds);

                // Map quantities from inventory to products
                Map<Integer, Inventory> inventoryMap = itemsByBranch.stream()
                        .collect(Collectors.toMap(Inventory::getProductId, item -> item));

                for (Product product : products) {
                    Inventory inventory = inventoryMap.get(product.getProductId());
                    product.setQuantity(inventory.getQuantity());
                    product.setReservedQuantity(inventory.getReservedQuantity());
                }

                return products;
            }
        };

        // When the task is complete, update the TableView
        task.setOnSucceeded(event -> {
            productTableView.setItems(task.getValue());
        });

        // Handle any errors that occur during the background task
        task.setOnFailed(event -> {
            productTableView.setPlaceholder(new Label("Failed to load products"));
            Throwable exception = task.getException();
            DialogUtils.showErrorMessage("Error", "Failed to load products: " + exception.getMessage());
        });

        // Run the task in a background thread
        Thread thread = new Thread(task);
        thread.setDaemon(true);  // Allows the thread to exit when the application exits
        thread.start();
    }





    private void addSelectedProductToSalesOrder(Product selectedProduct, SalesOrder salesOrder) {
        if (selectedProduct != null) {
            // Check if the available quantity is sufficient
            int availableQuantity = selectedProduct.getQuantity() - selectedProduct.getReservedQuantity();
            if (availableQuantity <= 0) {
                DialogUtils.showErrorMessage("Stock Error", "Product " + selectedProduct.getDescription() + " is out of stock.");
                return; // Exit the method if the product is out of stock
            }
            ProductsInTransact productsInTransact = setProductProfileForSalesOrder(selectedProduct, salesOrder);
            salesOrderIOperationsController.addProductToSalesOrderTable(productsInTransact);
            productTableView.getItems().remove(selectedProduct);
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

        productTableView.getColumns().addAll(productDescriptionColumn, productUnitColumn);
    }



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        productTableView.setPlaceholder(progressIndicator);
        createTableColumns();

        //filter by product description when user types in productDescriptionTextField
        productDescriptionTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            handleDescriptionSearch(newValue);
        });

    }

    private void handleDescriptionSearch(String searchText) {
        Comparator<Product> comparator = Comparator.comparing(product ->
                product.getDescription().toLowerCase().indexOf(searchText.toLowerCase())
        );
        productTableView.getItems().sort(comparator.reversed());
    }
}
