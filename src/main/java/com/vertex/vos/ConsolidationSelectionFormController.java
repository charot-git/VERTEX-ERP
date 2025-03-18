package com.vertex.vos;

import com.vertex.vos.DAO.DispatchPlanDAO;
import com.vertex.vos.Objects.DispatchPlan;
import com.vertex.vos.Objects.StockTransfer;
import com.vertex.vos.Utilities.BranchDAO;
import com.vertex.vos.Utilities.DragDropDataStore;
import com.vertex.vos.Utilities.StockTransferDAO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.sql.Timestamp;
import java.util.Date;

public class ConsolidationSelectionFormController {

    @FXML
    private TableView<DispatchPlan> dispatchTableView;
    @FXML
    private TableView<StockTransfer> stockTransferTable;

    @FXML
    private TableColumn<DispatchPlan, String> dispatchNoCol;
    @FXML
    private TableColumn<DispatchPlan, String> clusterCol;
    @FXML
    private TableColumn<DispatchPlan, String> driverCol;
    @FXML
    private TableColumn<DispatchPlan, Timestamp> dispatchDateCol;

    @FXML
    private TableColumn<StockTransfer, String> stockNoCol;
    @FXML
    private TableColumn<StockTransfer, String> destinationCol;
    @FXML
    private TableColumn<StockTransfer, Date> leadDateCol;

    private final StockTransferDAO stockTransferDAO = new StockTransferDAO();
    private final DispatchPlanDAO dispatchPlanDAO = new DispatchPlanDAO();


    ObservableList<DispatchPlan> dispatchPlans = FXCollections.observableArrayList();

    private void initializeDispatchTable() {
        dispatchNoCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDispatchNo()));
        clusterCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCluster().getClusterName()));
        driverCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDriver().getUser_fname() + " " + cellData.getValue().getDriver().getUser_lname()));
        dispatchDateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDispatchDate()));

        dispatchTableView.setItems(dispatchPlans);

        dispatchPlans.removeAll(consolidationFormController.consolidation.getDispatchPlans());

        dispatchTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        dispatchTableView.setOnDragDetected(event -> {
            if (!dispatchTableView.getSelectionModel().isEmpty()) {
                Dragboard db = dispatchTableView.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                DragDropDataStore.setDraggedItems(dispatchTableView.getSelectionModel().getSelectedItems());
                content.putString("dragged");

                db.setContent(content);
                event.consume();
            }
        });
    }

    ObservableList<StockTransfer> stockTransfers = FXCollections.observableArrayList();


    private void initializeStockTransferTable() {
        BranchDAO branchDAO = new BranchDAO(); // Only used here, so instantiate locally

        stockNoCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStockNo()));
        destinationCol.setCellValueFactory(cellData -> new SimpleStringProperty(branchDAO.getBranchNameById(cellData.getValue().getTargetBranch())));
        leadDateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getLeadDate()));

        stockTransferTable.setItems(stockTransfers);

        stockTransfers.removeAll(consolidationFormController.consolidation.getStockTransfers());

        stockTransferTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        stockTransferTable.setOnDragDetected(event -> {
            if (!stockTransferTable.getSelectionModel().getSelectedItems().isEmpty()) {
                Dragboard db = stockTransferTable.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                DragDropDataStore.setDraggedItems(stockTransferTable.getSelectionModel().getSelectedItems());
                content.putString("dragged");
                db.setContent(content);
                event.consume();
            }
        });
    }

    ConsolidationFormController consolidationFormController;

    public void loadData(ConsolidationFormController consolidationFormController) {
        stockTransfers.setAll(stockTransferDAO.getAllGoodStockTransferHeaderForConsolidation());
        dispatchPlans.setAll(dispatchPlanDAO.getAllDispatchPlansForConsolidation());
        this.consolidationFormController = consolidationFormController;
        initializeDispatchTable();
        initializeStockTransferTable();
    }
}