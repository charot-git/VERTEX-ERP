package com.vertex.vos;

import com.vertex.vos.Objects.ComboBoxFilterUtil;
import com.vertex.vos.Objects.PurchaseOrder;
import com.vertex.vos.Objects.SupplierAccounts;
import com.vertex.vos.Utilities.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class SupplierAccountsController implements Initializable {

    public ComboBox<String> glAccount;
    public Button exportButton;
    @FXML
    private TableView<SupplierAccounts> accountTable;

    @FXML
    private ComboBox<String> supplier;

    @FXML
    private ComboBox<String> type;

    @FXML
    private DatePicker dateStart;

    @FXML
    private DatePicker dateEnd;

    SupplierDAO supplierDAO = new SupplierDAO();
    SupplierAccountsDAO supplierAccountsDAO = new SupplierAccountsDAO();
    ChartOfAccountsDAO chartOfAccountsDAO = new ChartOfAccountsDAO();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ObservableList<String> suppliers = supplierDAO.getAllSupplierNames();
        ObservableList<String> glAccounts = chartOfAccountsDAO.getAllAccountNames();
        supplier.setItems(suppliers);
        ComboBoxFilterUtil.setupComboBoxFilter(supplier, suppliers);
        ComboBoxFilterUtil.setupComboBoxFilter(glAccount, glAccounts);

        dateStart.setValue(LocalDate.now().minusYears(5));
        dateEnd.setValue(LocalDate.now().plusDays(1));

        accountTable.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                // Handle double-click
                handleDoubleClick();
            }
        });


        ObservableList<String> types = FXCollections.observableArrayList("All", "Credit", "Debit");
        type.setItems(types);
        type.getSelectionModel().select("All");

        TableViewFormatter.formatTableView(accountTable);

        // Define columns
        TableColumn<SupplierAccounts, String> documentTypeColumn = new TableColumn<>("Document Type");
        documentTypeColumn.setCellValueFactory(new PropertyValueFactory<>("documentType"));

        TableColumn<SupplierAccounts, String> documentNumberColumn = new TableColumn<>("Document Number");
        documentNumberColumn.setCellValueFactory(new PropertyValueFactory<>("documentNumber"));

        TableColumn<SupplierAccounts, String> chartOfAccountNameColumn = new TableColumn<>("Account");
        chartOfAccountNameColumn.setCellValueFactory(new PropertyValueFactory<>("chartOfAccountName"));

        TableColumn<SupplierAccounts, BigDecimal> creditColumn = new TableColumn<>("Credit");
        creditColumn.setCellValueFactory(new PropertyValueFactory<>("creditAmount"));

        TableColumn<SupplierAccounts, BigDecimal> debitColumn = new TableColumn<>("Debit");
        debitColumn.setCellValueFactory(new PropertyValueFactory<>("debitAmount"));

        TableColumn<SupplierAccounts, Timestamp> updatedAtColumn = getAccountTransactionDate();

        // Add columns to table
        accountTable.getColumns().addAll(documentTypeColumn, documentNumberColumn,
                chartOfAccountNameColumn, debitColumn, creditColumn, updatedAtColumn);

        // Load accounts when supplier selection changes
        supplier.setOnAction(event -> loadSupplierAccounts());
        type.setOnAction(event -> loadSupplierAccounts());
        dateStart.setOnAction(event -> loadSupplierAccounts());
        dateEnd.setOnAction(event -> loadSupplierAccounts());
        glAccount.setOnAction(event -> loadSupplierAccounts());

        // Apply custom styling to the total row
        accountTable.setRowFactory(tv -> new TableRow<SupplierAccounts>() {
            @Override
            protected void updateItem(SupplierAccounts item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setStyle("");
                } else if ("Total".equals(item.getDocumentType())) {
                    setStyle("-fx-background-color: #5A90CF; -fx-text-fill: whitesmoke; -fx-font-weight: bold;");
                } else {
                    setStyle("");
                }
            }
        });

        exportButton.setOnMouseClicked(mouseEvent -> {
            openExportDialog();
        });

    }

    private void openExportDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(new Stage());

        if (file != null) {
            try {
                ExcelExporter.exportToExcel(accountTable, file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleDoubleClick() {
        SupplierAccounts selectedAccount = accountTable.getSelectionModel().getSelectedItem();
        if (selectedAccount != null) {
            switch (selectedAccount.getDocumentType()) {
                case "Voucher" -> openVoucher(selectedAccount);
                case "Supplier Memo" -> openMemo(selectedAccount);
                case "PO" -> openPO(selectedAccount);
            }
        }
    }

    PurchaseOrderDAO purchaseOrderDAO = new PurchaseOrderDAO();

    private void openPO(SupplierAccounts selectedAccount) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vertex/vos/PayablesForm.fxml"));
                Parent content = loader.load();
                PayablesFormController controller = loader.getController();

                controller.openPayables(selectedAccount);

                Stage stage = new Stage();
                stage.setTitle("PO#" + selectedAccount.getDocumentNumber());
                stage.setResizable(true);
                stage.setMaximized(true);
                stage.setScene(new Scene(content));
                stage.showAndWait();
            } catch (IOException e) {
                DialogUtils.showErrorMessage("Error", "Failed to load the Payables Form: " + e.getMessage());
                e.printStackTrace();  // Add this for debugging
            } catch (Exception e) {
                DialogUtils.showErrorMessage("Error", "An unexpected error occurred: " + e.getMessage());
                e.printStackTrace();  // Add this for debugging
            }
        });
    }

    private void openMemo(SupplierAccounts selectedAccount) {
    }

    private void openVoucher(SupplierAccounts selectedAccount) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vertex/vos/VoucherForm.fxml"));
                Parent content = loader.load();
                VoucherFormController controller = loader.getController();

                controller.openVoucher(selectedAccount);

                Stage stage = new Stage();
                stage.setTitle("Voucher#" + selectedAccount.getDocumentNumber());
                stage.setResizable(true);
                stage.setMaximized(true);
                stage.setScene(new Scene(content));
                stage.showAndWait();
            } catch (IOException e) {
                DialogUtils.showErrorMessage("Error", "Failed to load the Payables Form: " + e.getMessage());
                e.printStackTrace();  // Add this for debugging
            } catch (Exception e) {
                DialogUtils.showErrorMessage("Error", "An unexpected error occurred: " + e.getMessage());
                e.printStackTrace();  // Add this for debugging
            }
        });
    }

    private static TableColumn<SupplierAccounts, Timestamp> getAccountTransactionDate() {
        TableColumn<SupplierAccounts, Timestamp> updatedAtColumn = new TableColumn<>("Date");
        updatedAtColumn.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));

        // Custom cell factory to format Timestamp as Date
        updatedAtColumn.setCellFactory(new Callback<TableColumn<SupplierAccounts, Timestamp>, TableCell<SupplierAccounts, Timestamp>>() {
            @Override
            public TableCell<SupplierAccounts, Timestamp> call(TableColumn<SupplierAccounts, Timestamp> param) {
                return new TableCell<SupplierAccounts, Timestamp>() {
                    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                    @Override
                    protected void updateItem(Timestamp item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(dateFormat.format(item));
                        }
                    }
                };
            }
        });
        return updatedAtColumn;
    }

    private void loadSupplierAccounts() {
        String selectedSupplier = supplier.getSelectionModel().getSelectedItem();
        String selectedType = type.getSelectionModel().getSelectedItem();
        String selectedChartOfAccount = glAccount.getSelectionModel().getSelectedItem();
        LocalDate startDate = dateStart.getValue();
        LocalDate endDate = dateEnd.getValue();

        if (selectedSupplier != null) {
            int supplierId = supplierDAO.getSupplierIdByName(selectedSupplier);

            // Call the unified method to get both purchase order payments and supplier memos
            List<SupplierAccounts> supplierAccountsList = supplierAccountsDAO.getSupplierAccounts(supplierId);

            // Apply filters
            List<SupplierAccounts> filteredList = supplierAccountsList.stream()
                    .filter(sa -> {
                        boolean typeMatch = "All".equals(selectedType) || sa.getTransactionTypeName().equals(selectedType);
                        boolean dateMatch = (startDate == null || !sa.getUpdatedAt().toLocalDateTime().toLocalDate().isBefore(startDate)) &&
                                (endDate == null || !sa.getUpdatedAt().toLocalDateTime().toLocalDate().isAfter(endDate));
                        boolean chartOfAccountMatch = (selectedChartOfAccount == null || selectedChartOfAccount.isEmpty()) ||
                                selectedChartOfAccount.equals(sa.getChartOfAccountName());
                        return typeMatch && dateMatch && chartOfAccountMatch;
                    })
                    .collect(Collectors.toList());

            // Transform list to include the correct credit and debit amounts without summing them
            ObservableList<SupplierAccounts> observableList = FXCollections.observableArrayList();
            for (SupplierAccounts sa : filteredList) {
                SupplierAccounts updatedSa = getSupplierAccounts(sa);
                observableList.add(updatedSa);
            }

            // Calculate totals
            BigDecimal totalCredit = observableList.stream()
                    .map(SupplierAccounts::getCreditAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalDebit = observableList.stream()
                    .map(SupplierAccounts::getDebitAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Create a total row
            SupplierAccounts totalRow = new SupplierAccounts();
            totalRow.setDocumentType("Total");
            totalRow.setDocumentNumber("");
            totalRow.setChartOfAccountName("");
            totalRow.setCreditAmount(totalCredit);
            totalRow.setDebitAmount(totalDebit);
            totalRow.setUpdatedAt(null); // To ensure it appears at the end

            // Add the total row to the list
            observableList.add(totalRow);

            // Sort by updatedAt timestamp
            observableList.sort((a, b) -> {
                if (a.getUpdatedAt() == null) return 1; // Total row goes to the end
                if (b.getUpdatedAt() == null) return -1; // Total row goes to the end
                return b.getUpdatedAt().compareTo(a.getUpdatedAt());
            });

            // Set items to TableView
            accountTable.setItems(observableList);
        }
    }


    private static SupplierAccounts getSupplierAccounts(SupplierAccounts sa) {
        SupplierAccounts updatedSa = new SupplierAccounts(
                sa.getSupplierId(),
                sa.getSupplierName(),
                sa.getDocumentType(),
                sa.getDocumentNumber(),
                sa.getAmount(),
                sa.getChartOfAccountId(),
                sa.getChartOfAccountName(),
                sa.getCreatedAt(),
                sa.getUpdatedAt(),
                sa.getTransactionTypeId(),
                sa.getTransactionTypeName()
        );

        // Set creditAmount or debitAmount based on transaction type
        if ("Credit".equals(sa.getTransactionTypeName())) {
            updatedSa.setCreditAmount(sa.getAmount());
            updatedSa.setDebitAmount(BigDecimal.ZERO);
        } else if ("Debit".equals(sa.getTransactionTypeName())) {
            updatedSa.setDebitAmount(sa.getAmount());
            updatedSa.setCreditAmount(BigDecimal.ZERO);
        } else {
            updatedSa.setCreditAmount(BigDecimal.ZERO);
            updatedSa.setDebitAmount(BigDecimal.ZERO);
        }
        return updatedSa;
    }
}
