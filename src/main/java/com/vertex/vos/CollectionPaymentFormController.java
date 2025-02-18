package com.vertex.vos;

import com.vertex.vos.DAO.DenominationDAO;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.BalanceTypeDAO;
import com.vertex.vos.Utilities.BankAccountDAO;
import com.vertex.vos.Utilities.ChartOfAccountsDAO;
import com.vertex.vos.Utilities.TextFieldUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
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
            TextFields.bindAutoCompletion(bankNameTextField, collectionFormController.bankNamesList);
            balanceType.setItems(balanceTypeDAO.getAllBalanceTypes());

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
                    }
                }
            }));

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
                    setText(item.getBalanceName()); // Assuming BalanceType has a method getName()
                }
            }
        });
        balanceType.setConverter(new StringConverter<BalanceType>() {
            @Override
            public String toString(BalanceType balanceType) {
                return balanceType == null ? "" : balanceType.getBalanceName(); // Display the BalanceName in the selected item text
            }

            @Override
            public BalanceType fromString(String string) {
                // Convert back from string to BalanceType if needed
                return null; // This can be modified if needed based on your logic
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
        TextFields.bindAutoCompletion(coaTextField, collectionFormController.chartOfAccounts.stream()
                .filter(ChartOfAccounts::isPayment)
                .map(ChartOfAccounts::getAccountTitle)
                .collect(Collectors.toList()));
        collectionDetail = new CollectionDetail();
        balanceType.getSelectionModel().selectFirst();
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

        denominationGridPane.getChildren().clear();

        // Iterate over the denominations and create UI elements for each
        for (int i = 0; i < denominations.size(); i++) {
            Denomination denomination = denominations.get(i);

            // Create denomination label
            Label denominationLabel = new Label("₱ " + denomination.getAmount());

            // Create quantity input
            TextField quantityTextField = new TextField();
            quantityTextField.setPromptText("Quantity");
            quantityTextField.setAlignment(Pos.TOP_RIGHT);

            // Restrict input to numbers
            TextFieldUtils.addNumericInputRestriction(quantityTextField);

            // Create and bind CollectionDetailsDenomination
            CollectionDetailsDenomination detailsDenomination = new CollectionDetailsDenomination();
            detailsDenomination.setDenomination(denomination);
            collectionDetailsDenominations.add(detailsDenomination);

            // Live update total amount on every input change
            quantityTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    int quantity = newValue.isEmpty() ? 0 : Integer.parseInt(newValue); // Convert input to integer

                    // Update denomination quantity and amount
                    detailsDenomination.setQuantity(quantity);
                    detailsDenomination.setAmount(denomination.getAmount() * quantity);

                    // Update total amount after changing the quantity

                } catch (NumberFormatException e) {
                    System.out.println("Invalid input: " + newValue);
                }
                updateAmount();
            });

            // Add elements to GridPane
            denominationGridPane.add(denominationLabel, 0, i);
            denominationGridPane.add(quantityTextField, 1, i);
        }
        confirmButton.setOnAction(event -> addPayment());
        stage.sizeToScene();
    }


    public void createNewAdjustment(Stage parentStage, Stage adjustmentStage, Collection collection, CollectionFormController collectionFormController) {
        this.parentStage = parentStage;
        this.collection = collection;
        this.collectionFormController = collectionFormController;
        this.stage = adjustmentStage;

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

        confirmButton.setOnAction(event -> addAdjustment());
    }

    SalesInvoiceHeader salesInvoiceHeader;

    private void addAdjustment() {
        BankName bankName = collectionFormController.bankNames.stream().filter(bank -> bank.getName().equals(bankNameTextField.getText())).findFirst().orElse(null);
        ChartOfAccounts selectedCOA = collectionFormController.chartOfAccounts.stream().filter(coa -> coa.getAccountTitle().equals(coaTextField.getText())).findFirst().orElse(null);

        collectionDetail.setCheckNo(chequeNumberTextField.getText());
        collectionDetail.setCheckDate(Timestamp.valueOf(chequeDate.getValue().atStartOfDay()));
        collectionDetail.setBank(bankName);
        collectionDetail.setType(selectedCOA);
        collectionDetail.setBalanceType(balanceType.getSelectionModel().getSelectedItem());
        collectionDetail.setAmount(Double.parseDouble(collectionAmount.getText()));
        collectionDetail.setRemarks(remarksTextArea.getText());
        collectionDetail.setDenominations(collectionDetailsDenominations);
        collectionDetail.setEncoderId(UserSession.getInstance().getUserId());
        collectionDetail.setPayment(false);
        collectionDetail.setSalesInvoiceHeader(salesInvoiceHeader);
        collectionFormController.collectionDetails.add(collectionDetail);
        collectionFormController.updateLabelAmounts();
        // Close the stage and reset the reference to allow reopening
        stage.close();
        collectionFormController.adjustmentStage = null;
    }
}
