package com.vertex.vos;

import com.vertex.vos.Constructors.ProductsInTransact;
import com.vertex.vos.Utilities.BranchDAO;
import com.vertex.vos.Utilities.InventoryDAO;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class stockTransferController {
    AnchorPane contentPane;

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    @FXML
    private VBox addBoxes;

    @FXML
    private VBox addProductButton;

    @FXML
    private Label addProductLabel;

    @FXML
    private HBox confirmBox;

    @FXML
    private Button confirmButton;

    @FXML
    private Label date;

    @FXML
    private Label grandTotal;

    @FXML
    private ComboBox<String> sourceBranch;

    @FXML
    private VBox sourceBranchBox;

    @FXML
    private Label sourceBranchErr;

    @FXML
    private HBox statusBox;

    @FXML
    private ImageView statusImage;

    @FXML
    private Label statusLabel;

    @FXML
    private ComboBox<String> targetBranch;

    @FXML
    private VBox targetBranchBox;

    @FXML
    private Label targetBranchErr;

    @FXML
    private HBox totalBox;

    @FXML
    private VBox totalBoxLabels;

    @FXML
    private VBox totalVBox;

    @FXML
    private TableView<ProductsInTransact> transferTable;

    InventoryDAO inventoryDAO = new InventoryDAO();
    BranchDAO branchDAO = new BranchDAO();

    public void createNewTransfer() {
        sourceBranch.setItems(inventoryDAO.getBranchNamesWithInventory());
        targetBranchBox.setDisable(true);

        // Add a listener to the sourceBranch ComboBox
        sourceBranch.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                targetBranchBox.setDisable(newValue == null);
                ObservableList<String> allBranchNames = branchDAO.getAllBranchNames();
                if (newValue != null) {
                    allBranchNames.remove(newValue);
                }
                targetBranch.setItems(allBranchNames);
            }
        });

        initializeTable();

    }

    private void initializeTable() {
        transferTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ProductsInTransact, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<ProductsInTransact, String> unitColumn = new TableColumn<>("Unit");
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));

        TableColumn<ProductsInTransact, Integer> quantityAvailableColumn = new TableColumn<>("Quantity Available");
        quantityAvailableColumn.setCellValueFactory(new PropertyValueFactory<>("quantityAvailable"));

        TableColumn<ProductsInTransact, Integer> orderQuantityColumn = new TableColumn<>("Order Quantity");
        orderQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("orderQuantity"));

        TableColumn<ProductsInTransact, Double> totalAmountColumn = new TableColumn<>("Total Amount");
        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        // Add columns to the transferTable
        transferTable.getColumns().addAll(descriptionColumn, unitColumn, quantityAvailableColumn, orderQuantityColumn, totalAmountColumn);
    }
}
