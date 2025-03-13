package com.vertex.vos;

import com.vertex.vos.DAO.ConsolidationDAO;
import com.vertex.vos.Enums.ConsolidationStatus;
import com.vertex.vos.Objects.Consolidation;
import com.vertex.vos.Objects.User;
import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.EmployeeDAO;
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
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ResourceBundle;

public class ConsolidationListController implements Initializable {
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

    @Getter
    private Stage consolidationStage;

    @Setter
    private InternalOperationsContentController internalOperationsContentController;

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
        confirmButton.setOnAction(event -> createNewConsolidation());
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
                return consolidationDAO.getAllConsolidations(pageSize, offset, consolidationNoFilter.getText(), null, dateFrom, dateTo, statusFilter.getValue());
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

    private void createNewConsolidation() {
        try {
            if (consolidationStage != null) {
                consolidationStage.show();
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ConsolidationForm.fxml"));
            Parent root = fxmlLoader.load();
            ConsolidationFormController controller = fxmlLoader.getController();
            controller.setConsolidationListController(this);
            controller.initializeConsolidationCreation();
            consolidationStage = new Stage();
            consolidationStage.setMaximized(true);
            consolidationStage.setScene(new Scene(root));
            consolidationStage.show();
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
    Stage checklistForm;
    public void openConsolidationForChecking(Consolidation selectedConsolidation, ObservableList<ChecklistDTO> checklistProducts) {
        try {
            if (checklistForm != null) {
                checklistForm.show();
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ConsolidationCheckList.fxml"));
            Parent root = fxmlLoader.load();
            ConsolidationCheckListController controller = fxmlLoader.getController();
            controller.setConsolidation(selectedConsolidation);
            controller.setConsolidationListController(this);
            controller.updateFields();
            checklistForm = new Stage();
            checklistForm.setMaximized(true);
            checklistForm.setScene(new Scene(root));
            checklistForm.show();
        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Failed to open consolidation form.");
            e.printStackTrace();
        }
    }
}
