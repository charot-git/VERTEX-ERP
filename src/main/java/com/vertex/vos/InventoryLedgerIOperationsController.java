package com.vertex.vos;

import com.vertex.vos.DAO.PackageBreakdownDAO;
import com.vertex.vos.Objects.ComboBoxFilterUtil;
import com.vertex.vos.Objects.Inventory;
import com.vertex.vos.Objects.ProductBreakdown;
import com.vertex.vos.Utilities.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class InventoryLedgerIOperationsController implements Initializable {

    @FXML
    private ComboBox<String> branchListComboBox;
    @FXML
    private TableView<Inventory> inventoryTableView;
    private ObservableList<Inventory> originalInventoryItems;

    @FXML
    private Label inventoryLabel;
    @FXML
    private HBox inventoryLabelBox;
    @FXML
    private ComboBox<String> brandComboBox;
    @FXML
    private ComboBox<String> categoryComboBox;
    @FXML
    private ComboBox<String> classComboBox;
    @FXML
    private ComboBox<String> segmentComboBox;
    @FXML
    private ComboBox<String> sectionComboBox;

    private AnchorPane contentPane;
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final BranchDAO branchDAO = new BranchDAO();
    private final BrandDAO brandDAO = new BrandDAO();
    private final CategoriesDAO categoriesDAO = new CategoriesDAO();
    private final ProductClassDAO classDAO = new ProductClassDAO();
    private final SegmentDAO segmentDAO = new SegmentDAO();
    private final SectionsDAO sectionsDAO = new SectionsDAO();
    private final HistoryManager historyManager = new HistoryManager();

    private final PackageBreakdownDAO packageBreakdownDAO = new PackageBreakdownDAO();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableView();
        setComboBoxBehaviour();
        setComboBoxFilters();
        populateComboBoxes();
        configureBranchListComboBox();
    }

    private void setComboBoxFilters() {
        brandComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filterInventory(originalInventoryItems);
        });

        categoryComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filterInventory(originalInventoryItems);
        });

        classComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filterInventory(originalInventoryItems);
        });

        segmentComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filterInventory(originalInventoryItems);
        });

        sectionComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filterInventory(originalInventoryItems);
        });
    }


    private void filterInventory(ObservableList<Inventory> allInventoryItems) {
        String selectedBrand = brandComboBox.getValue();
        String selectedCategory = categoryComboBox.getValue();
        String selectedClass = classComboBox.getValue();
        String selectedSegment = segmentComboBox.getValue();
        String selectedSection = sectionComboBox.getValue();

        // Check if all filters are cleared
        boolean noFiltersApplied = (selectedBrand == null || selectedBrand.equals("All")) &&
                (selectedCategory == null || selectedCategory.equals("All")) &&
                (selectedClass == null || selectedClass.equals("All")) &&
                (selectedSegment == null || selectedSegment.equals("All")) &&
                (selectedSection == null || selectedSection.equals("All"));

        if (noFiltersApplied) {
            // Show all items if no filters are applied
            inventoryTableView.setItems(originalInventoryItems);
        } else {
            // Filter based on selected values
            List<Inventory> filteredItems = allInventoryItems.stream()
                    .filter(item -> (selectedBrand == null || selectedBrand.equals("All") || selectedBrand.equals(item.getBrand())) &&
                            (selectedCategory == null || selectedCategory.equals("All") || selectedCategory.equals(item.getCategory())) &&
                            (selectedClass == null || selectedClass.equals("All") || selectedClass.equals(item.getProductClass())) &&
                            (selectedSegment == null || selectedSegment.equals("All") || selectedSegment.equals(item.getProductSegment())) &&
                            (selectedSection == null || selectedSection.equals("All") || selectedSection.equals(item.getProductSection())))
                    .collect(Collectors.toList());

            inventoryTableView.setItems(FXCollections.observableArrayList(filteredItems));
        }
    }

    private void resetFilters() {
        // Reset all combo boxes to "All"
        branchListComboBox.setValue("All");
        brandComboBox.setValue("All");
        categoryComboBox.setValue("All");
        classComboBox.setValue("All");
        segmentComboBox.setValue("All");
        sectionComboBox.setValue("All");

        // Display all items
        inventoryTableView.setItems(originalInventoryItems);
    }


    private void initializePackageConversion(int branchId) {
        ContextMenu contextMenu = new ContextMenu();
        Menu convertToMenu = new Menu("Convert To");
        contextMenu.getItems().add(convertToMenu);

        inventoryTableView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                Inventory selectedInventory = inventoryTableView.getSelectionModel().getSelectedItem();
                if (selectedInventory != null) {
                    convertToMenu.getItems().clear();

                    CompletableFuture.runAsync(() -> {
                        List<ProductBreakdown> breakdowns = inventoryDAO.fetchPackageBreakdowns(selectedInventory.getProductId()).toCompletableFuture().join();

                        if (breakdowns.isEmpty()) {
                            MenuItem noConfigItem = new MenuItem("No Configurations");
                            noConfigItem.setDisable(true);
                            convertToMenu.getItems().add(noConfigItem);
                        } else {
                            for (ProductBreakdown breakdown : breakdowns) {
                                MenuItem menuItem = new MenuItem(breakdown.getProductId() + " - " + breakdown.getDescription() + " - " + breakdown.getUnitName() + " (" + breakdown.getUnitShortcut() + ")");
                                menuItem.setOnAction(e -> handleConversion(selectedInventory, breakdown, branchId));
                                convertToMenu.getItems().add(menuItem);
                            }
                        }

                        // Update UI on JavaFX Application Thread
                        javafx.application.Platform.runLater(() -> contextMenu.show(inventoryTableView, event.getScreenX(), event.getScreenY()));
                    });
                }
            }
            if (event.getButton() == MouseButton.PRIMARY) {
                Inventory selectedInventory = inventoryTableView.getSelectionModel().getSelectedItem();
                System.out.println(selectedInventory.getBrand() + " " + selectedInventory.getCategory());
            }
        });
    }

    private void handleConversion(Inventory selectedInventory, ProductBreakdown inventoryToConvert, int branchId) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Convert Quantity");
        dialog.setHeaderText("Convert " + selectedInventory.getProductDescription() + " to " + inventoryToConvert.getDescription());
        dialog.setContentText("Enter how many " + inventoryToConvert.getDescription() + " to convert:");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            try {
                int quantity = Integer.parseInt(result.get());
                if (quantity > 0) {
                    CompletableFuture.runAsync(() -> performConversion(selectedInventory.getProductId(), inventoryToConvert.getProductId(), quantity, branchId));
                } else {
                    DialogUtils.showErrorMessage("Invalid quantity", "Quantity must be greater than zero.");
                }
            } catch (NumberFormatException e) {
                DialogUtils.showErrorMessage("Invalid input", "Please enter a valid number.");
            }
        }
    }

    private void performConversion(int productIdToConvert, int productIdForConversion, int quantityRequested, int branchId) {
        boolean converted = packageBreakdownDAO.convertQuantity(productIdToConvert, productIdForConversion, quantityRequested, branchId);

        javafx.application.Platform.runLater(() -> {
            if (converted) {
                loadAllInventoryItems();
            } else {
                DialogUtils.showErrorMessage("Conversion Failed", "Failed to convert quantity.");
            }
        });
    }

    private void setupTableView() {
        TableViewFormatter.formatTableView(inventoryTableView);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy hh:mm a");

        TableColumn<Inventory, String> branchNameColumn = new TableColumn<>("Branch Name");
        TableColumn<Inventory, String> productDescriptionColumn = new TableColumn<>("Product Description");
        TableColumn<Inventory, Integer> quantityColumn = new TableColumn<>("Quantity");
        TableColumn<Inventory, LocalDateTime> lastRestockDateColumn = new TableColumn<>("Last Restock Date");

        branchNameColumn.setCellValueFactory(new PropertyValueFactory<>("branchName"));
        productDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("productDescription"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        lastRestockDateColumn.setCellValueFactory(new PropertyValueFactory<>("lastRestockDate"));
        lastRestockDateColumn.setCellFactory(new Callback<TableColumn<Inventory, LocalDateTime>, TableCell<Inventory, LocalDateTime>>() {
            @Override
            public TableCell<Inventory, LocalDateTime> call(TableColumn<Inventory, LocalDateTime> param) {
                return new TableCell<Inventory, LocalDateTime>() {
                    @Override
                    protected void updateItem(LocalDateTime item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.format(formatter));
                        }
                    }
                };
            }
        });

        inventoryTableView.getColumns().addAll(productDescriptionColumn, quantityColumn, lastRestockDateColumn);
        inventoryTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    private void setComboBoxBehaviour() {
        TextFieldUtils.setComboBoxBehavior(branchListComboBox);
        TextFieldUtils.setComboBoxBehavior(brandComboBox);
        TextFieldUtils.setComboBoxBehavior(categoryComboBox);
        TextFieldUtils.setComboBoxBehavior(classComboBox);
        TextFieldUtils.setComboBoxBehavior(segmentComboBox);
        TextFieldUtils.setComboBoxBehavior(sectionComboBox);
    }

    private void populateComboBoxes() {
        CompletableFuture.runAsync(() -> {
            ObservableList<String> branchNames = branchDAO.getAllBranchNames();
            ObservableList<String> brandNames = brandDAO.getBrandNames();
            ObservableList<String> categoryNames = categoriesDAO.getCategoryNames();
            ObservableList<String> classNames = classDAO.getProductClassNames();
            ObservableList<String> segmentNames = segmentDAO.getSegmentNames();
            ObservableList<String> sectionNames = sectionsDAO.getSectionNames();

            javafx.application.Platform.runLater(() -> {
                branchListComboBox.setItems(branchNames);
                branchListComboBox.getItems().add("All");
                brandComboBox.setItems(brandNames);
                categoryComboBox.setItems(categoryNames);
                classComboBox.setItems(classNames);
                segmentComboBox.setItems(segmentNames);
                sectionComboBox.setItems(sectionNames);

                ComboBoxFilterUtil.setupComboBoxFilter(branchListComboBox, branchNames);
                ComboBoxFilterUtil.setupComboBoxFilter(brandComboBox, brandNames);
                ComboBoxFilterUtil.setupComboBoxFilter(categoryComboBox, categoryNames);
                ComboBoxFilterUtil.setupComboBoxFilter(classComboBox, classNames);
                ComboBoxFilterUtil.setupComboBoxFilter(segmentComboBox, segmentNames);
                ComboBoxFilterUtil.setupComboBoxFilter(sectionComboBox, sectionNames);
            });
        });
    }

    private void configureBranchListComboBox() {
        branchListComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.equals("All")) {
                loadAllInventoryItems();
            } else {
                filterInventoryByBranch(newValue);
            }
        });
    }

    private void loadAllInventoryItems() {
        inventoryLabel.setText("All");

        CompletableFuture.runAsync(() -> {
            ObservableList<Inventory> allInventoryItems = inventoryDAO.getAllInventoryItems();
            javafx.application.Platform.runLater(() -> inventoryTableView.setItems(allInventoryItems));
        });
    }

    private void filterInventoryByBranch(String branchName) {
        inventoryLabel.setText(branchName);
        int branchId = branchDAO.getBranchIdByName(branchName);

        CompletableFuture.runAsync(() -> {
            originalInventoryItems = inventoryDAO.getInventoryItemsByBranch(branchId);
            javafx.application.Platform.runLater(() -> inventoryTableView.setItems(originalInventoryItems));

            if (!branchName.equals("All")) {
                initializePackageConversion(branchId);
            }
        });
    }

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }
}
