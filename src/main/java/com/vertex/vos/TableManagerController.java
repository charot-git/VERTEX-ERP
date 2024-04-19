package com.vertex.vos;

import com.vertex.vos.Constructors.*;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.lang3.RandomStringUtils;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TableManagerController implements Initializable {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    private final HistoryManager historyManager = new HistoryManager();
    private int currentNavigationId = -1;

    private PurchaseOrderEntryController purchaseOrderEntryController;
    @FXML
    private AnchorPane tableAnchor;
    @FXML
    private VBox contentManager;
    private TilePane tilePane = new TilePane();

    public void setPurchaseOrderEntryController(PurchaseOrderEntryController purchaseOrderEntryController) {
        this.purchaseOrderEntryController = purchaseOrderEntryController;
    }

    private final SupplierDAO supplierDAO = new SupplierDAO();
    BrandDAO brandDAO = new BrandDAO();
    DiscountDAO discountDAO = new DiscountDAO();
    CategoriesDAO categoriesDAO = new CategoriesDAO();
    SegmentDAO segmentDAO = new SegmentDAO();
    SectionsDAO sectionsDAO = new SectionsDAO();
    ProductsPerSupplierDAO productsPerSupplierDAO = new ProductsPerSupplierDAO();
    private String registrationType;
    @FXML
    private ProgressIndicator loadingSpinner;
    @FXML
    private TextField searchBar;
    @FXML
    private TextField categoryBar;

    private AnchorPane contentPane; // Declare contentPane variable

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    public void setRegistrationType(String registrationType) {
        this.registrationType = registrationType;
    }


    private ObservableList<Map<String, String>> brandData;
    private final ObservableList<Map<String, String>> classData = FXCollections.observableArrayList();
    private final ObservableList<Map<String, String>> segmentData = FXCollections.observableArrayList();
    private final ObservableList<Map<String, String>> categoryData = FXCollections.observableArrayList();
    private final ObservableList<Map<String, String>> natureData = FXCollections.observableArrayList();
    private final ObservableList<Map<String, String>> sectionData = FXCollections.observableArrayList();
    private final ObservableList<Map<String, String>> unitData = FXCollections.observableArrayList();

    @FXML
    private ImageView tableImg, addImage;
    @FXML
    private Label tableHeader;
    @FXML
    private TableView defaultTable;
    @FXML
    private TableColumn column1;
    @FXML
    private Label columnHeader1;
    @FXML
    private TableColumn column2;
    @FXML
    private Label columnHeader2;
    @FXML
    private TableColumn column3;
    @FXML
    private Label columnHeader3;
    @FXML
    private TableColumn column4;
    @FXML
    private Label columnHeader4;
    @FXML
    private TableColumn column5;
    @FXML
    private Label columnHeader5;
    @FXML
    private TableColumn column6;
    @FXML
    private Label columnHeader6;
    @FXML
    private TableColumn column7;
    @FXML
    private Label columnHeader7;
    @FXML
    private TableColumn column8;
    @FXML
    private Label columnHeader8;

    private List<Product> productsFromSupplier = new ArrayList<>();
    private List<Product> selectedProduct = new ArrayList<>();


    public void loadSupplierProductsTable(int supplierId, ObservableList<ProductsInTransact> selectedProducts) {
        addImage.setVisible(false);

        List<Product> filteredProducts = new ArrayList<>(productsFromSupplier);
        for (ProductsInTransact selectedProduct : selectedProducts) {
            filteredProducts.removeIf(product -> product.getProductId() == selectedProduct.getProductId());
        }

        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/package.png")));
        searchBar.setVisible(true);
        categoryBar.setVisible(true);
        searchBar.setPromptText("Search product description");
        categoryBar.setPromptText("Search specifics");

        PauseTransition pause = getPauseTransition();

        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            pause.playFromStart(); // Restart the pause timer on every text change
        });

        categoryBar.textProperty().addListener((observable, oldValue, newValue) -> {
            pause.playFromStart(); // Restart the pause timer on category text change
        });


        tableImg.setImage(image);
        columnHeader1.setText("Product Name");
        columnHeader2.setText("Product Code");
        columnHeader3.setText("Description");
        columnHeader4.setText("Product Image");
        columnHeader5.setText("Brand");
        columnHeader6.setText("Category");
        columnHeader7.setText("Segment");
        columnHeader8.setText("Section");

        defaultTable.setRowFactory(tv -> new TableRow<Product>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle(""); // Set default style if the row is empty
                } else {
                    if (item.getParentId() == 0) {
                        // Apply a different background color to rows with parent_id = 0
                        setStyle("-fx-background-color: #5A90CF;");
                    } else {
                        // Set default background for other rows
                        setStyle("");
                    }
                }
            }
        });

        column1.setCellValueFactory(new PropertyValueFactory<>("productName"));
        column2.setCellValueFactory(new PropertyValueFactory<>("productCode"));
        column3.setCellValueFactory(new PropertyValueFactory<>("description"));
        column4.setCellValueFactory(new PropertyValueFactory<>("productImage"));
        column4.setCellFactory(param -> new TableCell<Product, String>() {
            private final ImageView imageView = new ImageView();

            {
                ImageCircle.cicular(imageView);
                imageView.setFitHeight(50);
                imageView.setFitWidth(50);
                setGraphic(imageView);
                setContentDisplay(ContentDisplay.CENTER);

            }

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty || imagePath == null) {
                    imageView.setImage(null);
                } else {
                    // Convert imagePath to Image and set it to the ImageView
                    Image image = new Image(new File(imagePath).toURI().toString());
                    imageView.setImage(image);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        column5.setCellValueFactory(new PropertyValueFactory<>("productBrandString"));
        column6.setCellValueFactory(new PropertyValueFactory<>("productCategoryString"));
        column7.setCellValueFactory(new PropertyValueFactory<>("productSegmentString"));
        column8.setCellValueFactory(new PropertyValueFactory<>("productSectionString"));

        productsFromSupplier = fetchProductsForSupplier(supplierId);

        populateProductsPerSupplierTable(filteredProducts);
        populateProductsPerSupplierTable(productsFromSupplier);
        defaultTable.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Product rowData = row.getItem();
                    ConfirmationAlert confirmationAlert = new ConfirmationAlert("Add product to PO", "Add this product to the PO?",
                            "You are adding " + rowData.getDescription() + " to the purchase");

                    boolean userConfirmed = confirmationAlert.showAndWait();
                    if (userConfirmed) {
                        int productId = rowData.getProductId();
                        purchaseOrderEntryController.addProductToBranchTables(productId);
                        selectedProduct.add(rowData);
                        productsFromSupplier.remove(rowData);
                        populateProductsPerSupplierTable(productsFromSupplier);
                    } else {
                        DialogUtils.showErrorMessage("Cancelled", "You have cancelled adding " + rowData.getDescription() + " to your PO");
                    }
                }
            });
            return row;
        });

    }

    private PauseTransition getPauseTransition() {
        PauseTransition pause = new PauseTransition(Duration.millis(300));
        pause.setOnFinished(event -> {
            String searchText = searchBar.getText().toLowerCase();
            String categoryFilter = categoryBar.getText().toLowerCase(); // Assuming categoryBar is a TextField

            List<Product> filteredProducts = productsFromSupplier.stream()
                    .filter(product ->
                            (searchText.isEmpty() || product.getDescription().toLowerCase().contains(searchText)) &&
                                    (categoryFilter.isEmpty() || matchesCategoryCriteria(product, categoryFilter)))
                    .collect(Collectors.toList());

            populateProductsPerSupplierTable(filteredProducts);
        });
        return pause;
    }

    private boolean matchesCategoryCriteria(Product product, String categoryFilter) {
        String brandString = product.getProductBrandString().toLowerCase();
        String categoryString = product.getProductCategoryString().toLowerCase();
        String segmentString = product.getProductSegmentString().toLowerCase();
        String sectionString = product.getProductSectionString().toLowerCase();

        return brandString.contains(categoryFilter) ||
                categoryString.contains(categoryFilter) ||
                segmentString.contains(categoryFilter) ||
                sectionString.contains(categoryFilter);
    }

    private List<Product> fetchProductsForSupplier(int supplierId) {
        List<Product> products = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            List<Integer> supplierProducts = productsPerSupplierDAO.getProductsForSupplier(supplierId);

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
                    product.setParentId(resultSet.getInt("parent_id"));
                    product.setProductId(resultSet.getInt("product_id"));
                    products.add(product);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception according to your needs
        }

        return products;
    }

    private void populateProductsPerSupplierTable(List<Product> products) {
        defaultTable.getItems().clear();
        defaultTable.getItems().addAll(products);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        defaultTable.setVisible(false);
        tilePane.setHgap(10);
        tilePane.setVgap(10);
        tilePane.setPadding(new Insets(10));
        Platform.runLater(() -> {

            if (!registrationType.contains("employee")) {
                Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Add.png"));
                addImage.setImage(image);
            }

            switch (registrationType) {
                case "company" -> loadCompanyTable();
                case "branch" -> loadBranchTable();
                case "employee" -> loadEmployeeTable();
                case "product" -> loadProductTable();
                case "product_supplier" -> tableHeader.setText("Add a product to supplier");
                case "supplier" -> loadSupplierTable();
                case "system_employee" -> loadSystemEmployeeTable();
                case "industry" -> loadIndustryTable();
                case "division" -> loadDivisionTable();
                case "department" -> loadDepartmentTable();
                case "category" -> loadCategoryTable();
                case "customer" -> loadCustomerTable();
                case "brand" -> loadBrandTable();
                case "segment" -> loadSegmentTable();
                case "delivery_terms" -> loadDeliveryTerms();
                case "payment_terms" -> loadPaymentTerms();
                case "class" -> loadClassTable();
                case "nature" -> loadNatureTable();
                case "section" -> loadSectionTable();
                case "unit" -> loadUnitTable();
                case "chart_of_accounts" -> loadChartOfAccountsTable();
                case "purchase_order_products" -> tableHeader.setText("Select products");
                case "branch_selection_po" -> loadBranchForPOTable();
                case "discount_type" -> loadDiscountTypeTable();
                case "line_discount" -> loadLineDiscountTable();
                case "assets_and_equipments" -> loadAssetsAndEquipmentTable();
                case "salesman" -> loadSalesmanTable();
                case "sales_order" -> loadSalesOrders();
                default -> tableHeader.setText("Unknown Type");
            }
            defaultTable.setVisible(true);
        });

        defaultTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Check for double-click
                if (registrationType.equals("product_supplier")) {
                    System.out.println(registrationType);
                } else if (registrationType.equals("purchase_order_products")) {
                    System.out.println(registrationType);
                } else {
                    handleTableDoubleClick(defaultTable.getSelectionModel().getSelectedItem());
                }
            }
        });
    }

    SalesDAO salesDAO = new SalesDAO();

    private void loadSalesOrders() {
        tableHeader.setText("Sales Orders");
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Create Order.png"));
        tableImg.setImage(image);

        defaultTable.getColumns().clear();

        // Define your table columns
        TableColumn<SalesOrder, String> orderIdColumn = new TableColumn<>("Order ID");
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));

        TableColumn<SalesOrder, String> customerNameColumn = new TableColumn<>("Customer Name");
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));

        TableColumn<SalesOrder, String> storeNameColumn = new TableColumn<>("Store Name");
        storeNameColumn.setCellValueFactory(new PropertyValueFactory<>("storeName"));

        TableColumn<SalesOrder, String> salesManColumn = new TableColumn<>("Sales Man");
        salesManColumn.setCellValueFactory(new PropertyValueFactory<>("salesMan"));

        TableColumn<SalesOrder, String> createdDateColumn = new TableColumn<>("Created Date");
        createdDateColumn.setCellValueFactory(new PropertyValueFactory<>("createdDate"));

        TableColumn<SalesOrder, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("soStatus"));

        defaultTable.getColumns().addAll(orderIdColumn, customerNameColumn, storeNameColumn, salesManColumn, createdDateColumn, statusColumn);

        // Fetch data from the database using DAO
        ObservableList<SalesOrder> salesOrdersList = FXCollections.observableArrayList();

        try {
            List<SalesOrder> salesOrders = salesDAO.getAllSalesOrders();
            salesOrdersList.addAll(salesOrders);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        defaultTable.setItems(salesOrdersList);

        // Add event listener for table selection
        defaultTable.setRowFactory(tv -> {
            TableRow<SalesOrder> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    SalesOrder selectedOrder = row.getItem();
                    String orderId = selectedOrder.getOrderId();
                    openFormWithOrderId(orderId);
                }
            });
            return row;
        });

    }

    // Method to open another form with the selected order ID
    private void openFormWithOrderId(String orderId) {

    }


    private void loadSalesmanTable() {
    }

    CustomerDAO customerDAO = new CustomerDAO();

    private void loadCustomerTable() {
        tableHeader.setText("Customers");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Customer.png")));
        tableImg.setImage(image);

        defaultTable.getColumns().clear();

        // Define your table columns
        TableColumn<Customer, String> storeNameColumn = new TableColumn<>("Store Name");
        storeNameColumn.setCellValueFactory(new PropertyValueFactory<>("storeName"));

        TableColumn<Customer, String> signageNameColumn = new TableColumn<>("Signage Name");
        signageNameColumn.setCellValueFactory(new PropertyValueFactory<>("storeSignage"));

        TableColumn<Customer, String> provinceColumn = new TableColumn<>("Province");
        provinceColumn.setCellValueFactory(new PropertyValueFactory<>("province"));

        TableColumn<Customer, String> cityColumn = new TableColumn<>("City");
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));

        TableColumn<Customer, String> brgyColumn = new TableColumn<>("Barangay");
        brgyColumn.setCellValueFactory(new PropertyValueFactory<>("brgy"));

        TableColumn<Customer, String> customerImageColumn = new TableColumn<>("Customer Image");
        customerImageColumn.setCellValueFactory(new PropertyValueFactory<>("customerImage"));

        defaultTable.getColumns().addAll(storeNameColumn, signageNameColumn, provinceColumn, cityColumn, brgyColumn, customerImageColumn);

        // Fetch data from the database using DAO
        ObservableList<Customer> customersList = FXCollections.observableArrayList();

        List<Customer> customers = customerDAO.getAllCustomers();

        customersList.addAll(customers);

        // Populate the table with the fetched data
        defaultTable.setItems(customersList);
    }

    private final AssetsAndEquipmentDAO assetsAndEquipmentDAO = new AssetsAndEquipmentDAO();

    public void loadAssetsAndEquipmentTable() {
        tableHeader.setText("Assets And Equipments");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/assets.png")));
        tableImg.setImage(image);

        defaultTable.getColumns().clear();

        // Define your table columns
        TableColumn<AssetsAndEquipment, String> itemNameColumn = new TableColumn<>("Item Name");
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));

        TableColumn<AssetsAndEquipment, ImageView> itemImageColumn = new TableColumn<>("Item Image");
        itemImageColumn.setCellValueFactory(new PropertyValueFactory<>("itemImage"));

        TableColumn<AssetsAndEquipment, Integer> quantityColumn = new TableColumn<>("Quantity");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<AssetsAndEquipment, String> departmentColumn = new TableColumn<>("Department");
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));

        TableColumn<AssetsAndEquipment, String> assigneeColumn = new TableColumn<>("Assignee");
        assigneeColumn.setCellValueFactory(new PropertyValueFactory<>("employee"));

        TableColumn<AssetsAndEquipment, Double> totalColumn = new TableColumn<>("Total");

        defaultTable.getColumns().addAll(itemNameColumn, itemImageColumn, departmentColumn, quantityColumn, assigneeColumn, totalColumn);

        // Fetch data from the database using DAO
        ObservableList<AssetsAndEquipment> assetsList = FXCollections.observableArrayList();

        List<AssetsAndEquipment> assets = assetsAndEquipmentDAO.getAllAssetsAndEquipment();

        assetsList.addAll(assets);

        // Populate the table with the fetched data
        defaultTable.setItems(assetsList);
    }

    private void loadDiscountTypeTable() {
        tableHeader.setText("Discount Types");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Line Discount.png")));
        tableImg.setImage(image);
        contentManager.getChildren().remove(defaultTable);
        contentManager.getChildren().add(tilePane);

        List<DiscountType> discountTypeList = null;
        try {
            discountTypeList = discountDAO.getAllDiscountTypes();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for (DiscountType discountType : discountTypeList) {
            String typeName = discountType.getTypeName(); // Get the type name
            VBox tile = createTile(typeName);
            tile.setOnMouseClicked(mouseEvent -> openDiscountLink(typeName));
            tilePane.getChildren().add(tile);
        }
    }


    private void openDiscountLink(String discountType) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("DiscountType.fxml"));
            Parent root = loader.load();

            // Access the controller of the loaded FXML file if needed
            DiscountTypeController controller = loader.getController();
            controller.setDiscountType(discountType);

            Scene scene = new Scene(root);
            Stage newStage = new Stage();
            newStage.setResizable(false);
            newStage.setTitle(discountType);
            newStage.setScene(scene);
            newStage.show();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
    }

    private void loadLineDiscountTable() {
        tableHeader.setText("Line Discount");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Discount Type.png")));
        tableImg.setImage(image);
        defaultTable.setBackground(Background.fill(Color.TRANSPARENT));
        contentManager.getChildren().remove(defaultTable);
        contentManager.getChildren().add(tilePane);

        List<LineDiscount> lineDiscountsList = null;
        try {
            lineDiscountsList = discountDAO.getAllLineDiscounts();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for (LineDiscount lineDiscount : lineDiscountsList) {
            String discountInfo = lineDiscount.getLineDiscount() + " - " + lineDiscount.getPercentage() + "%";
            VBox tile = createTile(discountInfo);
            tilePane.getChildren().add(tile);
        }
    }

    private Consumer<Event> discountChangeEventConsumer;

    public void setDiscountChangeEventConsumer(Consumer<Event> consumer) {
        this.discountChangeEventConsumer = consumer;
    }

    public void loadLineDiscountTableForLink(String discountName) throws SQLException {
        tableHeader.setText("Select line discount for " + discountName);
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Discount Type.png")));
        tableImg.setImage(image);
        defaultTable.setBackground(Background.fill(Color.TRANSPARENT));
        contentManager.getChildren().remove(defaultTable);
        contentManager.getChildren().add(tilePane);

        List<LineDiscount> lineDiscountsList = null;
        try {
            lineDiscountsList = discountDAO.getAllLineDiscounts();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for (LineDiscount lineDiscount : lineDiscountsList) {
            String discountInfo = lineDiscount.getLineDiscount() + " - " + lineDiscount.getPercentage() + "%";
            VBox tile = createTile(discountInfo);
            int discountId = lineDiscount.getId();
            int discountTypeId = discountDAO.getDiscountTypeIdByName(discountName);


            if (!discountDAO.isLineDiscountLinkedWithType(discountId, discountTypeId)) {
                tile.setOnMouseClicked(event -> {
                    try {
                        if (!discountDAO.isLineDiscountLinkedWithType(discountId, discountTypeId)) {
                            boolean registered = discountDAO.linkLineDiscountWithType(discountId, discountTypeId);
                            if (registered) {
                                DialogUtils.showConfirmationDialog("Link Success", discountInfo + " successfully linked to " + discountName);
                                discountChangeEventConsumer.accept(new DiscountChangeEvent());
                            } else {
                                DialogUtils.showErrorMessage("Failed", "Linking failed for " + discountInfo);
                            }
                        } else {
                            DialogUtils.showErrorMessage("Error", "Line discount already linked to the discount type");
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
                tilePane.getChildren().add(tile);
            }
        }

    }

    @FXML
    private void loadContent(String fxmlFileName, String registrationType) {
        System.out.println("Loading content: " + fxmlFileName + " for registration type: " + registrationType); // Debug statement
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Parent content = loader.load();

            // Set the controller for the loaded FXML file
            if (fxmlFileName.equals("tableManager.fxml")) {
                TableManagerController controller = loader.getController();
                controller.setRegistrationType(registrationType);
                controller.setContentPane(contentPane);
            }
            String sessionId = UserSession.getInstance().getSessionId();
            currentNavigationId = historyManager.addEntry(sessionId, fxmlFileName);

            ContentManager.setContent(contentPane, content); // Assuming contentPane is your AnchorPane
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading " + fxmlFileName + ": " + e.getMessage());
        }
    }


    private static final String LABEL_STYLE = "-fx-font-size: 14px;\n" +
            "    -fx-text-fill: #3E4756;\n" +
            "    -fx-font-weight: 500;";

    private VBox createTile(String tileContent) {
        VBox tile = new VBox(); // Create a new VBox
        tile.setPrefSize(100, 50);
        tile.setPadding(new Insets(5));
        tile.setBackground(new Background(new BackgroundFill(Color.valueOf("#f0f0f0"), new CornerRadii(10), Insets.EMPTY)));
        Label label = new Label(tileContent);
        label.setStyle(LABEL_STYLE);
        tile.getChildren().add(label);
        new HoverAnimation(tile);
        return tile;
    }


    public void loadProductParentsTable(String supplierName) {
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/package.png")));

        tableImg.setImage(image);
        columnHeader1.setText("Product Name");
        columnHeader2.setText("Product Code");
        columnHeader3.setText("Description");
        columnHeader4.setText("Product Image");
        columnHeader5.setText("Brand");
        columnHeader6.setText("Category");
        columnHeader7.setText("Segment");
        columnHeader8.setText("Section");


        SupplierDAO supplierDAO = new SupplierDAO();
        BrandDAO brandDAO = new BrandDAO();
        CategoriesDAO categoriesDAO = new CategoriesDAO();
        SegmentDAO segmentDAO = new SegmentDAO();
        SectionsDAO sectionsDAO = new SectionsDAO();

        defaultTable.setRowFactory(tv -> new TableRow<Product>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle(""); // Set default style if the row is empty
                } else {
                    if (item.getParentId() == 0) {
                        // Apply a different background color to rows with parent_id = 0
                        setStyle("-fx-background-color: #5A90CF;");
                    } else {
                        // Set default background for other rows
                        setStyle("");
                    }
                }
            }
        });

        column1.setCellValueFactory(new PropertyValueFactory<>("productName"));
        column2.setCellValueFactory(new PropertyValueFactory<>("productCode"));
        column3.setCellValueFactory(new PropertyValueFactory<>("description"));
        column4.setCellValueFactory(new PropertyValueFactory<>("productImage"));


        column4.setCellFactory(param -> new TableCell<Product, String>() {
            private final ImageView imageView = new ImageView();

            {
                ImageCircle.cicular(imageView);
                imageView.setFitHeight(50);
                imageView.setFitWidth(50);
                setGraphic(imageView);
                setContentDisplay(ContentDisplay.CENTER);

            }

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty || imagePath == null) {
                    imageView.setImage(null);
                } else {
                    // Convert imagePath to Image and set it to the ImageView
                    Image image = new Image(new File(imagePath).toURI().toString());
                    imageView.setImage(image);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        column5.setCellValueFactory(new PropertyValueFactory<>("productBrandString"));
        column6.setCellValueFactory(new PropertyValueFactory<>("productCategoryString"));
        column7.setCellValueFactory(new PropertyValueFactory<>("productSegmentString"));
        column8.setCellValueFactory(new PropertyValueFactory<>("productSectionString"));

        String query = "SELECT * FROM products WHERE parent_id = 0 OR parent_id IS NULL ORDER BY product_name";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            defaultTable.getItems().clear();

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
                product.setParentId(resultSet.getInt("parent_id"));
                product.setProductId(resultSet.getInt("product_id"));

                defaultTable.getItems().add(product);
            }


        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception according to your needs
        }

        defaultTable.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Product rowData = row.getItem();
                    addNewProductToSupplier(supplierName, rowData.getProductId());
                }
            });
            return row;
        });

    }

    private void loadChartOfAccountsTable() {
        BSISDAo bsisdAo = new BSISDAo();
        BalanceTypeDAO balanceTypeDAO = new BalanceTypeDAO();
        AccountTypeDAO accountTypeDAO = new AccountTypeDAO();
        tableHeader.setText("Chart Of Accounts");

        defaultTable.getColumns().removeAll(column1, column8, column7);

        // Set column headers
        columnHeader1.setText("GLCode");
        columnHeader2.setText("Account Title");
        columnHeader3.setText("BS/IS");
        columnHeader4.setText("Account Type");
        columnHeader5.setText("Balance Type");
        columnHeader6.setText("Description");

        column1.setCellValueFactory(new PropertyValueFactory<>("glCode"));
        column2.setCellValueFactory(new PropertyValueFactory<>("accountTitle"));
        column3.setCellValueFactory(new PropertyValueFactory<>("bsisCodeString"));
        column4.setCellValueFactory(new PropertyValueFactory<>("accountTypeString"));
        column5.setCellValueFactory(new PropertyValueFactory<>("balanceTypeString"));
        column6.setCellValueFactory(new PropertyValueFactory<>("description"));

        String query = "SELECT * FROM chart_of_accounts"; // Assuming employees have a specific role ID
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            // Clear existing items in the table
            defaultTable.getItems().clear();
            // Iterate through the result set and populate the table with employee data
            while (resultSet.next()) {
                ChartOfAccounts account = new ChartOfAccounts(
                        resultSet.getInt("coa_id"),
                        resultSet.getInt("gl_code"),
                        resultSet.getString("account_title"),
                        resultSet.getInt("bsis_code"),
                        bsisdAo.getBSISCodeById(resultSet.getInt("bsis_code")),
                        resultSet.getInt("account_type"),
                        accountTypeDAO.getBalanceTypeNameById(resultSet.getInt("account_type")),
                        resultSet.getInt("balance_type"),
                        balanceTypeDAO.getBalanceTypeNameById(resultSet.getInt("balance_type")),
                        resultSet.getString("description")
                );
                defaultTable.getItems().add(account);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void addNew(MouseEvent mouseEvent) {
        switch (registrationType) {
            case "company" -> addNewCompany();
            case "branch" -> addNewBranch();
            case "employee" -> addNewEmployee();
            case "supplier" -> addNewSupplier();
            case "product" -> addNewProduct();
            case "product_supplier" -> System.out.println(registrationType);
            case "system_employee" -> addNewSystemEmployeeTable();
            case "industry" -> addNewIndustry();
            case "division" -> addNewDivision();
            case "department" -> addNewDepartment();
            case "category" -> addNewCategory();
            case "customer" -> addNewCustomer();
            case "brand" -> addNewBrand();
            case "segment" -> addNewSegment();
            case "nature" -> addNewNature();
            case "class" -> addNewClass();
            case "section" -> addNewSection();
            case "unit" -> addNewUnit();
            case "chart_of_accounts" -> addNewChartOfAccounts();
            case "assets_and_equipments" -> addNewAsset();
            case "salesman" -> addNewSalesman();
            case "discount_type" -> addNewDiscountType();
            case "line_discount" -> addNewLineDiscount();
            default -> tableHeader.setText("Unknown Type");
        }
    }

    public void addNewLineDiscount() {
        String lineDiscountName = EntryAlert.showEntryAlert("Line Discount Registration", "Please enter line discount to be registered", "Line Discount: ");

        if (!lineDiscountName.isEmpty()) {
            double percentage = Double.parseDouble(EntryAlert.showEntryAlert("Percentage Registration", "Please enter percentage for the line discount", "Percentage: "));

            try {
                if (discountDAO.lineDiscountCreate(lineDiscountName, percentage)) {
                    DialogUtils.showConfirmationDialog("Success", "Line discount created successfully: " + lineDiscountName);
                } else {
                    DialogUtils.showErrorMessage("Error", "Failed to create line discount: " + lineDiscountName);
                }
            } catch (Exception e) {
                DialogUtils.showErrorMessage("Error", "An error occurred: " + e.getMessage());
            }
        } else {
            DialogUtils.showErrorMessage("Error", "Line discount name is empty or null. Line discount creation canceled.");
        }
    }

    public void addNewDiscountType() {
        String discountType = EntryAlert.showEntryAlert("Discount Type Registration", "Please enter discount type to be registered", "Discount Type: ");

        if (!discountType.isEmpty()) {
            try {
                if (discountDAO.discountTypeCreate(discountType)) {
                    DialogUtils.showConfirmationDialog("Success", "Discount type created successfully: " + discountType);
                } else {
                    DialogUtils.showErrorMessage("Error", "Failed to create discount type: " + discountType);
                }
            } catch (Exception e) {
                DialogUtils.showErrorMessage("Error", "An error occurred: " + e.getMessage());
            }
        } else {
            DialogUtils.showErrorMessage("Error", "Discount type name is empty or null. Discount type creation canceled.");
        }
    }


    private void addNewSalesman() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("salesmanRegistration.fxml"));
            Parent content = loader.load();
            SalesmanRegistrationController controller = loader.getController();
            controller.salesmanRegistration();

            Stage stage = new Stage();
            stage.setTitle("Add new salesman"); // Set the title of the new stage
            stage.setResizable(true);
            stage.setScene(new Scene(content)); // Set the scene with the loaded content
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading salesmanRegistration.fxml: " + e.getMessage());
        }
    }

    private void addNewCustomer() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("customerRegistration.fxml"));
            Parent content = loader.load();
            CustomerRegistrationController controller = loader.getController();
            controller.customerRegistration();

            Stage stage = new Stage();
            stage.setTitle("Add new customer"); // Set the title of the new stage
            stage.setResizable(true);
            stage.setScene(new Scene(content)); // Set the scene with the loaded content
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading customerRegistration.fxml: " + e.getMessage());
        }
    }

    private void addNewAsset() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("assetsEquipmentsRegistration.fxml"));
            Parent content = loader.load();

            AssetsEquipmentsController controller = loader.getController();
            controller.assetRegistration();

            Stage stage = new Stage();
            stage.setTitle("Add new asset"); // Set the title of the new stage
            stage.setResizable(false);
            stage.setScene(new Scene(content)); // Set the scene with the loaded content
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading companyRegistration.fxml: " + e.getMessage());
        }
    }

    ProductDAO productDAO = new ProductDAO();

    private void addNewProductToSupplier(String supplierName, int productId) {
        ProductsPerSupplierDAO perSupplierDAO = new ProductsPerSupplierDAO();
        SupplierDAO supplierDAO = new SupplierDAO();
        int supplierId = supplierDAO.getSupplierIdByName(supplierName);
        Product product = productDAO.getProductById(productId);
        String productName = product.getProductName();
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Add item to " + supplierName + " ?", "You are adding " + productName + " to " + supplierName, "");
        boolean userConfirmed = confirmationAlert.showAndWait();
        if (userConfirmed) {
            int id = perSupplierDAO.addProductForSupplier(supplierId, productId);
            if (id != -1) {
                DialogUtils.showConfirmationDialog("Success", productName + " has been added to " + supplierName);
                supplierInfoRegistrationController.populateSupplierProducts(supplierId);
            } else {
                DialogUtils.showErrorMessage("Error", "Failed to add " + productName + " to " + supplierName);
            }
        } else {
            DialogUtils.showErrorMessage("Cancelled", "You have cancelled adding " + productName + " to " + supplierName);
        }
    }

    private void addNewChartOfAccounts() {

    }

    public void addNewUnit() {
        String newUnitName = EntryAlert.showEntryAlert("Unit Registration", "Please enter unit to be registered", "Unit: ");
        if (!newUnitName.isEmpty()) {
            UnitDAO unitDAO = new UnitDAO(); // Assuming UnitDAO is your class handling unit operations
            boolean unitAdded = unitDAO.createUnit(newUnitName);
            if (unitAdded) {
                DialogUtils.showConfirmationDialog("Success", "Unit created successfully: " + newUnitName);
            } else {
                DialogUtils.showErrorMessage("Error", "Failed to create unit: " + newUnitName);
            }
        } else {
            DialogUtils.showErrorMessage("Error", "Unit name is empty or null. Unit creation canceled.");
        }

        try {
            loadUnitData();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private void addNewClass() {
        String productClass = EntryAlert.showEntryAlert("Class Registration", "Please enter class to be registered", "Class : ");
        ProductClassDAO productClassDAO = new ProductClassDAO();
        if (!productClass.isEmpty()) {
            boolean natureRegistered = productClassDAO.createProductClass(productClass);
            if (natureRegistered) {
                DialogUtils.showConfirmationDialog("Class Created", "Class created successfully: " + productClass);
            } else {
                DialogUtils.showErrorMessage("Class Creation Failed", "Failed to create class: " + productClass);
            }
        } else {
            DialogUtils.showErrorMessage("Invalid Class", "Class name is empty or null. Class creation canceled.");
        }
        try {
            loadClassData();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addNewSection() {
        String sectionName = EntryAlert.showEntryAlert("Section Registration", "Please enter section to be registered", "Section: ");
        SectionsDAO sectionsDAO = new SectionsDAO(); // Assuming you have a DAO class for handling sections

        if (sectionName != null && !sectionName.isEmpty()) {
            boolean sectionAdded = sectionsDAO.addSection(sectionName);
            if (sectionAdded) {
                DialogUtils.showConfirmationDialog("Section Created", "Section created successfully: " + sectionName);
                // Additional actions upon successful section creation
            } else {
                DialogUtils.showErrorMessage("Section Creation Failed", "Failed to create section: " + sectionName);
                // Handle the case where section creation failed
            }
        } else {
            DialogUtils.showErrorMessage("Invalid Section", "Section name is empty or null. Section creation canceled.");
            // Handle the case where the section name is empty or null
        }
        try {
            loadSectionData();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void addNewNature() {
        String natureName = EntryAlert.showEntryAlert("Nature Registration", "Please enter nature to be registered", "Nature : ");
        NatureDAO natureDAO = new NatureDAO();
        if (natureName != null && !natureName.isEmpty()) {
            boolean natureRegistered = natureDAO.createNature(natureName);
            if (natureRegistered) {
                DialogUtils.showConfirmationDialog("Nature Created", "Nature created successfully: " + natureName);
            } else {
                DialogUtils.showErrorMessage("Nature Creation Failed", "Failed to create nature: " + natureName);
            }
        } else {
            DialogUtils.showErrorMessage("Invalid Nature", "Nature name is empty or null. Nature creation canceled.");
        }

        try {
            loadNatureData();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private void addNewIndustry() {
        ToDoAlert.showToDoAlert();
    }

    private void addNewDivision() {
        ToDoAlert.showToDoAlert();
    }

    private void addNewDepartment() {
        ToDoAlert.showToDoAlert();
    }

    private void addNewCategory() {
        String productCategory = EntryAlert.showEntryAlert("Category Registration", "Please enter category to be registered", "Category : ");
        CategoriesDAO categoriesDAO = new CategoriesDAO();
        if (!productCategory.isEmpty()) {
            boolean categoryRegistered = categoriesDAO.createCategory(productCategory);
            if (categoryRegistered) {
                DialogUtils.showConfirmationDialog("Category Created", "Category created successfully: " + productCategory);
                // The category was created successfully, perform additional actions if needed
            } else {
                DialogUtils.showErrorMessage("Category Creation Failed", "Failed to create category: " + productCategory);
                // Handle the case where category creation failed
            }
        } else {
            DialogUtils.showErrorMessage("Invalid Category", "Category name is empty or null. Category creation canceled.");
        }
        try {
            loadCategoryData();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void addNewBrand() {
        String productBrand = EntryAlert.showEntryAlert("Brand Registration", "Please enter brand to be registered", "Brand : ");
        BrandDAO brandDAO = new BrandDAO();
        boolean brandRegistered = brandDAO.createBrand(productBrand);

        if (brandRegistered) {
            DialogUtils.showConfirmationDialog("Brand registration", productBrand + " successfully registered");
        } else {
            DialogUtils.showErrorMessage("Brand registration failed", "Registration of brand " + productBrand + " has failed, please try again later.");
        }

        loadBrandData();
    }

    private void addNewSegment() {
        String productSegment = EntryAlert.showEntryAlert("Segment Registration", "Please enter segment to be registered", "Segment : ");
        SegmentDAO segmentDAO = new SegmentDAO();
        boolean segmentRegistered = segmentDAO.createSegment(productSegment);
        if (segmentRegistered) {
            DialogUtils.showConfirmationDialog("Segment registration", productSegment + " successfully registered");
        } else {
            DialogUtils.showErrorMessage("Segment registration failed", "Registration of segment " + productSegment + " has failed, please try again later.");
        }

        try {
            loadSegmentData();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private void addNewSystemEmployeeTable() {
        User selectedEmployee = (User) defaultTable.getSelectionModel().getSelectedItem();
        if (selectedEmployee != null) {
            ConfirmationAlert confirmationAlert = new ConfirmationAlert("Add Employee to System?",
                    "Add " + selectedEmployee.getUser_fname() + " to the system?", "Add employee to system?");

            boolean userConfirmed = confirmationAlert.showAndWait();

            if (userConfirmed) {
                String generatedPassword = RandomStringUtils.randomAlphanumeric(8);
                selectedEmployee.setUser_password(generatedPassword);

                // Update the password in the database
                String updateQuery = "UPDATE user SET user_password = ? WHERE user_id = ?";
                try (Connection connection = dataSource.getConnection();
                     PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                    preparedStatement.setString(1, generatedPassword);
                    preparedStatement.setInt(2, selectedEmployee.getUser_id());
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace(); // Print the stack trace for debugging purposes
                    // You can also show an error message to the user if the update fails
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Generated Password");
                alert.setHeaderText("Password generated for " + selectedEmployee.getUser_fname());
                alert.setContentText("Generated Password: " + generatedPassword);
                alert.showAndWait();
            }
        } else {
            // Handle the case where no employee is selected
            System.out.println("No employee selected.");
        }
    }

    private void addNewProduct() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("registerProduct.fxml"));
            Parent content = loader.load();

            RegisterProductController controller = loader.getController();
            controller.addNewParentProduct();

            // Create a new stage (window) for company registration
            Stage stage = new Stage();
            stage.setTitle("Product Registration"); // Set the title of the new stage
            stage.setScene(new Scene(content)); // Set the scene with the loaded content
            stage.setMaximized(true);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading companyRegistration.fxml: " + e.getMessage());
        }
    }

    private void addNewSupplier() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("supplierInfoRegistration.fxml"));
            Parent content = loader.load();

            SupplierInfoRegistrationController controller = loader.getController();
            controller.setTableManagerController(this);
            controller.initializeRegistration();
            Stage stage = new Stage();
            stage.setTitle("Supplier Registration"); // Set the title of the new stage
            stage.setScene(new Scene(content)); // Set the scene with the loaded content
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading companyRegistration.fxml: " + e.getMessage());
        }
    }

    private void addNewEmployee() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("employeeDetails.fxml"));
            Parent content = loader.load();

            EmployeeDetailsController controller = loader.getController();
            controller.registerNewEmployee();

            // Create a new stage (window) for company registration
            Stage stage = new Stage();
            stage.setTitle("Register new employee"); // Set the title of the new stage
            stage.setScene(new Scene(content)); // Set the scene with the loaded content
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading companyRegistration.fxml: " + e.getMessage());
        }
    }

    private void addNewBranch() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("branchRegistration.fxml"));
            Parent content = loader.load();

            BranchRegistrationController controller = loader.getController();
            controller.tableManagerController(this);
            // Create a new stage (window) for company registration
            Stage stage = new Stage();
            stage.setTitle("Supplier Registration"); // Set the title of the new stage
            stage.setScene(new Scene(content)); // Set the scene with the loaded content
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading companyRegistration.fxml: " + e.getMessage());
        }
    }

    private void addNewCompany() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("companyRegistration.fxml"));
            Parent content = loader.load();

            CompanyRegistrationController controller = loader.getController();

            // Create a new stage (window) for company registration
            Stage stage = new Stage();
            stage.setTitle("Company Registration"); // Set the title of the new stage
            stage.setScene(new Scene(content)); // Set the scene with the loaded content
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading companyRegistration.fxml: " + e.getMessage());
        }
    }

    private void loadNatureTable() {
        tableHeader.setText("Nature");
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Product Nature.png"));
        tableImg.setImage(image);

        columnHeader1.setText("Nature Name");

        TableColumn<Map<String, String>, String> column1 = new TableColumn<>("Nature Name");
        column1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("nature_name")));

        try {
            loadNatureData(); // Load data into the 'natureData' ObservableList

            defaultTable.setItems(natureData); // Set items from 'natureData' to the table
        } catch (SQLException e) {
            e.printStackTrace();
        }

        defaultTable.getColumns().clear(); // Clear existing columns
        defaultTable.getColumns().add(column1);
    }

    private void loadNatureData() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM nature";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                natureData.clear(); // Clear existing data before loading new data

                while (resultSet.next()) {
                    Map<String, String> natureRow = new HashMap<>();
                    natureRow.put("nature_name", resultSet.getString("nature_name"));
                    natureData.add(natureRow);
                }
            }
        }
    }

    private void loadSectionTable() {
        tableHeader.setText("Section");
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/section.png"));
        tableImg.setImage(image);

        columnHeader1.setText("Section Name");

        TableColumn<Map<String, String>, String> column1 = new TableColumn<>("Section Name");
        column1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("section_name")));

        try {
            loadSectionData(); // Load data into the 'sectionData' ObservableList

            defaultTable.setItems(sectionData); // Set items from 'sectionData' to the table
        } catch (SQLException e) {
            e.printStackTrace();
        }

        defaultTable.getColumns().clear(); // Clear existing columns
        defaultTable.getColumns().add(column1);
    }

    private void loadSectionData() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM sections";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                sectionData.clear(); // Clear existing data before loading new data

                while (resultSet.next()) {
                    Map<String, String> sectionRow = new HashMap<>();
                    sectionRow.put("section_name", resultSet.getString("section_name"));
                    sectionData.add(sectionRow);
                }
            }
        }
    }

    private void loadClassTable() {
        tableHeader.setText("Class");
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Prduct Class.png"));
        tableImg.setImage(image);

        columnHeader1.setText("Class Name");

        TableColumn<Map<String, String>, String> column1 = new TableColumn<>("Class Name");
        column1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("class_name")));

        try {
            loadClassData(); // Load data into the 'classData' ObservableList

            defaultTable.setItems(classData); // Set items from 'classData' to the table
        } catch (SQLException e) {
            e.printStackTrace();
        }

        defaultTable.getColumns().clear(); // Clear existing columns
        defaultTable.getColumns().add(column1);
    }

    private void loadClassData() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM classes";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                classData.clear(); // Clear existing data before loading new data

                while (resultSet.next()) {
                    Map<String, String> classRow = new HashMap<>();
                    classRow.put("class_name", resultSet.getString("class_name"));
                    classData.add(classRow);
                }
            }
        }
    }

    private void loadDiscountSetUpTable() {
        ToDoAlert.showToDoAlert();
        tableHeader.setText("Discount Set Up");
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Discount.png"));
        tableImg.setImage(image);

        columnHeader1.setText("Discount Name");

        TableColumn<Map<String, String>, String> column1 = new TableColumn<>("Discount Name");
        column1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("segment_name")));

        try {
            ObservableList<Map<String, String>> segmentData = FXCollections.observableArrayList();

            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM segment";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                     ResultSet resultSet = preparedStatement.executeQuery()) {

                    while (resultSet.next()) {
                        Map<String, String> segmentRow = new HashMap<>();
                        segmentRow.put("segment_name", resultSet.getString("segment_name"));
                        segmentData.add(segmentRow);
                    }
                }
            }

            defaultTable.setItems(segmentData);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        defaultTable.getColumns().clear(); // Clear existing columns
        defaultTable.getColumns().add(column1);
    }

    private void loadPaymentTerms() {
        tableHeader.setText("Payment Terms");
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Card Payment.png"));
        tableImg.setImage(image);

        columnHeader1.setText("Payment Term Names");

        TableColumn<Map<String, String>, String> column1 = new TableColumn<>("Payment Names");
        column1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("payment_name")));

        try {
            ObservableList<Map<String, String>> paymentData = FXCollections.observableArrayList();

            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM payment_terms";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                     ResultSet resultSet = preparedStatement.executeQuery()) {

                    while (resultSet.next()) {
                        Map<String, String> paymentRow = new HashMap<>();
                        paymentRow.put("payment_name", resultSet.getString("payment_name"));
                        paymentData.add(paymentRow);
                    }
                }
            }

            defaultTable.setItems(paymentData);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        defaultTable.getColumns().clear(); // Clear existing columns
        defaultTable.getColumns().add(column1);

    }

    private void loadDeliveryTerms() {
        tableHeader.setText("Delivery Terms");
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Delivery.png"));
        tableImg.setImage(image);

        TableColumn<Map<String, String>, String> column1 = new TableColumn<>("Delivery Terms");
        column1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("delivery_name")));

        try {
            ObservableList<Map<String, String>> deliveryData = FXCollections.observableArrayList();

            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM delivery_terms";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                     ResultSet resultSet = preparedStatement.executeQuery()) {

                    while (resultSet.next()) {
                        Map<String, String> deliveryRow = new HashMap<>();
                        deliveryRow.put("delivery_terms", resultSet.getString("delivery_terms"));
                        deliveryData.add(deliveryRow);
                    }
                }
            }

            defaultTable.setItems(deliveryData);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        defaultTable.getColumns().clear(); // Clear existing columns
        defaultTable.getColumns().add(column1);

    }

    private void loadSegmentTable() {
        tableHeader.setText("Segment");
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Sorting Category.png"));
        tableImg.setImage(image);

        columnHeader1.setText("Segment Name");

        TableColumn<Map<String, String>, String> column1 = new TableColumn<>("Segment Name");
        column1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("segment_name")));

        try {
            loadSegmentData(); // Load data into the 'segmentData' ObservableList

            defaultTable.setItems(segmentData); // Set items from 'segmentData' to the table
        } catch (SQLException e) {
            e.printStackTrace();
        }

        defaultTable.getColumns().clear(); // Clear existing columns
        defaultTable.getColumns().add(column1);
    }

    private void loadSegmentData() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM segment";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                segmentData.clear(); // Clear existing data before loading new data

                while (resultSet.next()) {
                    Map<String, String> segmentRow = new HashMap<>();
                    segmentRow.put("segment_name", resultSet.getString("segment_name"));
                    segmentData.add(segmentRow);
                }
            }
        }
    }

    private void loadUnitTable() {
        tableHeader.setText("Unit");
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/unit.png"));
        tableImg.setImage(image);

        columnHeader1.setText("Unit Name");

        TableColumn<Map<String, String>, String> column1 = new TableColumn<>("Unit Name");
        column1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("unit_name")));

        try {
            loadUnitData(); // Load data into the 'unitData' ObservableList

            defaultTable.setItems(unitData); // Set items from 'unitData' to the table
        } catch (SQLException e) {
            e.printStackTrace();
        }

        defaultTable.getColumns().clear(); // Clear existing columns
        defaultTable.getColumns().add(column1);
    }

    private void loadUnitData() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM units";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                unitData.clear(); // Clear existing data before loading new data

                while (resultSet.next()) {
                    Map<String, String> unitRow = new HashMap<>();
                    unitRow.put("unit_name", resultSet.getString("unit_name"));
                    unitData.add(unitRow);
                }
            }
        }
    }


    private void loadBrandTable() {
        tableHeader.setText("Brand");
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/brand.png"));
        tableImg.setImage(image);

        columnHeader1.setText("Brand Name");

        TableColumn<Map<String, String>, String> column1 = new TableColumn<>("Brand Name");
        column1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("brand_name")));

        brandData = FXCollections.observableArrayList();
        loadBrandData();

        defaultTable.setItems(brandData);
        defaultTable.getColumns().clear(); // Clear existing columns
        defaultTable.getColumns().add(column1);
    }

    public void loadBrandData() {
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM brand";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                brandData.clear(); // Clear existing data before loading new data

                while (resultSet.next()) {
                    Map<String, String> brandRow = new HashMap<>();
                    brandRow.put("brand_name", resultSet.getString("brand_name"));
                    brandData.add(brandRow);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle SQL exceptions here
        }
    }


    private void loadCategoryTable() {
        tableHeader.setText("Category");
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/categorization.png"));
        tableImg.setImage(image);

        columnHeader1.setText("Category Name");

        TableColumn<Map<String, String>, String> column1 = new TableColumn<>("Category Name");
        column1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("category_name")));

        try {
            loadCategoryData(); // Load data into the 'categoryData' ObservableList

            defaultTable.setItems(categoryData); // Set items from 'categoryData' to the table
        } catch (SQLException e) {
            e.printStackTrace();
        }

        defaultTable.getColumns().clear(); // Clear existing columns
        defaultTable.getColumns().add(column1);
    }

    private void loadCategoryData() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM categories";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                categoryData.clear(); // Clear existing data before loading new data

                while (resultSet.next()) {
                    Map<String, String> categoryRow = new HashMap<>();
                    categoryRow.put("category_name", resultSet.getString("category_name"));
                    categoryData.add(categoryRow);
                }
            }
        }
    }

    private void loadDepartmentTable() {
        tableHeader.setText("Department");
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Organization Chart People.png"));
        tableImg.setImage(image);

        defaultTable.getColumns().removeAll(column7, column8);

        columnHeader1.setText("Division");
        columnHeader2.setText("Department Name");
        columnHeader3.setText("Department Head");
        columnHeader4.setText("Department Description");
        columnHeader5.setText("Date Added");
        columnHeader6.setText("Tax ID");

        column1.setCellValueFactory(new PropertyValueFactory<>("parentDivision"));
        column2.setCellValueFactory(new PropertyValueFactory<>("departmentName"));
        column3.setCellValueFactory(new PropertyValueFactory<>("departmentHead"));
        column4.setCellValueFactory(new PropertyValueFactory<>("departmentDescription"));
        column5.setCellValueFactory(new PropertyValueFactory<>("dateAdded"));
        column6.setCellValueFactory(new PropertyValueFactory<>("taxId"));

        String query = "SELECT * FROM department";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            defaultTable.getItems().clear();
            while (resultSet.next()) {
                Department department = new Department(
                        resultSet.getInt("department_id"),
                        resultSet.getString("parent_division"),
                        resultSet.getString("department_name"),
                        resultSet.getString("department_head"),
                        resultSet.getString("department_description"),
                        resultSet.getInt("tax_id"),
                        resultSet.getDate("date_added")

                );
                defaultTable.getItems().add(department);

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadDivisionTable() {
        tableHeader.setText("Division");
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/division.png"));
        tableImg.setImage(image);

        defaultTable.getColumns().removeAll(column5, column6, column7, column8);

        columnHeader1.setText("Division Name");
        columnHeader2.setText("Division Head");
        columnHeader3.setText("Division Description");
        columnHeader4.setText("Date Added");
        column1.setCellValueFactory(new PropertyValueFactory<>("divisionName"));
        column2.setCellValueFactory(new PropertyValueFactory<>("divisionHead"));
        column3.setCellValueFactory(new PropertyValueFactory<>("divisionDescription"));
        column4.setCellValueFactory(new PropertyValueFactory<>("dateAdded"));

        String query = "SELECT * FROM division";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            defaultTable.getItems().clear();
            while (resultSet.next()) {
                Division division = new Division(
                        resultSet.getInt("division_id"),
                        resultSet.getString("division_name"),
                        resultSet.getString("division_head"),
                        resultSet.getString("division_description"),
                        resultSet.getString("division_code"),
                        resultSet.getDate("date_added")
                );
                defaultTable.getItems().add(division);

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadIndustryTable() {
        tableHeader.setText("Industries");
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Manufacturing.png"));
        tableImg.setImage(image);

        columnHeader1.setText("Industry Name");
        columnHeader2.setText("Industry Head");
        columnHeader3.setText("Industry Description");
        columnHeader4.setText("Date Added");
        columnHeader5.setText("Tax ID");

        defaultTable.getColumns().removeAll(column3, column6, column7, column8);

        column1.setCellValueFactory(new PropertyValueFactory<>("industryName"));
        column2.setCellValueFactory(new PropertyValueFactory<>("industryHead"));
        column3.setCellValueFactory(new PropertyValueFactory<>("industryDescription"));
        column4.setCellValueFactory(new PropertyValueFactory<>("dateAdded"));
        column5.setCellValueFactory(new PropertyValueFactory<>("taxId"));

        String query = "SELECT * FROM industry"; // Exclude users without passwords
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            // Clear existing items in the table
            defaultTable.getItems().clear();
            while (resultSet.next()) {
                Industry industry = new Industry(
                        resultSet.getInt("id"),
                        resultSet.getString("industry_name"),
                        resultSet.getString("industry_head"),
                        resultSet.getString("industry_description"),
                        resultSet.getDate("date_added"),
                        resultSet.getInt("tax_id")
                );
                defaultTable.getItems().add(industry);

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadSystemEmployeeTable() {
        tableHeader.setText("System Employees");

        // Set column headers
        columnHeader1.setText("Employee ID");
        columnHeader2.setText("First Name");
        columnHeader3.setText("Middle Name");
        columnHeader4.setText("Last Name");
        columnHeader5.setText("Email");
        columnHeader6.setText("Contact");
        columnHeader7.setText("Position");
        columnHeader8.setText("Department");

        // Set cell value factories for table columns
        column1.setCellValueFactory(new PropertyValueFactory<>("user_id"));
        column2.setCellValueFactory(new PropertyValueFactory<>("user_fname"));
        column3.setCellValueFactory(new PropertyValueFactory<>("user_mname"));
        column4.setCellValueFactory(new PropertyValueFactory<>("user_lname"));
        column5.setCellValueFactory(new PropertyValueFactory<>("user_email"));
        column6.setCellValueFactory(new PropertyValueFactory<>("user_contact"));
        column7.setCellValueFactory(new PropertyValueFactory<>("user_position"));
        column8.setCellValueFactory(new PropertyValueFactory<>("user_department"));

        defaultTable.setRowFactory(tv -> new TableRow<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle(""); // Set default style for empty rows
                } else {
                    String password = item.getUser_password(); // Replace with the appropriate method to get user_password
                    if (password == null || password.isEmpty()) {
                        setStyle("-fx-background-color: orange;"); // Set orange background for rows with null or empty password
                        defaultTable.setOnMouseClicked(event -> {
                            if (event.getClickCount() == 2) {
                                addNewSystemEmployeeTable();
                            }
                        });
                    } else {
                        setStyle(""); // Set default style for rows with non-empty password
                        defaultTable.setOnMouseClicked(event -> {
                            if (event.getClickCount() == 2) { // Check for double-click
                                handleTableDoubleClick(defaultTable.getSelectionModel().getSelectedItem());
                            }
                        });
                    }
                }
            }
        });
        String query = "SELECT * FROM user"; // Exclude users without passwords
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            // Clear existing items in the table
            defaultTable.getItems().clear();
            while (resultSet.next()) {
                User employee = new User(
                        resultSet.getInt("user_id"),
                        resultSet.getString("user_email"),
                        resultSet.getString("user_password"),
                        resultSet.getString("user_fname"),
                        resultSet.getString("user_mname"),
                        resultSet.getString("user_lname"),
                        resultSet.getString("user_contact"),
                        resultSet.getString("user_province"),
                        resultSet.getString("user_city"),
                        resultSet.getString("user_brgy"),
                        resultSet.getString("user_sss"),
                        resultSet.getString("user_philhealth"),
                        resultSet.getString("user_tin"),
                        resultSet.getString("user_position"),
                        resultSet.getInt("user_department"),
                        resultSet.getDate("user_dateOfHire"),
                        resultSet.getString("user_tags"),
                        resultSet.getDate("user_bday"),
                        resultSet.getInt("role_id"),
                        resultSet.getString("user_image")
                );
                defaultTable.getItems().add(employee);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    public void loadSupplierTable() {
        tableHeader.setText("Suppliers");
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Supplier Info.png"));
        tableImg.setImage(image);
        columnHeader1.setText("Supplier Name");
        columnHeader2.setText("Logo");
        columnHeader3.setText("Contact Person");
        columnHeader4.setText("Email Address");
        columnHeader5.setText("Phone Number");
        columnHeader6.setText("Province");
        columnHeader7.setText("City");
        columnHeader8.setText("Baranggay");

        column1.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        column2.setCellValueFactory(new PropertyValueFactory<>("supplierImage"));
        column3.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));
        column4.setCellValueFactory(new PropertyValueFactory<>("emailAddress"));
        column5.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        column6.setCellValueFactory(new PropertyValueFactory<>("stateProvince"));
        column7.setCellValueFactory(new PropertyValueFactory<>("city"));
        column8.setCellValueFactory(new PropertyValueFactory<>("Barangay"));

        column2.setCellFactory(param -> new TableCell<Supplier, String>() {
            private final ImageView imageView = new ImageView();

            {
                setAlignment(Pos.CENTER);
                ImageCircle.cicular(imageView);
            }

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty || imagePath == null || imagePath.isEmpty()) {
                    setGraphic(null);
                } else {
                    // Load the image using the imagePath
                    try {
                        File file = new File(imagePath);
                        Image image = new Image(file.toURI().toString());
                        imageView.setImage(image);
                        imageView.setFitWidth(50);
                        imageView.setFitHeight(50);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Handle the exception according to your needs
                    }
                }
            }
        });
        defaultTable.getItems().clear();
        defaultTable.setItems(supplierDAO.getAllSuppliers());
    }


    public void loadProductTable() {
        tableHeader.setText("Products");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/package.png")));

        tableImg.setImage(image);
        columnHeader1.setText("Product Name");
        columnHeader2.setText("Product Code");
        columnHeader3.setText("Description");
        columnHeader4.setText("Product Image");
        columnHeader5.setText("Brand");
        columnHeader6.setText("Category");
        columnHeader7.setText("Segment");
        columnHeader8.setText("Section");


        defaultTable.setRowFactory(tv -> new TableRow<Product>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle(""); // Set default style if the row is empty
                } else {
                    if (item.getParentId() == 0) {
                        // Apply a different background color to rows with parent_id = 0
                        setStyle("-fx-background-color: #5A90CF;");
                    } else {
                        // Set default background for other rows
                        setStyle("");
                    }
                }
            }
        });

        column1.setCellValueFactory(new PropertyValueFactory<>("productName"));
        column2.setCellValueFactory(new PropertyValueFactory<>("productCode"));
        column3.setCellValueFactory(new PropertyValueFactory<>("description"));
        column4.setCellValueFactory(new PropertyValueFactory<>("productImage"));

        column4.setCellFactory(param -> new TableCell<Product, String>() {
            private final ImageView imageView = new ImageView();

            {
                ImageCircle.cicular(imageView);
                imageView.setFitHeight(50);
                imageView.setFitWidth(50);
                setGraphic(imageView);
                setContentDisplay(ContentDisplay.CENTER);

                // Add event handler for mouse click
                setOnMouseClicked(event -> {
                    loadProductImage(getItem());
                });
            }

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty || imagePath == null) {
                    imageView.setImage(null);
                } else {
                    // Set a placeholder image or loading indicator if the image is not loaded yet
                    imageView.setImage(new Image(getClass().getResource("/com/vertex/vos/assets/icons/package.png").toString()));

                    // If you want to load the image only when the cell is clicked, you can remove the following line
                    loadProductImage(imagePath);
                }
            }

            private void loadProductImage(String imagePath) {
                // Load the image in a background thread to avoid blocking the UI
                Task<Image> imageLoadTask = new Task<>() {
                    @Override
                    protected Image call() {
                        return new Image(new File(imagePath).toURI().toString());
                    }

                    @Override
                    protected void succeeded() {
                        // Set the loaded image to the ImageView
                        imageView.setImage(getValue());
                    }

                    @Override
                    protected void failed() {
                        // Handle the failure to load the image (e.g., set an error image)
                        imageView.setImage(new Image(getClass().getResource("/com/vertex/vos/assets/icons/package.png").toString()));
                    }
                };

                // Run the image loading task in a separate thread
                new Thread(imageLoadTask).start();
            }
        });


        column5.setCellValueFactory(new PropertyValueFactory<>("productBrandString"));
        column6.setCellValueFactory(new PropertyValueFactory<>("productCategoryString"));
        column7.setCellValueFactory(new PropertyValueFactory<>("productSegmentString"));
        column8.setCellValueFactory(new PropertyValueFactory<>("productSectionString"));

        defaultTable.getItems().clear();

        String query = "SELECT * FROM products ORDER BY product_name";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
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
                product.setParentId(resultSet.getInt("parent_id"));
                product.setProductId(resultSet.getInt("product_id"));

                defaultTable.getItems().add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        defaultTable.getColumns().removeAll(column1, column2);

        searchBar.setVisible(true);
        searchBar.requestFocus();
        final StringBuilder barcodeBuilder = new StringBuilder();
        final PauseTransition pauseTransition = getPauseTransition(barcodeBuilder);
        final AtomicBoolean processingBarcode = new AtomicBoolean(false);

        searchBar.addEventHandler(KeyEvent.KEY_TYPED, event -> {
            String character = event.getCharacter();
            if (character.length() == 1 && Character.isDigit(character.charAt(0))) {
                // If the first character typed is a digit, start barcode scanning
                processingBarcode.set(true);
                pauseTransition.playFromStart();
                if (isValidBarcodeCharacter(character)) {
                    barcodeBuilder.append(character);
                }
            } else {
                // Otherwise, treat it as a description search
                processingBarcode.set(false);
                pauseTransition.stop(); // Stop barcode scanning if in progress
                handleDescriptionSearch(searchBar.getText() + character); // Include the typed character in the search
            }
        });

        searchBar.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (processingBarcode.get()) {
                    // If barcode processing is in progress, handle barcode scan
                    handleBarcodeScan(searchBar.getText());
                    searchBar.clear();
                    barcodeBuilder.setLength(0);
                    processingBarcode.set(false);
                } else {
                    // Otherwise, handle description search
                    handleDescriptionSearch(searchBar.getText());
                }
            }
        });


    }

    private void handleDescriptionSearch(String searchText) {
        Comparator<Product> comparator = Comparator.comparing(product ->
                product.getDescription().toLowerCase().indexOf(searchText.toLowerCase())
        );
        defaultTable.getItems().sort(comparator.reversed());
    }


    private PauseTransition getPauseTransition(StringBuilder barcodeBuilder) {
        final PauseTransition pauseTransition = new PauseTransition(Duration.millis(500)); // Set the duration as needed

        pauseTransition.setOnFinished(event -> {
            String barcode = barcodeBuilder.toString();
            if (!barcode.isEmpty()) {
                handleBarcodeScan(barcode);
                barcodeBuilder.setLength(0); // Clear the barcode builder
                searchBar.clear(); // Clear the search bar text
            }
        });
        return pauseTransition;
    }

    private void handleBarcodeScan(String barcode) {
        int productId = productDAO.getProductIdByBarcode(barcode);
        String description = productDAO.getProductDescriptionByBarcode(barcode);
        if (productId != -1 && !description.isEmpty()) {
            openProductDetails(productId);
        } else {
            promptProductRegistration(barcode);
        }
    }

    private Stage productDetailsStage = null;
    ErrorUtilities errorUtilities = new ErrorUtilities();

    private void openProductDetails(int productId) {
        Platform.runLater(() -> {
            if (productDetailsStage == null) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("registerProduct.fxml"));
                    Parent root = loader.load();
                    RegisterProductController controller = loader.getController();
                    controller.initData(productId);

                    productDetailsStage = new Stage();
                    productDetailsStage.setMaximized(true);
                    productDetailsStage.setTitle("Product Details");
                    productDetailsStage.setScene(new Scene(root));
                    productDetailsStage.setOnCloseRequest(event -> productDetailsStage = null);
                    productDetailsStage.show();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                // If the window is already open, shake it instead of opening a new one
                errorUtilities.shakeWindow(productDetailsStage);
            }
        });
    }


    private boolean isPromptProductRegistrationRunning = false; // Flag to track whether the method is already running

    private void promptProductRegistration(String barcode) {
        if (!isPromptProductRegistrationRunning) { // Check if the method is not already running
            isPromptProductRegistrationRunning = true; // Set the flag to indicate that the method is running
            Platform.runLater(() -> {
                ConfirmationAlert confirmationAlert = new ConfirmationAlert("Product registration", "No product found", barcode + " has no associated product in the system, would you like to add it?");
                boolean confirm = confirmationAlert.showAndWait();
                if (confirm) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("initialProductRegistration.fxml"));
                        Parent root = loader.load();
                        InitialProductRegistrationController controller = loader.getController();
                        controller.initializeProduct(barcode);
                        controller.setTableManagerController(this); // Pass the TableManagerController reference

                        Stage stage = new Stage();
                        stage.setTitle("Create new product");
                        stage.setScene(new Scene(root));
                        stage.setOnHidden(event -> isPromptProductRegistrationRunning = false); // Reset the flag when the stage is closed
                        stage.show();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    isPromptProductRegistrationRunning = false; // Reset the flag if the user cancels
                }
            });
        }
    }

    private boolean isValidBarcodeCharacter(String character) {
        return character.matches("[0-9]");
    }

    private void handleTableDoubleClick(Object selectedItem) {
        if (selectedItem instanceof User selectedEmployee) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("employeeDetails.fxml"));
                Parent root = loader.load();

                EmployeeDetailsController controller = loader.getController();
                controller.initData(selectedEmployee);

                // Create a new stage (window) for employee details
                Stage stage = new Stage();
                stage.setTitle("Employee Details");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace(); // Handle the exception according to your needs
            }
        } else if (selectedItem instanceof Product selectedProduct) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("registerProduct.fxml"));
                Parent root = loader.load();

                int parentId = selectedProduct.getParentId();
                RegisterProductController controller = loader.getController();
                controller.initData(selectedProduct.getProductId());
                controller.isParent(parentId);

                Stage stage = new Stage();
                stage.setMaximized(true);
                stage.setTitle("Product Details");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (selectedItem instanceof Supplier selectedSupplier) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("supplierInfoRegistration.fxml"));
                Parent root = loader.load();

                // Pass the selected supplier data to the controller of supplierDetails.fxml
                SupplierInfoRegistrationController controller = loader.getController();
                controller.initData(selectedSupplier);

                // Create a new stage (window) for supplier details
                Stage stage = new Stage();
                stage.setTitle("Supplier Details");
                stage.setMaximized(true);
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace(); // Handle the exception according to your needs
            }
        }
    }


    private void loadEmployeeTable() {
        tableHeader.setText("Employees");

        columnHeader1.setText("Employee ID");
        columnHeader2.setText("First Name");
        columnHeader3.setText("Middle Name");
        columnHeader4.setText("Last Name");
        columnHeader5.setText("Email");
        columnHeader6.setText("Contact");
        columnHeader7.setText("Position");
        columnHeader8.setText("Department");

        column1.setCellValueFactory(new PropertyValueFactory<>("user_id"));
        column2.setCellValueFactory(new PropertyValueFactory<>("user_fname"));
        column3.setCellValueFactory(new PropertyValueFactory<>("user_mname"));
        column4.setCellValueFactory(new PropertyValueFactory<>("user_lname"));
        column5.setCellValueFactory(new PropertyValueFactory<>("user_email"));
        column6.setCellValueFactory(new PropertyValueFactory<>("user_contact"));
        column7.setCellValueFactory(new PropertyValueFactory<>("user_position"));
        column8.setCellValueFactory(new PropertyValueFactory<>("user_department"));

        String query = "SELECT * FROM user"; // Assuming employees have a specific role ID
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            // Clear existing items in the table
            defaultTable.getItems().clear();
            // Iterate through the result set and populate the table with employee data
            while (resultSet.next()) {
                User employee = new User(
                        resultSet.getInt("user_id"),
                        resultSet.getString("user_email"),
                        resultSet.getString("user_password"),
                        resultSet.getString("user_fname"),
                        resultSet.getString("user_mname"),
                        resultSet.getString("user_lname"),
                        resultSet.getString("user_contact"),
                        resultSet.getString("user_province"),
                        resultSet.getString("user_city"),
                        resultSet.getString("user_brgy"),
                        resultSet.getString("user_sss"),
                        resultSet.getString("user_philhealth"),
                        resultSet.getString("user_tin"),
                        resultSet.getString("user_position"),
                        resultSet.getInt("user_department"),
                        resultSet.getDate("user_dateOfHire"),
                        resultSet.getString("user_tags"),
                        resultSet.getDate("user_bday"),
                        resultSet.getInt("role_id"),
                        resultSet.getString("user_image")
                );
                defaultTable.getItems().add(employee);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Branch> branchList = new ArrayList<>();

    private void loadBranchForPOTable() {
        tableHeader.setText("Select branch");
        addImage.setVisible(false);
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Franchise.png"));

        tableImg.setImage(image);

        columnHeader2.setText("Description");
        columnHeader3.setText("Branch Name");
        columnHeader4.setText("Branch Head");
        columnHeader5.setText("Branch Code");
        columnHeader6.setText("State/Province");
        columnHeader7.setText("City");
        columnHeader8.setText("Barangay");

        column2.setCellValueFactory(new PropertyValueFactory<>("branchDescription"));
        column3.setCellValueFactory(new PropertyValueFactory<>("branchName"));
        column4.setCellValueFactory(new PropertyValueFactory<>("branchHead"));
        column5.setCellValueFactory(new PropertyValueFactory<>("branchCode"));
        column6.setCellValueFactory(new PropertyValueFactory<>("stateProvince"));
        column7.setCellValueFactory(new PropertyValueFactory<>("city"));
        column8.setCellValueFactory(new PropertyValueFactory<>("brgy"));

        defaultTable.getColumns().remove(column1);

        String query = "SELECT * FROM branches";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            defaultTable.getItems().clear();
            branchList.clear();
            while (resultSet.next()) {
                Branch branch = new Branch(
                        resultSet.getInt("id"),
                        resultSet.getString("branch_description"),
                        resultSet.getString("branch_name"),
                        resultSet.getString("branch_head"),
                        resultSet.getString("branch_code"),
                        resultSet.getString("state_province"),
                        resultSet.getString("city"),
                        resultSet.getString("brgy"),
                        resultSet.getString("phone_number"),
                        resultSet.getString("postal_code"),
                        resultSet.getDate("date_added")
                );

                defaultTable.getItems().add(branch);
                branchList.add(branch);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        defaultTable.setRowFactory(tv -> {
            TableRow<Branch> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Branch rowData = row.getItem();
                    ConfirmationAlert confirmationAlert = new ConfirmationAlert("Add branch to PO", "Add " + rowData.getBranchDescription() + " to the PO?",
                            "You are adding " + rowData.getBranchDescription() + " to the purchase");

                    boolean userConfirmed = confirmationAlert.showAndWait();
                    if (userConfirmed) {
                        int branchId = rowData.getId();
                        purchaseOrderEntryController.addBranchToTable(branchId);
                        branchList.remove(rowData);

                        populateBranchForPO(branchList);
                    } else {
                        DialogUtils.showErrorMessage("Cancelled", "You have cancelled adding " + rowData.getBranchDescription() + " to your PO");
                    }
                }
            });
            return row;
        });
    }

    private void populateBranchForPO(List<Branch> branchList) {
        defaultTable.getItems().clear();
        defaultTable.getItems().addAll(branchList);
    }

    public void loadBranchTable() {
        tableHeader.setText("Branches");

        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Franchise.png"));

        tableImg.setImage(image);

        // Set column headers
        columnHeader1.setText("Branch ID");
        columnHeader2.setText("Description");
        columnHeader3.setText("Branch Name");
        columnHeader4.setText("Branch Head");
        columnHeader5.setText("Branch Code");
        columnHeader6.setText("State/Province");
        columnHeader7.setText("City");
        columnHeader8.setText("Barangay");

        // Set cell value factories for table columns
        column1.setCellValueFactory(new PropertyValueFactory<>("id"));
        column2.setCellValueFactory(new PropertyValueFactory<>("branchDescription"));
        column3.setCellValueFactory(new PropertyValueFactory<>("branchName"));
        column4.setCellValueFactory(new PropertyValueFactory<>("branchHead"));
        column5.setCellValueFactory(new PropertyValueFactory<>("branchCode"));
        column6.setCellValueFactory(new PropertyValueFactory<>("stateProvince"));
        column7.setCellValueFactory(new PropertyValueFactory<>("city"));
        column8.setCellValueFactory(new PropertyValueFactory<>("brgy"));

        // Execute a database query to fetch branch data
        String query = "SELECT * FROM branches";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            // Clear existing items in the table
            defaultTable.getItems().clear();

            // Iterate through the result set and populate the table
            while (resultSet.next()) {
                Branch branch = new Branch(
                        resultSet.getInt("id"),
                        resultSet.getString("branch_description"),
                        resultSet.getString("branch_name"),
                        resultSet.getString("branch_head"),
                        resultSet.getString("branch_code"),
                        resultSet.getString("state_province"),
                        resultSet.getString("city"),
                        resultSet.getString("brgy"),
                        resultSet.getString("phone_number"),
                        resultSet.getString("postal_code"),
                        resultSet.getDate("date_added")
                );

                // Add the branch to the table
                defaultTable.getItems().add(branch);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception according to your needs
        }
    }


    private void loadCompanyTable() {
        ObservableList<Company> companies = FXCollections.observableArrayList();

        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/business-and-trade.png"));

        tableImg.setImage(image);

        tableHeader.setText("Companies");
        columnHeader1.setText("Company ID");
        columnHeader2.setText("Company Name");
        columnHeader3.setText("Logo");
        columnHeader4.setText("Company Code");
        columnHeader5.setText("Company Type");
        columnHeader6.setText("First Address");
        columnHeader7.setText("Registration Number");
        columnHeader8.setText("TIN");
        // Set cell value factories for table columns
        column1.setCellValueFactory(new PropertyValueFactory<>("companyId"));
        column2.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        column3.setCellValueFactory(new PropertyValueFactory<>("companyLogo")); // Using PropertyValueFactory for demonstration purposes
        column4.setCellValueFactory(new PropertyValueFactory<>("companyCode"));
        column5.setCellValueFactory(new PropertyValueFactory<>("companyType"));
        column6.setCellValueFactory(new PropertyValueFactory<>("companyFirstAddress"));
        column7.setCellValueFactory(new PropertyValueFactory<>("companyRegistrationNumber"));
        column8.setCellValueFactory(new PropertyValueFactory<>("companyTIN"));


        column3.setCellFactory(param -> new TableCell<Company, byte[]>() {
            private final ImageView imageView = new ImageView();

            {
                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(byte[] logo, boolean empty) {
                super.updateItem(logo, empty);
                if (empty || logo == null) {
                    setGraphic(null);
                } else {
                    // Assuming logo is a byte array containing image data
                    Image image = new Image(new ByteArrayInputStream(logo));
                    imageView.setImage(image);
                    imageView.setFitWidth(50);  // Set the width of the displayed image
                    imageView.setFitHeight(50); // Set the height of the displayed image
                    setGraphic(imageView);
                }
            }
        });
        // Execute a database query to fetch company data
        String query = "SELECT * FROM company";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            // Clear existing items in the table
            defaultTable.getItems().clear();
            // Iterate through the result set and populate the table
            while (resultSet.next()) {
                Company company = new Company(
                        resultSet.getInt("company_id"),
                        resultSet.getString("company_name"),
                        resultSet.getString("company_type"),
                        resultSet.getString("company_code"),
                        resultSet.getString("company_firstAddress"),
                        resultSet.getString("company_secondAddress"),
                        resultSet.getString("company_registrationNumber"),
                        resultSet.getString("company_tin"),
                        resultSet.getDate("company_dateAdmitted"),
                        resultSet.getString("company_contact"),
                        resultSet.getString("company_email"),
                        resultSet.getString("company_department"),
                        resultSet.getBytes("company_logo"),
                        resultSet.getString("company_tags")

                );
                companies.add(company);
                defaultTable.setItems(companies);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private SupplierInfoRegistrationController supplierInfoRegistrationController;

    void setSupplierController(SupplierInfoRegistrationController supplierInfoRegistrationController) {
        this.supplierInfoRegistrationController = supplierInfoRegistrationController;
    }

    BranchDAO branchDAO = new BranchDAO();
    InventoryDAO inventoryDAO = new InventoryDAO();
    stockTransferController stockTransferController;

    public void setStockTransferController(stockTransferController stockTransferController) {
        this.stockTransferController = stockTransferController;
    }

    public void loadBranchProductsTable(int sourceBranchId, ObservableList<ProductsInTransact> productsList) {
        defaultTable.getColumns().clear();
        addImage.setVisible(false);
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/package.png")));
        searchBar.setVisible(true);
        categoryBar.setVisible(true);
        searchBar.setPromptText("Search product description");
        categoryBar.setPromptText("Search specifics");

        InventoryDAO inventoryDAO = new InventoryDAO();

        ObservableList<Inventory> filteredInventoryItems = inventoryDAO.getInventoryItemsByBranch(sourceBranchId);

        TableColumn<Inventory, String> productDescriptionColumn = new TableColumn<>("Product Description");
        productDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("productDescription"));

        TableColumn<Inventory, String> unit = new TableColumn<>("Unit");
        unit.setCellValueFactory(cellData -> {
            int productId = cellData.getValue().getProductId();
            ProductDAO productDAO = new ProductDAO();
            Product product = productDAO.getProductDetails(productId);
            return new SimpleStringProperty(product.getUnitOfMeasurementString());
        });

        TableColumn<Inventory, Integer> quantityColumn = new TableColumn<>("Quantity");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<Inventory, String> productNameColumn = new TableColumn<>("Product Name");
        productNameColumn.setCellValueFactory(cellData -> {
            int productId = cellData.getValue().getProductId();
            ProductDAO productDAO = new ProductDAO();
            Product product = productDAO.getProductDetails(productId);
            return new SimpleStringProperty(product.getProductName());
        });

        TableColumn<Inventory, String> brandColumn = new TableColumn<>("Brand");
        brandColumn.setCellValueFactory(cellData -> {
            int productId = cellData.getValue().getProductId();
            ProductDAO productDAO = new ProductDAO();
            Product product = productDAO.getProductDetails(productId);
            return new SimpleStringProperty(product.getProductBrandString());
        });

        TableColumn<Inventory, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(cellData -> {
            int productId = cellData.getValue().getProductId();
            ProductDAO productDAO = new ProductDAO();
            Product product = productDAO.getProductDetails(productId);
            return new SimpleStringProperty(product.getProductCategoryString());
        });

        defaultTable.getColumns().addAll(
                productDescriptionColumn,
                unit,
                quantityColumn,
                brandColumn,
                categoryColumn
        );

        defaultTable.setRowFactory(tv -> {
            TableRow<Inventory> row = new TableRow<>(); // Adjust TableRow type to Inventory
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Inventory rowData = row.getItem(); // Adjust type to Inventory
                    ConfirmationAlert confirmationAlert = new ConfirmationAlert("Add product", "Add this product to the branch?",
                            "You are adding " + rowData.getProductDescription() + " to the stock transfer");

                    boolean userConfirmed = confirmationAlert.showAndWait();
                    if (userConfirmed) {
                        int productId = rowData.getProductId(); // Assuming productId is a property of Inventory
                        stockTransferController.addProductToBranchTables(productId);
                    } else {
                        DialogUtils.showErrorMessage("Cancelled", "You have cancelled adding " + rowData.getProductDescription() + " to your PO");
                    }
                }
            });
            return row;

        });
        defaultTable.setItems(filteredInventoryItems);
    }
}
