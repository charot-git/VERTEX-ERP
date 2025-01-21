package com.vertex.vos;

import com.vertex.vos.DAO.PhysicalInventoryDAO;
import com.vertex.vos.Objects.PhysicalInventory;
import com.vertex.vos.Utilities.DialogUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ResourceBundle;

public class PhysicalInventorySummaryController implements Initializable {

    @FXML
    private TableColumn<PhysicalInventory, String> branchCodeCol;

    @FXML
    private TableColumn<PhysicalInventory, String> branchDescriptionCol;

    @FXML
    private TableColumn<PhysicalInventory, String> categoryCol;

    @FXML
    private Button createButton;

    @FXML
    private TableColumn<PhysicalInventory, Timestamp> dateCutOffCol;

    @FXML
    private TableColumn<PhysicalInventory, Timestamp> dateEncodedCol;

    @FXML
    private TextField noFilter;

    @FXML
    private TableColumn<PhysicalInventory, String> phNoCol;

    @FXML
    private TableView<PhysicalInventory> physicalInventoryHeaderTableView;

    @FXML
    private TableColumn<PhysicalInventory, String> postStatusCol;

    @FXML
    private TableColumn<PhysicalInventory, String> supplierCol;

    private final PhysicalInventoryDAO physicalInventoryDAO = new PhysicalInventoryDAO();

    private final ObservableList<PhysicalInventory> physicalInventoryList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Bind columns to the appropriate properties of PhysicalInventory
        phNoCol.setCellValueFactory(new PropertyValueFactory<>("phNo"));
        dateEncodedCol.setCellValueFactory(new PropertyValueFactory<>("dateEncoded"));
        dateCutOffCol.setCellValueFactory(new PropertyValueFactory<>("cutOffDate"));
        supplierCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSupplier().getSupplierName()));
        categoryCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory().getCategoryName()));
        branchCodeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBranch().getBranchCode()));
        branchDescriptionCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBranch().getBranchDescription()));
        postStatusCol.setCellValueFactory(cellData -> {
            boolean isCommitted = cellData.getValue().isCommitted();
            return new SimpleStringProperty(isCommitted ? "Committed" : "Not Committed");
        });
        physicalInventoryList.setAll(physicalInventoryDAO.getAllPhysicalInventories());
        physicalInventoryHeaderTableView.setItems(physicalInventoryList);
        
        createButton.setOnMouseClicked(mouseEvent -> createNewPhysicalInventory());

        physicalInventoryHeaderTableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                PhysicalInventory selectedInventory = physicalInventoryHeaderTableView.getSelectionModel().getSelectedItem();
                if (selectedInventory != null) {
                    openExistingPhysicalInventory(selectedInventory);
                }
            }
        });

    }

    private void openExistingPhysicalInventory(PhysicalInventory selectedInventory) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PhysicalInventory.fxml"));
            Parent root = loader.load();

            // Get the controller for the PhysicalInventory.fxml
            PhysicalInventoryController controller = loader.getController();

            // Call a method to load the existing inventory
            controller.loadExistingPhysicalInventory(selectedInventory);

            Stage stage = new Stage();
            stage.setTitle("Edit Physical Inventory");
            stage.setMaximized(true);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Unable to open the selected physical inventory.");
            e.printStackTrace();
        }
    }

    private void createNewPhysicalInventory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PhysicalInventory.fxml"));
            Parent root = loader.load();
            PhysicalInventoryController controller = loader.getController();

            controller.createNewPhysicalInventory(physicalInventoryDAO.getNextNo());;

            Stage stage = new Stage();
            stage.setTitle("Physical Inventory");
            stage.setMaximized(true);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Unable to open receiving.");
            e.printStackTrace();
        }

    }
}
