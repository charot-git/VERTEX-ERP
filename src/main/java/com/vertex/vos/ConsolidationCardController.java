package com.vertex.vos;

import com.vertex.vos.Enums.ConsolidationStatus;
import com.vertex.vos.Objects.Consolidation;
import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.LoadingButton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import lombok.Setter;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

public class ConsolidationCardController {

    @FXML
    private ButtonBar buttonBar;

    @FXML
    private Label checkedBy;

    @FXML
    private Label createdAt;

    @FXML
    private Label createdBy;

    @FXML
    private Label docNo;

    @FXML
    private Label status;

    @FXML
    private Label updateAt;

    private final Button updateButton = new Button("Update");
    private final Button checking = new Button("View for checking");

    private final ChecklistDAO checklistDAO = new ChecklistDAO();

    @Setter
    private ConsolidationListController consolidationListController;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void setConsolidation(Consolidation selectedConsolidation) {
        buttonBar.getButtons().clear();
        docNo.setText(selectedConsolidation.getConsolidationNo());
        status.setText(selectedConsolidation.getStatus().toString());
        createdBy.setText(selectedConsolidation.getCreatedBy().getUser_fname() + " " + selectedConsolidation.getCreatedBy().getUser_lname());
        createdAt.setText(DATE_FORMATTER.format(selectedConsolidation.getCreatedAt().toLocalDateTime()));
        updateAt.setText(DATE_FORMATTER.format(selectedConsolidation.getUpdatedAt().toLocalDateTime()));
        // Null check for checkedBy (might not exist)
        if (selectedConsolidation.getCheckedBy() != null) {
            checkedBy.setText(selectedConsolidation.getCheckedBy().getUser_fname() + " " + selectedConsolidation.getCheckedBy().getUser_lname());
        } else {
            checkedBy.setText("Not Checked");
        }

        // Clear previous buttons to prevent duplicates
        buttonBar.getButtons().clear();

        if (selectedConsolidation.getStatus().equals(ConsolidationStatus.PENDING)) {
            buttonBar.getButtons().addAll(updateButton, checking);
        } else if (selectedConsolidation.getStatus().equals(ConsolidationStatus.PICKING)) {
            buttonBar.getButtons().add(checking);
        } else if (selectedConsolidation.getStatus().equals(ConsolidationStatus.PICKED)) {
            buttonBar.setDisable(true);
        }

        // Set event handlers
        updateButton.setOnAction(event -> {
            selectedConsolidation.setStockTransfers(FXCollections.observableArrayList(
                    consolidationListController.getConsolidationDAO().getStockTransfersForConsolidation(selectedConsolidation)
            ));
            selectedConsolidation.setDispatchPlans(FXCollections.observableArrayList(
                    consolidationListController.getConsolidationDAO().getDispatchPlansForConsolidation(selectedConsolidation)
            ));
            consolidationListController.openConsolidationForUpdate(selectedConsolidation);
        });

        checking.setOnAction(event -> {
            LoadingButton loadingButton = new LoadingButton(checking);
            loadingButton.start();

            ObservableList<ConsolidationDetails> checklist = checklistDAO.getChecklistForConsolidation(selectedConsolidation);

            selectedConsolidation.setStockTransfers(FXCollections.observableArrayList(
                    consolidationListController.getConsolidationDAO().getStockTransfersForConsolidation(selectedConsolidation)
            ));
            selectedConsolidation.setDispatchPlans(FXCollections.observableArrayList(
                    consolidationListController.getConsolidationDAO().getDispatchPlansForConsolidation(selectedConsolidation)
            ));

            selectedConsolidation.getDispatchPlans().forEach(dispatchPlan ->
                    dispatchPlan.setSalesOrders(
                            consolidationListController.getConsolidationDAO().getSalesOrdersForDispatchPlan(dispatchPlan)
                    )
            );

            openConsolidationForChecking(selectedConsolidation, checklist);
            loadingButton.stop();
        });
    }

    Stage checklistForm;


    public void openConsolidationForChecking(Consolidation selectedConsolidation, ObservableList<ConsolidationDetails> checklistProducts) {
        try {
            if (checklistForm != null) {
                checklistForm.show();
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ConsolidationCheckList.fxml"));
            Parent root = fxmlLoader.load();
            ConsolidationCheckListController controller = fxmlLoader.getController();
            controller.setConsolidation(selectedConsolidation);
            controller.setConsolidationListController(consolidationListController);
            controller.updateFields(checklistProducts);
            checklistForm = new Stage();
            checklistForm.setMaximized(true);
            checklistForm.setScene(new Scene(root));
            checklistForm.show();
        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Failed to open consolidation form.");
            e.printStackTrace();
        }
    }
}
