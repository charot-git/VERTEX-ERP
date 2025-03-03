package com.vertex.vos;

import com.vertex.vos.DAO.PackageBreakdownDAO;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class InventoryLedgerIOperationsController implements Initializable {

    public Button exportButton;
    public Label totalAmount;
    public TextField sectionTextField;
    public TextField segmentTextField;
    public TextField classTextField;
    public TextField brandTextField;
    public TextField categoryTextField;
    @FXML
    private ComboBox<String> branchListComboBox;
    @FXML
    private TableView<Inventory> inventoryTableView;
    private final ObservableList<Inventory> inventoryObservableList = FXCollections.observableArrayList();

    @FXML
    private Label inventoryLabel;
    @FXML
    private HBox inventoryLabelBox;

    @Setter
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
        populateComboBoxes();
        configureBranchListComboBox();
        inventoryTableView.setItems(inventoryObservableList);
        exportButton.setOnMouseClicked(mouseEvent -> {
            openExportDialog();
        });
    }

    private void openExportDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(new Stage());

        if (file != null) {
            try {
                ExcelExporter.exportToExcel(inventoryTableView, file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void calculateTotalAmount() {
        double total = 0;

        // Iterate through all the items in the TableView
        for (Inventory inventory : inventoryTableView.getItems()) {
            total += inventory.getUnitPrice() * inventory.getQuantity();
        }
        totalAmount.setText(String.format("%.2f", total));
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
                        javafx.application.Platform.runLater(() -> contextMenu.show(inventoryTableView, event.getScreenX(), event.getScreenY()));
                    });
                }
            }
        });
    }

    private void handleConversion(Inventory selectedInventory, ProductBreakdown inventoryToConvert, int branchId) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Convert Quantity");
        dialog.setHeaderText("Convert " + selectedInventory.getProductDescription() + " to " + inventoryToConvert.getDescription());
        dialog.setContentText("Enter how many " + inventoryToConvert.getDescription() + " to convert:");

        // Show the dialog and wait for the user input
        Optional<String> result = dialog.showAndWait();

        // If the user entered a value, process the conversion
        if (result.isPresent()) {
            try {
                int quantityRequested = Integer.parseInt(result.get());

                // Validate if the entered quantity is greater than zero
                if (quantityRequested > 0) {
                    // Use CompletableFuture to perform the conversion asynchronously
                    CompletableFuture.runAsync(() -> {
                        // Perform the conversion and handle the result
                        boolean conversionSuccessful = performConversion(
                                selectedInventory.getProductId(),
                                selectedInventory.getQuantity(),
                                inventoryToConvert.getProductId(),
                                quantityRequested,
                                branchId
                        );

                        // Once the conversion is complete, update the UI on the JavaFX thread
                        if (conversionSuccessful) {
                            Platform.runLater(() -> {
                                DialogUtils.showCompletionDialog("Conversion Successful", "Successfully converted " + quantityRequested + " units.");
                                Platform.runLater(() -> filterInventoryByBranchId(branchId));

                            });
                        } else {
                            Platform.runLater(() -> DialogUtils.showErrorMessage("Conversion Failed", "Conversion failed. Please try again."));
                        }
                    });
                } else {
                    DialogUtils.showErrorMessage("Invalid quantity", "Quantity must be greater than zero.");
                }
            } catch (NumberFormatException e) {
                DialogUtils.showErrorMessage("Invalid input", "Please enter a valid number.");
            }
        }
    }

    private void filterInventoryByBranchId(int branchId) {
        CompletableFuture.runAsync(() -> {
            ObservableList<Inventory> items = inventoryDAO.getInventoryItemsByBranch(branchId);
            inventoryObservableList.setAll(items);
            initializePackageConversion(branchId);
        });
    }


    private boolean performConversion(int productIdToConvert, int availableQuantity, int productIdForConversion, int quantityRequested, int branchId) {
        return packageBreakdownDAO.convertQuantity(productIdToConvert, availableQuantity, productIdForConversion, quantityRequested, branchId);
    }


    private void setupTableView() {
        TableViewFormatter.formatTableView(inventoryTableView);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy hh:mm a");
        TableColumn<Inventory, String> productDescriptionColumn = new TableColumn<>("Product Description");
        TableColumn<Inventory, String> unitColumn = new TableColumn<>("Unit");
        TableColumn<Inventory, Integer> quantityColumn = new TableColumn<>("Quantity");
        TableColumn<Inventory, LocalDateTime> lastRestockDateColumn = new TableColumn<>("Last Restock Date");
        TableColumn<Inventory, String> brandColumn = new TableColumn<>("Brand");
        TableColumn<Inventory, String> categoryColumn = new TableColumn<>("Category");
        TableColumn<Inventory, String> classColumn = new TableColumn<>("Class");
        TableColumn<Inventory, String> segmentColumn = new TableColumn<>("Segment");
        TableColumn<Inventory, String> sectionColumn = new TableColumn<>("Section");
        TableColumn<Inventory, Double> totalAmountColumn = new TableColumn<>("Total Amount");

        // Set up product description column to wrap text and auto-fit content
        productDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("productDescription"));
        productDescriptionColumn.setCellFactory(tc -> {
            return new TableCell<Inventory, String>() {
                private final Text text = new Text();

                {
                    setGraphic(text);
                    setPrefHeight(Control.USE_COMPUTED_SIZE);  // Allow height to be calculated based on content
                    text.wrappingWidthProperty().bind(productDescriptionColumn.widthProperty());  // Bind text width to column width
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        text.setText(null);
                    } else {
                        text.setText(item);
                    }
                }
            };
        });

        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
        // Quantity column
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        // Last restock date column with formatting
        lastRestockDateColumn.setCellValueFactory(new PropertyValueFactory<>("lastRestockDate"));
        lastRestockDateColumn.setCellFactory(param -> new TableCell<Inventory, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        });

        // Other columns
        brandColumn.setCellValueFactory(new PropertyValueFactory<>("brand"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        classColumn.setCellValueFactory(new PropertyValueFactory<>("productClass"));
        segmentColumn.setCellValueFactory(new PropertyValueFactory<>("productSegment"));
        sectionColumn.setCellValueFactory(new PropertyValueFactory<>("productSection"));

        // Calculate and display the total amount column
        totalAmountColumn.setCellValueFactory(cellData -> {
            Inventory inventory = cellData.getValue();
            Double totalAmount = inventory.getQuantity() * inventory.getUnitPrice();
            return new ReadOnlyObjectWrapper<>(totalAmount);
        });

        // Add columns to TableView
        inventoryTableView.getColumns().addAll(
                brandColumn,
                categoryColumn,
                productDescriptionColumn,
                unitColumn,
                quantityColumn,
                totalAmountColumn,
                classColumn,
                segmentColumn,
                sectionColumn,
                lastRestockDateColumn
        );

        // Set the resize policy to constrain all columns
        inventoryTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }


    private void setComboBoxBehaviour() {
        TextFieldUtils.setComboBoxBehavior(branchListComboBox);
    }

    private void populateComboBoxes() {
        CompletableFuture.runAsync(() -> {
            ObservableList<String> branchNames = branchDAO.getAllBranchNames();

            javafx.application.Platform.runLater(() -> {
                branchListComboBox.setItems(branchNames);
                ComboBoxFilterUtil.setupComboBoxFilter(branchListComboBox, branchNames);
            });
        });
    }

    private void configureBranchListComboBox() {
        branchListComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filterInventoryByBranch(newValue);
            calculateTotalAmount();
        });
    }

    private void filterInventoryByBranch(String branchName) {
        int branchId = branchDAO.getBranchIdByName(branchName);

        CompletableFuture.runAsync(() -> {
            ObservableList<Inventory> inventoryItemsByBranch = inventoryDAO.getInventoryItemsByBranch(branchId);
            javafx.application.Platform.runLater(() -> {
                inventoryObservableList.setAll(inventoryItemsByBranch);
                setUpComboBoxFilters();
                initializePackageConversion(branchId);
                calculateTotalAmount();
            });
        });
    }

    ObservableList<String> brands = FXCollections.observableArrayList();
    ObservableList<String> categories = FXCollections.observableArrayList();
    ObservableList<String> classes = FXCollections.observableArrayList();
    ObservableList<String> segments = FXCollections.observableArrayList();
    ObservableList<String> sections = FXCollections.observableArrayList();
    private static final Logger LOGGER = Logger.getLogger(InventoryLedgerIOperationsController.class.getName());

    private void setUpComboBoxFilters() {
        LOGGER.info("Setting up combo box filters...");

        // Populate filter options
        brands.setAll(getDistinctValues(inventoryObservableList, Inventory::getBrand));
        categories.setAll(getDistinctValues(inventoryObservableList, Inventory::getCategory));
        classes.setAll(getDistinctValues(inventoryObservableList, Inventory::getProductClass));
        segments.setAll(getDistinctValues(inventoryObservableList, Inventory::getProductSegment));
        sections.setAll(getDistinctValues(inventoryObservableList, Inventory::getProductSection));

        TextFields.bindAutoCompletion(brandTextField, brands);
        TextFields.bindAutoCompletion(categoryTextField, categories);
        TextFields.bindAutoCompletion(categoryTextField, classes);
        TextFields.bindAutoCompletion(segmentTextField, segments);
        TextFields.bindAutoCompletion(sectionTextField, sections);

        brandTextField.textProperty().addListener((obs, oldValue, newValue) -> filterInventoryTableView());
        categoryTextField.textProperty().addListener((obs, oldValue, newValue) -> filterInventoryTableView());
        classTextField.textProperty().addListener((obs, oldValue, newValue) -> filterInventoryTableView());
        segmentTextField.textProperty().addListener((obs, oldValue, newValue) -> filterInventoryTableView());
        sectionTextField.textProperty().addListener((obs, oldValue, newValue) -> filterInventoryTableView());
    }

    private <T> ObservableList<T> getDistinctValues(ObservableList<Inventory> list, Function<Inventory, T> mapper) {
        return list.stream()
                .map(mapper)
                .filter(Objects::nonNull) // Avoid null values
                .distinct()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    private void filterInventoryTableView() {
        String brandFilter = brandTextField.getText().trim().toLowerCase();
        String categoryFilter = categoryTextField.getText().trim().toLowerCase();
        String classFilter = classTextField.getText().trim().toLowerCase();
        String segmentFilter = segmentTextField.getText().trim().toLowerCase();
        String sectionFilter = sectionTextField.getText().trim().toLowerCase();

        // If all fields are empty, reset inventory
        if (brandFilter.isEmpty() && categoryFilter.isEmpty() && classFilter.isEmpty()
                && segmentFilter.isEmpty() && sectionFilter.isEmpty()) {
            Platform.runLater(() -> inventoryTableView.setItems(inventoryObservableList));
            return;
        }

        // Filter inventory based on text fields
        ObservableList<Inventory> filteredList = inventoryObservableList.stream()
                .filter(item -> brandFilter.isEmpty() || item.getBrand().toLowerCase().contains(brandFilter))
                .filter(item -> categoryFilter.isEmpty() || item.getCategory().toLowerCase().contains(categoryFilter))
                .filter(item -> classFilter.isEmpty() || item.getProductClass().toLowerCase().contains(classFilter))
                .filter(item -> segmentFilter.isEmpty() || item.getProductSegment().toLowerCase().contains(segmentFilter))
                .filter(item -> sectionFilter.isEmpty() || item.getProductSection().toLowerCase().contains(sectionFilter))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        // Apply filtered results
        Platform.runLater(() -> inventoryTableView.setItems(filteredList));
    }


}