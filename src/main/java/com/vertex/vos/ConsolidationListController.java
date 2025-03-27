package com.vertex.vos;

import com.vertex.vos.DAO.ConsolidationDAO;
import com.vertex.vos.Enums.ConsolidationStatus;
import com.vertex.vos.Objects.Consolidation;
import com.vertex.vos.Objects.User;
import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.EmployeeDAO;
import com.vertex.vos.Utilities.MaintenanceAlert;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import lombok.Getter;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ResourceBundle;

public class ConsolidationListController implements Initializable {
    public Label header;
    @FXML
    private TextField checkedByFilter;
    @FXML
    private TextField consolidationNoFilter;
    @FXML
    private Button confirmButton;
    @FXML
    private TableView<Consolidation> consolidationTable;
    @FXML
    private TableColumn<Consolidation, String> checkedByCol;
    @FXML
    private TableColumn<Consolidation, String> consolidationNoCol;
    @FXML
    private TableColumn<Consolidation, String> consolidationStatusCol;
    @FXML
    private TableColumn<Consolidation, Timestamp> createAtCol;
    @FXML
    private TableColumn<Consolidation, String> createdByCol;
    @FXML
    private DatePicker dateFromFilter;
    @FXML
    private DatePicker dateToFilter;
    @FXML
    private ComboBox<ConsolidationStatus> statusFilter;
    @Getter
    ConsolidationDAO consolidationDAO = new ConsolidationDAO();
    private final ObservableList<Consolidation> consolidations = FXCollections.observableArrayList();
    private boolean isLoading = false;
    private int offset = 0;
    private final int pageSize = 35;

    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    @Getter
    private final ObservableList<User> checkers = FXCollections.observableArrayList(employeeDAO.getAllEmployeesWhereDepartment(5));
    Stage newConsolidationStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        statusFilter.setItems(FXCollections.observableArrayList(ConsolidationStatus.values()));
        initializeColumns();

        // Bind auto-completion for checkedByFilter
        TextFields.bindAutoCompletion(checkedByFilter, checkers.stream()
                .map(user -> user.getUser_fname() + " " + user.getUser_lname())
                .toArray(String[]::new));

