package com.vertex.vos;

import com.vertex.vos.Objects.Brand;
import com.vertex.vos.Objects.Category;
import com.vertex.vos.Objects.Product;
import com.vertex.vos.Utilities.*;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.textfield.TextFields;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

public class ProductListController implements Initializable {

    public Label sizeLabel;
    public BorderPane borderPane;
    public Label lastUpdated;
    public Button searchButton;
    @FXML
    private Button addButton;

    @FXML
    private TableColumn<Product, String> barcodeCol;

    @FXML
    private TextField barcodeFilter;

    @FXML
    private TableColumn<Product, String> brandCol;

    @FXML
    private TextField brandFilter;

    @FXML
    private TableColumn<Product, String> categoryCol;

    @FXML
    private TextField categoryFilter;

    @FXML
    private TableColumn<Product, Date> dateAddedCol;

    @FXML
    private TableColumn<Product, String> productCodeCol;

    @FXML
    private ImageView productImage;

    @FXML
    private Label productName;

    @FXML
    private TableColumn<Product, String> productNameCol;

    @FXML
    private TextField productNameFilter;

    @FXML
    private Label productPrice;

    @FXML
    private TableView<Product> productTableView;

    @FXML
    private Button updateButton;
    ProductDAO productDAO = new ProductDAO();

    BrandDAO brandDAO = new BrandDAO();
    CategoriesDAO categoriesDAO = new CategoriesDAO();
    ObservableList<Product> products = FXCollections.observableArrayList();


    ObservableList<Category> categories = FXCollections.observableArrayList(); // Observable list for categories
    ObservableList<Brand> brands = FXCollections.observableArrayList(); // >

    private int offset = 0; // Keep track of loaded items
    private final int PAGE_SIZE = 35; //
    private final StringBuilder barcode = new StringBuilder();
    private Timer barcodeTimer;
    private static final int BARCODE_TIMEOUT = 300; // Time in milliseconds
    Pane productCardPane;

    Product selectedProduct;


