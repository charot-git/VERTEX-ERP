package com.vertex.vos.DAO;

import com.vertex.vos.Objects.PurchaseOrderVoucher;
import com.vertex.vos.Objects.PurchaseOrder;
import com.vertex.vos.Objects.Supplier;
import com.vertex.vos.Objects.BankAccount;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderVoucherDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    private final PurchaseOrderDAO purchaseOrderDAO = new PurchaseOrderDAO();
    private final ChartOfAccountsDAO chartOfAccountsDAO = new ChartOfAccountsDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final BankAccountDAO bankAccountDAO = new BankAccountDAO();

    // Method to create a new PurchaseOrderVoucher
    public boolean create(PurchaseOrderVoucher voucher) {
        String query = "INSERT INTO purchase_order_voucher (purchase_order_id, supplier_id, coa_id, amount, created_at, updated_at, ref_no, bank_id, created_by, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, voucher.getPurchaseOrder().getPurchaseOrderId());
            pstmt.setInt(2, voucher.getSupplierId());
            pstmt.setInt(3, voucher.getCoaId());
            pstmt.setBigDecimal(4, voucher.getAmount());
            pstmt.setTimestamp(5, voucher.getCreatedAt());
            pstmt.setTimestamp(6, voucher.getUpdatedAt());
            pstmt.setString(7, voucher.getRefNo());
            pstmt.setInt(8, voucher.getBankAccount().getBankId());
            pstmt.setInt(9, voucher.getCreatedBy());
            pstmt.setString(10, voucher.getStatus());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method to get a PurchaseOrderVoucher by ID
    public PurchaseOrderVoucher getById(int voucherId) {
        String query = "SELECT * FROM purchase_order_voucher WHERE voucher_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, voucherId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToPurchaseOrderVoucher(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Method to update an existing PurchaseOrderVoucher
    public boolean update(PurchaseOrderVoucher voucher) {
        String query = "UPDATE purchase_order_voucher SET purchase_order_id = ?, supplier_id = ?, coa_id = ?, amount = ?, updated_at = ?, ref_no = ?, bank_id = ? WHERE voucher_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, voucher.getPurchaseOrder().getPurchaseOrderId());
            pstmt.setInt(2, voucher.getSupplierId());
            pstmt.setInt(3, voucher.getCoaId());
            pstmt.setBigDecimal(4, voucher.getAmount());
            pstmt.setTimestamp(5, voucher.getUpdatedAt());
            pstmt.setString(6, voucher.getRefNo());
            pstmt.setInt(7, voucher.getBankAccount().getBankId());
            pstmt.setInt(8, voucher.getVoucherId());
            pstmt.setInt(9, voucher.getCreatedBy());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method to delete a PurchaseOrderVoucher by ID
    public boolean delete(int voucherId) {
        String query = "DELETE FROM purchase_order_voucher WHERE voucher_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, voucherId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method to get all PurchaseOrderVouchers
    public List<PurchaseOrderVoucher> getAll() {
        List<PurchaseOrderVoucher> vouchers = new ArrayList<>();
        String query = "SELECT * FROM purchase_order_voucher";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                vouchers.add(mapResultSetToPurchaseOrderVoucher(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vouchers;
    }

    // Method to map ResultSet to PurchaseOrderVoucher
    private PurchaseOrderVoucher mapResultSetToPurchaseOrderVoucher(ResultSet rs) throws SQLException {
        PurchaseOrderVoucher voucher = new PurchaseOrderVoucher();
        voucher.setVoucherId(rs.getInt("voucher_id"));
        voucher.setPurchaseOrder(purchaseOrderDAO.getPurchaseOrderByOrderId(rs.getInt("purchase_order_id"))); // Fetch purchase order
        voucher.setSupplierId(rs.getInt("supplier_id")); // Fetch supplier
        voucher.setCoaId(rs.getInt("coa_id"));
        voucher.setAmount(rs.getBigDecimal("amount"));
        voucher.setCreatedAt(rs.getTimestamp("created_at"));
        voucher.setUpdatedAt(rs.getTimestamp("updated_at"));
        voucher.setRefNo(rs.getString("ref_no"));
        voucher.setBankAccount(bankAccountDAO.getBankAccountById(rs.getInt("bank_id"))); // Fetch bank account
        voucher.setCreatedBy(rs.getInt("created_by"));
        voucher.setStatus(rs.getString("status"));
        voucher.setCoa(chartOfAccountsDAO.getChartOfAccountById(voucher.getCoaId()));
        // Set createdBy if needed
        return voucher;
    }

    public ObservableList<PurchaseOrderVoucher> getVouchersByOrderId(int orderId) {
        String query = "SELECT * FROM purchase_order_voucher WHERE purchase_order_id = ?";
        ObservableList<PurchaseOrderVoucher> vouchers = FXCollections.observableArrayList();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    vouchers.add(mapResultSetToPurchaseOrderVoucher(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vouchers;
    }
}
