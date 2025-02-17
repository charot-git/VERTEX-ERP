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
        Platform.runLater(()-> {
            confirmButton.setDefaultButton(true);
            TextFieldUtils.addDoubleInputRestriction(collectionAmount);
            TextFields.bindAutoCompletion(coaTextField, collectionFormController.chartOfAccountsNames);
            TextFields.bindAutoCompletion(bankNameTextField, collectionFormController.bankNamesList);
            balanceType.setItems(balanceTypeDAO.getAllBalanceTypes());
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

        assert selectedCOA != null;
        if (!selectedCOA.getAccountTitle().equals("Cash on Hand")) {
            collectionDetail.setCheckNo(chequeNumberTextField.getText());
            collectionDetail.setCheckDate(Timestamp.valueOf(chequeDate.getValue().atStartOfDay()));
            collectionDetail.setBank(bankName);
        }
        collectionDetail.setType(selectedCOA);
        collectionDetail.setAmount(Double.parseDouble(collectionAmount.getText()));
        collectionDetail.setRemarks(remarksTextArea.getText());
        collectionDetail.setDenominations(collectionDetailsDenominations);
        collectionDetail.setEncoderId(UserSession.getInstance().getUserId());
        collectionDetail.setPayment(true);
        collectionFormController.collectionDetails.add(collectionDetail);

        // Close the stage and reset the reference to allow reopening
        stage.close();
        collectionFormController.paymentStage = null;
    }


    Stage parentStage;
    CollectionFormController collectionFormController;
    Collection collection;

    private void updateAmount() {
        double totalAmount = 0;

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

    private void addAdjustment() {
    }
}
