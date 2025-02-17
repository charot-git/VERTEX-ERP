package com.vertex.vos.DAO;

import com.vertex.vos.Objects.SalesInvoicePayment;
import com.vertex.vos.Utilities.BankAccountDAO;
import com.vertex.vos.Utilities.ChartOfAccountsDAO;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesInvoicePaymentsDAO {
    private final HikariDataSource hikariDataSource = DatabaseConnectionPool.getDataSource();

    SalesInvoiceDAO salesInvoiceDAO = new SalesInvoiceDAO();
    ChartOfAccountsDAO chartOfAccountsDAO = new ChartOfAccountsDAO();
    BankAccountDAO bankAccountDAO = new BankAccountDAO();


    public List<SalesInvoicePayment> getPaymentsByInvoice(int invoiceId) {
        String sql = "SELECT * FROM sales_invoice_payments WHERE invoice_id = ?";
        List<SalesInvoicePayment> payments = new ArrayList<>();

        try (Connection conn = hikariDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, invoiceId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                SalesInvoicePayment payment = new SalesInvoicePayment();
                payment.setId(rs.getInt("id"));
                payment.setInvoice(salesInvoiceDAO.loadSalesInvoiceById(invoiceId));
                payment.setOrderId(rs.getString("order_id"));
                payment.setChartOfAccount(chartOfAccountsDAO.getChartOfAccountById(rs.getInt("coa_id")));
                payment.setBank(bankAccountDAO.getBankNameById(rs.getInt("bank_id")));
                payment.setReferenceNo(rs.getString("reference_no"));
                payment.setPaidAmount(rs.getDouble("paid_amount"));
                payment.setDatePaid(rs.getTimestamp("date_paid"));
                payment.setDateEncoded(rs.getTimestamp("date_encoded"));

                payments.add(payment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }

    // delete payments
    public boolean deletePayments(Connection conn, List<SalesInvoicePayment> salesInvoicePayments) {
        String sql = "DELETE FROM sales_invoice_payments WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Add each payment to the batch for deletion
            for (SalesInvoicePayment payment : salesInvoicePayments) {
                pstmt.setInt(1, payment.getId());
                pstmt.addBatch();  // Add to batch
            }

            int[] rowsAffected = pstmt.executeBatch();  // Execute the batch of delete operations

            // Check if any rows were affected, meaning the operation was successful
            return rowsAffected.length > 0;

        } catch (SQLException e) {
            e.printStackTrace();  // Log the exception
            return false;
        }
    }


    public boolean addPayments(Connection conn, List<SalesInvoicePayment> payments) {
        // SQL statement to insert or update the payment based on duplicate key violation
        String sql = "INSERT INTO sales_invoice_payments (invoice_id, order_id, coa_id, bank_id, reference_no, paid_amount, date_paid, date_encoded) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "order_id = VALUES(order_id), " +
                "coa_id = VALUES(coa_id), " +
                "bank_id = VALUES(bank_id), " +
                "reference_no = VALUES(reference_no), " +
                "paid_amount = VALUES(paid_amount), " +
                "date_paid = VALUES(date_paid)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Add each payment to the batch
            for (SalesInvoicePayment payment : payments) {
                pstmt.setInt(1, payment.getInvoice().getInvoiceId());
                pstmt.setString(2, payment.getOrderId());
                pstmt.setInt(3, payment.getChartOfAccount().getCoaId());
                pstmt.setObject(4, payment.getBank() != null ? payment.getBank().getId() : null);
                pstmt.setString(5, payment.getReferenceNo());
                pstmt.setDouble(6, payment.getPaidAmount());
                pstmt.setTimestamp(7, payment.getDatePaid());
                pstmt.setTimestamp(8, payment.getDateEncoded());

                pstmt.addBatch();  // Add to batch
            }

            int[] rowsAffected = pstmt.executeBatch();  // Execute the batch of inserts/updates

            // Check if any rows were affected, meaning the operation was successful
            return rowsAffected.length > 0;

        } catch (SQLException e) {
            try {
                conn.rollback();  // Rollback if there's an error
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();  // Handle rollback exception
            }
            e.printStackTrace();  // Log the original exception
            return false;
        }
    }


}
