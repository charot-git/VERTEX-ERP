package com.vertex.vos;

import com.vertex.vos.DAO.PackageBreakdownDAO;
import com.vertex.vos.Objects.Inventory;
import com.vertex.vos.Objects.Product;
import com.vertex.vos.Objects.ProductBreakdown;
import com.vertex.vos.Utilities.*;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class InventoryLedgerIOperationsController implements Initializable {

    @FXML
    private ComboBox<String> branchListComboBox;
    @FXML
    private TableView<Inventory> inventoryTableView;
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
    private ComboBox<String> natureComboBox;
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableView();
        setComboBoxBehaviour();
        populateComboBoxes();
        configureBranchListComboBox();
        loadAllInventoryItems();
    }

    private void initializePackageConversion(int branchId) {
        // Create context menu and "Convert To" menu
        ContextMenu contextMenu = new ContextMenu();
        Menu convertToMenu = new Menu("Convert To");
        contextMenu.getItems().add(convertToMenu);

        // Handle right-click to show context menu
        inventoryTableView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) { // Right-click
                Inventory selectedInventory = inventoryTableView.getSelectionModel().getSelectedItem();
                if (selectedInventory != null) {
                    convertToMenu.getItems().clear(); // Clear existing menu items
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
                    contextMenu.show(inventoryTableView, event.getScreenX(), event.getScreenY());
                }
            }
        });
    }


    private void handleConversion(Inventory selectedInventory, ProductBreakdown inventoryToConvert, int branchId) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Convert Quantity");
        dialog.setHeaderText("Convert " + selectedInventory.getProductDescription() + " to " + inventoryToConvert.getDescription());
        dialog.setContentText("Enter how many " + inventoryToConvert.getDescription() + " to convert:");

        // Show the dialog and wait for user input
        Optional<String> result = dialog.showAndWait();

        // Check if the user clicked "OK"
        if (result.isPresent()) {
            try {
                int quantity = Integer.parseInt(result.get());

                if (quantity > 0) {
                    performConversion(selectedInventory.getProductId(), inventoryToConvert.getProductId(), quantity, branchId);
                } else {
                    DialogUtils.showErrorMessage("Invalid quantity", "Quantity must be greater than zero.");
                }
            } catch (NumberFormatException e) {
                DialogUtils.showErrorMessage("Invalid input", "Please enter a valid number.");
            }
        }
    }

    PackageBreakdownDAO packageBreakdownDAO = new PackageBreakdownDAO();

    private void performConversion(int productIdToConvert, int productIdForConversion, int quantityRequested, int branchId) {
        boolean converted = packageBreakdownDAO.convertQuantity(productIdToConvert, productIdForConversion, quantityRequested, branchId);
        if (converted) {
            loadAllInventoryItems();
        } else {
            DialogUtils.showErrorMessage("Conversion Failed", "Failed to convert quantity.");
        }

    }


    private void setupTableView() {
        TableViewFormatter.formatTableView(inventoryTableView);

        TableColumn<Inventory, String> branchNameColumn = new TableColumn<>("Branch Name");
        TableColumn<Inventory, String> productDescriptionColumn = new TableColumn<>("Product Description");
        TableColumn<Inventory, Integer> quantityColumn = new TableColumn<>("Quantity");
        TableColumn<Inventory, LocalDateTime> lastRestockDateColumn = new TableColumn<>("Last Restock Date");

        branchNameColumn.setCellValueFactory(new PropertyValueFactory<>("branchName"));
        productDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("productDescription"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        lastRestockDateColumn.setCellValueFactory(new PropertyValueFactory<>("lastRestockDate"));

        inventoryTableView.getColumns().addAll(productDescriptionColumn, quantityColumn, lastRestockDateColumn);
        inventoryTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    private void setComboBoxBehaviour() {
        TextFieldUtils.setComboBoxBehavior(branchListComboBox);
        TextFieldUtils.setComboBoxBehavior(brandComboBox);
        TextFieldUtils.setComboBoxBehavior(categoryComboBox);
        TextFieldUtils.setComboBoxBehavior(classComboBox);
        TextFieldUtils.setComboBoxBehavior(segmentComboBox);
        TextFieldUtils.setComboBoxBehavior(natureComboBox);
        TextFieldUtils.setComboBoxBehavior(sectionComboBox);
    }

    private void populateComboBoxes() {
        branchListComboBox.setItems(branchDAO.getAllBranchNames());
        branchListComboBox.getItems().add("All");
        brandComboBox.setItems(brandDAO.getBrandNames());
        categoryComboBox.setItems(categoriesDAO.getCategoryNames());
        classComboBox.setItems(classDAO.getProductClassNames());
        segmentComboBox.setItems(segmentDAO.getSegmentNames());
        sectionComboBox.setItems(sectionsDAO.getSectionNames());
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
        ObservableList<Inventory> allInventoryItems = inventoryDAO.getAllInventoryItems();
        inventoryTableView.setItems(allInventoryItems);
    }

    private void filterInventoryByBranch(String branchName) {
        inventoryLabel.setText(branchName);
        int branchId = branchDAO.getBranchIdByName(branchName);
        ObservableList<Inventory> filteredInventoryItems = inventoryDAO.getInventoryItemsByBranch(branchId);
        inventoryTableView.setItems(filteredInventoryItems);
        if (!branchName.equals("All")) {
            initializePackageConversion(branchId);
        }
    }

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }
}
