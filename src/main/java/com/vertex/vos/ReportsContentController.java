package com.vertex.vos;

import com.vertex.vos.HoverAnimation;
import com.vertex.vos.Utilities.HistoryManager;
import com.vertex.vos.Utilities.ModuleManager;
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

public class ReportsContentController implements Initializable {
    public VBox openProductLedger;
    public VBox openBadProductsSummary;
    @Setter
    private AnchorPane contentPane; // Declare contentPane variable

    private final HistoryManager historyManager = new HistoryManager();

    private final int currentNavigationId = -1; // Initialize to a default value

    @FXML
    private VBox openSupplierAccount;
    @FXML
    private VBox openBranchPerformanceReport;

    @FXML
    private VBox openCustomerAccount;

    @FXML
    private VBox openEmployeePerformanceReport;

    @FXML
    private VBox openFastMovingReport;

    @FXML
    private VBox openSalesmanPerformanceReport;


    @FXML
    private TilePane tilePane;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ModuleManager moduleManager = new ModuleManager(tilePane, List.of(openBadProductsSummary, openBranchPerformanceReport, openCustomerAccount, openEmployeePerformanceReport, openFastMovingReport, openSalesmanPerformanceReport, openProductLedger));
        moduleManager.updateTilePane();
        new HoverAnimation(openSupplierAccount);
        new HoverAnimation(openProductLedger);
        new HoverAnimation(openBadProductsSummary);
        new HoverAnimation(openBranchPerformanceReport);
        new HoverAnimation(openCustomerAccount);
        new HoverAnimation(openEmployeePerformanceReport);
        new HoverAnimation(openFastMovingReport);
        new HoverAnimation(openSalesmanPerformanceReport);

        openSupplierAccount.setOnMouseClicked(mouseEvent -> openSupplierAccounts());
        openProductLedger.setOnMouseClicked(mouseEvent -> openProductLedgerWindow());
        openBadProductsSummary.setOnMouseClicked(mouseEvent -> openBadProductsSummaryWindow());
    }

    private void openBadProductsSummaryWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("BadOrderSummary.fxml"));
            Parent root = loader.load();

            BadOrderSummaryController controller = loader.getController();
            controller.initializeDataForBadProducts();

            Stage stage = new Stage();
            stage.setTitle("Bad Product Summary");
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void openProductLedgerWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vertex/vos/ProductLedger.fxml"));
            Parent root = loader.load();

            ProductLedgerController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Product Ledger");
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void openSupplierAccounts() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("supplierAccounts.fxml"));
            Parent root = loader.load();

            SupplierAccountsController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Supplier Accounts");
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
