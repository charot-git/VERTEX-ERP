package com.vertex.vos;

import com.vertex.vos.DAO.ProductPerCustomerDAO;
import com.vertex.vos.DAO.SalesInvoiceDAO;
import com.vertex.vos.DAO.SalesReturnDAO;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import javafx.util.Duration;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class SalesReturnFormController implements Initializable {

    public CheckBox tplCheckBox;
    public ComboBox<String> priceType;
    public TextField customerCode;
    public TableColumn<SalesReturnDetail, Double> discountCol;
    public ComboBox<SalesReturnType> returnTypeComboBox;
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
    public TableColumn<SalesReturnDetail, String> discountTypeCol;
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
    private final SalesReturnDAO salesReturnDAO = new SalesReturnDAO();
    private ObservableList<Customer> customers = FXCollections.observableArrayList();
    private SalesmanDAO salesmanDAO = new SalesmanDAO();
    private ObservableList<SalesReturnType> salesReturnTypes = FXCollections.observableArrayList();
    private final ObservableList<SalesReturnDetail> productsForSalesReturn = FXCollections.observableArrayList();
    private final ObservableList<SalesReturnDetail> deletedSalesReturnDetails = FXCollections.observableArrayList();
    ObservableList<String> priceTypes = FXCollections.observableArrayList("A", "B", "C", "D", "E");

    SalesReturn salesReturn;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeTableView();
        priceType.setItems(priceTypes);

        // Load all sales return types from DAO
        salesReturnTypes.setAll(salesReturnDAO.getAllReturnTypes());

        // ComboBox setup for editing SalesReturnType
        returnTypeComboBox.setItems(salesReturnTypes);
        returnTypeComboBox.setConverter(new StringConverter<SalesReturnType>() {
            @Override
            public String toString(SalesReturnType object) {
                return object != null ? object.getTypeName() : "Unknown";
            }

            @Override
            public SalesReturnType fromString(String string) {
                // Optionally lookup and return SalesReturnType by typeName
                return salesReturnTypes.stream()
                        .filter(returnType -> returnType.getTypeName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
        returnTypeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                returnDetailTable.getSelectionModel().getSelectedItems().forEach(returnDetail -> {
                    returnDetail.setSalesReturnType(newValue);
                });

                returnDetailTable.refresh();
            }
        });
        returnDetailTable.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.DELETE)) {
                // Add selected items to deleted list and remove from the main list
                productsForSalesReturn.removeAll(returnDetailTable.getSelectionModel().getSelectedItems());
                deletedSalesReturnDetails.addAll(returnDetailTable.getSelectionModel().getSelectedItems());
                returnDetailTable.refresh();
            }
        });

        // Handle click event for adding product
        addProductButton.setOnMouseClicked(event -> {
            if (selectedSalesman != null && selectedCustomer != null) {
                // Ensure salesman and customer are selected before adding product
                addProductToSalesReturn(salesReturn);
            } else {
                DialogUtils.showErrorMessage("Missing Data", "Please select a salesman and a customer.");
            }
        });
    }


    DiscountDAO discountDAO = new DiscountDAO();

    List<DiscountType> discountTypes = discountDAO.getAllDiscountTypes();
    List<String> discountTypeNames = discountTypes.stream().map(DiscountType::getTypeName).collect(Collectors.toList());


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
            updateAmountForItem(product);
            returnDetailTable.requestFocus();
            returnDetailTable.refresh();

            updateTotalAmount();
        });
        unitPriceCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getUnitPrice()).asObject());
        unitPriceCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        unitPriceCol.setOnEditCommit(event -> {
            SalesReturnDetail product = event.getRowValue();
            product.setUnitPrice(event.getNewValue());
            product.setGrossAmount(product.getQuantity() * product.getUnitPrice());
            product.setTotalAmount(product.getGrossAmount() - product.getDiscountAmount());

            returnDetailTable.requestFocus();
            returnDetailTable.refresh();

            updateAmountForItem(product);
            updateTotalAmount();
        });

        discountTypeCol.setCellValueFactory(cellData -> {
            DiscountType discountType = cellData.getValue().getDiscountType();
            return new SimpleStringProperty(discountType != null ? discountType.getTypeName() : "No Discount");
        });
        discountTypeCol.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(discountTypeNames)));
        discountTypeCol.setOnEditCommit(event -> {
            SalesReturnDetail returnDetail = event.getRowValue();
            DiscountType newDiscountType = discountTypes.stream()
                    .filter(dt -> dt.getTypeName().equals(event.getNewValue()))
                    .findFirst().orElse(null);

            if (newDiscountType != null) {
                returnDetail.setDiscountType(newDiscountType);
                updateAmountForItem(returnDetail);
                updateTotalAmount();
            } else {
                DialogUtils.showErrorMessage("Error", "Invalid discount type selected.");
            }
        });

        grossAmountCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getGrossAmount()).asObject());
        discountCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getDiscountAmount()).asObject());

        returnTypeCol.setCellValueFactory(cellData -> {
            SalesReturnType returnType = cellData.getValue().getSalesReturnType();
            return new SimpleStringProperty(returnType != null ? returnType.getTypeName() : "Unknown");
        });

        returnTypeCol.setOnEditCommit(event -> {
            String selectedTypeName = event.getNewValue();
            SalesReturnType selectedReturnType = null;

            // Find the SalesReturnType corresponding to the selected typeName
            for (SalesReturnType returnType : salesReturnTypes) {
                if (returnType.getTypeName().equals(selectedTypeName)) {
                    selectedReturnType = returnType;
                    break;
                }
            }

            // Set the selected SalesReturnType object to the row
            event.getRowValue().setSalesReturnType(selectedReturnType);
            returnDetailTable.requestFocus();
        });


        reasonCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReason()));
        reasonCol.setCellFactory(TextFieldTableCell.forTableColumn());
        reasonCol.setOnEditCommit(event -> event.getRowValue().setReason(event.getNewValue()));
        totalAmountCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTotalAmount()));
    }

    private void updateAmountForItem(SalesReturnDetail returnDetail) {
        double grossAmount = returnDetail.getGrossAmount();
        if (returnDetail.getDiscountType() != null) {
            List<BigDecimal> lineDiscounts = discountDAO.getLineDiscountsByDiscountTypeId(returnDetail.getDiscountType().getId());
            if (lineDiscounts != null && !lineDiscounts.isEmpty()) {
                double discount = DiscountCalculator.calculateTotalDiscountAmount(BigDecimal.valueOf(grossAmount), lineDiscounts).doubleValue();
                returnDetail.setDiscountAmount(discount);
            } else {
                returnDetail.setDiscountAmount(0); // No discounts available
            }

            double totalAmount = returnDetail.getGrossAmount() - returnDetail.getDiscountAmount();
            returnDetail.setTotalAmount(totalAmount);

            returnDetailTable.refresh();
            returnDetailTable.requestFocus();
            updateTotalAmount();
        }
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
            // Set sales return object properties
            salesReturn.setThirdParty(tplCheckBox.isSelected());
            salesReturn.setRemarks(remarks.getText());
            salesReturn.setCreatedBy(UserSession.getInstance().getUserId()); // Get actual user ID
            salesReturn.setReturnDate(Timestamp.valueOf(returnDate.getValue().atStartOfDay()));
            salesReturn.setStatus("Pending"); // Default status set to Pending
            salesReturn.setPriceType(priceType.getSelectionModel().getSelectedItem());
            salesReturn.setSalesman(selectedSalesman);
            salesReturn.setSalesInvoiceOrderNumber(orderNoTextField.getText());
            salesReturn.setSalesInvoiceNumber(invoiceNoComboBox.getValue());
            salesReturn.setCustomer(selectedCustomer);
            salesReturn.setRemarks(remarks.getText());
            createOrUpdateSalesReturn(salesReturn);
        });

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
            try (Connection connection = DatabaseConnectionPool.getConnection()) {
                connection.setAutoCommit(false); // Start transaction

                // Delete sales return details first if necessary
                boolean deleteSuccess = true;
                if (!deletedSalesReturnDetails.isEmpty()) {
                    deleteSuccess = salesReturnDAO.deleteSalesReturnDetails(salesReturn, deletedSalesReturnDetails, connection);
                    if (!deleteSuccess) {
                        DialogUtils.showErrorMessage("Error", "Failed to delete some sales return details.");
                        connection.rollback();
                        return;
                    }
                }

                // Create or update sales return
                SalesReturn createdOrUpdatedSalesReturn = salesReturnDAO.createSalesReturn(salesReturn, returnDetailTable.getItems(), connection);

                if (createdOrUpdatedSalesReturn != null) {
                    connection.commit(); // Commit transaction

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
                    connection.rollback(); // Rollback if creation/update failed
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

        if (returnTypeComboBox.getValue() != null) {
            salesReturnDetail.setSalesReturnType(returnTypeComboBox.getValue());
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
                salesReturn.setRemarks(remarks.getText());
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
                try (Connection connection = DatabaseConnectionPool.getConnection()) {
                    connection.setAutoCommit(false); // Start transaction

                    SalesReturn updatedSalesReturn = salesReturnDAO.createSalesReturn(salesReturn, productsForSalesReturn, connection);
                    if (updatedSalesReturn != null) {
                        salesReturnDAO.receiveProducts(productsForSalesReturn, salesReturn, connection);
                        connection.commit(); // Commit transaction

                        Platform.runLater(() -> {
                            DialogUtils.showCompletionDialog("Success", "Sales Return received successfully.");
                            stage.close();

                            if (salesReturnsListController != null) {
                                salesReturnsListController.loadSalesReturn();
                            }
                        });
                    } else {
                        connection.rollback(); // Rollback if creation/update failed
                        Platform.runLater(() -> {
                            DialogUtils.showErrorMessage("Error", "Failed to receive sales return.");
                        });
                    }
                } catch (SQLException e) {
                    DialogUtils.showErrorMessage("Database Error", "Failed to update sales return.");
                    e.printStackTrace();
                } finally {
                    receiveButton.setDisable(false);
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
