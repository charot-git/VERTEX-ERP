package com.vertex.vos;

import com.vertex.vos.DAO.DispatchPlanDAO;
import com.vertex.vos.Enums.ConsolidationStatus;
import com.vertex.vos.Enums.SalesOrderStatus;
import com.vertex.vos.Objects.Consolidation;
import com.vertex.vos.Objects.DispatchPlan;
import com.vertex.vos.Objects.SalesOrder;
import com.vertex.vos.Objects.UserSession;
import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.DragDropDataStore;
import com.vertex.vos.Utilities.SalesOrderDAO;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PickingDispatchFormController implements Initializable {

    @FXML
    private BorderPane borderPane;

    @FXML
    private ButtonBar buttonBar;

    @FXML
    private TextField checkedByField;

    @FXML
    private Button confirmButton;

    @FXML
    private TextField createdByField;

    @FXML
    private TableColumn<SalesOrder, String> customerCol;

    @FXML
    private ButtonBar dispatchButtonBar;

    @FXML
    private Button dispatchConfirmButton;

    @FXML
    private TableColumn<DispatchPlan, String> dispatchNoCol;

    @FXML
    private BorderPane dispatchPane;

    @FXML
    private TableView<DispatchPlan> dispatchTableView;

    @FXML
    private Label docNoField;

    @FXML
    private TableColumn<DispatchPlan, String> driverCol;

    @FXML
    private TableColumn<SalesOrder, Button> pickButtonCol;

    @FXML
    private TableColumn<SalesOrder, String> poNoCol;

    @FXML
    private TableView<SalesOrder> salesOrderTableView;

    @FXML
    private TableColumn<SalesOrder, String> salesmanCol;

    @FXML
    private TableColumn<SalesOrder, String> soNoCol;

    @FXML
    private TableColumn<DispatchPlan, String> statusCol;

    @FXML
    private ComboBox<ConsolidationStatus> statusField;

    @FXML
    private TableColumn<SalesOrder, String> supplierCol;

    @Setter
    ConsolidationListController consolidationListController;

    Consolidation consolidation;

    ObservableList<DispatchPlan> dispatchPlans = FXCollections.observableArrayList();
    ObservableList<SalesOrder> salesOrders = FXCollections.observableArrayList();

    public void createNewConsolidationForDispatch() {
        consolidation = new Consolidation();
        consolidation.setConsolidationNo(consolidationListController.getConsolidationDAO().generateConsolidationNoForDispatch());
        consolidation.setCreatedBy(UserSession.getInstance().getUser());
        consolidation.setStatus(ConsolidationStatus.PENDING);
        consolidation.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        consolidation.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        TextFields.bindAutoCompletion(checkedByField, consolidationListController.getCheckers().stream()
                .map(user -> user.getUser_fname() + " " + user.getUser_lname())
                .toArray(String[]::new));
        checkedByField.textProperty().addListener((observable, oldValue, newValue) -> {
            consolidation.setCheckedBy(consolidationListController.getCheckers().stream()
                    .filter(user -> (user.getUser_fname() + " " + user.getUser_lname()).equals(newValue))
                    .findFirst().orElse(null));
        });

        Platform.runLater(() -> consolidationListController.newConsolidationStage.setTitle(consolidation.getConsolidationNo()));
        updateFields();

        consolidation.getDispatchPlans().addListener((ListChangeListener<DispatchPlan>) c -> {
            dispatchPlans.setAll(consolidation.getDispatchPlans());
        });

        consolidation.getDispatchPlans().addListener((ListChangeListener<DispatchPlan>) c -> {
            salesOrders.setAll(consolidation.getDispatchPlans().stream()
                    .flatMap(dispatchPlan -> dispatchPlan.getSalesOrders().stream())
                    .collect(Collectors.toList()));
        });

        confirmButton.setOnAction(actionEvent -> {
            createConsolidation();
        });
    }

    private void createConsolidation() {
        if (consolidation.getCheckedBy() == null) {
            DialogUtils.showErrorMessage("Error", "Please select a checker.");
            return;
        }
        if (consolidation.getDispatchPlans().isEmpty()) {
            DialogUtils.showErrorMessage("Error", "Please add dispatch plans.");
            return;
        }

        consolidation = consolidationListController.getConsolidationDAO().saveConsolidation(consolidation);
        if (consolidation.getId() > 0) {
            if (DialogUtils.showConfirmationDialog("Success", "Consolidation created successfully. Do you want to close this window?")) {
                consolidationListController.newConsolidationStage.close();
            }
            consolidationListController.loadConsolidationList();
        } else {
            DialogUtils.showErrorMessage("Error", "Unable to create consolidation.");
        }
    }

    private void updateFields() {
        docNoField.setText(consolidation.getConsolidationNo());
        createdByField.setText(consolidation.getCreatedBy().getUser_fname() + " " + consolidation.getCreatedBy().getUser_lname());
        if (consolidation.getCheckedBy() != null) {
            checkedByField.setText(consolidation.getCheckedBy().getUser_fname() + " " + consolidation.getCheckedBy().getUser_lname());
        } else {
            checkedByField.setPromptText("N/A");
        }
        statusField.setValue(consolidation.getStatus());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            if (consolidationListController != null) {
                statusField.setItems(FXCollections.observableArrayList(ConsolidationStatus.values()));
            }
        });

        initializeDispatchTable();
        initializeSalesOrderTable();


        dispatchConfirmButton.setOnAction(actionEvent -> openDispatchesForConsolidation());
    }

    Stage dispatchStage;

    DispatchPlanListController dispatchPlanListController;

    private void openDispatchesForConsolidation() {
        if (consolidation.getCheckedBy() == null) {
            DialogUtils.showErrorMessage("Error", "Please select a checker.");
            return;
        }

        if (dispatchStage == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("DispatchPlanList.fxml"));
                BorderPane root = loader.load();
                dispatchPlanListController = loader.getController();
                dispatchPlanListController.setConsolidation(consolidation);
                dispatchStage = new Stage();
                dispatchStage.setTitle("Dispatch Plans For Consolidation");
                dispatchStage.setScene(new Scene(root));
                dispatchStage.initStyle(StageStyle.UTILITY);
                dispatchStage.show();
                dispatchStage.setOnCloseRequest(event -> dispatchStage = null);
            } catch (IOException e) {
                DialogUtils.showErrorMessage("Error", "Unable to open.");
                e.printStackTrace();
            }
        } else {
            dispatchStage.toFront();
        }

    }

    private void initializeSalesOrderTable() {
        salesOrderTableView.setItems(salesOrders);
        soNoCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrderNo()));
        poNoCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPurchaseNo()));
        customerCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomer().getStoreName()));
        supplierCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSupplier().getSupplierName()));
        salesmanCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSalesman().getSalesmanName()));
        pickButtonCol.setCellFactory(col -> new TableCell<SalesOrder, Button>() {
            private final Button button = new Button();

            @Override
            protected void updateItem(Button item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    SalesOrder salesOrder = getTableRow().getItem();

                    if (consolidation.getStatus() != ConsolidationStatus.PICKING) {
                        button.setDisable(true);
                    }
                    button.setText(SalesOrderStatus.PICKED.getDbValue());
                    button.setOnAction(event -> {
                        pickSalesOrder(salesOrder);
                    });
                    setGraphic(button);
                }
            }

            private void pickSalesOrder(SalesOrder salesOrder) {
                boolean picked = salesOrderDAO.pickSalesOrder(salesOrder);
                button.setDisable(picked);
            }
        });
    }

    SalesOrderDAO salesOrderDAO = new SalesOrderDAO();
    DispatchPlanDAO dispatchPlanDAO = new DispatchPlanDAO();

    private void initializeDispatchTable() {
        dispatchTableView.setItems(dispatchPlans);
        dispatchNoCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDispatchNo()));
        driverCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDriver().getUser_fname() + " " + cellData.getValue().getDriver().getUser_lname()));
        statusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().toString()));


        implementReceiveOnDrag();
    }


    private void implementReceiveOnDrag() {
        dispatchTableView.setOnDragOver(event -> {
            if (event.getGestureSource() != dispatchTableView && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        dispatchTableView.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if ("dragged".equals(db.getString())) {
                List<DispatchPlan> droppedItems = DragDropDataStore.getDraggedItems();

                if (droppedItems == null || droppedItems.isEmpty()) {
                    DialogUtils.showErrorMessage("Error", "No valid items found.");
                    return;
                }

                for (DispatchPlan item : droppedItems) {
                    boolean exists = consolidation.getDispatchPlans().stream()
                            .anyMatch(dp -> Objects.equals(dp.getDispatchNo(), item.getDispatchNo()));

                    if (exists) {
                        DialogUtils.showErrorMessage("Error", "Dispatch plan already exists in the consolidation: " + item.getDispatchNo());
                    } else {
                        item.setSalesOrders(dispatchPlanDAO.getSalesOrdersForDispatchPlan(item.getDispatchId()));
                        consolidation.getDispatchPlans().add(item);
                        dispatchPlanListController.getDispatchPlans().remove(item);
                    }
                }

                success = true;
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }
}
