package com.vertex.vos;

import com.vertex.vos.DAO.SalesReturnDAO;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.ConfirmationAlert;
import com.vertex.vos.Utilities.CustomerDAO;
import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.GenericSelectionWindow;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.ResourceBundle;

public class SalesReturnFormController implements Initializable {

    public CheckBox tplCheckBox;
    public ComboBox<String> priceType;
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
    private TextArea remarksTextArea;

    @FXML
    private DatePicker returnDate;

    @FXML
    private TableColumn<SalesReturnDetail, String> returnTypeCol;

    @FXML
    private VBox selectCustomer;

    @FXML
    private HBox selectCustomerButton;

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
    private CustomerDAO customerDAO = new CustomerDAO();
    private SalesReturnDAO salesReturnDAO = new SalesReturnDAO();
    private ObservableList<Customer> customers = FXCollections.observableArrayList();

    private Map<Integer, String> typeIdToNameMap;
    private Map<String, Integer> typeNameToIdMap;
    private ObservableList<String> salesReturnTypes;
    private ObservableList<SalesReturnDetail> productsForSalesReturn = FXCollections.observableArrayList();
    ObservableList<String> priceTypes = FXCollections.observableArrayList("A", "B", "C", "D", "E");


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeMappings();
        initializeTableView();
        priceType.setItems(priceTypes);

    }

    private void initializeMappings() {
        // Fetch mappings from the database
        typeIdToNameMap = SalesReturnDAO.getTypeIdToNameMap();
        typeNameToIdMap = SalesReturnDAO.getTypeNameToIdMap();
        salesReturnTypes = FXCollections.observableArrayList(typeIdToNameMap.values());
    }

    private void initializeTableView() {
        // Set the items for the TableView
        returnDetailTable.setItems(productsForSalesReturn);

        // Define column cell value factories
        descriptionCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getDescription()));
        unitCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getUnitOfMeasurementString()));
        quantityCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        quantityCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        quantityCol.setOnEditCommit(event -> {
            SalesReturnDetail product = event.getRowValue();
            product.setQuantity(event.getNewValue());
            product.setTotalAmount(product.getQuantity() * product.getUnitPrice());
            returnDetailTable.requestFocus();
            returnDetailTable.refresh();

        });
        unitPriceCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getUnitPrice()).asObject());
        totalAmountCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotalAmount()).asObject());

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

    public Timestamp getTimestampFromDatePicker(DatePicker datePicker) {
        // Step 1: Get the LocalDate from DatePicker
        LocalDate localDate = datePicker.getValue();

        // Step 2: Convert LocalDate to LocalDateTime (defaulting to start of the day)
        LocalDateTime localDateTime = localDate.atStartOfDay();

        // Step 3: Convert LocalDateTime to Timestamp
        Timestamp timestamp = Timestamp.valueOf(localDateTime);

        return timestamp;
    }

    SalesReturnsListController salesReturnsListController;

    public void createNewSalesReturn(Stage stage, int salesReturnNo, SalesReturnsListController salesReturnsListController) {
        this.salesReturnsListController = salesReturnsListController;
        try {
            // Initialize the SalesReturn object
            SalesReturn salesReturn = new SalesReturn();
            salesReturn.setReturnNumber("MEN-" + salesReturnNo);
            salesReturn.setStatus("Entry");
            status.setText(salesReturn.getStatus());
            documentNo.setText("Sales Return #" + salesReturnNo);

            // Set up listener for return date field
            returnDate.valueProperty().addListener((observable, oldValue, newValue) -> {
                salesReturn.setReturnDate(getTimestampFromDatePicker(returnDate));
            });

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
                    returnDetailTable.refresh();
                }
            });

            // Initialize customer selection window
            GenericSelectionWindow<Customer> selectionWindow = new GenericSelectionWindow<>();

            // Set up customer selection button click event
            selectCustomerButton.setOnMouseClicked(event -> {
                customers.setAll(customerDAO.getAllActiveCustomers());
                selectedCustomer = selectionWindow.showSelectionWindow(stage, "Select Customer", customers);

                if (selectedCustomer != null) {
                    storeName.setText(selectedCustomer.getStoreName());
                    salesReturn.setCustomerId(selectedCustomer.getCustomerId());
                }
            });


            // Set up add product button click event
            addProductButton.setOnMouseClicked(event -> addProductToSalesReturn(salesReturn));

            // Set up confirm button click event
            confirmButton.setOnMouseClicked(event -> {
                try {
                    // Gather sales return details from form
                    salesReturn.setThirdParty(tplCheckBox.isSelected());
                    salesReturn.setRemarks(remarksTextArea.getText());
                    salesReturn.setCreatedBy(UserSession.getInstance().getUserId()); // Set actual user ID
                    salesReturn.setStatus("Pending"); // Set status to Pending
                    salesReturn.setPriceType(priceType.getSelectionModel().getSelectedItem());

                    // Show confirmation alert before creating sales return
                    ConfirmationAlert confirmationAlert = new ConfirmationAlert(
                            "Confirmation",
                            "Are you sure you want to create this sales return?",
                            "Please verify the details",
                            true
                    );
                    boolean confirmed = confirmationAlert.showAndWait();

                    if (!confirmed) {
                        DialogUtils.showConfirmationDialog("Cancelled", "Sales return creation cancelled.");
                        return;
                    }

                    // Create sales return in database
                    boolean success = salesReturnDAO.createSalesReturn(salesReturn, productsForSalesReturn);

                    if (success) {
                        DialogUtils.showConfirmationDialog("Success", "Sales return created successfully.");
                        stage.close();
                        salesReturnsListController.loadSalesReturn();

                    } else {
                        DialogUtils.showErrorMessage("Error", "Failed to create sales return.");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    DialogUtils.showErrorMessage("Error", "An error occurred while creating the sales return.");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            DialogUtils.showErrorMessage("Error", "Failed to initialize the sales return process.");
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

        // Add the product to the sales return list
        productsForSalesReturn.add(salesReturnDetail);

        // Refresh the table to reflect the updated price for the new product
        returnDetailTable.refresh();
    }

}
