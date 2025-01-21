package com.vertex.vos;

import com.vertex.vos.DAO.PhysicalInventoryDetailsDAO;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import org.controlsfx.control.textfield.TextFields;

import java.time.LocalDate;
import java.util.List;

public class PhysicalInventoryController {

    @FXML private TextField branchCode, branchFilter, productCategoryFilter, supplierFilter;
    @FXML private Button commitButton, confirmButton;
    @FXML private DatePicker cutOffDate, dateEncoded;
    @FXML private Label differentialAmount, header;
    @FXML private ComboBox<String> inventoryType, priceType;
    @FXML private TextArea remarks;
    @FXML private TableColumn<PhysicalInventoryDetails, String> breakdownCol, codeCol, nameCol, unitCol;
    @FXML private TableColumn<PhysicalInventoryDetails, Integer> physCountCol, sysCountCol, varianceCol;
    @FXML private TableColumn<PhysicalInventoryDetails, Double> differenceCol, priceCol;

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
        physicalInventory = new PhysicalInventory();
        physicalInventory.setPhNo("PH " + nextNo);
        physicalInventory.setEncoderId(UserSession.getInstance().getUserId());
        header.setText(physicalInventory.getPhNo());

        setupAutoCompletion();
        initializeComboBoxes();
        setupDatePickers();
        setupListeners();
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
        supplierFilter.textProperty().addListener((observable, oldValue, newValue) -> getSupplierProducts());
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
        Task<List<Integer>> task = new Task<>() {
            @Override
            protected List<Integer> call() {
                return productsPerSupplierDAO.getProductsForSupplier(supplier.getId());
            }
        };

        task.setOnSucceeded(event -> {
            List<Integer> supplierProducts = task.getValue();
            supplierProducts.forEach(productId -> details.add(physicalInventoryDetailsDAO.getInventory(productId, branch, category)));
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            DialogUtils.showErrorMessage("Error", exception.getMessage());
        });

        new Thread(task).start();
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

    private void getSupplierProducts() {
        supplier = supplierDAO.getSupplierByName(supplierFilter.getText());
    }
}
