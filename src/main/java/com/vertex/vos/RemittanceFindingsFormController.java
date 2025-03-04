package com.vertex.vos;

import com.vertex.vos.DAO.CollectionDAO;
import com.vertex.vos.DAO.RemittanceAuditFindingsDAO;
import com.vertex.vos.Objects.CollectionDetail;
import com.vertex.vos.Objects.RemittanceAuditFinding;
import com.vertex.vos.Objects.Salesman;
import com.vertex.vos.Objects.UserSession;
import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.SalesmanDAO;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class RemittanceFindingsFormController implements Initializable {

    public DatePicker dateAuditedPicker;
    public TableColumn<CollectionDetail, Timestamp> collectionDateCol;
    public TableColumn<CollectionDetail, Timestamp> collectionDateColSelection;
    @FXML
    private Button addSelectedItems;

    @FXML
    private TableColumn<CollectionDetail, Double> amountCol;

    @FXML
    private TableColumn<CollectionDetail, Double> amountColSelection;

    @FXML
    private TextField auditeeField;

    @FXML
    private TableColumn<CollectionDetail, String> balanceTypeCol;

    @FXML
    private TableColumn<CollectionDetail, String> balanceTypeColSelection;

    @FXML
    private TableColumn<CollectionDetail, String> coaCol;

    @FXML
    private TableColumn<CollectionDetail, String> coaColSelection;

    @FXML
    private TableView<CollectionDetail> collectionDetailsTableView;

    @FXML
    private TableColumn<CollectionDetail, String> collectionNoCol;

    @FXML
    private TableColumn<CollectionDetail, String> collectionNoColSelection;

    @FXML
    private TableView<CollectionDetail> collectionSelectionTableView;

    @FXML
    private Button confirmButton;

    @FXML
    private DatePicker dateCreatedPicker;

    @FXML
    private DatePicker dateFromPicker;

    @FXML
    private DatePicker dateToPicker;

    @FXML
    private Label header;

    @FXML
    private Label rafAmount;

    @FXML
    private TableColumn<CollectionDetail, String> remarksCol;

    @FXML
    private TitledPane selectionTiltedPane;

    @Setter
    RemittanceFindingsController remittanceFindingsController;

    RemittanceAuditFinding raf;

    SalesmanDAO salesmanDAO = new SalesmanDAO();

    Salesman selectedSalesman;

    ObservableList<Salesman> salesmanList = FXCollections.observableArrayList(salesmanDAO.getAllSalesmen());

    public void createNewRaf(String s) {
        raf = new RemittanceAuditFinding();
        raf.setDateCreated(Timestamp.valueOf(LocalDate.now().atStartOfDay()));
        raf.setDocNo(s);
        dateCreatedPicker.setValue(raf.getDateCreated().toLocalDateTime().toLocalDate());
        header.setText(raf.getDocNo());

        // Update raf when date pickers change
        dateAuditedPicker.valueProperty().addListener((observable, oldValue, newValue) ->
                raf.setDateAudited(newValue != null ? Timestamp.valueOf(newValue.atStartOfDay()) : null)
        );
        dateCreatedPicker.valueProperty().addListener((observable, oldValue, newValue) ->
                raf.setDateCreated(newValue != null ? Timestamp.valueOf(newValue.atStartOfDay()) : null)
        );
        dateFromPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            raf.setDateFrom(newValue != null ? Timestamp.valueOf(newValue.atStartOfDay()) : null);
            updateSelectionPaneState();
        });
        dateToPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            raf.setDateTo(newValue != null ? Timestamp.valueOf(newValue.atStartOfDay()) : null);
            updateSelectionPaneState();
        });

        auditeeField.textProperty().addListener((observable, oldValue, newValue) -> {
            selectedSalesman = salesmanList.stream().filter(salesman -> salesman.getSalesmanName().equals(newValue)).findFirst().orElse(null);
            raf.setAuditee(selectedSalesman);
        });

        confirmButton.setOnAction(event -> insertRaf());

    }

    CollectionDAO collectionDAO = new CollectionDAO();


    private void loadCollectionDetailsForSelection() {
        collectionSelectionTableView.setPlaceholder(new ProgressIndicator());
        CompletableFuture.supplyAsync(() -> collectionDAO.fetchCollectionForRAFSelection(raf.getDateFrom(), raf.getDateTo(), raf.getAuditee()))
                .thenAccept(details -> Platform.runLater(() -> {
                    collectionDetailsForSelection.setAll(details);
                    collectionDetailsForSelection.removeAll(collectionDetails);
                    collectionSelectionTableView.setPlaceholder(details.isEmpty() ? new Label("No records found.") : null);
                }));
    }

    // Helper method to enable/disable the selection pane
    private void updateSelectionPaneState() {
        boolean enablePane = raf.getDateFrom() != null && raf.getDateTo() != null && raf.getAuditee() != null;
        selectionTiltedPane.setDisable(!enablePane);
    }

    ObservableList<String> salesmanNames = FXCollections.observableArrayList(salesmanList.stream().map(Salesman::getSalesmanName).toList());

    ObservableList<CollectionDetail> collectionDetailsForSelection = FXCollections.observableArrayList();
    ObservableList<CollectionDetail> collectionDetails = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TextFields.bindAutoCompletion(auditeeField, salesmanNames);
        collectionDetailsTableView.setItems(collectionDetails);
        collectionSelectionTableView.setItems(collectionDetailsForSelection);
        setUpSelectionTable();
        setUpRafTable();

        selectionTiltedPane.setDisable(true);

        collectionDetails.addListener((ListChangeListener<CollectionDetail>) change -> updateAmount());

        selectionTiltedPane.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                loadCollectionDetailsForSelection();
            }
        });
    }

    RemittanceAuditFindingsDAO remittanceAuditFindingsDAO = new RemittanceAuditFindingsDAO();

    private void insertRaf() {
        raf.setAmount(calculateTotalAmount());
        raf.setCollectionDetails(collectionDetails);
        raf.setAuditor(UserSession.getInstance().getUser());

        boolean success = remittanceAuditFindingsDAO.insertAuditFinding(raf);
        if (success) {
            remittanceFindingsController.loadRemittanceFindings();
            DialogUtils.showCompletionDialog("Remittance Audit Finding", "Remittance Audit Finding has been created.");
        } else {
            DialogUtils.showErrorMessage("Remittance Audit Finding", "Remittance Audit Finding could not be created.");
        }
    }

    private void updateAmount() {
        rafAmount.setText(String.format("%.2f", calculateTotalAmount()));
    }

    private double calculateTotalAmount() {
        double totalAmount = 0;
        for (CollectionDetail cd : collectionDetails) {
            if (cd.getBalanceType().getId() == 1) {
                totalAmount += cd.getAmount();
            } else {
                totalAmount -= cd.getAmount();
            }
        }
        return totalAmount;
    }

    private void setUpRafTable() {
        collectionNoCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCollection().getDocNo()));
        balanceTypeCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getBalanceType().getBalanceName()));
        coaCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getAccountTitle()));
        amountCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getAmount()));
        remarksCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getRemarks()));
        collectionDateCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getCollection().getCollectionDate()));

        collectionDetailsTableView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                CollectionDetail selectedCD = collectionDetailsTableView.getSelectionModel().getSelectedItem();
                if (selectedCD != null) {
                    collectionDetails.remove(selectedCD);
                    collectionDetailsForSelection.add(selectedCD);
                    collectionDetailsTableView.getSelectionModel().clearSelection();
                }
            }
        });
    }

    private void setUpSelectionTable() {
        collectionNoColSelection.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCollection().getDocNo()));
        balanceTypeColSelection.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getBalanceType().getBalanceName()));
        coaColSelection.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getAccountTitle()));
        amountColSelection.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getAmount()));
        collectionDateColSelection.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getCollection().getCollectionDate()));

        collectionSelectionTableView.setRowFactory(tableView -> new TableRow<CollectionDetail>() {
            @Override
            protected void updateItem(CollectionDetail item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    if (item.getBalanceType().getId() == 2) {
                        setStyle("-fx-background-color: #ff8080;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        collectionSelectionTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        collectionSelectionTableView.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                selectItem();
            }
        });

        collectionSelectionTableView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                selectItem();
            }
        });

        addSelectedItems.setOnAction(event -> {
            ObservableList<CollectionDetail> selectedItems = FXCollections.observableArrayList(collectionSelectionTableView.getSelectionModel().getSelectedItems());
            collectionDetails.addAll(selectedItems);
            collectionSelectionTableView.getItems().removeAll(selectedItems);
            collectionSelectionTableView.getSelectionModel().clearSelection();
        });

    }

    private void selectItem() {
        CollectionDetail selectedCollectionDetail = collectionSelectionTableView.getSelectionModel().getSelectedItem();
        if (selectedCollectionDetail != null) {
            collectionSelectionTableView.getSelectionModel().select(selectedCollectionDetail);
        }
    }

    public void openExistingRaf(RemittanceAuditFinding selectedRaf) {
        raf = selectedRaf;
        dateFromPicker.setValue(raf.getDateFrom().toLocalDateTime().toLocalDate());
        dateToPicker.setValue(raf.getDateTo().toLocalDateTime().toLocalDate());
        dateAuditedPicker.setValue(raf.getDateAudited().toLocalDateTime().toLocalDate());
        dateCreatedPicker.setValue(raf.getDateCreated().toLocalDateTime().toLocalDate());
        auditeeField.setText(raf.getAuditee().getSalesmanName());
        header.setText(raf.getDocNo());
        rafAmount.setText(String.format("%.2f", raf.getAmount()));
        collectionDetails.addAll(raf.getCollectionDetails());
        updateSelectionPaneState();

        confirmButton.setText("Update");
        confirmButton.setOnAction(event -> updateRaf());
    }

    private void updateRaf() {
        raf.setAmount(calculateTotalAmount());
        raf.setDateUpdated(Timestamp.valueOf(LocalDate.now().atStartOfDay()));
        raf.setCollectionDetails(collectionDetails);
        boolean success = remittanceAuditFindingsDAO.updateRemittanceAuditFinding(raf);
        if (success) {
            remittanceFindingsController.loadRemittanceFindings();
            DialogUtils.showCompletionDialog("Remittance Audit Finding", "Remittance Audit Finding has been updated.");
        } else {
            DialogUtils.showErrorMessage("Remittance Audit Finding", "Remittance Audit Finding could not be updated.");
        }
    }
}
