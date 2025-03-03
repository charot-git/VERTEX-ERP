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
        String sql = "INSERT INTO customer_memo_invoices (invoice_id, memo_id, amount, date_applied) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE amount = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (MemoInvoiceApplication memoInvoice : memoInvoices) {
                stmt.setInt(1, memoInvoice.getSalesInvoiceHeader().getInvoiceId()); // Get invoice ID
                stmt.setInt(2, memoInvoice.getCustomerMemo().getId()); // Get memo ID
                stmt.setDouble(3, memoInvoice.getAmount());
                stmt.setTimestamp(4, memoInvoice.getDateApplied());
                stmt.setDouble(5, memoInvoice.getAmount());
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
    public ObservableList<MemoInvoiceApplication> getInvoicesByMemoId(CustomerMemo memo) {
        String sql = "SELECT invoice_id, amount, date_applied FROM customer_memo_invoices WHERE memo_id = ?";
        ObservableList<MemoInvoiceApplication> list = FXCollections.observableArrayList();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memo.getId());
            ResultSet rs = stmt.executeQuery();

            List<Integer> invoiceIds = new ArrayList<>();
            while (rs.next()) {
                MemoInvoiceApplication memoInvoiceApplication = new MemoInvoiceApplication();
                memoInvoiceApplication.setCustomerMemo(memo);
                memoInvoiceApplication.setSalesInvoiceHeader(salesInvoiceDAO.getSalesInvoiceById(rs.getInt("invoice_id")));
                memoInvoiceApplication.setAmount(rs.getDouble("amount"));
                memoInvoiceApplication.setDateApplied(rs.getTimestamp("date_applied"));
                list.add(memoInvoiceApplication);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Delete a memo-invoice link using memo_id and invoice_id
    public boolean unlinkMemosFromInvoices(ObservableList<MemoInvoiceApplication> invoices) {
        String sql = "DELETE FROM customer_memo_invoices WHERE memo_id = ? AND invoice_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (MemoInvoiceApplication invoice : invoices) {
                stmt.setInt(1, invoice.getCustomerMemo().getId());
                stmt.setInt(2, invoice.getSalesInvoiceHeader().getInvoiceId());
                stmt.addBatch();
            }
            int[] affectedRows = stmt.executeBatch();

            // Check if at least one row was deleted
            boolean success = false;
            for (int count : affectedRows) {
                if (count > 0) {
                    success = true;
                    break;
                }
            }

            return success;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



}
