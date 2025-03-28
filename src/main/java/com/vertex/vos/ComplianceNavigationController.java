package com.vertex.vos;

import com.vertex.vos.Utilities.HistoryManager;
import com.vertex.vos.Utilities.MaintenanceAlert;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class ComplianceNavigationController implements Initializable {
    private AnchorPane contentPane;

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    private final HistoryManager historyManager = new HistoryManager();

    private final int currentNavigationId = -1; // Initialize to a default value
    @FXML
    private VBox securitiesBox;
    @FXML
    private VBox taxTablesBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        animationInitialization();
    }

    private void animationInitialization() {
        new HoverAnimation(securitiesBox);
        new HoverAnimation(taxTablesBox);

        securitiesBox.setOnMouseClicked(mouseEvent -> MaintenanceAlert.showMaintenanceAlert());
        taxTablesBox.setOnMouseClicked(mouseEvent -> MaintenanceAlert.showMaintenanceAlert());
    }
}
