package com.vertex.vos.DAO;

import com.vertex.vos.Objects.CustomerMemo;
import com.vertex.vos.Objects.MemoInvoiceApplication;
import com.vertex.vos.Objects.SalesInvoiceHeader;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomerMemoInvoiceDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public boolean linkMemoToInvoices(List<MemoInvoiceApplication> memoInvoices) {
        String sql = "INSERT INTO customer_memo_invoices (invoice_id, memo_id, amount, date_applied) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (MemoInvoiceApplication memoInvoice : memoInvoices) {
                stmt.setInt(1, memoInvoice.getSalesInvoiceHeader().getInvoiceId()); // Get invoice ID
                stmt.setInt(2, memoInvoice.getCustomerMemo().getId()); // Get memo ID
                stmt.setDouble(3, memoInvoice.getAmount());
                stmt.setTimestamp(4, memoInvoice.getDateApplied());
                stmt.addBatch();
            }

            int[] affectedRows = stmt.executeBatch();
            return affectedRows.length > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    SalesInvoiceDAO salesInvoiceDAO = new SalesInvoiceDAO();

    // Retrieve all invoices linked to a specific memo
    public ObservableList<SalesInvoiceHeader> getInvoicesByMemoId(int memoId) {
        String sql = "SELECT invoice_id FROM customer_memo_invoices WHERE memo_id = ?";
        ObservableList<SalesInvoiceHeader> list = FXCollections.observableArrayList();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memoId);
            ResultSet rs = stmt.executeQuery();

            List<Integer> invoiceIds = new ArrayList<>();
            while (rs.next()) {
                invoiceIds.add(rs.getInt("invoice_id"));
            }

            // Fetch all invoices using the extracted IDs
            if (!invoiceIds.isEmpty()) {
                list.addAll(salesInvoiceDAO.getInvoicesByInvoiceIds(invoiceIds));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Delete a memo-invoice link using memo_id and invoice_id
    public boolean unlinkMemosFromInvoices(int[] memoIds, int[] invoiceIds) {
        String sql = "DELETE FROM customer_memo_invoices WHERE (memo_id, invoice_id) IN ((?, ?))";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < memoIds.length; i++) {
                stmt.setInt(1, memoIds[i]);
                stmt.setInt(2, invoiceIds[i]);
                stmt.addBatch();
            }
            int[] affectedRows = stmt.executeBatch();
            return affectedRows.length > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
