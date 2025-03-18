package com.vertex.vos;

import com.vertex.vos.Utilities.DialogUtils;
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

public class ConsolidationSubModulesController implements Initializable {

    @FXML
    private VBox openPickingDispatch;

    @FXML
    private VBox openPickingStockTransfer;

    @FXML
    private VBox openDispatchPlan;

    @FXML
    private TilePane tilePane;

    @Setter
    AnchorPane contentPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<VBox> vboxes = List.of(openPickingDispatch, openPickingStockTransfer, openDispatchPlan);
        ModuleManager moduleManager = new ModuleManager(tilePane, vboxes);
        moduleManager.updateTilePane();

        openDispatchPlan.setOnMouseClicked(event -> openDispatchPlanWindow());
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
}
