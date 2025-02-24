package com.vertex.vos.DAO;

import com.vertex.vos.Objects.StockAdjustment;
import com.vertex.vos.Objects.Product;
import com.vertex.vos.Objects.Branch;
import com.vertex.vos.Objects.StockAdjustment.AdjustmentType;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockAdjustmentDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    // Insert a stock adjustment
    public boolean insertStockAdjustment(StockAdjustment adjustment) {
        String sql = "INSERT INTO stock_adjustment (doc_no, product_id, branch_id, type, quantity, created_at, created_by) VALUES (?, ?, ?, ?, ?, NOW(), ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, adjustment.getDocNo());
            stmt.setInt(2, adjustment.getProduct().getProductId());
            stmt.setInt(3, adjustment.getBranch().getId());
            stmt.setString(4, adjustment.getAdjustmentType().name());
            stmt.setInt(5, adjustment.getQuantity());
            stmt.setInt(6, adjustment.getCreatedBy()); // New field

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        adjustment.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    // Retrieve a stock adjustment by ID
    public StockAdjustment getStockAdjustmentById(int id) {
        String sql = "SELECT id, doc_no, product_id, branch_id, type, quantity, created_at, created_by FROM stock_adjustment WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    StockAdjustment adjustment = new StockAdjustment();
                    adjustment.setId(rs.getInt("id"));
                    adjustment.setDocNo(rs.getString("doc_no"));

                    Product product = new Product();
                    product.setProductId(rs.getInt("product_id"));

                    Branch branch = new Branch();
                    branch.setId(rs.getInt("branch_id"));

                    adjustment.setProduct(product);
                    adjustment.setBranch(branch);
                    adjustment.setQuantity(rs.getInt("quantity"));
                    adjustment.setAdjustmentType(AdjustmentType.valueOf(rs.getString("type")));
                    adjustment.setCreatedBy(rs.getInt("created_by"));

                    return adjustment;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Get all stock adjustments
    public List<StockAdjustment> getAllStockAdjustments() {
        List<StockAdjustment> adjustments = new ArrayList<>();
        String sql = "SELECT id, doc_no, product_id, branch_id, type, quantity, created_at, created_by FROM stock_adjustment";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                StockAdjustment adjustment = new StockAdjustment();
                adjustment.setId(rs.getInt("id"));
                adjustment.setDocNo(rs.getString("doc_no"));

                Product product = new Product();
                product.setProductId(rs.getInt("product_id"));

                Branch branch = new Branch();
                branch.setId(rs.getInt("branch_id"));

                adjustment.setProduct(product);
                adjustment.setBranch(branch);
                adjustment.setQuantity(rs.getInt("quantity"));
                adjustment.setAdjustmentType(AdjustmentType.valueOf(rs.getString("type")));
                adjustment.setCreatedBy(rs.getInt("created_by"));

                adjustments.add(adjustment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return adjustments;
    }
    public synchronized int getNextStockAdjustmentNo() {
        String selectSQL = "SELECT no FROM stock_adjustment_no FOR UPDATE";
        String updateSQL = "UPDATE stock_adjustment_no SET no = no + 1";
        String insertSQL = "INSERT INTO stock_adjustment_no (no) VALUES (1)";

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement selectStmt = conn.prepareStatement(selectSQL);
                 ResultSet rs = selectStmt.executeQuery()) {

                if (rs.next()) {
                    int nextNo = rs.getInt("no");

                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {
                        updateStmt.executeUpdate();
                    }

                    conn.commit();
                    return nextNo;
                } else {
                    // If no entry exists, initialize with 1
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                        insertStmt.executeUpdate();
                    }

                    conn.commit();
                    return 1;
                }
            } catch (SQLException e) {
                conn.rollback(); // Rollback in case of error
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true); // Reset auto-commit
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Indicate failure
    }
}
