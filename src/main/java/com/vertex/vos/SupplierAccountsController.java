package com.vertex.vos;

import com.vertex.vos.Objects.ComboBoxFilterUtil;
import com.vertex.vos.Objects.SupplierAccounts;
import com.vertex.vos.Utilities.SupplierAccountsDAO;
import com.vertex.vos.Utilities.SupplierDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class SupplierAccountsController implements Initializable {

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ObservableList<String> suppliers = supplierDAO.getAllSupplierNames();
        supplier.setItems(suppliers);
        ComboBoxFilterUtil.setupComboBoxFilter(supplier, suppliers);

        dateStart.setValue(LocalDate.now().minusYears(5));
        dateEnd.setValue(LocalDate.now().plusDays(1));

        ObservableList<String> types = FXCollections.observableArrayList("All", "Credit", "Debit");
        type.setItems(types);
        type.getSelectionModel().select("All");

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
                chartOfAccountNameColumn, creditColumn, debitColumn, updatedAtColumn);

        // Load accounts when supplier selection changes
        supplier.setOnAction(event -> loadSupplierAccounts());
        type.setOnAction(event -> loadSupplierAccounts());
        dateStart.setOnAction(event -> loadSupplierAccounts());
        dateEnd.setOnAction(event -> loadSupplierAccounts());

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
                        return typeMatch && dateMatch;
                    })
                    .collect(Collectors.toList());

            // Transform list to include the correct credit and debit amounts without summing them
            ObservableList<SupplierAccounts> observableList = FXCollections.observableArrayList();
            for (SupplierAccounts sa : filteredList) {
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
}
