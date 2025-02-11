package com.vertex.vos;

import com.vertex.vos.DAO.ProductPerCustomerDAO;
import com.vertex.vos.DAO.ProductSelectionTempDAO;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.BrandDAO;
import com.vertex.vos.Utilities.CategoriesDAO;
import com.vertex.vos.Utilities.DialogUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductSelectionController implements Initializable {

    @FXML
    private Button addAll;

    @FXML
    private TableColumn<Product, String> brandCol;

    @FXML
    private TextField brandFilter;

    @FXML
    private TableColumn<Product, String> categoryCol;

    @FXML
    private TextField categoryFilter;

    @FXML
    private TableColumn<Product, String> codeCol;

    @FXML
    private TableColumn<Product, String> nameCol;

    @FXML
    private TextField productNameFilter;

    @FXML
    private TableView<Product> productTable;

    @Setter
    private Stage productSelectionStage;

    private final ProductSelectionTempDAO productDAO = new ProductSelectionTempDAO();
    private final BrandDAO brandDAO = new BrandDAO();
    private final CategoriesDAO categoriesDAO = new CategoriesDAO();

    private final ObservableList<Product> productObservableList = FXCollections.observableArrayList();
    private int currentOffset = 0;
    private final int rowsPerPage = 20;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    DiscountType discountType = new DiscountType(24);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            setupTableColumns();
            setupFilters();
            setupInfiniteScroll();

            // Load initial data asynchronously
            loadMoreProducts();

            productTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            addAll.setOnAction(event -> {
                List<Product> selectedProducts = productTable.getSelectionModel().getSelectedItems();
                if (selectedProducts.isEmpty()) {
                    DialogUtils.showErrorMessage("Error", "Please select at least one product to add.");
                } else {
                    addSelectedProductsToCustomer(selectedProducts);
                }
            });
        });

    }

    private final ProductPerCustomerDAO productPerCustomerDAO = new ProductPerCustomerDAO();

    private void addSelectedProductsToCustomer(List<Product> selectedProducts) {
        CompletableFuture.runAsync(() -> {
            // Collect products and their children in a thread-safe way
            List<Product> productsWithChildren = new ArrayList<>();
            for (Product product : selectedProducts) {
                productsWithChildren.add(product);
                productsWithChildren.addAll(productDAO.getProductChildren(product, passedCustomer));
            }

            // Perform batch insertion
            productPerCustomerDAO.addProductsForCustomer(passedCustomer, productsWithChildren, discountType);
        }, executor).thenRun(() ->
                Platform.runLater(() -> {
                    DialogUtils.showCompletionDialog("Success", "Products added successfully!");
                    customerRegistrationController.loadCustomerProducts(passedCustomer);
                })
        ).exceptionally(ex -> {
            // Handle errors
            Platform.runLater(() -> DialogUtils.showErrorMessage("Error", "Failed to add products: " + ex.getMessage()));
            return null;
        });
    }


    private void setupTableColumns() {
        brandCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductBrandString()));
        categoryCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductCategoryString()));
        codeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductCode()));
        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductName()));
    }

    private void setupFilters() {
        ObservableList<Brand> brandList = brandDAO.getBrandDetails();
        ObservableList<Category> categoryList = categoriesDAO.getCategoryDetails();

        ObservableList<String> brandNames = FXCollections.observableArrayList();
        ObservableList<String> categoryNames = FXCollections.observableArrayList();

        for (Brand brand : brandList) {
            brandNames.add(brand.getBrand_name());
        }

        for (Category category : categoryList) {
            categoryNames.add(category.getCategoryName());
        }

        TextFields.bindAutoCompletion(brandFilter, brandNames);
        TextFields.bindAutoCompletion(categoryFilter, categoryNames);

        // Listen for filter changes
        brandFilter.textProperty().addListener((observable, oldValue, newValue) -> resetAndLoadProducts());
        categoryFilter.textProperty().addListener((observable, oldValue, newValue) -> resetAndLoadProducts());
        productNameFilter.textProperty().addListener((observable, oldValue, newValue) -> resetAndLoadProducts());
    }

    private void setupInfiniteScroll() {
        productTable.setPlaceholder(new ProgressIndicator());

        productTable.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                ScrollBar scrollBar = (ScrollBar) productTable.lookup(".scroll-bar:vertical");
                if (scrollBar != null) {
                    scrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue.doubleValue() == 1.0) { // Bottom of the table
                            loadMoreProducts();
                        }
                    });
                }
            }
        });
    }

    private void resetAndLoadProducts() {
        currentOffset = 0;
        productObservableList.clear();
        loadMoreProducts();
    }

    private void loadMoreProducts() {
        String brandFilterText = brandFilter.getText();
        String categoryFilterText = categoryFilter.getText();
        String productNameFilterText = productNameFilter.getText();

        productTable.setPlaceholder(new ProgressIndicator());

        CompletableFuture.supplyAsync(() ->
            productDAO.getFilteredParentProducts(
                brandFilterText,
                categoryFilterText,
                productNameFilterText,
                rowsPerPage,
                currentOffset,
                passedCustomer,
                customerRegistrationController.productListTableView.getItems()),
                executor)
            .thenAcceptAsync(moreProducts -> {
                if (!moreProducts.isEmpty()) {
                    productObservableList.addAll(moreProducts);
                    currentOffset += rowsPerPage;
                }
                if (productTable.getItems() != productObservableList) {
                    productTable.setItems(productObservableList);
                }
            }, Platform::runLater)
            .exceptionally(ex -> {
                Platform.runLater(() ->
                    DialogUtils.showErrorMessage("Error", "Failed to load products: " + ex.getMessage()));
                return null;
            });
    }

    private CustomerRegistrationController customerRegistrationController;

    public void setCustomerController(CustomerRegistrationController customerRegistrationController) {
        this.customerRegistrationController = customerRegistrationController;
    }

    @Setter
    Customer passedCustomer;
}
