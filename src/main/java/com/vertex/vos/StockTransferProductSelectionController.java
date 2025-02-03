package com.vertex.vos;

import com.dlsc.formsfx.view.controls.SimpleIntegerControl;
import com.vertex.vos.DAO.StockTransferProductSelectionDAO;
import com.vertex.vos.Objects.*;
import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;
import javafx.concurrent.Task;
import javafx.application.Platform;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class StockTransferProductSelectionController implements Initializable {

    @FXML
    private Button addAllButton;

    @FXML
    private TableView<ProductsInTransact> availableProductsTableView;

    @FXML
    private TableColumn<ProductsInTransact, Integer> availableQuantityCol;

    @FXML
    private TableColumn<ProductsInTransact, String> brandCol;

    @FXML
    private TextField brandFilter;

    @FXML
    private TableColumn<ProductsInTransact, String> categoryCol;

    @FXML
    private TextField categoryFilter;

    @FXML
    private TableColumn<ProductsInTransact, String> codeCol;

    @FXML
    private TableColumn<ProductsInTransact, String> nameCol;

    @FXML
    private TextField productNameFilter;

    @FXML
    private TableColumn<ProductsInTransact, String> unitCol;

    @FXML
    private TextField unitFilter;

    private final ProgressIndicator progressIndicator = new ProgressIndicator(); // The ProgressIndicator to show while loading

    @Setter
    StockTransferController stockTransferController;

    private final StockTransferProductSelectionDAO stockTransferProductSelectionDAO = new StockTransferProductSelectionDAO();

    private boolean isLoading = false; // Flag to indicate whether data is loading
    private static final int DEBOUNCE_DELAY_MS = 300; // Delay in milliseconds (e.g., 300ms)
    private PauseTransition filterPauseTransition; // To handle debouncing


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initially hide the progress indicator
        availableProductsTableView.setPlaceholder(null); // Hide the progress indicator after task completion
        categoryCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductCategoryString()));
        brandCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductBrandString()));
        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductName()));
        codeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductCode()));
        availableQuantityCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getReceivedQuantity()).asObject());
        unitCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getUnitOfMeasurementString()));

        //add enter listener to availableProductsTableView
        availableProductsTableView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                addSelectedProduct();
            }
        });
    }

    private void addSelectedProduct() {
        ProductsInTransact selectedProduct = availableProductsTableView.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            stockTransferController.addProductToBranchTables(selectedProduct.getProduct().getProductId());
        }
    }

    public void loadData(int sourceBranchId) {
        List<Category> categories = stockTransferProductSelectionDAO.getCategoriesWithInventory(sourceBranchId);
        List<Brand> brands = stockTransferProductSelectionDAO.getBrandsWithInventory(sourceBranchId);
        List<String> products = stockTransferProductSelectionDAO.getProductNamesWithInventory(sourceBranchId);
        List<Unit> units = stockTransferProductSelectionDAO.getUnits();
        filterPauseTransition = new PauseTransition(Duration.millis(DEBOUNCE_DELAY_MS));
        filterPauseTransition.setOnFinished(event -> resetAndFilterProducts(sourceBranchId));

        bindAutoCompletion(categories, brands, products, units);
        addFilterListeners(sourceBranchId);

        // Handle scrolling for pagination
        availableProductsTableView.setOnScroll(event -> {
            if (event.getTextDeltaY() < 0 && isScrollNearBottom() && !isLoading) { // Check if scrolling down and at the bottom
                currentPage++; // Move to the next page
                filterProductsAsync(sourceBranchId); // Load next page
            }
        });
    }

    private void bindAutoCompletion(List<Category> categories, List<Brand> brands, List<String> products, List<Unit> units) {
        TextFields.bindAutoCompletion(categoryFilter, categories.stream().map(Category::getCategoryName).toList());
        TextFields.bindAutoCompletion(brandFilter, brands.stream().map(Brand::getBrand_name).toList());
        TextFields.bindAutoCompletion(productNameFilter, products);
        TextFields.bindAutoCompletion(unitFilter, units.stream().map(Unit::getUnit_name).toList());
    }

    private void addFilterListeners(int sourceBranchId) {
        // Consolidate listener logic into one method
        categoryFilter.textProperty().addListener((observable, oldValue, newValue) -> restartFilterDelay());
        brandFilter.textProperty().addListener((observable, oldValue, newValue) -> restartFilterDelay());
        productNameFilter.textProperty().addListener((observable, oldValue, newValue) -> restartFilterDelay());
        unitFilter.textProperty().addListener((observable, oldValue, newValue) -> restartFilterDelay());
    }

    private static final int PAGE_SIZE = 20; // Number of products to load per page
    private int currentPage = 0; // Keep track of the current page


    private void resetAndFilterProducts(int sourceBranchId) {
        currentPage = 0;  // Reset page to 0 for new filter

        availableProductsTableView.getItems().clear(); // Clear existing items
        filterProductsAsync(sourceBranchId); // Reload filtered products
    }

    private void restartFilterDelay() {
        if (filterPauseTransition != null && filterPauseTransition.getStatus() == PauseTransition.Status.RUNNING) {
            filterPauseTransition.stop();  // Stop the previous delay if it's still running
        }
        assert filterPauseTransition != null;
        filterPauseTransition.playFromStart();  // Start the delay from the beginning
    }

    private void filterProductsAsync(int sourceBranchId) {
        if (isLoading) {
            return; // Avoid starting another filter task if one is already running
        }

        isLoading = true; // Indicate that data is being loaded
        availableProductsTableView.setPlaceholder(progressIndicator); // Show loading spinner

        // Create a new task to filter products in the background
        Task<List<ProductsInTransact>> filterTask = new Task<List<ProductsInTransact>>() {
            @Override
            protected List<ProductsInTransact> call() throws Exception {
                return stockTransferProductSelectionDAO.getFilteredProducts(
                        sourceBranchId,
                        categoryFilter.getText(),
                        brandFilter.getText(),
                        productNameFilter.getText(),
                        unitFilter.getText(),
                        PAGE_SIZE,
                        currentPage * PAGE_SIZE // Calculate the offset based on page size
                );
            }
        };

        // On success, update the table with the filtered results
        filterTask.setOnSucceeded(event -> {
            List<ProductsInTransact> productsInTransacts = filterTask.getValue();
            if (currentPage == 0) {
                // If it's the first page, replace the existing items
                availableProductsTableView.setItems(FXCollections.observableArrayList(productsInTransacts));
            } else {
                // If it's not the first page, add the new items to the existing list
                availableProductsTableView.getItems().addAll(productsInTransacts);
            }

            availableProductsTableView.setPlaceholder(null); // Hide progress indicator
            isLoading = false; // Reset loading flag
        });

        // On failure, handle the error and reset loading state
        filterTask.setOnFailed(event -> {
            Throwable exception = filterTask.getException();
            exception.printStackTrace(); // Log the error
            availableProductsTableView.setPlaceholder(null); // Hide progress indicator
            isLoading = false; // Reset loading flag
        });

        // Start the filtering task on a background thread
        new Thread(filterTask).start();
    }

    private boolean isScrollNearBottom() {
        // Retrieve the vertical ScrollBar from the TableView
        ScrollBar scrollBar = (ScrollBar) availableProductsTableView.lookup(".scroll-bar:vertical");

        // Ensure the ScrollBar is found and check if we're near the bottom
        return scrollBar != null && scrollBar.getValue() >= scrollBar.getMax() - scrollBar.getVisibleAmount();
    }


}
