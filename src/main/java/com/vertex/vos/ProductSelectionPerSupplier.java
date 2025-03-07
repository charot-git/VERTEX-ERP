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
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    InventoryDAO inventoryDAO = new InventoryDAO();


    ObservableList<String> allSupplierNames = supplierDAO.getAllSupplierNames();

    public void addProductForSalesReturn(SalesReturn salesReturn) {

        setupSupplierComboBox();

        productTableView.setItems(productListForStockIn);

        addProductToTableForSalesReturn(salesReturn);

    }

    private void addProductToTableForSalesReturn(SalesReturn salesReturn) {
        productTableView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                addProductToSalesReturn(productTableView.getSelectionModel().getSelectedItem(), salesReturn);
            }

        });
    }

    private void addProductToSalesReturn(Product selectedProduct, SalesReturn salesReturn) {

        if (selectedProduct != null) {
            SalesReturnDetail salesReturnDetail = setProductProfileForSalesReturn(selectedProduct, salesReturn);
            salesReturnFormController.addProductToSalesReturnDetail(salesReturnDetail);
            productListForStockIn.remove(selectedProduct);
        }

    }

    private SalesReturnDetail setProductProfileForSalesReturn(Product selectedProduct, SalesReturn salesReturn) {
        SalesReturnDetail salesReturnDetail = new SalesReturnDetail();
        salesReturnDetail.setSalesReturn(salesReturn);
        salesReturnDetail.setProductId(selectedProduct.getProductId());
        salesReturnDetail.setQuantity(0);
        salesReturnDetail.setProduct(selectedProduct);
        salesReturnDetail.setUnitPrice(selectedProduct.getPricePerUnit());
        return salesReturnDetail;
    }

    public void addProductForStockIn(PurchaseOrder purchaseOrder) {
        productTableView.setItems(productListForStockIn);
        String supplierName = supplierDAO.getSupplierNameById(purchaseOrder.getSupplierName());

        if (supplierName == null) {
            setupSupplierComboBox();
        } else {
            setupSupplier(purchaseOrder);
        }

        setUpProductTableViewForPurchaseOrders(purchaseOrder);
    }

    private void setupSupplierComboBox() {
        TextFieldUtils.setComboBoxBehavior(supplier);
        supplier.setItems(allSupplierNames);
        supplier.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                int supplierId = supplierDAO.getSupplierIdByName(newValue);
                loadProductsPerSupplier(supplierId);
                searchingSetUp(supplierId);
            }
        });
        ComboBoxFilterUtil.setupComboBoxFilter(supplier, allSupplierNames);
    }

    private void setupSupplier(PurchaseOrder purchaseOrder) {
        supplier.setDisable(true);
        supplier.setValue(supplierDAO.getSupplierNameById(purchaseOrder.getSupplierName()));
        loadProductsPerSupplier(purchaseOrder.getSupplierName());
        searchingSetUp(purchaseOrder.getSupplierName());
    }

    private void setUpProductTableViewForPurchaseOrders(PurchaseOrder purchaseOrder) {
        productTableView.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    addProductToTable(row.getItem(), purchaseOrder);
                }
            });
            return row;
        });

        productTableView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                addProductToTable(productTableView.getSelectionModel().getSelectedItem(), purchaseOrder);
            }
            if (event.isControlDown() && event.getCode() == KeyCode.A) {
                addAllProductsToTable(purchaseOrder);
            }
        });
    }

    private void searchingSetUp(int supplierId) {
        productDescriptionTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) {
                productDescriptionTextField.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        handleDescriptionSearch(supplierId, productDescriptionTextField.getText(), brandComboBox.getSelectionModel().getSelectedItem(), unitComboBox.getSelectionModel().getSelectedItem());
                    }
                });
            } else {
                loadMoreProducts(supplierId);
            }
        });

        brandComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                handleDescriptionSearch(supplierId, productDescriptionTextField.getText(), newValue, unitComboBox.getSelectionModel().getSelectedItem());
            }
        });

        unitComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                handleDescriptionSearch(supplierId, productDescriptionTextField.getText(), brandComboBox.getSelectionModel().getSelectedItem(), newValue);
            }
        });
    }


    private void handleDescriptionSearch(int supplierId, String text, String brand, String unit) {
        Task<ObservableList<Product>> searchTask = productsPerSupplierDAO.searchProductTask(supplierId, text, brand, unit);
        searchTask.setOnSucceeded(event -> {
            ObservableList<Product> products = searchTask.getValue();
            productListForStockIn.setAll(products);
        });
        searchTask.setOnFailed(event -> {
            searchTask.getException().printStackTrace();
        });
        new Thread(searchTask).start();
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

                productListForStockIn.remove(product);
            }
        }
    }


    private void addProductToTable(Product selectedProduct, PurchaseOrder purchaseOrder) {
        if (selectedProduct != null) {
            ProductsInTransact productsInTransact = setProductProfileForStockIn(selectedProduct, String.valueOf(purchaseOrder.getPurchaseOrderNo()));
            if (purchaseOrder.getPriceType().equals("General Receive Price")) {
                receivingIOperationsController.addProductToReceivingTable(productsInTransact);
            } else if (purchaseOrder.getPriceType().equals("Cost Per Unit")) {
                purchaseOrderEntryController.addProductToBranchTables(productsInTransact);
            }
            productListForStockIn.remove(selectedProduct);
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

    ObservableList<Product> productListForStockIn = FXCollections.observableArrayList();
    private boolean isLoading = false; // To prevent concurrent loading

    private void loadProductsPerSupplier(int supplierId) {
        productTableView.setItems(productListForStockIn);

        // Set initial placeholder
        ProgressIndicator progressIndicator = new ProgressIndicator();
        productTableView.setPlaceholder(progressIndicator);

        loadMoreProducts(supplierId);

        // Load more products when the user scrolls to the bottom
        productTableView.setOnScroll(event -> {
            if (isScrollNearBottom() && !isLoading) {
                loadMoreProducts(supplierId);
            }
        });
    }

    private boolean isScrollNearBottom() {
        ScrollBar scrollBar = (ScrollBar) productTableView.lookup(".scroll-bar:vertical");
        return scrollBar != null && scrollBar.getValue() >= scrollBar.getMax() - scrollBar.getVisibleAmount();
    }

    private void loadMoreProducts(int supplierId) {
        if (isLoading) {
            return; // Don't load next if already loading
        }

        isLoading = true;
        Task<ObservableList<Product>> task = productsPerSupplierDAO.getPaginatedProductsForSupplier(supplierId);
        task.setOnSucceeded(event -> {
            ObservableList<Product> products = task.getValue();
            productListForStockIn.addAll(products);
            isLoading = false; // Reset flag after loading is complete
        });
        task.setOnFailed(event -> {
            task.getException().printStackTrace();
            isLoading = false; // Reset flag after loading fails
        });

        new Thread(task).start();
    }


    ReceivingIOperationsController receivingIOperationsController;

    void setTargetController(ReceivingIOperationsController receivingIOperationsController) {
        this.receivingIOperationsController = receivingIOperationsController;
    }


    BranchDAO branchDAO = new BranchDAO();


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

    SalesReturnFormController salesReturnFormController;

    public void setSalesReturnController(SalesReturnFormController salesReturnFormController) {
        this.salesReturnFormController = salesReturnFormController;
    }
}
