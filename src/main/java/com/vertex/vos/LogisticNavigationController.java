package com.vertex.vos;

import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.ModuleManager;
import com.vertex.vos.Utilities.ToDoAlert;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class LogisticNavigationController implements Initializable {

    @FXML
    private VBox openPendingDeliveries;

    @FXML
    private VBox openTripSummary;
    @FXML
    private TilePane tilePane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<VBox> vboxes = List.of(openPendingDeliveries, openTripSummary);
        ModuleManager moduleManager = new ModuleManager(tilePane, vboxes);
        moduleManager.updateTilePane();

        new HoverAnimation(openPendingDeliveries);
        new HoverAnimation(openTripSummary);

        openTripSummary.setOnMouseClicked(event -> openTripSummaryWindow());
        openPendingDeliveries.setOnMouseClicked(event -> openPendingDeliveriesWindow());
    }

    private void openPendingDeliveriesWindow() {
        ToDoAlert.showToDoAlert();
    }

    Stage dispatchPlanStage;

    private void openDispatchPlanWindow() {
        if (dispatchPlanStage == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("DispatchPlanList.fxml"));
                Parent root = loader.load();
                DispatchPlanListController controller = loader.getController();
                dispatchPlanStage = new Stage();
                dispatchPlanStage.setTitle("Dispatch Plan List");
                dispatchPlanStage.setMaximized(true);
                dispatchPlanStage.setScene(new Scene(root));
                controller.loadDispatchPlanList();
                dispatchPlanStage.show();

                // Reset reference when the stage is closed
                dispatchPlanStage.setOnCloseRequest(event -> dispatchPlanStage = null);

            } catch (IOException e) {
                DialogUtils.showErrorMessage("Error", "Unable to open.");
                e.printStackTrace();
            }
        } else {
            dispatchPlanStage.toFront();
        }
    }

    Stage tripSummaryStage;

    private void openTripSummaryWindow() {
        if (tripSummaryStage == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("TripSummaryList.fxml"));
                Parent root = loader.load();
                TripSummaryListController controller = loader.getController();
                tripSummaryStage = new Stage();
                tripSummaryStage.setTitle("Trip Summary List");
                tripSummaryStage.setMaximized(true);
                tripSummaryStage.setScene(new Scene(root));
                controller.loadTripSummaryList();
                tripSummaryStage.show();

                // Reset reference when the stage is closed
                tripSummaryStage.setOnCloseRequest(event -> tripSummaryStage = null);

            } catch (IOException e) {
                DialogUtils.showErrorMessage("Error", "Unable to open.");
                e.printStackTrace();
            }
        } else {
            tripSummaryStage.toFront();
        }
    }
}
