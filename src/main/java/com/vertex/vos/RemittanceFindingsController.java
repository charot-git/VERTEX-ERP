package com.vertex.vos;

import com.vertex.vos.DAO.RemittanceAuditFindingsDAO;
import com.vertex.vos.Objects.RemittanceAuditFinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ResourceBundle;

public class RemittanceFindingsController implements Initializable {

    @FXML
    private TableColumn<RemittanceAuditFinding, Double> amountCol;

    @FXML
    private TableColumn<RemittanceAuditFinding, String> auditeeCol;

    @FXML
    private TextField auditeeFilter;

    @FXML
    private TableColumn<RemittanceAuditFinding, String> auditorCol;

    @FXML
    private TextField auditorFilter;

    @FXML
    private Button button;

    @FXML
    private TableColumn<RemittanceAuditFinding, Timestamp> dateAuditedCol;

    @FXML
    private TableColumn<RemittanceAuditFinding, Timestamp> dateFromCol;

    @FXML
    private TableColumn<RemittanceAuditFinding, Timestamp> dateToCol;

    @FXML
    private TableColumn<RemittanceAuditFinding, String> docNoCol;

    @FXML
    private TextField docNoFilter;

    @FXML
    private TableView<RemittanceAuditFinding> remittanceFindingsTable;

    ObservableList<RemittanceAuditFinding> auditFindings = FXCollections.observableArrayList();

    RemittanceAuditFindingsDAO remittanceAuditFindingsDAO = new RemittanceAuditFindingsDAO();

    public void loadRemittanceFindings() {
        auditFindings.setAll(remittanceAuditFindingsDAO.getAllAuditFindings());
        if (auditFindings.isEmpty()) {
            remittanceFindingsTable.setPlaceholder(new Label("No Findings"));
        }

        button.setOnAction(event -> {
            openNewRafWindow();
        });
    }

    private Stage remittanceFindingsFormStage;

    private void openNewRafWindow() {
        if (remittanceFindingsFormStage == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("RemittanceFindingsForm.fxml"));
                Parent root = loader.load();
                RemittanceFindingsFormController controller = loader.getController();
                remittanceFindingsFormStage = new Stage();
                controller.setRemittanceFindingsController(this);
                controller.createNewRaf(remittanceAuditFindingsDAO.generateNewDocNo());
                remittanceFindingsFormStage.setTitle("Remittance Findings Form");
                remittanceFindingsFormStage.setScene(new Scene(root));
                remittanceFindingsFormStage.show();

                remittanceFindingsFormStage.setOnCloseRequest(event -> remittanceFindingsFormStage = null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            remittanceFindingsFormStage.toFront(); // Bring the already opened stage to front if it's not null
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        remittanceFindingsTable.setItems(auditFindings);

        amountCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAmount()));
        auditeeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAuditee().getSalesmanName()));
        auditorCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAuditor().getUser_fname() + " " + cellData.getValue().getAuditor().getUser_lname()));
        dateAuditedCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDateAudited()));
        dateFromCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDateFrom()));
        dateToCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDateTo()));
        docNoCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDocNo()));
    }
}
