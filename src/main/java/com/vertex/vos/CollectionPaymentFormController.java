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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.TextFields;

import java.net.URL;
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

        TextFields.bindAutoCompletion(coaTextField, chartOfAccountsNames);
        TextFields.bindAutoCompletion(bankNameTextField, bankNamesList);
        detailsVBox.getChildren().removeAll(bankNameBox, chequeNumberBox);
        parentBorderPane.setCenter(null);

        for (int i = 0; i < denominations.size(); i++) {
            Denomination denomination = denominations.get(i);

            // Create the denomination label
            Label denominationLabel = new Label("₱ " + denomination.getAmount());
            // Create the quantity text field
            TextField quantityTextField = new TextField();
            quantityTextField.setPromptText("Quantity");

            // Create the CollectionDetailsDenomination object and bind the quantity to the TextField
            CollectionDetailsDenomination detailsDenomination = new CollectionDetailsDenomination();
            detailsDenomination.setDenomination(denomination);
            collectionDetailsDenominations.add(detailsDenomination);

            TextFieldUtils.addNumericInputRestriction(quantityTextField);

            // Bind the quantity of the CollectionDetailsDenomination to the TextField
            quantityTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    int quantity = Integer.parseInt(newValue); // Set quantity if the input is valid
                    detailsDenomination.setQuantity(quantity);
                    detailsDenomination.setAmount(denomination.getAmount() * quantity);
                    updateAmount();  // Recalculate the total amount whenever a quantity is updated
                } catch (NumberFormatException e) {
                    // Handle invalid input if the user enters something other than a number
                }
            });

            quantityTextField.setText("0");

            quantityTextField.setAlignment(Pos.TOP_RIGHT);

            // Add the denomination label and quantity text field to the GridPane
            denominationGridPane.add(denominationLabel, 0, i); // Column 0, Row i
            denominationGridPane.add(quantityTextField, 1, i); // Column 1, Row i
        }
    }

    // Method to recalculate and update the total amount
    private void updateAmount() {
        double totalAmount = collectionDetailsDenominations.stream()
                .mapToDouble(CollectionDetailsDenomination::getAmount)  // Get the amount for each denomination
                .sum();  // Sum all amounts

        amount.setText("₱ " + totalAmount);  // Update the amount label
    }

    Stage parentStage;
    CollectionFormController collectionFormController;
    Collection collection;

    public void createNewCollectionPayment(Stage parentStage, Stage stage, Collection collection, CollectionFormController collectionFormController) {
        this.parentStage = parentStage;
        this.collection = collection;
        this.collectionFormController = collectionFormController;

        collectionDetail = new CollectionDetail();

        coaTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                // Assuming you're using the newValue to set the Chart of Account on the collectionDetail
                chartOfAccounts.stream()
                        .filter(coa -> coa.getAccountTitle().equals(newValue))
                        .findFirst().ifPresent(selectedCOA -> collectionDetail.setType(selectedCOA));
            }

            if (collectionDetail.getType() != null) {
                if (!collectionDetail.getType().getAccountTitle().equals("Cash on Hand")) {
                    parentBorderPane.setCenter(null);
                    if (collectionDetail.getType().getAccountTitle().equals("Post Dated Check") || collectionDetail.getType().getAccountTitle().equals("Dated Check") || collectionDetail.getType().getAccountTitle().equals("Cash In Bank")) {
                        detailsVBox.getChildren().removeAll(bankNameBox, chequeNumberBox);
                    } else {
                        detailsVBox.getChildren().addAll(bankNameBox, chequeNumberBox);
                    }
                } else {
                    parentBorderPane.setCenter(denominationPane);
                }
                stage.sizeToScene();
            }
        });
    }
}
