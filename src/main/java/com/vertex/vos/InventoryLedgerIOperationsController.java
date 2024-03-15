package com.vertex.vos;

import com.vertex.vos.Constructors.Inventory;
import com.vertex.vos.Constructors.ProductInventory;
import com.vertex.vos.Constructors.ProductsInTransact;
import com.vertex.vos.Utilities.*;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class InventoryLedgerIOperationsController implements Initializable {
    InventoryDAO inventoryDAO = new InventoryDAO();
    BranchDAO branchDAO = new BranchDAO();
    ProductDAO productDAO = new ProductDAO();
    private AnchorPane contentPane;
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

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    private final HistoryManager historyManager = new HistoryManager();

    private int currentNavigationId = -1; // Initialize to a default value

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setComboBoxBehaviour();
        populateComboBoxes();
        branchListComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                loadAllInventoryItems();
            } else if (newValue.equals("All")) {
                loadAllInventoryItems();
            } else {
                inventoryLabel.setText(newValue);
                ObservableList<Inventory> filteredInventoryItems = inventoryDAO.getInventoryItemsByBranch(branchDAO.getBranchIdByName(newValue));
                inventoryTableView.setItems(filteredInventoryItems);
            }
        });

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

        // Load all inventory items initially
        loadAllInventoryItems();
    }
    BrandDAO brandDAO = new BrandDAO();
    CategoriesDAO categoriesDAO = new CategoriesDAO();
    ProductClassDAO classDAO = new ProductClassDAO();
    SegmentDAO segmentDAO = new SegmentDAO();
    NatureDAO natureDAO = new NatureDAO();
    SectionsDAO sectionsDAO = new SectionsDAO();

    private void populateComboBoxes() {
        branchListComboBox.setItems(branchDAO.getAllBranchNames());
        branchListComboBox.getItems().addFirst("All");
        brandComboBox.setItems(brandDAO.getBrandNames());
        categoryComboBox.setItems(categoriesDAO.getCategoryNames());
        classComboBox.setItems(classDAO.getProductClassNames());
        segmentComboBox.setItems(segmentDAO.getSegmentNames());
        natureComboBox.setItems(natureDAO.getNatureNames());
        sectionComboBox.setItems(sectionsDAO.getSectionNames());
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

    private void loadAllInventoryItems() {
        inventoryLabel.setText("All");
        ObservableList<Inventory> allInventoryItems = inventoryDAO.getAllInventoryItems();
        inventoryTableView.setItems(allInventoryItems);
    }

}
