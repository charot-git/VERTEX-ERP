package com.vertex.vos;

import com.vertex.vos.Objects.ComboBoxFilterUtil;
import com.vertex.vos.Objects.SalesOrder;
import com.vertex.vos.Utilities.*;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Setter;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;

public class PickListController {

    @FXML
    private HBox header;

    @FXML
    private TableView<SalesOrder> salesOrdersForPicking;

    @FXML
    private ComboBox<String> employeeComboBox;

    @FXML
    private Button assignBrand;

    @Setter
    private AnchorPane contentPane;
    private EmployeeDAO employeeDAO = new EmployeeDAO();
    private SalesOrderDAO salesOrderDAO = new SalesOrderDAO();

    private ContextMenu contextMenu = new ContextMenu();
    private MenuItem generateItem = new MenuItem("Generate Pick List");
    private MenuItem approveItem = new MenuItem("Approve Picking");

    @FXML
    private void initialize() {

    }


    private void openWarehouseLinker(int employeeId) {
        if (employeeId != -1) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("assignBrandToWarehouseMen.fxml"));
                Parent root = loader.load();
                AssignBrandToWarehouseMenController controller = loader.getController();
                controller.initData(employeeId);

                Stage stage = new Stage();
                stage.setTitle("Assign Brand To " + employeeComboBox.getValue());
                stage.setScene(new Scene(root));
                stage.setResizable(false);
                stage.initStyle(StageStyle.UTILITY);
                stage.show();
            } catch (IOException e) {
                DialogUtils.showErrorMessage("Error", "Unable to open warehouse linker.");
                e.printStackTrace();
            }
        } else {
            DialogUtils.showErrorMessage("Error", "Employee not found.");
        }
    }

}
