package com.vertex.vos;

import com.vertex.vos.Enums.ConsolidationStatus;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.BranchDAO;
import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.DragDropDataStore;
import javafx.application.Platform;
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
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
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
    private TableColumn<DispatchPlan, String> driverCol;
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

    private final BranchDAO branchDAO = new BranchDAO();
    Consolidation consolidation;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        statusField.setItems(FXCollections.observableArrayList(ConsolidationStatus.values()));
        Platform.runLater(() -> {
            initializeDispatchTable();
            initializeStockTransferTable();
            openSelectionButton.setOnAction(event -> {
                openSelectionWindow();
            });
        });
    }

    Stage consolidationStage;
    ConsolidationSelectionFormController selectionFormController;

    private void openSelectionWindow() {
        if (consolidationStage != null && consolidationStage.isShowing()) {
            consolidationStage.toFront();
            return;
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ConsolidationSelectionForm.fxml"));
            Parent root = fxmlLoader.load();
            selectionFormController = fxmlLoader.getController();
            selectionFormController.loadData(this);
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
        dispatchTableView.setItems(consolidation.getDispatchPlans());

        dispatchNoCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDispatchNo()));
        clusterCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCluster().getClusterName()));
        driverCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDriver().getUser_fname() + " " + cellData.getValue().getDriver().getUser_lname()));
        dispatchDateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDispatchDate()));

        dispatchTableView.setOnDragOver(event -> {
            if (event.getGestureSource() != dispatchTableView && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        dispatchTableView.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if ("dragged".equals(db.getString())) {
                List<DispatchPlan> draggedItems = DragDropDataStore.getDraggedItems();

                for (DispatchPlan item : draggedItems) {
                    if (!consolidation.getDispatchPlans().contains(item)) {
                        consolidation.getDispatchPlans().add(item);
                    }
                }
                selectionFormController.dispatchPlans.removeAll(draggedItems);
                dispatchTableView.refresh();
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        dispatchTableView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                consolidation.getDispatchPlans().removeAll(dispatchTableView.getSelectionModel().getSelectedItems());
            }
        });

    }

    private void initializeStockTransferTable() {
        stockTransferTable.setItems(consolidation.getStockTransfers());

        stockNoCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStockNo()));
        destinationCol.setCellValueFactory(cellData -> new SimpleStringProperty(branchDAO.getBranchNameById(cellData.getValue().getTargetBranch())));
        leadDateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getLeadDate()));

        stockTransferTable.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(DataFormat.PLAIN_TEXT) && event.getGestureSource() != stockTransferTable) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        stockTransferTable.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if ("dragged".equals(db.getString())) {
                List<StockTransfer> draggedItems = DragDropDataStore.getDraggedItems();

                for (StockTransfer item : draggedItems) {
                    if (!consolidation.getStockTransfers().contains(item)) {
                        consolidation.getStockTransfers().add(item);
                    }
                }
                selectionFormController.stockTransfers.removeAll(draggedItems);
                stockTransferTable.refresh();
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        stockTransferTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                consolidation.getStockTransfers().removeAll(stockTransferTable.getSelectionModel().getSelectedItems());
            }
        });
    }


    public void initializeConsolidationCreation() {
        consolidation = new Consolidation();
        consolidation.setConsolidationNo(consolidationListController.getConsolidationDAO().generateConsolidationNo());
        consolidation.setCreatedBy(UserSession.getInstance().getUser());
        consolidation.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        consolidation.setStatus(ConsolidationStatus.PENDING);
        consolidation.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        docnoLabel.setText(consolidation.getConsolidationNo());
        statusField.setValue(consolidation.getStatus());

        openSelectionButton.setDisable(true);
        confirmButton.setDisable(true);

        TextFields.bindAutoCompletion(checkerField, consolidationListController.getCheckers().stream()
                .map(user -> user.getUser_fname() + " " + user.getUser_lname())
                .toArray(String[]::new));

        checkerField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                consolidation.setCheckedBy(consolidationListController.getCheckers().stream()
                        .filter(user -> (user.getUser_fname() + " " + user.getUser_lname()).equals(newValue))
                        .findFirst().orElse(null));

                if (consolidation.getCheckedBy() != null) {
                    confirmButton.setDisable(false);
                    openSelectionButton.setDisable(false);
                }
            }
        });

        confirmButton.setOnAction(event -> {
            consolidation = consolidationListController.getConsolidationDAO().saveConsolidation(consolidation);
            if (consolidation.getId() > 0) {
                if (DialogUtils.showConfirmationDialog("Success", "Consolidation created successfully, close window?")) {
                    consolidationListController.getConsolidationStage().close();
                }
            }
            consolidationListController.loadConsolidationList();
            consolidationStage.close();
        });
    }

    @Setter
    ConsolidationListController consolidationListController;

    public void initializeConsolidationUpdate(Consolidation selectedConsolidation) {
        this.consolidation = selectedConsolidation;
        docnoLabel.setText(consolidation.getConsolidationNo());
        statusField.setValue(consolidation.getStatus());
        checkerField.setText(consolidation.getCheckedBy().getUser_fname() + " " + consolidation.getCheckedBy().getUser_lname());
        consolidation.setDispatchPlans(selectedConsolidation.getDispatchPlans());
        consolidation.setStockTransfers(selectedConsolidation.getStockTransfers());

        TextFields.bindAutoCompletion(checkerField, consolidationListController.getCheckers().stream()
                .map(user -> user.getUser_fname() + " " + user.getUser_lname())
                .toArray(String[]::new));

        checkerField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                consolidation.setCheckedBy(consolidationListController.getCheckers().stream()
                        .filter(user -> (user.getUser_fname() + " " + user.getUser_lname()).equals(newValue))
                        .findFirst().orElse(null));
            }
        });

        confirmButton.setOnAction(event -> {
            consolidation.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            consolidation = consolidationListController.getConsolidationDAO().updateConsolidation(consolidation);
            if (consolidation.getId() > 0) {
                if (DialogUtils.showConfirmationDialog("Success", "Consolidation updated successfully, close window?")) {
                    consolidationListController.getConsolidationFormStageForUpdate().close();
                }
            }
            consolidationListController.loadConsolidationList();
            consolidationStage.close();
        });

    }
}