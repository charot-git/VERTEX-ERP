package com.vertex.vos;

import com.vertex.vos.Objects.HoverAnimation;
import com.vertex.vos.Objects.UserSession;
import com.vertex.vos.Utilities.ModuleManager;
import com.vertex.vos.Utilities.ToDoAlert;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AccountingContentController implements Initializable {
    public TilePane tilePane;
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
    private VBox openCreditMemo;
    @FXML
    private VBox openDebitMemo;
    @FXML
    private VBox openPayables;

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

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
                } else if (fxmlFileName.equals("CreditDebitList.fxml")) {
                    CreditDebitListController controller = loader.getController();
                    controller.setRegistrationType(registrationType);
                    controller.openCustomerSupplierSelection();
                    controller.setContentPane(contentPane);
                } else if (fxmlFileName.equals("PurchaseOrdersPerSupplierForPayment.fxml")) {
                    PurchaseOrdersPerSupplierForPaymentController controller = loader.getController();
                    controller.loadPurchaseOrdersForPayment();
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
        List<VBox> vboxes = List.of(openChartOfAccounts, openJournalEntries, openTrialBalance, openSummaryReport, openPurchaseOrder, openDebitMemo, openCreditMemo, openPayables);
        ModuleManager moduleManager = new ModuleManager(tilePane, vboxes);
        moduleManager.updateTilePane();
        
        new HoverAnimation(openChartOfAccounts);
        new HoverAnimation(openJournalEntries);
        new HoverAnimation(openTrialBalance);
        new HoverAnimation(openSummaryReport);
        new HoverAnimation(openPurchaseOrder);
        new HoverAnimation(openDebitMemo);
        new HoverAnimation(openCreditMemo);
        new HoverAnimation(openPayables);

        openPurchaseOrder.setOnMouseClicked(MouseEvent -> loadContent("purchaseOrderTypeAccounting.fxml", ""));

        openSummaryReport.setOnMouseClicked(MouseEvent -> loadContent("purchaseOrderConfirmationAccounting.fxml", ""));

        openJournalEntries.setOnMouseClicked(event -> {
            ToDoAlert.showToDoAlert();
        });
        openTrialBalance.setOnMouseClicked(event -> {
            ToDoAlert.showToDoAlert();
        });
        openChartOfAccounts.setOnMouseClicked((mouseEvent -> loadContent("tableManager.fxml", "chart_of_accounts")));

        openDebitMemo.setOnMouseClicked(mouseEvent -> loadContent("CreditDebitList.fxml", "Debit"));
        openCreditMemo.setOnMouseClicked(mouseEvent -> loadContent("CreditDebitList.fxml", "Credit"));
        openPayables.setOnMouseClicked(mouseEvent -> loadContent("PurchaseOrdersPerSupplierForPayment.fxml", ""));
    }
}
