package com.vertex.vos;

import com.vertex.vos.DAO.PhysicalInventoryDAO;
import com.vertex.vos.DAO.PhysicalInventoryDetailsDAO;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.converter.IntegerStringConverter;
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PhysicalInventoryController implements Initializable {

    public TableView<PhysicalInventoryDetails> physicalInventoryDetailsTableView;
    public TableColumn<PhysicalInventoryDetails, String> statusCol;
    public Button exportButton;
    public TextField productNameFilter;
    public ComboBox<String> unitFilter;
    @FXML
    private TextField branchCode, branchFilter, productCategoryFilter, supplierFilter;
    @FXML
    private Button commitButton, confirmButton;
    @FXML
    private DatePicker cutOffDate, dateEncoded;
    @FXML
    private Label differentialAmount, header;
    @FXML
    private ComboBox<String> inventoryType, priceType;
    @FXML
    private TextArea remarks;
    @FXML
    private TableColumn<PhysicalInventoryDetails, String> breakdownCol, codeCol, nameCol, unitCol;
    @FXML
    private TableColumn<PhysicalInventoryDetails, Integer> physCountCol, sysCountCol, varianceCol;
    @FXML
    private TableColumn<PhysicalInventoryDetails, Double> differenceCol, priceCol;

    private PhysicalInventory physicalInventory = null;
    private SupplierDAO supplierDAO = new SupplierDAO();
    private BranchDAO branchDAO = new BranchDAO();
    private CategoriesDAO categoriesDAO = new CategoriesDAO();
    private PhysicalInventoryDetailsDAO physicalInventoryDetailsDAO = new PhysicalInventoryDetailsDAO();
    private ProductsPerSupplierDAO productsPerSupplierDAO = new ProductsPerSupplierDAO();

    private Supplier supplier = new Supplier();
    private Branch branch = new Branch();
    private Category category = new Category();
    private ObservableList<PhysicalInventoryDetails> details = FXCollections.observableArrayList();

    public void createNewPhysicalInventory(int nextNo) {
        commitButton.setDisable(true);
        physicalInventory = new PhysicalInventory();
        physicalInventory.setPhNo("PH " + nextNo);
        physicalInventory.setEncoderId(UserSession.getInstance().getUserId());
        header.setText(physicalInventory.getPhNo());

        setupAutoCompletion();
        initializeComboBoxes();
        setupDatePickers();
        setupListeners();

        confirmButton.setOnMouseClicked(mouseEvent -> initiateInsert());
    }

    private void openExportDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(new Stage());

        if (file != null) {
            try {
                ExcelExporter.exportToExcel(physicalInventoryDetailsTableView, file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    PhysicalInventoryDAO physicalInventoryDAO = new PhysicalInventoryDAO();

    private void initiateInsert() {
        physicalInventory.setCutOffDate(Timestamp.valueOf(cutOffDate.getValue().atStartOfDay()));
        physicalInventory.setBranch(branch);
        physicalInventory.setSupplier(supplier);
        physicalInventory.setStockType(inventoryType.getValue());
        physicalInventory.setPriceType(priceType.getValue());
        physicalInventory.setRemarks(remarks.getText());
        physicalInventory.setDateEncoded(Timestamp.valueOf(dateEncoded.getValue().atStartOfDay()));
        physicalInventory.setCategory(category);

        // Insert PhysicalInventory record
        boolean result = physicalInventoryDAO.createPhysicalInventory(physicalInventory, details);

        if (!result) {
            DialogUtils.showErrorMessage("Error", "Failed to create physical inventory.");
        } else {
            showSuccessMessage();
            commitButton.setDisable(false);
            commitButton.setOnMouseClicked(mouseEvent -> commit());
        }
    }

    private void showSuccessMessage() {
        DialogUtils.showCompletionDialog("Success", "Physical inventory created successfully.");
        physicalInventorySummaryController.loadPhysicalInventoryData();
    }

    private void setupAutoCompletion() {
        ObservableList<String> supplierNames = FXCollections.observableArrayList(supplierDAO.getAllSupplierNames());
        ObservableList<Category> categories = FXCollections.observableArrayList(categoriesDAO.getAllCategories());
        ObservableList<String> categoryNames = FXCollections.observableArrayList();
        ObservableList<Branch> branches = FXCollections.observableArrayList(branchDAO.getAllBranches());
        ObservableList<String> branchNames = FXCollections.observableArrayList();

        branches.forEach(b -> branchNames.add(b.getBranchName()));
        categories.forEach(c -> categoryNames.add(c.getCategoryName()));

        TextFields.bindAutoCompletion(branchFilter, branchNames);
        TextFields.bindAutoCompletion(supplierFilter, supplierNames);
        TextFields.bindAutoCompletion(productCategoryFilter, categoryNames);

        priceType.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                switch (newValue) {
                    case "A":
                        priceCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getProduct().getPriceA()));
                        break;
                    case "B":
                        priceCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getProduct().getPriceB()));
                        break;
                    case "C":
                        priceCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getProduct().getPriceC()));
                        break;
                    case "D":
                        priceCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getProduct().getPriceD()));
                        break;
                    case "E":
                        priceCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getProduct().getPriceE()));
                        break;
                    default:
                        break;
                }
                recalculateDifferenceCosts();
                physicalInventoryDetailsTableView.refresh();
            }
        });
    }

    private void recalculateDifferenceCosts() {
        for (PhysicalInventoryDetails details : physicalInventoryDetailsTableView.getItems()) {
            // Dynamically update the price based on selected price type
            double unitPrice = getPriceBasedOnSelectedType(details);
            // Recalculate the difference cost
            details.setDifferenceCost(details.getVariance() * unitPrice);
            details.setUnitPrice(unitPrice);
        }
        // Refresh the table to show updated values
        physicalInventoryDetailsTableView.refresh();
    }

    private void initializeComboBoxes() {
        inventoryType.getItems().addAll("Good", "Bad");
        inventoryType.setValue("Good");
        priceType.getItems().addAll("A", "B", "C", "D", "E");
        priceType.setValue("A");
    }

    private void setupDatePickers() {
        dateEncoded.setValue(LocalDate.now());
        cutOffDate.setValue(LocalDate.now());
    }

    private void setupListeners() {
        supplierFilter.textProperty().addListener((observable, oldValue, newValue) -> supplier = supplierDAO.getSupplierByName(supplierFilter.getText()));
        branchFilter.textProperty().addListener((observable, oldValue, newValue) -> branchProcess(newValue));
        productCategoryFilter.textProperty().addListener((observable, oldValue, newValue) -> handleCategoryFilter(newValue));
    }

    private void handleCategoryFilter(String newValue) {
        // Create a PauseTransition that waits for a specified duration before executing the filter
        PauseTransition pause = new PauseTransition(Duration.millis(500)); // 500ms delay
        pause.setOnFinished(event -> {
            if (areFiltersValid(newValue)) {
                category = getCategory(newValue);
                filterProducts();
            }
        });
        // Cancel any previous pause and restart the debounce countdown
        pause.playFromStart();
    }

    private boolean areFiltersValid(String newValue) {
        return !supplierFilter.getText().isEmpty() && !branchFilter.getText().isEmpty() && !newValue.isEmpty();
    }

    private Category getCategory(String newValue) {
        return categoriesDAO.getAllCategories().stream()
                .filter(c -> c.getCategoryName().equalsIgnoreCase(newValue))
                .findFirst()
                .orElse(null);
    }

    private void filterProducts() {
        details.clear();
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        physicalInventoryDetailsTableView.setPlaceholder(loadingIndicator);
        CompletableFuture.supplyAsync(() -> {
                    // Perform the database operation on a separate thread
                    return physicalInventoryDetailsDAO.getInventory(supplier, branch, category, cutOffDate.getValue());
                }).thenAcceptAsync(result -> {
                    // Update the UI on the JavaFX Application Thread
                    details.setAll(result);
                }, Platform::runLater)
                .exceptionally(ex -> {
                    // Handle any exceptions that occurred during the async operation
                    DialogUtils.showErrorMessage("Error", "Failed to filter products: " + ex.getMessage());
                    ex.printStackTrace();
                    return null;
                });
    }


    private void branchProcess(String newValue) {
        if (newValue != null && !newValue.isEmpty()) {
            branch = branchDAO.getAllBranches().stream()
                    .filter(b -> b.getBranchName().equalsIgnoreCase(newValue))
                    .findFirst()
                    .orElse(null);
            branchCode.setText(branch != null ? branch.getBranchCode() : "");
        }
    }

    private FilteredList<PhysicalInventoryDetails> filteredDetails;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        exportButton.setOnMouseClicked(mouseEvent -> openExportDialog());
        physicalInventoryDetailsTableView.setItems(details);
        // Setting up columns
        codeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductCode()));
        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductName()));
        unitCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getUnitOfMeasurementString()));
        priceCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getUnitPrice()));
        breakdownCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getProduct().getUnitOfMeasurementCount())));
        sysCountCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getSystemCount()));
        physCountCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getPhysicalCount()));
        physCountCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        statusCol.setCellValueFactory(cellData -> {
            PhysicalInventoryDetails details = cellData.getValue();
            double variance = details.getVariance();
            String status = "";
            if (variance < 0) {
                status = "Short";  // Negative variance
            } else if (variance > 0) {
                status = "Over";   // Positive variance
            } else {
                status = "Balance"; // Zero variance
            }

            return new SimpleStringProperty(status);
        });

        statusCol.setCellFactory(column -> {
            return new TableCell<PhysicalInventoryDetails, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        PhysicalInventoryDetails details = getTableRow().getItem();
                        if (details != null) {
                            double variance = details.getVariance();
                            if (variance < 0) {
                                setStyle("-fx-background-color: #D11141 ; -fx-text-fill: white;");  // Short (negative variance)
                            } else if (variance > 0) {
                                setStyle("-fx-background-color: #FFC425; -fx-text-fill: black;");  // Over (positive variance)
                            } else {
                                setStyle("-fx-background-color: #00B159 ; -fx-text-fill: white;");  // Balance (zero variance)
                            }
                        }
                    }
                }
            };
        });
        // When Physical Count is edited
        physCountCol.setOnEditCommit(event -> {
            PhysicalInventoryDetails editedItem = event.getRowValue();
            Integer newPhysicalCount = event.getNewValue();
            editedItem.setPhysicalCount(newPhysicalCount);
            // Update variance and difference cost
            editedItem.setVariance(editedItem.getPhysicalCount() - editedItem.getSystemCount()); // Example logic
            // Dynamically update the price based on selected price type
            double unitPrice = getPriceBasedOnSelectedType(editedItem);
            editedItem.setDifferenceCost(editedItem.getVariance() * unitPrice); // Example logic
            physicalInventoryDetailsTableView.refresh(); // Refresh the table to show updated values
            updateDifferentialAmount();  // Update the differential amount
        });
        varianceCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getVariance()));
        differenceCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getDifferenceCost()));

        filteredDetails = new FilteredList<>(details, p -> true);
        physicalInventoryDetailsTableView.setItems(filteredDetails);

        setupFilters();
    }

    private void setupFilters() {
        TextFields.bindAutoCompletion(productNameFilter, details.stream()
                .map(PhysicalInventoryDetails::getProduct)
                .map(Product::getProductName)
                .distinct()
                .collect(Collectors.toList()));

        productNameFilter.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        unitFilter.setOnAction(event -> applyFilters());
    }

    private void applyFilters() {
        String productFilter = productNameFilter.getText().trim().toLowerCase();
        String unitFilterValue = unitFilter.getValue();

        filteredDetails.setPredicate(detail -> {
            boolean matchesProduct = productFilter.isEmpty() ||
                    detail.getProduct().getProductName().toLowerCase().contains(productFilter);

            boolean matchesUnit = (unitFilterValue == null || unitFilterValue.isEmpty()) ||
                    detail.getProduct().getUnitOfMeasurementString().equals(unitFilterValue);

            return matchesProduct && matchesUnit;
        });

        physicalInventoryDetailsTableView.refresh();
    }

    private double getPriceBasedOnSelectedType(PhysicalInventoryDetails details) {
        String selectedPriceType = priceType.getValue();
        return switch (selectedPriceType) {
            case "A" -> details.getProduct().getPriceA();
            case "B" -> details.getProduct().getPriceB();
            case "C" -> details.getProduct().getPriceC();
            case "D" -> details.getProduct().getPriceD();
            case "E" -> details.getProduct().getPriceE();
            default -> 0.0;
        };
    }

    private void updateDifferentialAmount() {
        double totalDifference = 0.0;

        // Sum all the difference costs from the details list
        for (PhysicalInventoryDetails details : physicalInventoryDetailsTableView.getItems()) {
            totalDifference += details.getDifferenceCost();
        }

        physicalInventory.setTotalAmount(totalDifference);

        // Update the differentialAmount label
        differentialAmount.setText(String.format("Total Differential Amount: %.2f", totalDifference));
    }

    public void loadExistingPhysicalInventory(PhysicalInventory selectedInventory) {
        physicalInventory = selectedInventory;
        header.setText(selectedInventory.getPhNo());
        supplierFilter.setText(selectedInventory.getSupplier().getSupplierName());
        branchFilter.setText(selectedInventory.getBranch().getBranchDescription());
        branchCode.setText(selectedInventory.getBranch().getBranchCode());
        productCategoryFilter.setText(selectedInventory.getCategory().getCategoryName());
        dateEncoded.setValue(selectedInventory.getDateEncoded().toLocalDateTime().toLocalDate());
        cutOffDate.setValue(selectedInventory.getCutOffDate().toLocalDateTime().toLocalDate());
        priceType.setValue(selectedInventory.getPriceType());
        inventoryType.setValue(selectedInventory.getStockType());
        remarks.setText(selectedInventory.getRemarks());
        details.setAll(physicalInventoryDetailsDAO.getPhysicalInventoryDetails(selectedInventory));
        physicalInventoryDetailsTableView.setItems(details);
        updateDifferentialAmount();
        confirmButton.setText("Update");
        confirmButton.setOnMouseClicked(mouseEvent -> initiateUpdate(selectedInventory));
        commitButton.setOnMouseClicked(mouseEvent -> commit());

        if (physicalInventory.isCommitted()) {
            commitButton.setDisable(true);
            confirmButton.setDisable(true);
        }
    }

    private void initiateUpdate(PhysicalInventory selectedInventory) {
        selectedInventory.setSupplier(selectedInventory.getSupplier());
        selectedInventory.setBranch(selectedInventory.getBranch());
        selectedInventory.setCategory(selectedInventory.getCategory());
        selectedInventory.setDateEncoded(Timestamp.valueOf(dateEncoded.getValue().atStartOfDay()));
        selectedInventory.setCutOffDate(Timestamp.valueOf(cutOffDate.getValue().atStartOfDay()));
        selectedInventory.setPriceType(priceType.getValue());
        selectedInventory.setStockType(inventoryType.getValue());
        selectedInventory.setRemarks(remarks.getText());

        boolean result = physicalInventoryDAO.updatePhysicalInventory(selectedInventory, details);

        if (!result) {
            DialogUtils.showErrorMessage("Error", "Failed to update physical inventory.");
        } else {
            showSuccessMessage();
        }
    }

    private void commit() {
        physicalInventory.setCommitted(true);

        boolean result = physicalInventoryDAO.commitPhysicalInventory(physicalInventory, details);

        if (!result) {
            DialogUtils.showErrorMessage("Error", "Failed to commit physical inventory.");
        } else {
            showSuccessMessage();
            commitButton.setDisable(true);
            confirmButton.setDisable(true);
        }
    }

    @Setter
    PhysicalInventorySummaryController physicalInventorySummaryController;
}
