package com.vertex.vos;

import com.vertex.vos.DAO.DenominationDAO;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.controlsfx.control.textfield.TextFields;

import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CollectionPaymentFormController implements Initializable {

    @FXML
    public GridPane denominationGridPane;
    public BorderPane denominationPane;
    public BorderPane parentBorderPane;
    public VBox detailsVBox;
    public VBox chartOfAccountBox;
    public VBox bankNameBox;
    public VBox chequeNumberBox;
    public VBox amountBox;
    public DatePicker chequeDate;
    public VBox customerNameBox;
    public TextField customerNameTextField;
    public VBox invoiceNoBox;
    public TextField invoiceNoTextField;
    public ComboBox<BalanceType> balanceType;
    @FXML
    private Label amount;

    @FXML
    private TextField bankNameTextField;

    @FXML
    private TextField chequeNumberTextField;

    @FXML
    private TextField coaTextField;

    @FXML
    private TextField collectionAmount;

    @FXML
    private Button confirmButton;

    @FXML
    private TextArea remarksTextArea;

    @FXML
    private VBox remarksBox;

    private final DenominationDAO denominationDAO = new DenominationDAO();

    private final CustomerDAO customerDAO = new CustomerDAO();

    // Observable list to store CollectionDetailsDenomination
    private final ObservableList<CollectionDetailsDenomination> collectionDetailsDenominations = FXCollections.observableArrayList();

    CollectionDetail collectionDetail;

    // Observable list to store Denominations
    private final ObservableList<Denomination> denominations = FXCollections.observableArrayList(denominationDAO.getAllDenominations());
    BalanceTypeDAO balanceTypeDAO = new BalanceTypeDAO();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            confirmButton.setDefaultButton(true);
            TextFieldUtils.addDoubleInputRestriction(collectionAmount);

            if (collectionFormController != null) {
                balanceType.setItems(balanceTypeDAO.getAllBalanceTypes());
                TextFields.bindAutoCompletion(bankNameTextField, collectionFormController.bankNamesList);

                List<String> invoiceNumbers = collectionFormController.salesInvoices.stream()
                        .map(SalesInvoiceHeader::getInvoiceNo)
                        .toList();

                TextFields.bindAutoCompletion(invoiceNoTextField, invoiceNumbers);

                invoiceNoTextField.textProperty().addListener(((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        salesInvoiceHeader = collectionFormController.salesInvoices.stream()
                                .filter(s -> s.getInvoiceNo().equals(newValue))
                                .findFirst()
                                .orElse(null);

                        if (salesInvoiceHeader != null) {
                            customerNameTextField.setText(salesInvoiceHeader.getCustomer().getStoreName());
                            chequeDate.setValue(salesInvoiceHeader.getInvoiceDate().toLocalDateTime().toLocalDate());
                        }
                    }
                }));
            }

            TextFields.bindAutoCompletion(customerNameTextField, customerDAO.getCustomerStoreNamesWithInvoices());

            balanceType.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    collectionDetail.setBalanceType(newValue);
                }
            });
        });

        balanceType.setCellFactory(param -> new ListCell<BalanceType>() {
            @Override
            protected void updateItem(BalanceType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getBalanceName());
                }
            }
        });
        balanceType.setConverter(new StringConverter<BalanceType>() {
            @Override
            public String toString(BalanceType balanceType) {
                return balanceType == null ? "" : balanceType.getBalanceName();
            }

            @Override
            public BalanceType fromString(String string) {
                return null;
            }
        });

        parentBorderPane.setCenter(null);
        collectionAmount.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                double amount = Double.parseDouble(newValue);
                confirmButton.setDisable(!(amount > 0));
            } else {
                confirmButton.setDisable(true);
            }
        });
    }

    private void addPayment() {
        BankName bankName = collectionFormController.bankNames.stream().filter(bank -> bank.getName().equals(bankNameTextField.getText())).findFirst().orElse(null);
        ChartOfAccounts selectedCOA = collectionFormController.chartOfAccounts.stream().filter(coa -> coa.getAccountTitle().equals(coaTextField.getText())).findFirst().orElse(null);

        collectionDetail.setCheckNo(chequeNumberTextField.getText());
        if (selectedCOA != null && selectedCOA.getAccountTitle().equals("Cash On Hand")) {
            collectionDetail.setCheckDate(Timestamp.valueOf(collectionFormController.getCollectionDate().atStartOfDay()));
        } else {
            collectionDetail.setCheckDate(Timestamp.valueOf(chequeDate.getValue().atStartOfDay()));
        }
        collectionDetail.setBank(bankName);
        collectionDetail.setSalesInvoiceHeader(salesInvoiceHeader);
        collectionDetail.setBalanceType(balanceType.getSelectionModel().getSelectedItem());
        collectionDetail.setType(selectedCOA);
        collectionDetail.setAmount(Double.parseDouble(collectionAmount.getText()));

        String storeName = customerNameTextField.getText();

        selectedCustomer = customerDAO.getCustomerByStoreName(storeName);

        collectionDetail.setCustomer(selectedCustomer);
        collectionDetail.setRemarks(remarksTextArea.getText());
        collectionDetail.setDenominations(FXCollections.observableArrayList(collectionDetailsDenominations.stream()
                .filter(detailsDenomination -> detailsDenomination.getQuantity() != null && detailsDenomination.getQuantity() > 0 || detailsDenomination.getDenomination() != null)
                .collect(Collectors.toList())));
        collectionDetail.setEncoderId(UserSession.getInstance().getUserId());
        collectionDetail.setPayment(true);
        collectionFormController.collectionDetails.add(collectionDetail);

        collectionFormController.updateLabelAmounts();
        // Close the stage and reset the reference to allow reopening
        stage.close();
        collectionFormController.paymentStage = null;
    }


    Stage parentStage;
    CollectionFormController collectionFormController;
    Collection collection;

    private void updateAmount() {
        double totalAmount = 0;
        collectionFormController.updateLabelAmounts();
        for (CollectionDetailsDenomination detailsDenomination : collectionDetailsDenominations) {
            try {
                int quantity = detailsDenomination.getQuantity();
                double denominationAmount = detailsDenomination.getDenomination().getAmount();
                totalAmount += denominationAmount * quantity;
            } catch (Exception e) {
                // Handle any potential exceptions (e.g., null values)
                System.out.println("Error calculating amount for denomination: " + e.getMessage());
            }
        }

        // Update the total amount text fields
        collectionAmount.setText(String.valueOf(totalAmount));
        amount.setText("₱ " + totalAmount);  // Update the amount label
    }

    Stage stage;

    public void createNewCollectionPayment(Stage parentStage, Stage stage, Collection collection, CollectionFormController collectionFormController) {
        this.parentStage = parentStage;
        this.collection = collection;
        this.collectionFormController = collectionFormController;
        this.stage = stage;

        // Auto-complete for COA selection
        TextFields.bindAutoCompletion(coaTextField, collectionFormController.chartOfAccounts.stream()
                .filter(ChartOfAccounts::isPayment)
                .map(ChartOfAccounts::getAccountTitle)
                .collect(Collectors.toList()));

        collectionDetail = new CollectionDetail();
        balanceType.getSelectionModel().selectFirst();
        chequeDate.setValue(collection.getCollectionDate().toLocalDateTime().toLocalDate());

        // COA selection listener
        coaTextField.textProperty().addListener((observable, oldValue, newValue) -> handleCOASelection(newValue));

        // Initialize denomination UI
        updateDenominationUI();

        confirmButton.setOnAction(event -> addPayment());
        stage.sizeToScene();
    }

    /**
     * Handles COA selection and updates UI accordingly.
     */
    private void handleCOASelection(String newValue) {
        if (newValue != null && !newValue.isEmpty()) {
            collectionFormController.chartOfAccounts.stream()
                    .filter(coa -> coa.getAccountTitle().equals(newValue))
                    .findFirst()
                    .ifPresent(collectionDetail::setType);
        }

        if (collectionDetail.getType() != null) {
            boolean isCashOnHand = "Cash on Hand".equals(collectionDetail.getType().getAccountTitle());
            parentBorderPane.setCenter(isCashOnHand ? denominationPane : null);
            amountBox.setDisable(isCashOnHand);
            if (isCashOnHand) {
                updateDenominationUI();
            }
        }
    }

    /**
     * Updates the denomination UI, setting up fields and handling input changes.
     */
    private void updateDenominationUI() {
        denominationGridPane.getChildren().clear();

        int rowIndex = 0; // Manually track row index
        for (Denomination denomination : denominations) {
            Label denominationLabel = new Label("₱ " + denomination.getAmount());
            TextField quantityTextField = createQuantityField(denomination);

            denominationGridPane.add(denominationLabel, 0, rowIndex);
            denominationGridPane.add(quantityTextField, 1, rowIndex);

            rowIndex++; // Increment row index for the next set of elements
        }
    }

    /**
     * Creates a quantity input field for a given denomination.
     */
    private TextField createQuantityField(Denomination denomination) {
        TextField quantityTextField = new TextField();
        quantityTextField.setPromptText("Quantity");
        quantityTextField.setAlignment(Pos.TOP_RIGHT);
        TextFieldUtils.addNumericInputRestriction(quantityTextField);

        // Check if this denomination already exists
        CollectionDetailsDenomination detailsDenomination = collectionDetailsDenominations.stream()
                .filter(d -> Double.compare(d.getDenomination().getAmount(), denomination.getAmount()) == 0)
                .findFirst()
                .orElse(null);

        if (detailsDenomination == null) {
            detailsDenomination = new CollectionDetailsDenomination();
            detailsDenomination.setDenomination(denomination);
            detailsDenomination.setQuantity(0); // 👈 Prevents null issue
            collectionDetailsDenominations.add(detailsDenomination);

            Logger.getLogger(CollectionPaymentFormController.class.getName()).log(Level.INFO, "Added new denomination: " + denomination);
        } else {
            if (detailsDenomination.getQuantity() == null) {
                detailsDenomination.setQuantity(0); // 👈 Ensure it's never null
            }
            quantityTextField.setText(String.valueOf(detailsDenomination.getQuantity()));

            Logger.getLogger(CollectionPaymentFormController.class.getName()).log(Level.INFO, "Using existing denomination: " + denomination);
        }

        // Live update total amount on input change
        CollectionDetailsDenomination finalDetailsDenomination = detailsDenomination;
        quantityTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                // Parse the quantity from the input text, ensuring it's an integer
                int quantity = newValue.isEmpty() ? 0 : Integer.parseInt(newValue);

                // Set the updated quantity in the denomination object
                finalDetailsDenomination.setQuantity(quantity);

                // Update the amount based on the quantity
                finalDetailsDenomination.setAmount(denomination.getAmount() * quantity);

                Logger.getLogger(CollectionPaymentFormController.class.getName()).log(Level.INFO, "Updated denomination quantity and amount: " + finalDetailsDenomination);

            } catch (NumberFormatException e) {
                System.out.println("Invalid input: " + newValue);
            }

            // After the text is updated, update the total denomination amount
            updateTotalDenominationAmount();
        });

        return quantityTextField;
    }


    private void updateTotalDenominationAmount() {
        double totalAmount = 0;
        for (CollectionDetailsDenomination detailsDenomination : collectionDetailsDenominations) {
            // ✅ Ensure quantity is not null before calculation
            int quantity = (detailsDenomination.getQuantity() != null) ? detailsDenomination.getQuantity() : 0;
            totalAmount += detailsDenomination.getDenomination().getAmount() * quantity;
        }

        amount.setText(String.format("%.2f", totalAmount));
        collectionAmount.setText(String.format("%.2f", totalAmount));

        Logger.getLogger(CollectionPaymentFormController.class.getName())
                .log(Level.INFO, "Updated total denomination amount: " + totalAmount);
    }


    public void createNewAdjustment(Stage parentStage, Stage adjustmentStage, Collection collection, CollectionFormController collectionFormController) {
        this.parentStage = parentStage;
        this.collection = collection;
        this.collectionFormController = collectionFormController;

        balanceType.getSelectionModel().selectLast();

        TextFields.bindAutoCompletion(coaTextField, collectionFormController.chartOfAccounts.stream()
                .map(ChartOfAccounts::getAccountTitle)
                .collect(Collectors.toList()));

        collectionDetail = new CollectionDetail();

        coaTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                collectionFormController.chartOfAccounts.stream()
                        .filter(coa -> coa.getAccountTitle().equals(newValue))
                        .findFirst()
                        .ifPresent(selectedCOA -> collectionDetail.setType(selectedCOA));
            }

            if (collectionDetail.getType() != null) {

                if (!collectionDetail.getType().getAccountTitle().equals("Cash on Hand")) {
                    parentBorderPane.setCenter(null);
                    amountBox.setDisable(false); // Re-enable in case it was disabled before
                } else {
                    parentBorderPane.setCenter(denominationPane);
                    amountBox.setDisable(true);
                }

                stage.sizeToScene();
            }
        });

        chequeDate.setValue(collection.getCollectionDate().toLocalDateTime().toLocalDate());

        confirmButton.setOnAction(event -> addAdjustment());
    }

    SalesInvoiceHeader salesInvoiceHeader;

    Customer selectedCustomer;

    private void addAdjustment() {
        BankName bankName = collectionFormController.bankNames.stream().filter(bank -> bank.getName().equals(bankNameTextField.getText())).findFirst().orElse(null);
        ChartOfAccounts selectedCOA = collectionFormController.chartOfAccounts.stream().filter(coa -> coa.getAccountTitle().equals(coaTextField.getText())).findFirst().orElse(null);

        collectionDetail.setCheckNo(chequeNumberTextField.getText());
        collectionDetail.setCheckDate(Timestamp.valueOf(chequeDate.getValue().atStartOfDay()));
        collectionDetail.setBank(bankName);
        collectionDetail.setType(selectedCOA);
        collectionDetail.setBalanceType(balanceType.getSelectionModel().getSelectedItem());
        collectionDetail.setSalesInvoiceHeader(salesInvoiceHeader);
        collectionDetail.setCustomer(selectedCustomer = customerDAO.getCustomerByStoreName(customerNameTextField.getText()));
        collectionDetail.setAmount(Double.parseDouble(collectionAmount.getText()));
        collectionDetail.setRemarks(remarksTextArea.getText());
        collectionDetail.setDenominations(collectionDetailsDenominations);
        collectionDetail.setEncoderId(UserSession.getInstance().getUserId());
        collectionDetail.setPayment(false);
        collectionDetail.setSalesInvoiceHeader(salesInvoiceHeader);
        collectionFormController.collectionDetails.add(collectionDetail);
        collectionFormController.updateLabelAmounts();
        // Close the stage and reset the reference to allow reopening
        if (collectionFormController.collectionDetailStage != null) {
            collectionFormController.collectionDetailStage.close();
        } else if (collectionFormController.adjustmentStage != null) {
            collectionFormController.adjustmentStage.close();
        }
        collectionFormController.adjustmentStage = null;
        collectionFormController.collectionDetailStage = null;
    }

    public void setCollectionDetail(CollectionDetail selectedItem, CollectionFormController collectionFormController) {
        collectionDetail = selectedItem;
        this.collectionFormController = collectionFormController;
        this.stage = collectionFormController.collectionDetailStage;

        // Null check for collectionDetail
        if (collectionDetail == null) {
            clearFields();
            return;
        }

        coaTextField.setText(collectionDetail.getType() != null ? collectionDetail.getType().getAccountTitle() : "");
        chequeNumberTextField.setText(collectionDetail.getCheckNo() != null ? collectionDetail.getCheckNo() : "");

        if (collectionDetail.getCheckDate() != null) {
            chequeDate.setValue(collectionDetail.getCheckDate().toLocalDateTime().toLocalDate());
        } else {
            chequeDate.setValue(null);
        }

        if (collectionDetail.getType() != null && "Cash on Hand".equals(collectionDetail.getType().getAccountTitle())) {
            parentBorderPane.setCenter(denominationPane);
            amountBox.setDisable(true);

            // **Load existing denominations into collectionDetailsDenominations**
            collectionDetailsDenominations.clear(); // Clear old data
            collectionDetailsDenominations.addAll(collectionDetail.getDenominations()); // Load from selectedItem

            for (CollectionDetailsDenomination collectionDetailsDenomination : collectionDetail.getDenominations()) {
                collectionDetailsDenomination.setCollectionDetail(collectionDetail);
            }

            Platform.runLater(() -> {
                updateTotalDenominationAmount();
                updateDenominationUI();
            });

        }

        bankNameTextField.setText(collectionDetail.getBank() != null ? collectionDetail.getBank().getName() : "");
        if (collectionDetail.getBalanceType() != null) {
            balanceType.getSelectionModel().select(collectionDetail.getBalanceType());
        } else {
            balanceType.getSelectionModel().clearSelection();
        }

        collectionAmount.setText(collectionDetail.getAmount() != null ? String.valueOf(collectionDetail.getAmount()) : "");
        remarksTextArea.setText(collectionDetail.getRemarks() != null ? collectionDetail.getRemarks() : "");

        if (collectionDetail.getSalesInvoiceHeader() != null) {
            invoiceNoTextField.setText(collectionDetail.getSalesInvoiceHeader().getInvoiceNo() != null ? collectionDetail.getSalesInvoiceHeader().getInvoiceNo() : "");
            customerNameTextField.setText(collectionDetail.getSalesInvoiceHeader().getCustomer() != null ? collectionDetail.getSalesInvoiceHeader().getCustomer().getStoreName() : "");
        }

        if (collectionDetail.getCustomer() != null) {
            customerNameTextField.setText(collectionDetail.getCustomer().getStoreName() != null ? collectionDetail.getCustomer().getStoreName() : "");
        } else {
            customerNameTextField.setText("");
        }
        confirmButton.setOnAction(event -> updateCollectionDetail());
    }


    private void updateCollectionDetail() {
        collectionDetail.setCheckNo(chequeNumberTextField.getText());
        collectionDetail.setCheckDate(chequeDate.getValue() != null ? Timestamp.valueOf(chequeDate.getValue().atStartOfDay()) : null);
        collectionDetail.setBank(collectionFormController.bankNames.stream()
                .filter(bank -> bank.getName().equals(bankNameTextField.getText()))
                .findFirst()
                .orElse(null));
        collectionDetail.setType(collectionFormController.chartOfAccounts.stream()
                .filter(coa -> coa.getAccountTitle().equals(coaTextField.getText()))
                .findFirst()
                .orElse(null));
        collectionDetail.setBalanceType(balanceType.getSelectionModel().getSelectedItem());
        collectionDetail.setAmount(collectionAmount.getText() != null && !collectionAmount.getText().isEmpty()
                ? Double.parseDouble(collectionAmount.getText())
                : null);
        collectionDetail.setRemarks(remarksTextArea.getText());
        collectionDetail.setDenominations(collectionDetailsDenominations);
        collectionDetail.setEncoderId(UserSession.getInstance().getUserId());
        collectionDetail.setPayment(collectionDetail.getType() != null && collectionDetail.getType().isPayment());
        collectionDetail.setSalesInvoiceHeader(salesInvoiceHeader);
        int index = collectionFormController.collectionDetails.indexOf(collectionDetail);
        if (index == -1) {
            collectionFormController.collectionDetails.add(collectionDetail);
        } else {
            collectionFormController.collectionDetails.set(index, collectionDetail);
        }
        collectionFormController.updateLabelAmounts();
        // Close the stage and reset the reference to allow reopening
        collectionFormController.collectionDetailStage.close();
        collectionFormController.collectionDetailStage = null;
    }


    // Helper method to clear all fields when selectedItem is null
    private void clearFields() {
        coaTextField.clear();
        chequeNumberTextField.clear();
        chequeDate.setValue(null);
        bankNameTextField.clear();
        balanceType.getSelectionModel().clearSelection();
        collectionAmount.clear();
        remarksTextArea.clear();
        customerNameTextField.clear();
    }

    public void setDataForInvoiceAdjustment(SalesInvoiceHeader selectedInvoice) {
        salesInvoiceHeader = selectedInvoice;

        // Set invoice number and customer name
        invoiceNoTextField.setText(salesInvoiceHeader.getInvoiceNo() != null ? salesInvoiceHeader.getInvoiceNo() : "");
        customerNameTextField.setText(salesInvoiceHeader.getCustomer() != null ? salesInvoiceHeader.getCustomer().getStoreName() : "");

        // Calculate the invoice balance
        double paidAmount = salesInvoiceHeader.getSalesInvoicePayments().stream()
                .mapToDouble(SalesInvoicePayment::getPaidAmount)
                .sum();
        double invoiceBalance = salesInvoiceHeader.getTotalAmount() - paidAmount;


        if (invoiceBalance < 0) {
            balanceType.getSelectionModel().select(new BalanceType(1, "CREDIT"));
        } else {
            balanceType.getSelectionModel().select(new BalanceType(2, "DEBIT"));
        }

        collectionAmount.setText(String.valueOf(Math.abs(invoiceBalance)));
        confirmButton.setOnAction(event -> addAdjustment());
    }

}
