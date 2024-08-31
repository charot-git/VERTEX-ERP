package com.vertex.vos;

import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
    public ComboBox<String> unitComboBox;
    public ComboBox<String> brandComboBox;

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

        // Fetch dependent entities in a batch rather than multiple calls
        int brandId = resultSet.getInt("product_brand");
        int categoryId = resultSet.getInt("product_category");
        int segmentId = resultSet.getInt("product_segment");
        int sectionId = resultSet.getInt("product_section");
        int unitId = resultSet.getInt("unit_of_measurement");

        // Use DAOs to get names
        product.setProductBrandString(brandDAO.getBrandNameById(brandId));
        product.setProductCategoryString(categoriesDAO.getCategoryNameById(categoryId));
        product.setProductSegmentString(segmentDAO.getSegmentNameById(segmentId));
        product.setProductSectionString(sectionsDAO.getSectionNameById(sectionId));
        product.setUnitOfMeasurementString(unitDAO.getUnitNameById(unitId));

        product.setPricePerUnit(resultSet.getDouble("price_per_unit"));
        product.setCostPerUnit(resultSet.getDouble("cost_per_unit"));
        product.setParentId(resultSet.getInt("parent_id"));
        product.setProductId(resultSet.getInt("product_id"));

        return product;
    }


    ObservableList<String> allSupplierNames = supplierDAO.getAllSupplierNames();

    public void addProductForStockIn(PurchaseOrder order) {
        filteredProductList = new FilteredList<>(originalProductList, p -> true);
        productTableView.setItems(filteredProductList);

        String supplierName = supplierDAO.getSupplierNameById(order.getSupplierName());

        if (supplierName == null) {
            TextFieldUtils.setComboBoxBehavior(supplier);
            supplier.setItems(allSupplierNames);
            supplier.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    int supplierId = supplierDAO.getSupplierIdByName(newValue);
                    loadProductsPerSupplier(supplierId);
                    order.setSupplierName(supplierId);
                }
            });
            ComboBoxFilterUtil.setupComboBoxFilter(supplier, allSupplierNames);
        } else {
            supplier.setDisable(true);
            supplier.setValue(supplierName);
            loadProductsPerSupplier(order.getSupplierName());
        }

        productTableView.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Product selectedProduct = row.getItem();
                    addProductToTable(selectedProduct, order);
                }
            });
            return row;
        });

        productTableView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                Product selectedProduct = productTableView.getSelectionModel().getSelectedItem();
                addProductToTable(selectedProduct, order);
            }
            if (event.isControlDown() && event.getCode() == KeyCode.A) {
                addAllProductsToTable(order);
            }
        });


    }

    private void addAllProductsToTable(PurchaseOrder order) {
        ObservableList<Product> items = productTableView.getItems();

        // If the list is empty, there is no need to proceed.
        if (items.isEmpty()) {
            return;
        }

        String confirmationMessage = String.format("Add %d products", items.size());
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Confirmation",
                confirmationMessage,
                "Are you sure you want to add all the products in the list?",
                false);

        if (confirmationAlert.showAndWait()) {
            // Create a copy of the items list to iterate over safely.
            List<Product> productsCopy = new ArrayList<>(items);

            for (Product product : productsCopy) {
                ProductsInTransact productsInTransact = setProductProfileForStockIn(product, String.valueOf(order.getPurchaseOrderNo()));

                switch (order.getPriceType()) {
                    case "General Receive Price":
                        receivingIOperationsController.addProductToReceivingTable(productsInTransact);
                        break;
                    case "Cost Per Unit":
                        purchaseOrderEntryController.addProductToBranchTables(productsInTransact);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown Price Type: " + order.getPriceType());
                }

                originalProductList.remove(product);
            }
        }
    }


    private void addProductToTable(Product selectedProduct, PurchaseOrder purchaseOrder) {
        if (selectedProduct != null) {
            ConfirmationAlert confirmationAlert = new ConfirmationAlert("Confirmation",
                    "Add " + selectedProduct.getDescription(),
                    "Are you sure you want to add this product?",
                    false);
            boolean confirmed = confirmationAlert.showAndWait();
            if (confirmed) {
                ProductsInTransact productsInTransact = setProductProfileForStockIn(selectedProduct, String.valueOf(purchaseOrder.getPurchaseOrderNo()));
                if (purchaseOrder.getPriceType().equals("General Receive Price")) {
                    receivingIOperationsController.addProductToReceivingTable(productsInTransact);
                } else if (purchaseOrder.getPriceType().equals("Cost Per Unit")) {
                    purchaseOrderEntryController.addProductToBranchTables(productsInTransact);
                }

                originalProductList.remove(selectedProduct);
            }
        }
    }


    private static ProductsInTransact setProductProfileForStockIn(Product selectedProduct, String PO_NO) {
        ProductsInTransact productsInTransact = new ProductsInTransact();
        productsInTransact.setProductId(selectedProduct.getProductId());
        productsInTransact.setDescription(selectedProduct.getDescription());
        productsInTransact.setUnit(selectedProduct.getUnitOfMeasurementString());
        productsInTransact.setUnitPrice(selectedProduct.getCostPerUnit());
        productsInTransact.setOrderId(Integer.parseInt(PO_NO));
        productsInTransact.setProductCategoryString(selectedProduct.getProductCategoryString());
        productsInTransact.setProductBrandString(selectedProduct.getProductBrandString());
        return productsInTransact;
    }


    private void loadProductsPerSupplier(int supplierId) {
        Task<ObservableList<Product>> fetchProductsTask = createFetchProductsTaskPerSupplier(supplierId);

        // Create a ProgressIndicator and set it as the placeholder
        ProgressIndicator progressIndicator = new ProgressIndicator();
        productTableView.setPlaceholder(progressIndicator);

        fetchProductsTask.setOnSucceeded(event -> {
            ObservableList<Product> products = fetchProductsTask.getValue();
            originalProductList.setAll(products);
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
        TableColumn<Product, String> productBrandColumn = new TableColumn<>("Brand");
        productBrandColumn.setCellValueFactory(new PropertyValueFactory<>("productBrandString"));

        TableColumn<Product, String> productDescriptionColumn = new TableColumn<>("Product Description");
        productDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Product, String> productUnitColumn = new TableColumn<>("Product Unit");
        productUnitColumn.setCellValueFactory(new PropertyValueFactory<>("unitOfMeasurementString"));

        productTableView.getColumns().addAll(productBrandColumn, productDescriptionColumn, productUnitColumn);
    }

    private ObservableList<Product> originalProductList = FXCollections.observableArrayList();
    private FilteredList<Product> filteredProductList;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> units = unitDAO.getUnitNames();
        unitComboBox.setItems(units);
        ObservableList<String> brands = brandDAO.getBrandNames();
        brandComboBox.setItems(brands);

        ComboBoxFilterUtil.setupComboBoxFilter(unitComboBox, units);
        ComboBoxFilterUtil.setupComboBoxFilter(brandComboBox, brands);


        ProgressIndicator progressIndicator = new ProgressIndicator();
        productTableView.setPlaceholder(progressIndicator);
        createTableColumns();

        //filter by product description when user types in productDescriptionTextField
        productDescriptionTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters(filteredProductList);
        });

        unitComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters(filteredProductList);
        });

        brandComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters(filteredProductList);
        });


    }

    private void applyFilters(FilteredList<Product> filteredProductList) {
        String filterText = productDescriptionTextField.getText().toLowerCase();
        String selectedUnit = unitComboBox.getValue();
        String selectedBrand = brandComboBox.getValue();

        filteredProductList.setPredicate(product -> {
            // Filter by description
            boolean matchesDescription = filterText == null || filterText.isEmpty() ||
                    product.getProductName().toLowerCase().contains(filterText) ||
                    product.getDescription().toLowerCase().contains(filterText);

            // Filter by unit
            boolean matchesUnit = selectedUnit == null || selectedUnit.isEmpty() ||
                    selectedUnit.equals(product.getUnitOfMeasurementString());

            boolean matchesBrand = selectedBrand == null || selectedBrand.isEmpty() ||
                    selectedBrand.equals(product.getProductBrandString());

            // Return true if both filters match
            return matchesDescription && matchesUnit && matchesBrand;
        });
    }


    PurchaseOrderEntryController purchaseOrderEntryController;

    public void setPOController(PurchaseOrderEntryController purchaseOrderEntryController) {
        this.purchaseOrderEntryController = purchaseOrderEntryController;
    }
}
