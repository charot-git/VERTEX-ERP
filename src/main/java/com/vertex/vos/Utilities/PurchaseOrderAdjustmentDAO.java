package com.vertex.vos.Utilities;

import com.zaxxer.hikari.HikariDataSource;
import com.vertex.vos.Objects.SupplierCreditDebitMemo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderAdjustmentDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    // Create
    public boolean insertAdjustment(int purchaseOrderId, SupplierCreditDebitMemo memo) {
        String sql = "INSERT INTO purchase_order_adjustment (purchase_order_id, memo_id, memo_type, amount) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, purchaseOrderId);
            pstmt.setInt(2, memo.getId());
            pstmt.setInt(3, memo.getType());
            pstmt.setDouble(4, memo.getAmount());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    SupplierMemoDAO supplierMemoDAO = new SupplierMemoDAO();

    // Read
    public List<SupplierCreditDebitMemo> getAdjustmentsByPurchaseOrderId(int purchaseOrderId) {
        List<SupplierCreditDebitMemo> adjustments = new ArrayList<>();
        String sql = "SELECT memo_id FROM purchase_order_adjustment WHERE purchase_order_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, purchaseOrderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    SupplierCreditDebitMemo memo = new SupplierCreditDebitMemo();
                    memo.setMemoNumber(rs.getString("memo_id"));
                    SupplierCreditDebitMemo memoFromTable = supplierMemoDAO.getSupplierMemoById(Integer.parseInt(memo.getMemoNumber()));
                    memo.setType(memoFromTable.getType());
                    memo.setTypeName(memoFromTable.getTypeName());
                    memo.setReason(memoFromTable.getReason());
                    memo.setAmount(memoFromTable.getAmount());
                    memo.setStatus(memoFromTable.getStatus());
                    memo.setChartOfAccount(memoFromTable.getChartOfAccount());
                    memo.setChartOfAccountName(memoFromTable.getChartOfAccountName());

                    adjustments.add(memo);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return adjustments;
    }

    // Update
    public boolean updateAdjustment(int purchaseOrderId, int memoId, int memoType) {
        String sql = "UPDATE purchase_order_adjustment SET memo_type = ? WHERE purchase_order_id = ? AND memo_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, memoType);
            pstmt.setInt(2, purchaseOrderId);
            pstmt.setInt(3, memoId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete
    public boolean deleteAdjustment(int purchaseOrderId, int memoId) {
        String sql = "DELETE FROM purchase_order_adjustment WHERE purchase_order_id = ? AND memo_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, purchaseOrderId);
            pstmt.setInt(2, memoId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
