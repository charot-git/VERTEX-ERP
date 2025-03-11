package com.vertex.vos;

import com.vertex.vos.DAO.ClusterDAO;
import com.vertex.vos.DAO.SalesInvoiceDAO;
import com.vertex.vos.DAO.TripSummaryDAO;
import com.vertex.vos.Enums.TripSummaryStatus;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TripSummaryFormController implements Initializable {
    @FXML
    private ImageView statusImage;

    @FXML
    private Button confirmButton;

    @FXML
    private Label statusLabel;

    @FXML
    private Label tripAmount;

    @FXML
    private Label tripNo;

    @FXML
    private DatePicker tripDatePicker;
    @FXML
    private HBox confirmBox;

    @FXML
    private HBox statusBox;

    @FXML
    private SplitPane orderSplitPane;

    @FXML
    private TableView<SalesInvoiceHeader> salesInvoiceForTripSummaryTable;

    @FXML
    private TableView<SalesInvoiceHeader> salesInvoiceInTrip;

    @FXML
    private TableColumn<SalesInvoiceHeader, String> customerCodeColAddForTripSummary;

    @FXML
    private TableColumn<SalesInvoiceHeader, String> customerCodeColAddInTrip;

    @FXML
    private TableColumn<SalesInvoiceHeader, String> customerColAddForTripSummary;

    @FXML
    private TableColumn<SalesInvoiceHeader, String> invoiceNoColAddForTripSummary;

    @FXML
    private TableColumn<SalesInvoiceHeader, String> invoiceNoColAddInTrip;

    @FXML
    private TableColumn<SalesInvoiceHeader, String> orderNoColAddForTripSummary;

    @FXML
    private TableColumn<SalesInvoiceHeader, String> orderNoColAddInTrip;

    @FXML
    private TableColumn<SalesInvoiceHeader, String> salesmanCodeColAddForTripSummary;

    @FXML
    private TableColumn<SalesInvoiceHeader, String> salesmanCodeColAddInTrip;

    @FXML
    private TableColumn<SalesInvoiceHeader, String> salesmanColAddForTripSummary;

    @FXML
    private TableColumn<SalesInvoiceHeader, String> salesmanNameColAddForTripSummary;

    @FXML
    private TableColumn<SalesInvoiceHeader, String> salesmanNameColAddInTrip;

    @FXML
    private TableColumn<SalesInvoiceHeader, String> storeNameColAddForTripSummary;

    @FXML
    private TableColumn<SalesInvoiceHeader, String> storeNameColAddInTrip;

    @FXML
    private TableColumn<SalesInvoiceHeader, Double> totalAmountColAddForTripSummary;

    @FXML
    private TableColumn<SalesInvoiceHeader, Double> totalAmountColAddInTrip;

    @FXML
    private VBox Trucks;

    @FXML
    private VBox tripDate;

    @FXML
    private TextField clusterTextField;

    @FXML
    private TextField vehicleTextField;

    TripSummary tripSummary;

    TripSummaryDAO tripSummaryDAO = new TripSummaryDAO();

    VehicleDAO vehicleDAO = new VehicleDAO();
    ClusterDAO clusterDAO = new ClusterDAO();

    ObservableList<Vehicle> vehicleList = FXCollections.observableArrayList();
    ObservableList<Cluster> clusterList = FXCollections.observableArrayList();

    Vehicle selectedVehicle;

    Cluster selectedCluster;

    ObservableList<SalesInvoiceHeader> availableSalesInvoices = FXCollections.observableArrayList();

    SalesInvoiceDAO salesInvoiceDAO = new SalesInvoiceDAO();

    public void createNewTripSummary() {
        String generatedNextTripNo = tripSummaryDAO.generateNextTripNo();
        tripSummary = new TripSummary();
        tripSummary.setTripNo(generatedNextTripNo);
        tripSummary.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        tripSummary.setCreatedBy(UserSession.getInstance().getUser());
        tripSummary.setStatus(TripSummaryStatus.TripStatus.Pending);
        statusLabel.setText(tripSummary.getStatus() != null ? tripSummary.getStatus().name() : "");
        tripAmount.setText(tripSummary.getTripAmount() != 0 ? String.valueOf(tripSummary.getTripAmount()) : "");
        tripNo.setText(tripSummary.getTripNo() != null ? tripSummary.getTripNo() : "");


        confirmButton.setOnAction(event -> {
            saveTripSummary();
        });
    }

    private void saveTripSummary() {
        if (tripDatePicker.getValue() == null) {
            DialogUtils.showErrorMessage("Error", "Please select trip date");
            return;
        }

        if (selectedVehicle == null) {
            DialogUtils.showErrorMessage("Error", "Please select truck");
            return;
        }
        if (tripSummary.getTripAmount() < selectedCluster.getMinimumAmount()) {
            DialogUtils.showErrorMessage("Error", "Minimum amount for " + selectedCluster.getClusterName() + " not met");
            return;
        }

        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Trip summary insert", "Save " + tripSummary.getTripNo() + " ?", "Please double check the invoices linked", true);
        if (confirmationAlert.showAndWait()) {
            if (tripSummaryDAO.saveTrip(tripSummary)) {
                confirmButton.setDisable(true);
                if (DialogUtils.showConfirmationDialog("Success", "Trip saved, would you like to close this window?")) {
                    tripSummaryListController.getNewTripSummaryStage().close();
                }
                tripSummaryListController.loadTripSummaryList();
            } else {
                DialogUtils.showErrorMessage("Error", "An error has occurred while saving trip summary.");
            }
        }

    }

    private void loadAvailableInvoices(Cluster cluster) {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        salesInvoiceForTripSummaryTable.setPlaceholder(progressIndicator);

        availableSalesInvoices.clear();

        CompletableFuture.supplyAsync(() -> salesInvoiceDAO.getSalesInvoicesForTripSummary(cluster))
                .thenAccept(invoices -> Platform.runLater(() -> {
                    availableSalesInvoices.setAll(invoices);
                    if (invoices.isEmpty()) {
                        salesInvoiceForTripSummaryTable.setPlaceholder(new Label("No Invoices found."));
                    }
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        e.printStackTrace();
                        salesInvoiceForTripSummaryTable.setPlaceholder(new Label("Failed to load invoices."));
                    });
                    return null;
                });
    }


    public void initializeEncodingData() {
        clusterList.setAll(clusterDAO.getAllClusters());
        vehicleList.setAll(vehicleDAO.getAllVehiclesByStatus("Active"));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeColumnsForTripSummaryTable();
        initializeColumnsForInTripTable();
        initializeEncodingData();

        tripDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                tripSummary.setTripDate(Timestamp.valueOf(newValue.atStartOfDay()));
            }
        });

        clusterTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                selectedCluster = clusterList.stream()
                        .filter(cluster -> cluster.getClusterName().equals(newValue))
                        .findFirst()
                        .orElse(null);
                if (selectedCluster != null) {
                    tripSummary.setCluster(selectedCluster);
                    loadAvailableInvoices(selectedCluster);
                }
            }
        });

        vehicleTextField.textProperty().addListener(((observable, oldValue, newValue) -> {
            selectedVehicle = vehicleList.stream().filter(vehicle -> vehicle.getVehiclePlate().equals(newValue)).findFirst().orElse(null);
            if (selectedVehicle != null) {
                tripSummary.setVehicle(selectedVehicle);
            }
        }));

        TextFields.bindAutoCompletion(vehicleTextField, vehicleList.stream().map(Vehicle::getVehiclePlate).collect(Collectors.toList()));
        TextFields.bindAutoCompletion(clusterTextField, clusterList.stream().map(Cluster::getClusterName).collect(Collectors.toList()));

        Platform.runLater(() -> {
            tripSummary.getSalesInvoices().addListener((ListChangeListener<SalesInvoiceHeader>) c -> {
                tripSummary.setTripAmount(calculateTripAmount());
                updateTripAmount();
            });
        });
    }

    private void initializeColumnsForTripSummaryTable() {
        customerCodeColAddForTripSummary.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCustomer().getCustomerCode()));
        customerColAddForTripSummary.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCustomer().getCustomerName()));
        invoiceNoColAddForTripSummary.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getInvoiceNo()));
        orderNoColAddForTripSummary.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getOrderId()));
        salesmanCodeColAddForTripSummary.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getSalesman().getSalesmanCode()));
        salesmanNameColAddForTripSummary.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getSalesman().getSalesmanName()));
        storeNameColAddForTripSummary.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCustomer().getStoreName()));
        totalAmountColAddForTripSummary.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getTotalAmount()));
        salesInvoiceForTripSummaryTable.setItems(availableSalesInvoices);

        if (availableSalesInvoices.isEmpty()) {
            if (selectedCluster != null) {
                salesInvoiceForTripSummaryTable.setPlaceholder(new Label("No sales invoices found for " + selectedCluster.getClusterName()));
            } else {
                salesInvoiceForTripSummaryTable.setPlaceholder(new Label("No sales invoices found."));
            }
        }
        salesInvoiceForTripSummaryTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        salesInvoiceForTripSummaryTable.setOnDragDetected(event -> {
            if (!salesInvoiceForTripSummaryTable.getSelectionModel().getSelectedItems().isEmpty()) {
                Dragboard db = salesInvoiceForTripSummaryTable.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();

                DragDropDataStore.setDraggedItems(salesInvoiceForTripSummaryTable.getSelectionModel().getSelectedItems());
                content.putString("dragged");

                db.setContent(content);
                event.consume();
            }
        });
    }

    private void initializeColumnsForInTripTable() {
        customerCodeColAddInTrip.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCustomer().getCustomerCode()));
        customerCodeColAddInTrip.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCustomer().getCustomerName()));
        invoiceNoColAddInTrip.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getInvoiceNo()));
        orderNoColAddInTrip.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getOrderId()));
        salesmanCodeColAddInTrip.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getSalesman().getSalesmanCode()));
        salesmanNameColAddInTrip.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getSalesman().getSalesmanName()));
        storeNameColAddInTrip.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCustomer().getStoreName()));
        totalAmountColAddInTrip.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getTotalAmount()));

        Platform.runLater(() -> {
            salesInvoiceInTrip.setItems(tripSummary.getSalesInvoices());
            if (tripSummary.getSalesInvoices().isEmpty()) {
                String clusterName = (selectedCluster != null) ? selectedCluster.getClusterName() : "the selected cluster";
                salesInvoiceInTrip.setPlaceholder(new Label("No sales invoices found for " + clusterName));
            }
        });

        salesInvoiceInTrip.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                ObservableList<SalesInvoiceHeader> deletedItems = salesInvoiceInTrip.getSelectionModel().getSelectedItems();
                availableSalesInvoices.addAll(deletedItems);
                tripSummary.getSalesInvoices().removeAll(deletedItems);
            }
        });

        salesInvoiceInTrip.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        implementReceiveOnDrag();

    }

    private void implementReceiveOnDrag() {
        salesInvoiceInTrip.setOnDragOver(event -> {
            if (event.getGestureSource() != salesInvoiceInTrip && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        salesInvoiceInTrip.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if ("dragged".equals(db.getString())) {
                List<SalesInvoiceHeader> droppedItems = DragDropDataStore.getDraggedItems();

                if (droppedItems == null || droppedItems.isEmpty()) {
                    DialogUtils.showErrorMessage("Error", "No valid items found.");
                    return;
                }


                ObservableList<SalesInvoiceHeader> currentItems = tripSummary.getSalesInvoices();

                for (SalesInvoiceHeader item : droppedItems) {
                    SalesInvoiceHeader invoice = getInvoice(item);

                    // 🛑 Check if the product is already in the invoice
                    boolean exists = currentItems.stream()
                            .anyMatch(i -> Objects.equals(i.getInvoiceNo(), invoice.getInvoiceNo()));

                    if (exists) {
                        DialogUtils.showErrorMessage("Error", "Invoice already exists in the trip summary: " + invoice.getInvoiceNo());
                    } else {
                        availableSalesInvoices.remove(item);
                        tripSummary.getSalesInvoices().add(item);
                    }
                }

                success = true;
            }

            event.setDropCompleted(success);
            event.consume();
        });

    }


    private void updateTripAmount() {
        tripAmount.setText(String.format("%.2f", calculateTripAmount()));
    }

    private double calculateTripAmount() {
        return tripSummary.getSalesInvoices().stream()
                .mapToDouble(SalesInvoiceHeader::getTotalAmount)
                .sum();
    }

    private SalesInvoiceHeader getInvoice(SalesInvoiceHeader item) {
        return item;
    }

    @Setter
    TripSummaryListController tripSummaryListController;


}
