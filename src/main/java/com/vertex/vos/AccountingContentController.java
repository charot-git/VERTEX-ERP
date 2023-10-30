package com.vertex.vos;

import com.vertex.vos.Constructors.HoverAnimation;
import com.vertex.vos.Constructors.UserSession;
import com.vertex.vos.Utilities.ToDoAlert;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AccountingContentController implements Initializable {
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

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    private final HistoryManager historyManager = new HistoryManager();

    private int currentNavigationId = -1; // Initialize to a default value

    private void loadContent(String fxmlFileName) {
        System.out.println("Loading content: " + fxmlFileName); // Debug statement
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Parent content = loader.load();


            if (fxmlFileName.equals("purchaseOrderTypeAccounting.fxml")) {
                PurchaseOrderTypeController controller = loader.getController();
                controller.setContentPane(contentPane);
            }

            // Add entry to navigation history and get the generated ID
            String sessionId = UserSession.getInstance().getSessionId();
            currentNavigationId = historyManager.addEntry(sessionId, fxmlFileName);

            ContentManager.setContent(contentPane, content); // Assuming contentPane is your AnchorPane
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading " + fxmlFileName + ": " + e.getMessage());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        new HoverAnimation(openChartOfAccounts);
        new HoverAnimation(openJournalEntries);
        new HoverAnimation(openTrialBalance);
        new HoverAnimation(openSummaryReport);
        new HoverAnimation(openPurchaseOrder);

        openPurchaseOrder.setOnMouseClicked(MouseEvent -> loadContent("purchaseOrderTypeAccounting.fxml"));

        openChartOfAccounts.setOnMouseClicked(event -> {
            ToDoAlert.showToDoAlert();
        });
        openJournalEntries.setOnMouseClicked(event -> {
            ToDoAlert.showToDoAlert();
        });
        openTrialBalance.setOnMouseClicked(event -> {
            ToDoAlert.showToDoAlert();
        });
        openSummaryReport.setOnMouseClicked(event -> {
            ToDoAlert.showToDoAlert();
        });
    }


}
