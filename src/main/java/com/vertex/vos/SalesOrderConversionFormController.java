package com.vertex.vos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.DragDropDataStore;
import com.vertex.vos.Utilities.GsonUtils;
import com.vertex.vos.Utilities.SalesOrderDAO;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Setter;
import org.apache.tools.ant.taskdefs.optional.ejb.IPlanetDeploymentTool;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class SalesOrderConversionFormController implements Initializable {

    public TabPane tabPane;
    public Button openAsWindowButton;
    public ToggleButton toggleForFulfilled;
    public BorderPane childBorderPane;
    @FXML
    private Label orderNo;
    @FXML
    private TextField branchField;
    @FXML
    private TextField customerCodeField;
    @FXML
    private TextField salesmanCode;
    @FXML
    private TextField salesmanNameField;
    @FXML
    private TextField storeNameField;
    @FXML
    private TextField supplierField;
    @FXML
    private DatePicker dateCreatedField;
    @FXML
    private DatePicker deliveryDateField;
    @FXML
    private DatePicker dueDateField;
    @FXML
    private DatePicker orderDateField;
    @FXML
    private Button addSales;
    @FXML
    private Button convertButton;
    @FXML
    private ComboBox<SalesInvoiceType> invoiceField;
    @FXML
    TableView<SalesOrderDetails> salesOrderTableView;
    @FXML
    private TableColumn<SalesOrderDetails, Double> discountCol;
    @FXML
    private TableColumn<SalesOrderDetails, String> discountTypeCol;
    @FXML
    private TableColumn<SalesOrderDetails, Double> grossCol;
    @FXML
    private TableColumn<SalesOrderDetails, Double> netCol;
    @FXML
    private TableColumn<SalesOrderDetails, Integer> orderedQuantityCol;
    @FXML
    private TableColumn<SalesOrderDetails, Double> priceCol;
    @FXML
    private TableColumn<SalesOrderDetails, String> productCodeCol;
    @FXML
    private TableColumn<SalesOrderDetails, String> productNameCol;
    @FXML
    private TableColumn<SalesOrderDetails, String> productUnitCol;
    @FXML
    private TableColumn<SalesOrderDetails, Integer> servedQuantityCol;
    @FXML
    private TitledPane tiltedPane;
    @FXML
    private ButtonBar buttonBar;
    @FXML
    private TextField productNameFilter;

    @Setter
    SalesOrderListController salesOrderListController;

    SalesOrder salesOrder;

    ObservableList<SalesInvoiceHeader> salesInvoiceHeaders = FXCollections.observableArrayList(); // <SalesInvoiceHeader>
    ObservableList<SalesOrderDetails> salesOrderDetails = FXCollections.observableArrayList();


    public TableView<SalesOrderDetails> getTableView() {
        return salesOrderTableView;
    }

    FilteredList<SalesOrderDetails> filteredList = new FilteredList<>(salesOrderDetails, p -> true);

    public void openSalesOrder(SalesOrder selectedItem) {
        this.salesOrder = selectedItem;
        supplierField.setText(selectedItem == null || selectedItem.getSupplier() == null ? null : selectedItem.getSupplier().getSupplierName());
        invoiceField.setValue(selectedItem == null ? null : selectedItem.getInvoiceType());
        orderNo.setText(selectedItem == null ? null : selectedItem.getOrderNo());
        branchField.setText(selectedItem == null || selectedItem.getBranch() == null ? null : selectedItem.getBranch().getBranchName());
        customerCodeField.setText(selectedItem == null || selectedItem.getCustomer() == null ? null : selectedItem.getCustomer().getCustomerCode());
        salesmanCode.setText(selectedItem == null || selectedItem.getSalesman() == null ? null : selectedItem.getSalesman().getSalesmanCode());
        salesmanNameField.setText(selectedItem == null || selectedItem.getSalesman() == null ? null : selectedItem.getSalesman().getSalesmanName());
        storeNameField.setText(selectedItem == null || selectedItem.getBranch() == null ? null : selectedItem.getBranch().getBranchName());
        dateCreatedField.setValue(selectedItem == null || selectedItem.getCreatedDate() == null ? null : selectedItem.getCreatedDate().toLocalDateTime().toLocalDate());
        deliveryDateField.setValue(selectedItem == null || selectedItem.getDeliveryDate() == null ? null : selectedItem.getDeliveryDate().toLocalDateTime().toLocalDate());
        dueDateField.setValue(selectedItem == null || selectedItem.getDueDate() == null ? null : selectedItem.getDueDate().toLocalDateTime().toLocalDate());
        orderDateField.setValue(selectedItem == null || selectedItem.getOrderDate() == null ? null : selectedItem.getOrderDate().toLocalDateTime().toLocalDate());

        deliveryDateField.valueProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                salesOrder.setDeliveryDate(Timestamp.valueOf(newValue.atStartOfDay()));
            }
        }));

        if (salesOrder != null && salesOrder.getSalesOrderDetails() != null) {
            salesOrderDetails.addAll(salesOrder.getSalesOrderDetails());
            tiltedPane.setText("Sales Order Products: " + salesOrder.getSalesOrderDetails().size());
        } else {
            tiltedPane.setText("Sales Order Products: 0");
        }


        assert selectedItem != null;
        addSales.setText("Add " + selectedItem.getInvoiceType().getName());
        buttonBar.getButtons().add(addSales);
        addSales.setOnAction(actionEvent -> {
            addSalesInvoice();
        });

        toggleForFulfilled.setOnAction(actionEvent -> {
            applyToggleFilter();
        });


        TextFields.bindAutoCompletion(productNameFilter,
                salesOrderDetails.stream().map(SalesOrderDetails::getProduct)
                        .map(Product::getProductName).toList());

        productNameFilter.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(product -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true; // Show all if no filter
                }
                return product.getProduct().getProductName().toLowerCase()
                        .contains(newValue.toLowerCase());
            });
        });
    }

    private void applyToggleFilter() {
        boolean showOnlyUnfulfilled = toggleForFulfilled.isSelected();

        filteredList.setPredicate(item -> {
            if (!showOnlyUnfulfilled) {
                return true; // Show all items
            }
            return item.getOrderedQuantity() == item.getServedQuantity(); // Show only unfulfilled
        });

        Platform.runLater(() -> salesOrderTableView.setItems(filteredList));
    }


    private void addSalesInvoice() {
        if (salesOrder.getDeliveryDate() == null) {
            DialogUtils.showErrorMessage("Error", "Delivery date is not set.");
            return;
        }
        if (salesOrder.getDueDate() == null) {
            DialogUtils.showErrorMessage("Error", "Due date is not set.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SalesInvoiceTemporary.fxml"));
            Parent root = loader.load();
            SalesInvoiceTemporaryController salesInvoiceTemporaryController = loader.getController();
            Tab tab = new Tab("New Sales Transaction");
            SalesInvoiceHeader salesInvoiceHeader = new SalesInvoiceHeader();
            salesInvoiceHeaders.add(salesInvoiceHeader);
            salesInvoiceTemporaryController.setInitialDataForSalesOrder(salesInvoiceHeader, salesOrder, tab, salesOrderDetails, this);
            ScrollPane scrollPane = new ScrollPane(root);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            tab.setContent(scrollPane);
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Unable to open sales invoice creation.");
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buttonBar.getButtons().clear(); // More efficient than removeAll
        salesInvoiceHeaders.addListener((ListChangeListener.Change<? extends SalesInvoiceHeader> c) -> {
            updateQuantity();
            applyToggleFilter();
        });

        openAsWindowButton.setOnAction(event -> {
            Platform.runLater(this::openTitledPaneAsWindow);
        });

        Platform.runLater(this::setupTableView);

        convertButton.setOnAction(actionEvent -> {
            convertSalesOrder();
        });
    }

    SalesOrderDAO salesOrderDAO = new SalesOrderDAO();

    private void convertSalesOrder() {
        salesOrder.setSalesOrderDetails(salesOrderDetails);
        boolean converted = salesOrderDAO.convertSalesOrder(salesOrder, salesInvoiceHeaders);
        if (converted) {

            Platform.runLater(() -> salesOrderListController.loadSalesOrder());
            if (DialogUtils.showConfirmationDialog("Conversion Complete", "Close this window?")) {
                salesOrderListController.getConversionStage().close();
            }

        }
        else {
            DialogUtils.showErrorMessage("Error", "Sales Order Conversion Error, Please contact system developer.");
        }
    }

    private void openTitledPaneAsWindow() {
        Pane placeholder = new Pane();
        childBorderPane.setCenter(placeholder);
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(tiltedPane);
        Stage stage = new Stage();
        stage.setTitle("Sales Order Products");
        Scene scene = new Scene(borderPane);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/vertex/vos/assets/style.css")).toExternalForm());
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> {
            childBorderPane.setCenter(tiltedPane);
        });

        stage.show();
    }


    private void setupTableView() {
        productCodeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductCode()));
        productNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductName()));
        productUnitCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getUnitOfMeasurementString()));
        orderedQuantityCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>((cellData.getValue().getOrderedQuantity())));
        servedQuantityCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>((cellData.getValue().getServedQuantity())));
        discountTypeCol.setCellValueFactory(cellData -> {
            String discountName = cellData.getValue().getDiscountType() == null ? "No Discount" : cellData.getValue().getDiscountType().getTypeName();
            return new SimpleStringProperty(discountName);
        });
        priceCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getUnitPrice()).asObject());
        grossCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getGrossAmount()).asObject());
        discountCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getDiscountAmount()).asObject());
        netCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getNetAmount()).asObject());
        salesOrderTableView.setItems(filteredList);

        salesOrderDetails.addListener((ListChangeListener<SalesOrderDetails>) change -> {
            applyToggleFilter(); // Reapply filter whenever the list changes
        });

        salesOrderTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        salesOrderTableView.setRowFactory(tv -> {
            TableRow<SalesOrderDetails> row = new TableRow<>() {
                @Override
                protected void updateItem(SalesOrderDetails item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setStyle("");
                        setDisable(false);
                    } else {
                        boolean shouldGrayOut = item.getServedQuantity() >= item.getOrderedQuantity();
                        setStyle(shouldGrayOut ? "-fx-background-color: lightgray; -fx-opacity: 0.6;" : "");
                        setDisable(shouldGrayOut);

                        if (shouldGrayOut) {
                            Platform.runLater(() -> {
                                if (getIndex() >= 0 && getIndex() < salesOrderTableView.getItems().size()) {
                                    salesOrderTableView.getSelectionModel().clearSelection(getIndex());
                                }
                            });
                        }
                    }
                }

                @Override
                public void updateSelected(boolean selected) {
                    if (getItem() != null && getItem().getServedQuantity() >= getItem().getOrderedQuantity()) {
                        Platform.runLater(() -> getTableView().getSelectionModel().clearSelection(getIndex()));
                    } else {
                        super.updateSelected(selected);
                    }
                }
            };

            row.setOnDragDetected(event -> {
                if (!row.isEmpty() && !row.isDisabled()) {
                    Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();

                    DragDropDataStore.setDraggedItems(salesOrderTableView.getSelectionModel().getSelectedItems());
                    content.putString("dragged");

                    db.setContent(content);
                    event.consume();
                }
            });

            return row;
        });

        salesOrderTableView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<SalesOrderDetails>) change -> {
            Platform.runLater(() -> {
                // Create a copy of the selected items list to avoid concurrent modification
                List<SalesOrderDetails> selectedItemsCopy = new ArrayList<>(salesOrderTableView.getSelectionModel().getSelectedItems());

                for (SalesOrderDetails item : selectedItemsCopy) {
                    if (item.getServedQuantity() >= item.getOrderedQuantity()) {
                        int index = salesOrderTableView.getItems().indexOf(item);
                        if (index >= 0) { // Ensure index is valid before clearing selection
                            salesOrderTableView.getSelectionModel().clearSelection(index);
                        }
                    }
                }
            });
        });


    }

    public void filterItems() {
        if (!toggleForFulfilled.isSelected()) {
            filteredList.setPredicate(salesOrderDetail ->
                    salesOrderDetail.getServedQuantity() != salesOrderDetail.getOrderedQuantity()
            );
        } else {
            filteredList.setPredicate(null); // Show all items
        }
        salesOrderTableView.setItems(filteredList);
        salesOrderTableView.refresh(); // Force refresh to apply styles
    }


    public void updateQuantity() {
        Map<Integer, Integer> productQuantityMap = new HashMap<>();

        // Aggregate quantities from all remaining salesInvoiceHeaders
        for (SalesInvoiceDetail invoiceDetail : salesInvoiceHeaders.stream()
                .flatMap(h -> h.getSalesInvoiceDetails().stream())
                .toList()) {
            int productId = invoiceDetail.getProduct().getProductId();
            productQuantityMap.put(productId,
                    productQuantityMap.getOrDefault(productId, 0) + invoiceDetail.getQuantity());
        }

        // Update servedQuantity based on the recalculated totals
        for (SalesOrderDetails salesOrderDetail : salesOrderDetails) {
            int productId = salesOrderDetail.getProduct().getProductId();
            salesOrderDetail.setServedQuantity(productQuantityMap.getOrDefault(productId, 0)); // If product is missing, set to 0
        }
    }


}