    public void loadProductList() {
        loadMoreProducts();

        productTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedProduct = newValue;
                borderPane.setRight(productCardPane);
                productName.setText(selectedProduct.getProductName());
                productPrice.setText(String.valueOf(selectedProduct.getPricePerUnit()));
                lastUpdated.setText(String.valueOf(selectedProduct.getLastUpdated()));
                loadImage(selectedProduct);

                updateButton.setOnAction(event -> {
                    openProductDetails(selectedProduct);
                });

                productTableView.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        openProductDetails(selectedProduct);
                    }
                });
                productTableView.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        openProductDetails(selectedProduct);
                    }
                });

            } else {
                borderPane.setRight(null);
            }
        });

        addButton.setOnAction(event -> {
            addNewProduct();
        });

        searchButton.setOnAction(event -> {
            performSearch(productNameFilter.getText(), brandFilter.getText(), categoryFilter.getText(), barcodeFilter.getText());
        });

    }

    private void addNewProduct() {
        if (productDetailsStage == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("registerProduct.fxml"));
                Parent content = loader.load();

                RegisterProductController controller = loader.getController();
                controller.addNewParentProduct();

                productDetailsStage = new Stage();
                productDetailsStage.setTitle("Product Registration");
                productDetailsStage.setScene(new Scene(content));
                productDetailsStage.setMaximized(true);
                productDetailsStage.showAndWait();
                productDetailsStage.setOnCloseRequest(event -> productDetailsStage = null);

            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error loading registerProduct.fxml: " + e.getMessage());
            }
        } else {
            if (productDetailsStage.isShowing()) {
                errorUtilities.shakeWindow(productDetailsStage);
            } else {
                productDetailsStage = null;
            }
        }
    }

    Stage productDetailsStage;
    ErrorUtilities errorUtilities = new ErrorUtilities();

    private void openProductDetails(Product product) {
        Platform.runLater(() -> {
            if (productDetailsStage != null) {
                if (productDetailsStage.isShowing()) {
                    errorUtilities.shakeWindow(productDetailsStage);
                } else {
                    productDetailsStage.show();
                }
            } else {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("registerProduct.fxml"));
                    Parent root = loader.load();
                    RegisterProductController controller = loader.getController();
                    controller.initData(product.getProductId());
                    controller.setProductListController(this);
                    productDetailsStage = new Stage();
                    productDetailsStage.setMaximized(true);
                    productDetailsStage.setTitle("Product Details");
                    productDetailsStage.setScene(new Scene(root));
                    productDetailsStage.setOnCloseRequest(event -> productDetailsStage = null);
                    productDetailsStage.show();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        });
    }

    private void loadImage(Product product) {
        String imageUrl = product.getProductImage();
        Image placeholderImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/package.png")));
        if (imageUrl != null) {
            productImage.setImage(placeholderImage);
            Task<Image> imageLoadTask = new Task<>() {
                @Override
                protected Image call() {
                    try {
                        return new Image(new File(imageUrl).toURI().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return placeholderImage;
                    }
                }

                @Override
                protected void succeeded() {
                    productImage.setImage(getValue());
                }

                @Override
                protected void failed() {
                    productImage.setImage(placeholderImage);
                }
            };
            new Thread(imageLoadTask).start();
        } else {
            productImage.setImage(placeholderImage);
        }
    }

    private void performSearch(String productName, String brand, String category, String barcode) {
        isLoading = true;
        products.clear(); // Clear previous results
        offset = 0; // Reset pagination offset
        sizeLabel.setText("Searching...");
        productTableView.setPlaceholder(new ProgressIndicator());

        Task<ObservableList<Product>> task = new Task<>() {
            @Override
            protected ObservableList<Product> call() {
                try {
                    // Perform search with the given filters
                    ObservableList<Product> searchResults = productDAO.searchParentProducts(
                            productName.isEmpty() ? null : productName, // Handle empty filter
                            getBrandId(brand.isEmpty() ? null : brand), // Handle empty filter
                            getCategoryId(category.isEmpty() ? null : category), // Handle empty filter
                            barcode.isEmpty() ? null : barcode, // Handle empty filter
                            PAGE_SIZE,
                            offset
                    );

                    // Sort search results by closest match to productName
                    if (!productName.isEmpty()) {
                        productTableView.getItems().sort(Comparator.comparingInt(p -> StringUtils.getLevenshteinDistance(p.getProductName(), productName)));
                    }
                    return searchResults;
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e; // Re-throw the exception to trigger onFailed
                }
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                ObservableList<Product> taskProducts = task.getValue();
                if (taskProducts.isEmpty()) {
                    sizeLabel.setText("No products found.");
                    productTableView.setPlaceholder(new Label("No products found."));
                } else {
                    products.addAll(taskProducts); // Add new results
                    offset += taskProducts.size(); // Update offset
                    sizeLabel.setText("Showing " + products.size() + " results.");
                }
                isLoading = false;
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                task.getException().printStackTrace();
                sizeLabel.setText("Error occurred during search.");
                isLoading = false;
            });
        });

        new Thread(task).start();
    }

    private int getBrandId(String brandName) {
        if (brandName == null || brandName.isEmpty()) {
            return -1;
        }
        return brands.stream()
                .filter(b -> b.getBrand_name().equalsIgnoreCase(brandName))
                .map(Brand::getBrand_id)
                .findFirst()
                .orElse(-1);
    }

    private int getCategoryId(String categoryName) {
        if (categoryName == null || categoryName.isEmpty()) {
            return -1;
        }
        return categories.stream()
                .filter(c -> c.getCategoryName().equalsIgnoreCase(categoryName))
                .map(Category::getCategoryId)
                .findFirst()
                .orElse(-1);
    }

    private boolean isLoading = false;

    void loadMoreProducts() {
        if (isLoading) return;
        isLoading = true;
        sizeLabel.setText("Loading more...");
        productTableView.setPlaceholder(new ProgressIndicator());

        String productName = productNameFilter.getText();
        String brand = brandFilter.getText();
        String category = categoryFilter.getText();
        String barcode = barcodeFilter.getText();

        Task<ObservableList<Product>> task = new Task<>() {
            @Override
            protected ObservableList<Product> call() {
                return productDAO.searchParentProducts(productName, getBrandId(brand), getCategoryId(category), barcode, PAGE_SIZE, offset);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                ObservableList<Product> taskProducts = task.getValue();
                if (taskProducts.isEmpty()) {
                    sizeLabel.setText("No more products.");
                } else {
                    products.addAll(taskProducts);
                    offset += taskProducts.size();
                    sizeLabel.setText("Showing " + products.size() + " products.");
                }
                isLoading = false;
            });
        });

        task.setOnFailed(event -> {
            task.getException().printStackTrace();
            isLoading = false;
        });

        new Thread(task).start();
    }

    List<String> productNames = new ArrayList<>();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        borderPane.addEventFilter(KeyEvent.KEY_PRESSED, this::handleBarcodeInput);

        products = FXCollections.observableArrayList();
        productTableView.setItems(products);
        productNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductName()));
        productCodeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductCode()));
        brandCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductBrandString()));
        categoryCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductCategoryString()));
        dateAddedCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDateAdded()));
        barcodeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBarcode()));

        categories.setAll(categoriesDAO.getAllCategories());
        brands.setAll(brandDAO.getAllBrands());
        productNames = productDAO.getAllProductNames();
        TextFields.bindAutoCompletion(brandFilter, brands.stream().map(Brand::getBrand_name).toList());
        TextFields.bindAutoCompletion(categoryFilter, categories.stream().map(Category::getCategoryName).toList());
        TextFields.bindAutoCompletion(productNameFilter, productNames);

        productTableView.setOnScroll(event -> {
            if (isScrollNearBottom()) {
                loadMoreProducts();
            }
        });

        Platform.runLater(() -> {
            if (borderPane != null) {
                productCardPane = (Pane) borderPane.getRight();
                borderPane.setRight(null);
            }
        });
    }

    private void handleBarcodeInput(KeyEvent event) {
        if (event.getText().isEmpty()) return;
        barcode.append(event.getText());

        if (barcodeTimer != null) {
            barcodeTimer.cancel();
        }

        barcodeTimer = new Timer();
        barcodeTimer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    processBarcode(barcode.toString());
                    barcode.setLength(0);
                });
            }
        }, BARCODE_TIMEOUT);

    }

    private void processBarcode(String barcode) {
        Product product = productDAO.getProductByBarcode(barcode);
        if (product != null) {
            openProductDetails(product);
        }
        else {
            DialogUtils.showErrorMessage("Error", "Product not found for barcode: " + barcode);
        }
    }

    private boolean isScrollNearBottom() {
        ScrollBar scrollBar = (ScrollBar) productTableView.lookup(".scroll-bar:vertical");
        if (scrollBar != null) {
            double value = scrollBar.getValue();
            double max = scrollBar.getMax();
            double visibleAmount = scrollBar.getVisibleAmount();
            return value >= max - visibleAmount;
        }
        return false;
    }
}
