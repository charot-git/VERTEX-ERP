package com.vertex.vos;

import com.vertex.vos.Enums.ConsolidationStatus;
import com.vertex.vos.Objects.Consolidation;
import com.vertex.vos.Objects.DispatchPlan;
import com.vertex.vos.Objects.StockTransfer;
import com.vertex.vos.Objects.UserSession;
import com.vertex.vos.Utilities.BranchDAO;
import com.vertex.vos.Utilities.DialogUtils;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import lombok.Setter;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Date;
import java.util.ResourceBundle;

public class ConsolidationFormController implements Initializable {
    @FXML
    private TableView<DispatchPlan> dispatchTableView;
    @FXML
    private TableView<StockTransfer> stockTransferTable;

    @FXML
    private TableColumn<DispatchPlan, String> dispatchNoCol;
    @FXML
    private TableColumn<DispatchPlan, String> clusterCol;
    @FXML
    private TableColumn<DispatchPlan, String> vehicleCol;
    @FXML
    private TableColumn<DispatchPlan, Timestamp> dispatchDateCol;

    @FXML
    private TableColumn<StockTransfer, String> stockNoCol;
    @FXML
    private TableColumn<StockTransfer, String> destinationCol;
    @FXML
    private TableColumn<StockTransfer, Date> leadDateCol;


    @FXML
    private ButtonBar buttonBar;

    @FXML
    private TextField checkerField;


    @FXML
    private Button confirmButton;


    @FXML
    private Label docnoLabel;


    @FXML
    private Button openSelectionButton;

    @FXML
    private ComboBox<ConsolidationStatus> statusField;

    Consolidation consolidation;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeDispatchTable();
        initializeStockTransferTable();
        statusField.setItems(FXCollections.observableArrayList(ConsolidationStatus.values()));

        openSelectionButton.setOnAction(event -> {
            openSelectionWindow();
        });
    }

    Stage consolidationStage;

    private void openSelectionWindow() {
        // Only allow one instance of the selection window
        if (consolidationStage != null && consolidationStage.isShowing()) {
            consolidationStage.toFront();
            return;
        }
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ConsolidationSelectionForm.fxml"));
            Parent root = fxmlLoader.load();
            ConsolidationSelectionFormController controller = fxmlLoader.getController();
            controller.loadData();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
            consolidationStage = stage;
        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Failed to open selection window.");
            e.printStackTrace();
        }
    }

    private void initializeDispatchTable() {
        dispatchNoCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDispatchNo()));
        clusterCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCluster().getClusterName()));
        vehicleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getVehicle().getVehiclePlate()));
        dispatchDateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDispatchDate()));

        dispatchTableView.setOnDragOver(event -> {
            if (event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
        });
    }

    private void initializeStockTransferTable() {
        BranchDAO branchDAO = new BranchDAO();
        stockNoCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStockNo()));
        destinationCol.setCellValueFactory(cellData -> new SimpleStringProperty(branchDAO.getBranchNameById(cellData.getValue().getTargetBranch())));
        leadDateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getLeadDate()));
        stockTransferTable.setOnDragOver(event -> {
            if (event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY);
                event.setDropCompleted(true);
            }
        });
    }


    public void initializeConsolidationCreation() {
        consolidation = new Consolidation();
        consolidation.setConsolidationNo(consolidationListController.getConsolidationDAO().generateConsolidationNo());
        consolidation.setCreatedBy(UserSession.getInstance().getUser());
        consolidation.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        consolidation.setStatus(ConsolidationStatus.PENDING);
        docnoLabel.setText(consolidation.getConsolidationNo());
    }

    @Setter
    ConsolidationListController consolidationListController;

}
