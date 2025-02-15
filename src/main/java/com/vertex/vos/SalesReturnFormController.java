package com.vertex.vos;

import com.vertex.vos.DAO.ProductPerCustomerDAO;
import com.vertex.vos.DAO.SalesInvoiceDAO;
import com.vertex.vos.DAO.SalesReturnDAO;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class SalesReturnFormController implements Initializable {

    public CheckBox tplCheckBox;
    public ComboBox<String> priceType;
    public TextField customerCode;
    public TableColumn<SalesReturnDetail, Double> discountCol;
    public ComboBox<String> returnTypeComboBox;
    public TableColumn<SalesReturnDetail, Double> grossAmountCol;
    public TextArea remarks;
    public TextField salesmanName;
    public TextField salesmanBranch;
    public Label grossAmount;
    public Label discountAmount;
    public Label netAmount;
    public Label appliedAmount;
    public Label dateApplied;
    public Label postStatus;
    public Label receivedStatus;
    public Button receiveButton;
    public DatePicker receivedDate;
    public TextField orderNoTextField;
    public ComboBox<String> invoiceNoComboBox;
    @FXML
    private TableView<SalesReturnDetail> returnDetailTable;

    @FXML
    private VBox addBoxes;

    @FXML
    private HBox addProductButton;

    @FXML
    private Button confirmButton;

    @FXML
    private VBox customerBox;

    @FXML
    private TableColumn<SalesReturnDetail, String> descriptionCol;

    @FXML
    private Label documentNo;

    @FXML
    private TableColumn<SalesReturnDetail, Integer> quantityCol;

    @FXML
    private TableColumn<SalesReturnDetail, String> reasonCol;

    @FXML
    private DatePicker returnDate;

    @FXML
    private TableColumn<SalesReturnDetail, String> returnTypeCol;

    @FXML
    private VBox selectCustomer;


    @FXML
    private Label status;

    @FXML
    private TextField storeName;

    @FXML
    private TableColumn<SalesReturnDetail, Double> totalAmountCol;

    @FXML
    private TableColumn<SalesReturnDetail, String> unitCol;

    @FXML
    private TableColumn<SalesReturnDetail, Double> unitPriceCol;

    private Customer selectedCustomer;
    private Salesman selectedSalesman;
    private CustomerDAO customerDAO = new CustomerDAO();
    private SalesReturnDAO salesReturnDAO = new SalesReturnDAO();
    private ObservableList<Customer> customers = FXCollections.observableArrayList();
    private SalesmanDAO salesmanDAO = new SalesmanDAO();

    private Map<Integer, String> typeIdToNameMap;
    private Map<String, Integer> typeNameToIdMap;
    private ObservableList<String> salesReturnTypes;
    private final ObservableList<SalesReturnDetail> productsForSalesReturn = FXCollections.observableArrayList();
    private final ObservableList<SalesReturnDetail> deletedSalesReturnDetails = FXCollections.observableArrayList();
    ObservableList<String> priceTypes = FXCollections.observableArrayList("A", "B", "C", "D", "E");

    SalesReturn salesReturn;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeMappings();
        initializeTableView();
        priceType.setItems(priceTypes);

        returnTypeComboBox.setItems(salesReturnTypes);
        returnTypeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                int selectedTypeId = typeNameToIdMap.getOrDefault(newValue, -1);
                for (SalesReturnDetail product : returnDetailTable.getSelectionModel().getSelectedItems()) {
                    product.setSalesReturnTypeId(selectedTypeId);
                }
            }
            returnDetailTable.refresh();
        });

        returnDetailTable.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.DELETE)) {
                deletedSalesReturnDetails.addAll(returnDetailTable.getSelectionModel().getSelectedItems());
                productsForSalesReturn.removeAll(returnDetailTable.getSelectionModel().getSelectedItems());

                returnDetailTable.refresh();
            }
        });

        addProductButton.setOnMouseClicked(event -> {
            if (selectedSalesman != null && selectedCustomer != null) {
                addProductToSalesReturn(salesReturn);
            } else {
                DialogUtils.showErrorMessage("Missing Data", "Please select a salesman and a customer.");
            }
        });


    }

    private void initializeMappings() {
        // Fetch mappings from the database
        typeIdToNameMap = SalesReturnDAO.getTypeIdToNameMap();
        typeNameToIdMap = SalesReturnDAO.getTypeNameToIdMap();
        salesReturnTypes = FXCollections.observableArrayList(typeIdToNameMap.values());
    }

    DiscountDAO discountDAO = new DiscountDAO();

    private void initializeTableView() {
        // Set the items for the TableView
        returnDetailTable.setItems(productsForSalesReturn);

        returnDetailTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        descriptionCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getDescription()));
        unitCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getUnitOfMeasurementString()));
        quantityCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        quantityCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        quantityCol.setOnEditCommit(event -> {
            SalesReturnDetail product = event.getRowValue();
            product.setQuantity(event.getNewValue());
            product.setGrossAmount(product.getQuantity() * product.getUnitPrice());

            // Check if discount is applicable
            double discountAmount = 0.0;
            if (product.getProduct().getDiscountType() != null) {
                List<BigDecimal> discounts = discountDAO.getLineDiscountsByDiscountTypeId(product.getProduct().getDiscountType().getId());
                if (!discounts.isEmpty()) {
                    discountAmount = DiscountCalculator.calculateTotalDiscountAmount(
                            BigDecimal.valueOf(product.getGrossAmount()), discounts
                    ).doubleValue();
                }
            }

            product.setDiscountAmount(discountAmount);
            product.setTotalAmount(product.getGrossAmount() - discountAmount);

            returnDetailTable.requestFocus();
            returnDetailTable.refresh();

            updateTotalAmount();
        });
        ;
        unitPriceCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getUnitPrice()).asObject());
        totalAmountCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotalAmount()).asObject());
        grossAmountCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getGrossAmount()).asObject());
        discountCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getDiscountAmount()).asObject());

        // Configure the returnTypeCol for ComboBox editing
        returnTypeCol.setCellValueFactory(cellData -> {
            int returnTypeId = cellData.getValue().getSalesReturnTypeId();
            String returnTypeName = typeIdToNameMap.getOrDefault(returnTypeId, "Unknown");
            return new SimpleStringProperty(returnTypeName);
        });

        // Set ComboBox options for return types
        returnTypeCol.setCellFactory(ComboBoxTableCell.forTableColumn(salesReturnTypes));

        // Handle edit commit to map the selected type_name back to type_id
        returnTypeCol.setOnEditCommit(event -> {
            String selectedTypeName = event.getNewValue();
            int selectedTypeId = typeNameToIdMap.getOrDefault(selectedTypeName, -1);
            event.getRowValue().setSalesReturnTypeId(selectedTypeId);
            returnDetailTable.requestFocus();
        });

        // Handle reasonCol editing
        reasonCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReason()));
        reasonCol.setCellFactory(TextFieldTableCell.forTableColumn());
        reasonCol.setOnEditCommit(event -> event.getRowValue().setReason(event.getNewValue()));
    }

    private void updateTotalAmount() {
        double grossAmountTotal = 0.0;
        double discountAmountTotal = 0.0;
        double netAmountTotal = 0.0;
        for (SalesReturnDetail product : productsForSalesReturn) {
            grossAmountTotal += product.getTotalAmount();
            discountAmountTotal += product.getDiscountAmount();
        }

        netAmountTotal = grossAmountTotal - discountAmountTotal;

        grossAmount.setText(String.format("%.2f", grossAmountTotal));
        discountAmount.setText(String.format("%.2f", discountAmountTotal));
        netAmount.setText(String.format("%.2f", netAmountTotal));
    }

    public Timestamp getTimestampFromDatePicker(DatePicker datePicker) {
        // Step 1: Get the LocalDate from DatePicker
        LocalDate localDate = datePicker.getValue();
        LocalDateTime localDateTime = localDate.atStartOfDay();
        return Timestamp.valueOf(localDateTime);
    }

    SalesReturnsListController salesReturnsListController;

    SalesInvoiceDAO salesInvoiceDAO = new SalesInvoiceDAO();

    public void createNewSalesReturn(Stage stage, int salesReturnNo, SalesReturnsListController salesReturnsListController) {
        this.salesReturnsListController = salesReturnsListController;
        this.stage = stage;

        // Initialize the SalesReturn object
        salesReturn = new SalesReturn();
        salesReturn.setReturnNumber("MEN-" + salesReturnNo);
        salesReturn.setStatus("Entry");
        salesReturn.setPosted(false);
        salesReturn.setReceived(false);
        postStatus.setText(salesReturn.isPosted() ? "Yes" : "No");
        receivedStatus.setText(salesReturn.isReceived() ? "Yes" : "No");
        status.setText(salesReturn.getStatus());
        documentNo.setText("Sales Return #" + salesReturnNo);

        List<Salesman> salesmen = salesmanDAO.getAllSalesmen(); // Fetch salesmen from the database
        List<String> salesmanNames = salesmen.stream().map(Salesman::getSalesmanName).toList();


        TextFields.bindAutoCompletion(salesmanName, salesmanNames);

        salesmanName.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) { // Only process when focus is lost
                String selectedSalesmanName = salesmanName.getText().trim();
                selectedSalesman = salesmen.stream()
                        .filter(salesman -> salesman.getSalesmanName().equalsIgnoreCase(selectedSalesmanName))
                        .findFirst()
                        .orElse(null);

                if (selectedSalesman != null) {
                    salesmanBranch.setText(selectedSalesman.getSalesmanCode());
                } else {
                    salesmanBranch.clear(); // Clear the branch field if no valid salesman is found
                }
            }
        });
        customerCode.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                if (selectedCustomer.getCustomerCode() != null && selectedSalesman.getSalesmanCode() != null) {
                    List<String> orderNoBySalesmanAndCustomer = salesInvoiceDAO.getAllSalesOrderBySalesmanAndCustomer(selectedSalesman.getId(), selectedCustomer.getCustomerCode());
                    TextFields.bindAutoCompletion(orderNoTextField, orderNoBySalesmanAndCustomer);
                }
            }
        });

        orderNoTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                List<String> salesInvoicePerOrderNo = salesInvoiceDAO.getAllInvoiceNumbersByOrderNo(newValue);
                invoiceNoComboBox.setItems(FXCollections.observableArrayList(salesInvoicePerOrderNo));
            }
        });

        returnDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            salesReturn.setReturnDate(getTimestampFromDatePicker(returnDate));
        });

        receivedDate.setDisable(true);

        priceType.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                salesReturn.setPriceType(newValue);
                switch (newValue) {
                    case "A":
                        for (SalesReturnDetail product : productsForSalesReturn) {
                            product.setUnitPrice(product.getProduct().getPriceA());
                        }
                        break;
                    case "B":
                        for (SalesReturnDetail product : productsForSalesReturn) {
                            product.setUnitPrice(product.getProduct().getPriceB());
                        }
                        break;
                    case "C":
                        for (SalesReturnDetail product : productsForSalesReturn) {
                            product.setUnitPrice(product.getProduct().getPriceC());
                        }
                        break;
                    case "D":
                        for (SalesReturnDetail product : productsForSalesReturn) {
                            product.setUnitPrice(product.getProduct().getPriceD());
                        }
                        break;
                    case "E":
                        for (SalesReturnDetail product : productsForSalesReturn) {
                            product.setUnitPrice(product.getProduct().getPriceE());
                        }
                        break;
                    default:
                        break;
                }
                // Refresh the table view to show updated prices
                updateTotalAmount();
                returnDetailTable.refresh();
            }
        });


        ObservableList<Customer> customers = FXCollections.observableArrayList(customerDAO.getAllActiveCustomers());
        List<String> customerNames = customers.stream().map(Customer::getStoreName).toList();
        TextFields.bindAutoCompletion(storeName, customerNames);

        storeName.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) { // Only process when focus is lost
                String selectedCustomerName = storeName.getText().trim();
                selectedCustomer = customers.stream()
                        .filter(customer -> customer.getStoreName().equalsIgnoreCase(selectedCustomerName))
                        .findFirst()
                        .orElse(null);

                if (selectedCustomer != null) {
                    customerCode.setText(selectedCustomer.getCustomerCode());
                    salesReturn.setCustomerCode(selectedCustomer.getCustomerCode());
                    salesReturn.setCustomer(selectedCustomer);
                } else {
                    customerCode.clear(); // Clear the branch field if no valid salesman is found
                }
            }
        });

        // Set up confirm button click event
        confirmButton.setOnMouseClicked(event -> {
                    salesReturn.setThirdParty(tplCheckBox.isSelected());
                    salesReturn.setRemarks(remarks.getText());
                    salesReturn.setCreatedBy(UserSession.getInstance().getUserId()); // Set actual user ID
                    salesReturn.setReturnDate(Timestamp.valueOf(returnDate.getValue().atStartOfDay()));
                    salesReturn.setStatus("Pending"); // Set status to Pending
                    salesReturn.setPriceType(priceType.getSelectionModel().getSelectedItem());
                    salesReturn.setSalesman(selectedSalesman);
                    salesReturn.setSalesInvoiceOrderNumber(orderNoTextField.getText());
                    salesReturn.setSalesInvoiceNumber(invoiceNoComboBox.getValue());
                    salesReturn.setCustomer(selectedCustomer);

                    createOrUpdateSalesReturn(salesReturn);
                }
        );
    }

    public void createOrUpdateSalesReturn(SalesReturn salesReturn) {
        String action = salesReturn.getStatus().equals("Pending") ? "create" : "update";

        ConfirmationAlert confirmationAlert = new ConfirmationAlert(
                "Confirmation",
                "Are you sure you want to " + action + " this sales return?",
                "Please verify the details",
                true
        );

        if (confirmationAlert.showAndWait()) {
            try {
                SalesReturn createdOrUpdatedSalesReturn = salesReturnDAO.createSalesReturn(salesReturn, returnDetailTable.getItems());

                if (createdOrUpdatedSalesReturn != null) {
                    DialogUtils.showCompletionDialog("Success", "Sales return " + action + "d successfully.");
                    stage.close();

                    // Update UI Controllers
                    if (salesReturnsListController != null) {
                        salesReturnsListController.loadSalesReturn();
                    }

                    if (salesInvoiceTemporaryController != null) {
                        createdOrUpdatedSalesReturn.setSalesReturnDetails(returnDetailTable.getItems());
                        salesInvoiceTemporaryController.salesReturn = createdOrUpdatedSalesReturn;
                        salesInvoiceTemporaryController.loadSalesReturnDetails();
                        salesInvoiceTemporaryController.returnTab.setText(createdOrUpdatedSalesReturn.getReturnNumber());
                    }
                } else {
                    DialogUtils.showErrorMessage("Error", "Failed to " + action + " sales return.");
                }
            } catch (SQLException e) {
                DialogUtils.showErrorMessage("Error", "Failed to " + action + " sales return: " + e.getMessage());
            }
        } else {
            DialogUtils.showCompletionDialog("Cancelled", "Sales return creation cancelled.");
        }
    }


    private void addProductToSalesReturn(SalesReturn salesReturn) {
        if (salesReturn.getPriceType() == null) {
            DialogUtils.showErrorMessage("Error", "Please select a price type.");
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("ProductSelectionBySupplier.fxml"));
                Parent root = loader.load();
                ProductSelectionPerSupplier controller = loader.getController();
                controller.addProductForSalesReturn(salesReturn);
                controller.setSalesReturnController(this);

                Stage stage = new Stage();
                stage.setTitle("Add Products");
                stage.setMaximized(true);
                stage.setScene(new Scene(root));
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    ProductPerCustomerDAO productPerCustomerDAO = new ProductPerCustomerDAO();

    public void addProductToSalesReturnDetail(SalesReturnDetail salesReturnDetail) {
        if (priceType.getValue() != null) {
            switch (priceType.getValue()) {
                case "A":
                    salesReturnDetail.setUnitPrice(salesReturnDetail.getProduct().getPriceA());
                    break;
                case "B":
                    salesReturnDetail.setUnitPrice(salesReturnDetail.getProduct().getPriceB());
                    break;
                case "C":
                    salesReturnDetail.setUnitPrice(salesReturnDetail.getProduct().getPriceC());
                    break;
                case "D":
                    salesReturnDetail.setUnitPrice(salesReturnDetail.getProduct().getPriceD());
                    break;
                case "E":
                    salesReturnDetail.setUnitPrice(salesReturnDetail.getProduct().getPriceE());
                    break;
                default:
                    break;
            }
        } else {
            priceType.getSelectionModel().select("A");
        }
        Product product = productPerCustomerDAO.getCustomerProductByCustomerAndProduct(salesReturnDetail.getProduct(), selectedCustomer);
        if (product != null) {
            salesReturnDetail.getProduct().setPricePerUnit(product.getPricePerUnit());
            salesReturnDetail.getProduct().setDiscountType(product.getDiscountType());
        }
        productsForSalesReturn.add(salesReturnDetail);
        returnDetailTable.refresh();
    }

    public void loadSalesReturn(SalesReturn selectedSalesReturn, SalesReturnsListController salesReturnsListController) {
        this.salesReturnsListController = salesReturnsListController;
        if (selectedSalesReturn != null) {
            this.salesReturn = selectedSalesReturn;
            documentNo.setText(salesReturn.getReturnNumber());
            salesmanName.setText(salesReturn.getSalesman().getSalesmanName());
            salesmanBranch.setText(salesReturn.getSalesman().getSalesmanCode());
            customerCode.setText(salesReturn.getCustomer().getCustomerCode());
            storeName.setText(salesReturn.getCustomer().getStoreName());
            returnDate.setValue(salesReturn.getReturnDate().toLocalDateTime().toLocalDate());
            remarks.setText(salesReturn.getRemarks());
            postStatus.setText(salesReturn.isPosted() ? "Yes" : "No");
            receivedStatus.setText(salesReturn.isReceived() ? "Yes" : "No");
            tplCheckBox.setSelected(salesReturn.isThirdParty());
            orderNoTextField.setText(String.valueOf(salesReturn.getSalesInvoiceOrderNumber()));
            invoiceNoComboBox.setValue(String.valueOf(salesReturn.getSalesInvoiceNumber()));
            salesReturn.setPriceType(salesReturn.getPriceType());
            salesReturn.setStatus("Viewing");
            if (salesReturn.getReceivedAt() != null) {
                receivedDate.setValue(selectedSalesReturn.getReceivedAt().toLocalDateTime().toLocalDate());
            } else {
                receivedDate.setValue(null);
            }
            if (salesReturn.getPriceType() != null) {
                priceType.setValue(selectedSalesReturn.getPriceType());
            }

            if (salesReturn.isReceived()) {
                receiveButton.setDisable(true);
            }

            productsForSalesReturn.clear();
            productsForSalesReturn.setAll(salesReturn.getSalesReturnDetails());
            returnDetailTable.refresh();

            selectedCustomer = salesReturn.getCustomer();
            selectedSalesman = salesReturn.getSalesman();



            confirmButton.setText("Update");
            confirmButton.setOnMouseClicked(mouseEvent -> {
                salesReturn.setStatus("Pending");
                salesReturn.setSalesInvoiceOrderNumber(orderNoTextField.getText());
                salesReturn.setSalesInvoiceNumber(invoiceNoComboBox.getValue());
                createOrUpdateSalesReturn(salesReturn);
            });

            updateTotalAmount();
        }


        receiveButton.setOnMouseClicked(event -> {
            if (salesReturn == null) {
                DialogUtils.showErrorMessage("Error", "No sales return selected.");
                return;
            }

            SalesReturn salesReturn = new SalesReturn();
            salesReturn.setReturnNumber(documentNo.getText());
            salesReturn.setSalesman(selectedSalesman);
            salesReturn.setCustomer(selectedCustomer);
            salesReturn.setReturnDate(Timestamp.valueOf(returnDate.getValue().atStartOfDay()));
            salesReturn.setRemarks(remarks.getText());
            salesReturn.setPosted(false);
            salesReturn.setReceived(true);
            if (receivedDate.getValue() != null) {
                salesReturn.setReceivedAt(Timestamp.valueOf(receivedDate.getValue().atStartOfDay()));
            } else {
                salesReturn.setReceivedAt(Timestamp.valueOf(LocalDateTime.now()));
            }
            salesReturn.setThirdParty(tplCheckBox.isSelected());
            salesReturn.setStatus("Received");
            salesReturn.setPriceType(priceType.getValue());
            salesReturn.setSalesReturnDetails(productsForSalesReturn);
            salesReturn.setSalesInvoiceOrderNumber(orderNoTextField.getText());
            salesReturn.setSalesInvoiceNumber(invoiceNoComboBox.getValue());
            receiveButton.setDisable(true); // Prevent multiple clicks
            new Thread(() -> {
                try {
                    SalesReturn updatedSalesReturn = salesReturnDAO.createSalesReturn(salesReturn, productsForSalesReturn);
                    Platform.runLater(() -> {
                        if (updatedSalesReturn != null) {
                            DialogUtils.showCompletionDialog("Success", "Sales Return received successfully.");
                            stage.close();

                            if (salesReturnsListController != null) {
                                salesReturnsListController.loadSalesReturn();
                            }
                        } else {
                            DialogUtils.showErrorMessage("Error", "Failed to receive sales return.");
                        }
                        receiveButton.setDisable(false); // Re-enable button
                    });
                } catch (SQLException e) {
                    Platform.runLater(() -> {
                        DialogUtils.showErrorMessage("Database Error", "Failed to update sales return.");
                        receiveButton.setDisable(false);
                    });
                    e.printStackTrace();
                }
            }).start();
        });
    }

    @Setter
    Stage stage;

    public void createCollection(Stage stage, int collectionNumber, CollectionListController collectionListController) {

    }

    SalesInvoiceTemporaryController salesInvoiceTemporaryController;

    public void setInitialDataForSalesInvoice(Salesman selectedSalesman, Customer selectedCustomer, SalesInvoiceHeader salesInvoiceHeader, LocalDate value, SalesInvoiceTemporaryController salesInvoiceTemporaryController) {
        salesmanName.setText(selectedSalesman.getSalesmanName());
        salesmanBranch.setText(selectedSalesman.getTruckPlate());
        customerCode.setText(selectedCustomer.getCustomerCode());
        storeName.setText(selectedCustomer.getStoreName());
        returnDate.setValue(value);
        orderNoTextField.setText(salesInvoiceHeader.getOrderId());
        invoiceNoComboBox.setValue(salesInvoiceHeader.getInvoiceNo());
        receivedDate.setValue(value);
        priceType.setValue(selectedSalesman.getPriceType());
        salesReturn.setPriceType(selectedSalesman.getPriceType());
        this.selectedCustomer = selectedCustomer;
        this.selectedSalesman = selectedSalesman;
        this.salesInvoiceTemporaryController = salesInvoiceTemporaryController;
    }
}
