package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.SupplierAccounts;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SupplierAccountsDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    //filter credit debit
    //date range
    //total amount

    public List<SupplierAccounts> getSupplierAccounts(int supplierId) {
        List<SupplierAccounts> supplierAccountsList = new ArrayList<>();
        String query = "SELECT p.purchase_order_id AS documentNumber, " +
                "p.paid_amount AS amount, p.chart_of_account AS chartOfAccountId, " +
                "c.account_title AS chartOfAccountName, p.created_at AS createdAt, p.updated_at AS updatedAt, " +
                "s.id AS supplierId, s.supplier_name AS supplierName, " +
                "2 AS transactionTypeId, 'Debit' AS transactionTypeName, " + // Assuming 'Credit' for payments
                "'PO' AS documentType " +
                "FROM purchase_order_payment p " +
                "JOIN suppliers s ON p.supplier_id = s.id " +
                "LEFT JOIN chart_of_accounts c ON p.chart_of_account = c.coa_id " +
                "WHERE s.id = ? " +
                "UNION ALL " +
                "SELECT sm.id AS documentNumber, " +
                "sm.amount AS amount, sm.chart_of_account AS chartOfAccountId, " +
                "c.account_title AS chartOfAccountName, sm.created_at AS createdAt, sm.updated_at AS updatedAt, " +
                "s.id AS supplierId, s.supplier_name AS supplierName, " +
                "sm.type AS transactionTypeId, " +
                "CASE WHEN sm.type = 1 THEN 'Credit' ELSE 'Debit' END AS transactionTypeName, " + // Assuming 1 for Credit, adjust as necessary
                "'Supplier Memo' AS documentType " +
                "FROM suppliers_memo sm " +
                "JOIN suppliers s ON sm.supplier_id = s.id " +
                "LEFT JOIN chart_of_accounts c ON sm.chart_of_account = c.coa_id " +
                "WHERE sm.supplier_id = ? AND sm.status = 'Applied'";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, supplierId);
            stmt.setInt(2, supplierId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SupplierAccounts supplierAccount = new SupplierAccounts(
                            rs.getInt("supplierId"),
                            rs.getString("supplierName"),
                            rs.getString("documentType"), // Document type for each record
                            rs.getString("documentNumber"),
                            rs.getBigDecimal("amount"),
                            rs.getInt("chartOfAccountId"),
                            rs.getString("chartOfAccountName"),
                            rs.getTimestamp("createdAt"),
                            rs.getTimestamp("updatedAt"),
                            rs.getInt("transactionTypeId"),
                            rs.getString("transactionTypeName")
                    );
                    supplierAccountsList.add(supplierAccount);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return supplierAccountsList;
    }
}
