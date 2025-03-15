package com.vertex.vos;

import com.vertex.vos.Enums.ConsolidationStatus;
import com.vertex.vos.Objects.Consolidation;
import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.LoadingButton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import lombok.Setter;

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

    public void setConsolidation(Consolidation selectedConsolidation) {
        docNo.setText(selectedConsolidation.getConsolidationNo());
        status.setText(selectedConsolidation.getStatus().toString());
        createdBy.setText(selectedConsolidation.getCreatedBy().getUser_fname() + " " + selectedConsolidation.getCreatedBy().getUser_lname());
        checkedBy.setText(selectedConsolidation.getCheckedBy().getUser_fname() + " " + selectedConsolidation.getCheckedBy().getUser_lname());
        createdAt.setText(selectedConsolidation.getCreatedAt().toString());
        updateAt.setText(selectedConsolidation.getUpdatedAt().toString());

        if (selectedConsolidation.getStatus().equals(ConsolidationStatus.PENDING)) {
            buttonBar.getButtons().add(updateButton);
            buttonBar.getButtons().add(checking);
        } else if (selectedConsolidation.getStatus().equals(ConsolidationStatus.PICKING)) {
            buttonBar.getButtons().add(checking);
        } else if (selectedConsolidation.getStatus().equals(ConsolidationStatus.PICKED)) {
            buttonBar.setDisable(true);
        }

        updateButton.setOnAction(event -> {
            selectedConsolidation.setStockTransfers(FXCollections.observableArrayList(consolidationListController.getConsolidationDAO().getStockTransfersForConsolidation(selectedConsolidation)));
            selectedConsolidation.setDispatchPlans(FXCollections.observableArrayList(consolidationListController.getConsolidationDAO().getDispatchPlansForConsolidation(selectedConsolidation)));
            consolidationListController.openConsolidationForUpdate(selectedConsolidation);
        });
        checking.setOnAction(event -> CompletableFuture.runAsync(() -> {
            LoadingButton loadingButton = new LoadingButton(checking);
            loadingButton.start();
            ObservableList<ChecklistDTO> checklist = checklistDAO.getChecklistForConsolidation(selectedConsolidation);
            javafx.application.Platform.runLater(() -> {
                consolidationListController.openConsolidationForChecking(selectedConsolidation, checklist);
                loadingButton.stop();
            });
        }));
    }

    Button updateButton = new Button("Update");
    Button checking = new Button("View for checking");

    ChecklistDAO checklistDAO = new ChecklistDAO();

    @Setter
    ConsolidationListController consolidationListController;

}
