package com.vertex.vos;

import com.vertex.vos.Objects.ComboBoxFilterUtil;
import com.vertex.vos.Objects.SalesOrderHeader;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;

public class PickListController {

    @FXML
    private HBox header;

    @FXML
    private TableView<SalesOrderHeader> salesOrdersForPicking;

    @FXML
    private ComboBox<String> employeeComboBox;

    @FXML
    private Button assignBrand;

    private AnchorPane contentPane;
    private EmployeeDAO employeeDAO = new EmployeeDAO();
    private SalesOrderDAO salesOrderDAO = new SalesOrderDAO();

    private ContextMenu contextMenu = new ContextMenu();
    private MenuItem generateItem = new MenuItem("Generate Pick List");
    private MenuItem approveItem = new MenuItem("Approve Picking");

    @FXML
    private void initialize() {
        addColumnsForOrders(salesOrdersForPicking);

        ObservableList<String> warehouseEmployees = employeeDAO.getAllEmployeeNamesWhereDepartment(5);
        employeeComboBox.setItems(warehouseEmployees);
        employeeComboBox.setPromptText("Select Employee");
        ComboBoxFilterUtil.setupComboBoxFilter(employeeComboBox, warehouseEmployees);
        TextFieldUtils.setComboBoxBehavior(employeeComboBox);

        loadSalesOrdersForPicking();
        employeeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                assignBrand.setDisable(false);
                generateItem.setText("Generate Pick List For " + newValue);
            } else {
                assignBrand.setDisable(true);
            }
        });

        assignBrand.setOnMouseClicked(event -> {
            String selectedEmployee = employeeComboBox.getValue();
            if (selectedEmployee != null) {
                int employeeId = employeeDAO.getUserIdByFullName(selectedEmployee);
                openWarehouseLinker(employeeId);
            }
        });

        generateItem.setOnAction(e -> {
            SalesOrderHeader selectedItem = salesOrdersForPicking.getSelectionModel().getSelectedItem();
            String selectedEmployee = employeeComboBox.getSelectionModel().getSelectedItem();
            generatePickingForEmployee(selectedItem, selectedEmployee);
        });

        approveItem.setOnAction(e -> {
            SalesOrderHeader selectedItem = salesOrdersForPicking.getSelectionModel().getSelectedItem();
            approvePicking(selectedItem);
        });

        salesOrdersForPicking.setOnContextMenuRequested(event -> {
            contextMenu.getItems().clear(); // Clear previous items
            contextMenu.getItems().addAll(generateItem, approveItem);
            contextMenu.show(salesOrdersForPicking, event.getScreenX(), event.getScreenY());
        });
    }

    private void generatePickingForEmployee(SalesOrderHeader selectedItem, String employee) {
        if (selectedItem != null && employee != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("pickListDetails.fxml"));
                Parent root = loader.load();
                PickListDetailsController controller = loader.getController();
                controller.initData(selectedItem, employeeDAO.getUserIdByFullName(employee));
                Stage stage = new Stage();
                stage.setTitle("Pick List For " + selectedItem.getOrderId());
                stage.setMaximized(true);
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                DialogUtils.showErrorMessage("Error", "Unable to open pick list details.");
                e.printStackTrace();
            }
        }
    }

    private void approvePicking(SalesOrderHeader selectedItem) {
        if (selectedItem != null) {
            ConfirmationAlert confirmationAlert = new ConfirmationAlert("Approve Picking", "Are you sure you want to approve picking?", "Approve picking for " + selectedItem.getOrderId(), true);
            boolean confirmed = confirmationAlert.showAndWait();
            if (confirmed) {
                selectedItem.setStatus("For Invoice");
                boolean updated = salesOrderDAO.updateSalesOrderStatus(selectedItem);
                if (updated) {
                    DialogUtils.showCompletionDialog("Success", "Picking approved successfully.");
                    loadSalesOrdersForPicking();
                }
                else {
                    DialogUtils.showErrorMessage("Error", "Unable to approve picking.");
                }
            }
        }
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


    private void openSalesOrderForPicking(SalesOrderHeader selectedOrder) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("pickListDetails.fxml"));
            Parent root = fxmlLoader.load();
            PickListDetailsController controller = fxmlLoader.getController();
            controller.initData(selectedOrder, employeeDAO.getUserIdByFullName(employeeComboBox.getValue()));
            Stage stage = new Stage();
            stage.setTitle(selectedOrder.getOrderId() + " - " + selectedOrder.getCustomerName());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Unable to open sales order.");
            e.printStackTrace();
        }
    }

    private void addColumnsForOrders(TableView<SalesOrderHeader> tableView) {
        tableView.getColumns().clear();

        TableColumn<SalesOrderHeader, Integer> orderIdCol = new TableColumn<>("Order ID");
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));

        TableColumn<SalesOrderHeader, String> customerNameCol = new TableColumn<>("Customer Name");
        customerNameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));

        TableColumn<SalesOrderHeader, Timestamp> orderDateCol = new TableColumn<>("Order Date");
        orderDateCol.setCellValueFactory(new PropertyValueFactory<>("orderDate"));

        TableColumn<SalesOrderHeader, BigDecimal> amountDueCol = new TableColumn<>("Amount Due");
        amountDueCol.setCellValueFactory(new PropertyValueFactory<>("amountDue"));

        TableColumn<SalesOrderHeader, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        tableView.getColumns().addAll(orderIdCol, customerNameCol, orderDateCol, amountDueCol, statusCol);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    public void loadSalesOrdersForPicking() {
        try {
            ObservableList<SalesOrderHeader> salesOrders = salesOrderDAO.getSalesOrderPerStatus("For Layout");
            salesOrdersForPicking.setItems(salesOrders);
        } catch (SQLException e) {
            DialogUtils.showErrorMessage("Error", "Unable to load sales orders.");
            e.printStackTrace();
        }
    }
}
