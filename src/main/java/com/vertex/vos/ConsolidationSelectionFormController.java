package com.vertex.vos;

import com.vertex.vos.DAO.DispatchPlanDAO;
import com.vertex.vos.Objects.DispatchPlan;
import com.vertex.vos.Objects.StockTransfer;
import com.vertex.vos.Utilities.BranchDAO;
import com.vertex.vos.Utilities.StockTransferDAO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.net.URL;
import java.sql.Timestamp;
import java.util.Date;
import java.util.ResourceBundle;

public class ConsolidationSelectionFormController implements Initializable {

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

    private final StockTransferDAO stockTransferDAO = new StockTransferDAO();
    private final DispatchPlanDAO dispatchPlanDAO = new DispatchPlanDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeDispatchTable();
        initializeStockTransferTable();
    }
    ObservableList<DispatchPlan> dispatchPlans = FXCollections.observableArrayList();

    private void initializeDispatchTable() {

        dispatchNoCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDispatchNo()));
        clusterCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCluster().getClusterName()));
        vehicleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getVehicle().getVehiclePlate()));
        dispatchDateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDispatchDate()));

        dispatchTableView.setItems(dispatchPlans);

        dispatchTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        dispatchTableView.setOnDragDetected(event -> {
            if (!dispatchTableView.getSelectionModel().isEmpty()) {
                Dragboard db = dispatchTableView.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(dispatchTableView.getSelectionModel().getSelectedItem().getDispatchNo());
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

        stockTransferTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        stockTransferTable.setOnDragDetected(event -> {
            if (!stockTransferTable.getSelectionModel().isEmpty()) {
                Dragboard db = stockTransferTable.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(stockTransferTable.getSelectionModel().getSelectedItem().getStockNo());
                db.setContent(content);
                event.consume();
            }
        });
    }

    public void loadData() {
        stockTransfers.setAll(stockTransferDAO.getAllGoodStockTransferHeaderForConsolidation());
        dispatchPlans.setAll(dispatchPlanDAO.getAllDispatchPlansForConsolidation());
    }
}
