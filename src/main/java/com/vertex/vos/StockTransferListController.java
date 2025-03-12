package com.vertex.vos;

import com.vertex.vos.Objects.StockTransfer;
import com.vertex.vos.Utilities.BranchDAO;
import com.vertex.vos.Utilities.StockTransferDAO;
import javafx.application.Platform;
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
import lombok.Setter;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class StockTransferListController implements Initializable {

    public TableColumn<StockTransfer, Date> dateRequestedCol;
    @FXML
    private Button addButton;

    @FXML
    private TableColumn<StockTransfer, String> destinationCol;

    @FXML
    private Label header;

    @FXML
    private TableColumn<StockTransfer, Date> leadDateCol;

    @FXML
    private TableColumn<StockTransfer, String> sourceCol;

    @FXML
    private TableColumn<StockTransfer, String> statusCol;

    @FXML
    private TableColumn<StockTransfer, String> transferRefNoCol;

    @FXML
    private TableView<StockTransfer> transferTable;

    BranchDAO branchDAO = new BranchDAO();

    int maxVisibleRows;

    ObservableList<StockTransfer> stockTransferList = FXCollections.observableArrayList();

    StockTransferDAO stockTransferDAO = new StockTransferDAO();

    public void loadStockTransfer() {
        header.setText("Stock Transfer List");
        stockTransferList.setAll(stockTransferDAO.getAllGoodStockTransferHeader());
        ;

        transferTable.setOnMouseClicked(event -> {
            openExistingTransfer();
        });

        addButton.setOnAction(event -> openNewTransferForm());
    }

    Stage stockTransferStage;

    private void openNewTransferForm() {
        if (stockTransferStage != null && stockTransferStage.isShowing()) {
            stockTransferStage.toFront(); // Bring the existing window to the front
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("stockTransfer.fxml"));
            Parent content = loader.load();

            StockTransferController controller = loader.getController();
            controller.createNewGoodStockTransfer();

            stockTransferStage = new Stage();
            stockTransferStage.setTitle("Create New Stock Transfer");
            stockTransferStage.setScene(new Scene(content));
            stockTransferStage.setMaximized(true);
            controller.setStockTransferStage(stockTransferStage);
            controller.setStockTransferListController(this);

            // Clear the reference when the window is closed
            stockTransferStage.setOnCloseRequest(event -> stockTransferStage = null);

            stockTransferStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openExistingTransfer() {
        StockTransfer stockTransfer = transferTable.getSelectionModel().getSelectedItem();
        if (stockTransfer != null) {
            try {
                openTransactionForm(stockTransfer);
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void openTransactionForm(StockTransfer selectedStockTransfer) throws SQLException, IOException {
        if (stockTransferStage != null && stockTransferStage.isShowing()) {
            stockTransferStage.toFront(); // Bring the existing window to the front
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("stockTransfer.fxml"));
        Parent root = loader.load();

        // Get the controller instance from the FXMLLoader
        StockTransferController controller = loader.getController();

        // Initialize the data on the controller
        Platform.runLater(() -> controller.initData(selectedStockTransfer, this));

        stockTransferStage = new Stage();
        controller.setStockTransferStage(stockTransferStage);
        stockTransferStage.setTitle("Stock Transfer Details");
        stockTransferStage.setScene(new Scene(root));
        stockTransferStage.setMaximized(true);

        // Clear the reference when the window is closed
        stockTransferStage.setOnCloseRequest(event -> stockTransferStage = null);

        stockTransferStage.show();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        transferTable.setItems(stockTransferList);
        transferRefNoCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStockNo()));
        sourceCol.setCellValueFactory(cellData -> {
            int branchId = cellData.getValue().getSourceBranch();
            String branchName = branchDAO.getBranchNameById(branchId);
            return new SimpleStringProperty(branchName);
        });
        destinationCol.setCellValueFactory(cellData -> {
            int branchId = cellData.getValue().getTargetBranch();
            String branchName = branchDAO.getBranchNameById(branchId);
            return new SimpleStringProperty(branchName);
        });
        dateRequestedCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDateRequested()));
        leadDateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getLeadDate()));
        statusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        Platform.runLater(() -> {
            maxVisibleRows = getVisibleRowCount(transferTable);
            System.out.println(maxVisibleRows);
        });

    }

    private int getVisibleRowCount(TableView<?> tableView) {
        if (tableView.getItems().isEmpty()) {
            return 0; // No rows, nothing to display
        }

        // Get the height of a single row (first row)
        TableRow<?> firstRow = new TableRow<>();
        firstRow.updateIndex(0);
        double rowHeight = firstRow.prefHeight(-1); // Default row height

        // Calculate visible row count
        return (int) (tableView.getHeight() / rowHeight);
    }


    @Setter
    Stage stage;

    public void loadBadStockTransfer() {
        header.setText("Bad Stock Transfer List");
        stockTransferList.setAll(stockTransferDAO.getAllBadStockTransferHeader());
        ;

        transferTable.setOnMouseClicked(event -> {
            openExistingTransfer();
        });

        addButton.setOnAction(event -> openNewBadStockTransferForm());
    }

    private void openNewBadStockTransferForm() {
        if (stockTransferStage != null && stockTransferStage.isShowing()) {
            stockTransferStage.toFront(); // Bring the existing window to the front
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("stockTransfer.fxml"));
            Parent content = loader.load();

            StockTransferController controller = loader.getController();
            controller.createNewBadStockTransfer();

            stockTransferStage = new Stage();
            stockTransferStage.setTitle("Create New Stock Transfer");
            stockTransferStage.setScene(new Scene(content));
            stockTransferStage.setMaximized(true);
            controller.setStockTransferStage(stockTransferStage);
            controller.setStockTransferListController(this);

            // Clear the reference when the window is closed
            stockTransferStage.setOnCloseRequest(event -> stockTransferStage = null);

            stockTransferStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
