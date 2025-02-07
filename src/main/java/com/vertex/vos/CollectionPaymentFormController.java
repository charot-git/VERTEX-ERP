package com.vertex.vos;

import com.vertex.vos.DAO.DenominationDAO;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.BankAccountDAO;
import com.vertex.vos.Utilities.ChartOfAccountsDAO;
import com.vertex.vos.Utilities.TextFieldUtils;
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

    ChartOfAccountsDAO coaDAO = new ChartOfAccountsDAO();

    ObservableList<ChartOfAccounts> chartOfAccounts = FXCollections.observableArrayList(coaDAO.getAllChartOfAccounts());

    List<String> chartOfAccountsNames = chartOfAccounts.stream().map(ChartOfAccounts::getAccountTitle).collect(Collectors.toList());

    BankAccountDAO bankAccountDAO = new BankAccountDAO();

    ObservableList<BankName> bankNames = FXCollections.observableArrayList(bankAccountDAO.getBankNames());

    List<String> bankNamesList = bankNames.stream().map(BankName::getName).collect(Collectors.toList());

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        confirmButton.setDefaultButton(true);

        TextFieldUtils.addDoubleInputRestriction(collectionAmount);
        TextFieldUtils.addNumericInputRestriction(chequeNumberTextField);
        TextFields.bindAutoCompletion(coaTextField, chartOfAccountsNames);
        TextFields.bindAutoCompletion(bankNameTextField, bankNamesList);
        detailsVBox.getChildren().removeAll(bankNameBox, chequeNumberBox);
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
        BankName bankName = bankNames.stream().filter(bank -> bank.getName().equals(bankNameTextField.getText())).findFirst().orElse(null);
        ChartOfAccounts selectedCOA = chartOfAccounts.stream().filter(coa -> coa.getAccountTitle().equals(coaTextField.getText())).findFirst().orElse(null);

        assert selectedCOA != null;
        if (!selectedCOA.getAccountTitle().equals("Cash on Hand")) {
            collectionDetail.setCheckNo(Integer.valueOf(chequeNumberTextField.getText()));
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

        // Store the original order of detailsVBox children
        List<Node> originalNodes = new ArrayList<>(detailsVBox.getChildren());

        coaTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                chartOfAccounts.stream()
                        .filter(coa -> coa.getAccountTitle().equals(newValue))
                        .findFirst()
                        .ifPresent(selectedCOA -> collectionDetail.setType(selectedCOA));
            }

            if (collectionDetail.getType() != null) {
                // Restore original order
                detailsVBox.getChildren().setAll(originalNodes);

                if (!collectionDetail.getType().getAccountTitle().equals("Cash on Hand")) {
                    parentBorderPane.setCenter(null);
                    amountBox.setDisable(false); // Re-enable in case it was disabled before

                    if (collectionDetail.getType().getAccountTitle().equals("Post Dated Check") ||
                            collectionDetail.getType().getAccountTitle().equals("Dated Check") ||
                            collectionDetail.getType().getAccountTitle().equals("Cash In Bank")) {

                        detailsVBox.getChildren().removeAll(bankNameBox, chequeNumberBox);
                    } else {
                        detailsVBox.getChildren().addAll(bankNameBox, chequeNumberBox);
                    }
                } else {
                    parentBorderPane.setCenter(denominationPane);
                    amountBox.setDisable(true);
                }

                // Ensure remarksBox is always last
                detailsVBox.getChildren().remove(remarksBox);
                detailsVBox.getChildren().add(remarksBox);

                stage.sizeToScene();
            }
        });

        // Clear existing GridPane before populating (avoids duplication)
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
    }


    public void createNewAdjustment(Stage parentStage, Stage adjustmentStage, Collection collection, CollectionFormController collectionFormController) {
        this.parentStage = parentStage;
        this.collection = collection;
        this.collectionFormController = collectionFormController;
        this.stage = adjustmentStage;

        collectionDetail = new CollectionDetail();

        // Store the original order of detailsVBox children
        List<Node> originalNodes = new ArrayList<>(detailsVBox.getChildren());

        coaTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                chartOfAccounts.stream()
                        .filter(coa -> coa.getAccountTitle().equals(newValue))
                        .findFirst()
                        .ifPresent(selectedCOA -> collectionDetail.setType(selectedCOA));
            }

            if (collectionDetail.getType() != null) {
                // Restore original order
                detailsVBox.getChildren().setAll(originalNodes);

                if (!collectionDetail.getType().getAccountTitle().equals("Cash on Hand")) {
                    parentBorderPane.setCenter(null);
                    amountBox.setDisable(false); // Re-enable in case it was disabled before

                    if (collectionDetail.getType().getAccountTitle().equals("Post Dated Check") ||
                            collectionDetail.getType().getAccountTitle().equals("Dated Check") ||
                            collectionDetail.getType().getAccountTitle().equals("Cash In Bank")) {

                        detailsVBox.getChildren().removeAll(bankNameBox, chequeNumberBox);
                    } else {
                        detailsVBox.getChildren().addAll(bankNameBox, chequeNumberBox);
                    }
                } else {
                    parentBorderPane.setCenter(denominationPane);
                    amountBox.setDisable(true);
                }

                // Ensure remarksBox is always last
                detailsVBox.getChildren().remove(remarksBox);
                detailsVBox.getChildren().add(remarksBox);

                stage.sizeToScene();
            }
        });

        confirmButton.setOnAction(event -> addAdjustment());
    }

    private void addAdjustment() {
    }
}
