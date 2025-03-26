package com.vertex.vos;

import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.ModuleManager;
import com.vertex.vos.Utilities.MaintenanceAlert;
import com.vertex.vos.Utilities.WindowLoader;
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

public class ConsolidationSubModulesController implements Initializable {

    public VBox openInvoicing;
    @FXML
    private VBox openDeliveryApproval;

    @FXML
    private VBox openWithdrawalsApproval;

    @FXML
    private VBox openPreDispatchPlan;

    @FXML
    private TilePane tilePane;

    @Setter
    AnchorPane contentPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<VBox> vboxes = List.of(openInvoicing, openDeliveryApproval, openWithdrawalsApproval, openPreDispatchPlan);
        ModuleManager moduleManager = new ModuleManager(tilePane, vboxes);
        moduleManager.updateTilePane();

        openPreDispatchPlan.setOnMouseClicked(event -> openPreDispatchPlanWindow());
        openDeliveryApproval.setOnMouseClicked(mouseEvent -> {
            WindowLoader.openWindowAsync(openDeliveryApproval, "com/vertex/vos/ConsolidationList.fxml", "Delivery Approval", controller -> {
                ((ConsolidationListController) controller).setConsolidationType("DISPATCH");
            });
        });
        openWithdrawalsApproval.setOnMouseClicked(mouseEvent -> openPickingStockTransferWindow());
        openInvoicing.setOnMouseClicked(mouseEvent -> openInvoicingWindow());
    }

    private void openInvoicingWindow() {
        MaintenanceAlert.showMaintenanceAlert();
    }

    private void openPickingStockTransferWindow() {
        MaintenanceAlert.showMaintenanceAlert();
    }


    Stage preDispatchPlanStage;

    private void openPreDispatchPlanWindow() {
        if (preDispatchPlanStage == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("PreDispatchPlanList.fxml"));
                Parent root = loader.load();
                PreDispatchPlanListController controller = loader.getController();
                controller.setConsolidationSubModulesController(this);
                preDispatchPlanStage = new Stage();
                preDispatchPlanStage.setTitle("Pre Dispatch Plan List");
                preDispatchPlanStage.setMaximized(true);
                preDispatchPlanStage.setScene(new Scene(root));
                controller.loadDispatchPlanList();
                preDispatchPlanStage.show();

                // Reset reference when the stage is closed
                preDispatchPlanStage.setOnCloseRequest(event -> preDispatchPlanStage = null);

            } catch (IOException e) {
                DialogUtils.showErrorMessage("Error", "Unable to open.");
                e.printStackTrace();
            }
        } else {
            preDispatchPlanStage.toFront();
        }
    }
}
