package com.vertex.vos;

import com.vertex.vos.Objects.BalanceType;
import com.vertex.vos.Objects.UserSession;
import com.vertex.vos.Utilities.HistoryManager;
import com.vertex.vos.Utilities.ModuleManager;
import com.vertex.vos.Utilities.ToDoAlert;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Setter;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AccountingContentController implements Initializable {
    public TilePane tilePane;
    public VBox openCollection;
    public VBox openSupplierCreditMemo;
    public VBox openSupplierDebitMemo;
    public VBox openCustomerCreditMemo;
    public VBox openCustomerDebitMemo;
    @Setter
    private AnchorPane contentPane; // Declare contentPane variable
    @FXML
    private VBox openTrialBalance;
    @FXML
    private VBox openJournalEntries;
    @FXML
    private VBox openChartOfAccounts;
    @FXML
    private VBox openPurchaseOrder;
    @FXML
    private VBox openSummaryReport;
    @FXML
    private VBox openPayables;


    private final HistoryManager historyManager = new HistoryManager();

    private int currentNavigationId = -1; // Initialize to a default value

    private void loadContent(String fxmlFileName, String registrationType) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
                Parent content = loader.load();

                if (fxmlFileName.equals("purchaseOrderTypeAccounting.fxml")) {
                    PurchaseOrderTypeController controller = loader.getController();
                    controller.setContentPane(contentPane);
                } else if (fxmlFileName.equals("purchaseOrderConfirmationAccounting.fxml")) {
                    PurchaseOrderConfirmationController controller = loader.getController();
                    controller.setContentPane(contentPane);
                } else if (fxmlFileName.equals("tableManager.fxml") && registrationType.equals("chart_of_accounts")) {
                    TableManagerController controller = loader.getController();
                    controller.setContentPane(contentPane);
                    controller.setRegistrationType(registrationType);
                } else if (fxmlFileName.equals("SupplierCreditDebitList.fxml")) {
                    SupplierCreditDebitListController controller = loader.getController();
                    controller.setRegistrationType(registrationType);
                    controller.openCustomerSupplierSelection();
                    controller.setContentPane(contentPane);
                } else if (fxmlFileName.equals("PurchaseOrdersPerSupplierForPayment.fxml")) {
                    PurchaseOrdersPerSupplierForPaymentController controller = loader.getController();
                    controller.loadPurchaseOrdersForPayment();
                } else if (fxmlFileName.equals("CollectionList.fxml")) {
                    CollectionListController collectionListController = loader.getController();
                    collectionListController.openCollectionForDisplay();
                }

                String sessionId = UserSession.getInstance().getSessionId();
                currentNavigationId = historyManager.addEntry(sessionId, fxmlFileName);

                ContentManager.setContent(contentPane, content); // Assuming contentPane is your AnchorPane
            } catch (IOException e) {
                e.printStackTrace(); // Handle the exception according to your needs
                System.err.println("Error loading " + fxmlFileName + ": " + e.getMessage());
            }

        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        List<VBox> vboxes = List.of(openCollection,
                openChartOfAccounts,
                openJournalEntries,
                openTrialBalance,
                openSummaryReport,
                openPurchaseOrder,
                openCustomerCreditMemo,
                openCustomerDebitMemo,
                openSupplierCreditMemo,
                openSupplierDebitMemo,
                openPayables);
        ModuleManager moduleManager = new ModuleManager(tilePane, vboxes);
        moduleManager.updateTilePane();

        new HoverAnimation(openChartOfAccounts);
        new HoverAnimation(openJournalEntries);
        new HoverAnimation(openTrialBalance);
        new HoverAnimation(openSummaryReport);
        new HoverAnimation(openPurchaseOrder);
        new HoverAnimation(openCustomerDebitMemo);
        new HoverAnimation(openCustomerCreditMemo);
        new HoverAnimation(openSupplierCreditMemo);
        new HoverAnimation(openSupplierDebitMemo);
        new HoverAnimation(openPayables);
        new HoverAnimation(openCollection);

        openPurchaseOrder.setOnMouseClicked(MouseEvent -> loadContent("purchaseOrderTypeAccounting.fxml", ""));

        openSummaryReport.setOnMouseClicked(MouseEvent -> loadContent("purchaseOrderConfirmationAccounting.fxml", ""));

        openJournalEntries.setOnMouseClicked(event -> {
            ToDoAlert.showToDoAlert();
        });
        openTrialBalance.setOnMouseClicked(event -> {
            ToDoAlert.showToDoAlert();
        });
        openChartOfAccounts.setOnMouseClicked((mouseEvent -> loadContent("tableManager.fxml", "chart_of_accounts")));

        openSupplierDebitMemo.setOnMouseClicked(mouseEvent -> loadContent("SupplierCreditDebitList.fxml", "Debit"));
        openSupplierCreditMemo.setOnMouseClicked(mouseEvent -> loadContent("SupplierCreditDebitList.fxml", "Credit"));
        openPayables.setOnMouseClicked(mouseEvent -> loadContent("PurchaseOrdersPerSupplierForPayment.fxml", ""));
        openCollection.setOnMouseClicked(mouseEvent -> loadContent("CollectionList.fxml", ""));

        openCustomerCreditMemo.setOnMouseClicked(event -> openCustomerMemoWindow(new BalanceType(1, "CREDIT")));
        openCustomerDebitMemo.setOnMouseClicked(event -> openCustomerMemoWindow(new BalanceType(2, "DEBIT")));
    }

    private Stage customerMemoWindow;

    private void openCustomerMemoWindow(BalanceType balanceType) {
        if (customerMemoWindow != null && customerMemoWindow.isShowing()) {
            customerMemoWindow.requestFocus();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CustomerCreditDebitList.fxml"));
            Parent root = loader.load();

            CustomerCreditDebitListController controller = loader.getController();
            controller.setBalanceType(balanceType);
            controller.loadData();

            customerMemoWindow = new Stage();
            customerMemoWindow.setTitle("Customer " + balanceType.getBalanceName().substring(0, 1).toUpperCase() + balanceType.getBalanceName().substring(1).toLowerCase());
            customerMemoWindow.setScene(new Scene(root));
            customerMemoWindow.setMaximized(true);
            customerMemoWindow.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