        // Add listeners to filters
        checkedByFilter.textProperty().addListener((observable, oldValue, newValue) -> reloadConsolidations());
        dateFromFilter.valueProperty().addListener((observable, oldValue, newValue) -> reloadConsolidations());
        dateToFilter.valueProperty().addListener((observable, oldValue, newValue) -> reloadConsolidations());
        statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> reloadConsolidations());
        consolidationNoFilter.textProperty().addListener((observable, oldValue, newValue) -> reloadConsolidations());

        // Set scroll event listener
        consolidationTable.setOnScroll(event -> {
            if (isScrollAtBottom() && !isLoading) {
                loadMoreData();
            }
        });

        consolidationTable.setOnMouseClicked(event -> {
            Consolidation selectedConsolidation = consolidationTable.getSelectionModel().getSelectedItem();
            if (selectedConsolidation != null) {
                loadConsolidationCardPane(selectedConsolidation);
            }
        });



        confirmButton.setOnAction(event ->
        {
            if (consolidationType.equals("DISPATCH")) {
                createConsolidationForDispatches();
            } else if (consolidationType.equals("STOCK TRANSFER")) {
                MaintenanceAlert.showMaintenanceAlert();
            }
        });
    }

    @FXML
    BorderPane borderPane;

    private void loadConsolidationCardPane(Consolidation selectedConsolidation) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ConsolidationCard.fxml"));
            Parent root = fxmlLoader.load();
            ConsolidationCardController controller = fxmlLoader.getController();
            controller.setConsolidation(selectedConsolidation);
            controller.setConsolidationListController(this);
            borderPane.setRight(root);
        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Failed to open consolidation card.");
            e.printStackTrace();
        }
    }

    public void loadConsolidationList() {
        offset = 0;
        consolidations.clear();
        loadMoreData();
    }

    private void reloadConsolidations() {
        offset = 0;
        consolidations.clear();
        loadMoreData();
    }

    private void loadMoreData() {
        if (isLoading) return;
        isLoading = true;

        Timestamp dateFrom = dateFromFilter.getValue() != null ? Timestamp.valueOf(dateFromFilter.getValue().atStartOfDay()) : null;
        Timestamp dateTo = dateToFilter.getValue() != null ? Timestamp.valueOf(dateToFilter.getValue().atStartOfDay()) : null;

        Task<ObservableList<Consolidation>> task = new Task<>() {
            @Override
            protected ObservableList<Consolidation> call() {
                return consolidationDAO.getAllConsolidations(pageSize, offset, consolidationType, consolidationNoFilter.getText(), null, dateFrom, dateTo, statusFilter.getValue());
            }
        };

        task.setOnSucceeded(event -> {
            ObservableList<Consolidation> newItems = task.getValue();
            if (!newItems.isEmpty()) {
                offset += pageSize; // Increase offset only when data is loaded
                consolidations.addAll(newItems);
            }
            isLoading = false;
        });

        task.setOnFailed(event -> {
            task.getException().printStackTrace();
            isLoading = false;
        });

        new Thread(task).start();
    }

    private boolean isScrollAtBottom() {
        if (consolidationTable.getItems().isEmpty()) return false;

        ScrollBar verticalScrollBar = (ScrollBar) consolidationTable.lookup(".scroll-bar:vertical");
        return verticalScrollBar != null && verticalScrollBar.getValue() >= verticalScrollBar.getMax() * 0.9;
    }

    private void createConsolidationForDispatches() {
        try {
            if (newConsolidationStage != null) {
                newConsolidationStage.show();
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PickingDispatchForm.fxml"));
            Parent root = fxmlLoader.load();
            PickingDispatchFormController controller = fxmlLoader.getController();
            controller.setConsolidationListController(this);
            controller.createNewConsolidationForDispatch();
            newConsolidationStage = new Stage();
            newConsolidationStage.setMaximized(true);
            newConsolidationStage.setScene(new Scene(root));
            newConsolidationStage.show();
            newConsolidationStage.setOnCloseRequest(event -> {
                newConsolidationStage = null;
            });
        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Failed to open consolidation form.");
            e.printStackTrace();
        }
    }

    private void initializeColumns() {
        consolidationNoCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getConsolidationNo()));
        consolidationStatusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().name()));
        checkedByCol.setCellValueFactory(cellData -> {
            User checkedBy = cellData.getValue().getCheckedBy();
            return new SimpleStringProperty(checkedBy != null ? checkedBy.getUser_fname() + " " + checkedBy.getUser_lname() : "N/A");
        });
        createAtCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCreatedAt()));
        createdByCol.setCellValueFactory(cellData -> {
            User createdBy = cellData.getValue().getCreatedBy();
            return new SimpleStringProperty(createdBy != null ? createdBy.getUser_fname() + " " + createdBy.getUser_lname() : "N/A");
        });

        consolidationTable.setItems(consolidations);
    }

    @Getter
    Stage consolidationFormStageForUpdate;

    public void openConsolidationForUpdate(Consolidation selectedConsolidation) {
        try {
            if (consolidationFormStageForUpdate != null) {
                consolidationFormStageForUpdate.show();
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ConsolidationForm.fxml"));
            Parent root = fxmlLoader.load();
            ConsolidationFormController controller = fxmlLoader.getController();
            controller.setConsolidationListController(this);
            controller.initializeConsolidationUpdate(selectedConsolidation);

            consolidationFormStageForUpdate = new Stage();
            consolidationFormStageForUpdate.setMaximized(true);
            consolidationFormStageForUpdate.setScene(new Scene(root));
            consolidationFormStageForUpdate.show();
        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Failed to open consolidation form.");
            e.printStackTrace();
        }
    }


    String consolidationType;



    public boolean startPicking(Consolidation consolidation) {
        boolean success = consolidationDAO.startPicking(consolidation);
        if (success) {
            DialogUtils.showConfirmationDialog("Success", "Consolidation is now for picking.");
            loadConsolidationList();
        } else {
            DialogUtils.showErrorMessage("Error", "Failed to pick consolidation.");
        }
        return success;
    }

    public void setConsolidationType(String consolidationType) {
        this.consolidationType = consolidationType;
        Platform.runLater(() -> {
            if (this.consolidationType.equals("DISPATCH")) {
                header.setText("Approved Deliveries");
            } else if (this.consolidationType.equals("STOCK TRANSFER")) {
                header.setText("Stock Transfer Consolidations");
            } else {
                header.setText("Error");
            }
            loadConsolidationList();
        });
    }

    public boolean pickConsolidation(Consolidation consolidation) {
        boolean success = consolidationDAO.pickConsolidation(consolidation);
        if (success) {
            DialogUtils.showConfirmationDialog("Success", "Consolidation picked successfully.");
            loadConsolidationList();
        } else {
            DialogUtils.showErrorMessage("Error", "Failed to pick consolidation.");
        }
        return success;
    }
}
