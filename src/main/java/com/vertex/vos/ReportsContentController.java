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

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ReportsContentController implements Initializable {
    private AnchorPane contentPane; // Declare contentPane variable

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

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
        ModuleManager moduleManager = new ModuleManager(tilePane, List.of(openSupplierAccount, openBranchPerformanceReport, openCustomerAccount, openEmployeePerformanceReport, openFastMovingReport, openSalesmanPerformanceReport));
        moduleManager.updateTilePane();
        new HoverAnimation(openSupplierAccount);

        openSupplierAccount.setOnMouseClicked(mouseEvent -> openSupplierAccounts());
    }

    private void openSupplierAccounts() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("supplierAccounts.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();

            SupplierAccountsController controller = loader.getController();


            stage.setMaximized(true);
            stage.setScene(new Scene(root));
            stage.setTitle("Supplier Accounts");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
