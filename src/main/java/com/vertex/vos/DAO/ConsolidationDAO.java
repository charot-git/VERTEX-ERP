package com.vertex.vos.DAO;

import com.vertex.vos.Enums.ConsolidationStatus;
import com.vertex.vos.Enums.DispatchStatus;
import com.vertex.vos.Objects.Consolidation;
import com.vertex.vos.Objects.DispatchPlan;
import com.vertex.vos.Objects.StockTransfer;
import com.vertex.vos.Objects.User;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.vertex.vos.Utilities.EmployeeDAO;
import com.vertex.vos.Utilities.StockTransferDAO;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConsolidationDAO {
    HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public ObservableList<Consolidation> getAllConsolidations(int pageSize, int offset, String consolidationType, String consolidationNo, User selectedChecker, Timestamp dateFrom, Timestamp dateTo, ConsolidationStatus status) {
        ObservableList<Consolidation> consolidations = FXCollections.observableArrayList();

        StringBuilder query = new StringBuilder("SELECT * FROM consolidator WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        if (consolidationNo != null && !consolidationNo.isEmpty()) {
            query.append(" AND consolidator_no LIKE ?");
            parameters.add("%" + consolidationNo + "%");
        }

        if (consolidationType != null) {
            if (consolidationType.equals("DISPATCH")) {
                query.append(" AND consolidator_no LIKE 'COND%'");
            } else if (consolidationType.equals("STOCK TRANSFER")) {
                query.append(" AND consolidator_no NOT LIKE 'COND%'");
            }
        }

        if (selectedChecker != null) {
            query.append(" AND checked_by = ?");
            parameters.add(selectedChecker.getUser_id());
        }

        if (dateFrom != null) {
            query.append(" AND created_at >= ?");
            parameters.add(dateFrom);
        }

        if (dateTo != null) {
            query.append(" AND created_at <= ?");
            parameters.add(dateTo);
        }

        if (status != null) {
            query.append(" AND status = ?");
            parameters.add(status.name());
        }

        // Pagination
        query.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        parameters.add(pageSize);
        parameters.add(offset);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Consolidation consolidation = mapConsolidation(rs);
                consolidations.add(consolidation);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return consolidations;
    }


    EmployeeDAO employeeDAO = new EmployeeDAO();

    private Consolidation mapConsolidation(ResultSet rs) {
        Consolidation consolidation = new Consolidation();
        try {
            consolidation.setId(rs.getInt("id"));
            consolidation.setConsolidationNo(rs.getString("consolidator_no"));
            consolidation.setStatus(ConsolidationStatus.fromString(rs.getString("status")));
            consolidation.setCreatedBy(employeeDAO.getUserById(rs.getInt("created_by")));
            consolidation.setCheckedBy(employeeDAO.getUserById(rs.getInt("checked_by")));
            consolidation.setCreatedAt(rs.getTimestamp("created_at"));
            consolidation.setUpdatedAt(rs.getTimestamp("updated_at"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return consolidation;
    }

    public Consolidation getConsolidation(int id) {
        return null;
    }

    public Consolidation saveConsolidation(Consolidation consolidation) {
        String insertQuery = "INSERT INTO consolidator (consolidator_no, status, created_by, checked_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
        String consolidateStockTransferQuery = "INSERT INTO consolidator_stock_transfers (consolidator_id, stock_transfer_no, created_at) VALUES (?, ?, ?)";
        String consolidateDispatchQuery = "INSERT INTO consolidator_dispatches (consolidator_id, dispatch_no, created_at) VALUES (?, ?, ?)";

        // Queries to update Stock Transfer and Dispatch Plan statuses
        String updateStockTransferQuery = "UPDATE stock_transfer SET status = 'Picking' WHERE order_no = ?";
        String updateDispatchPlanQuery = "UPDATE dispatch_plan SET status = ? WHERE dispatch_no = ?";

        Connection conn = null;
        PreparedStatement insertStmt = null;
        PreparedStatement stockTransferStmt = null;
        PreparedStatement dispatchStmt = null;
        PreparedStatement updateStockStmt = null;
        PreparedStatement updateDispatchStmt = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Step 1: Insert into consolidator and get generated ID
            insertStmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
            insertStmt.setString(1, consolidation.getConsolidationNo());
            insertStmt.setString(2, consolidation.getStatus().toString());
            insertStmt.setInt(3, consolidation.getCreatedBy().getUser_id());
            insertStmt.setInt(4, consolidation.getCheckedBy().getUser_id());
            insertStmt.setTimestamp(5, consolidation.getCreatedAt());
            insertStmt.setTimestamp(6, consolidation.getUpdatedAt());

            int affectedRows = insertStmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating consolidator failed, no rows affected.");
            }

            // Retrieve the generated consolidator ID
            rs = insertStmt.getGeneratedKeys();
            int consolidatorId;
            if (rs.next()) {
                consolidatorId = rs.getInt(1);
            } else {
                throw new SQLException("Failed to retrieve consolidator ID.");
            }

            // Step 2: Insert into consolidator_stock_transfer and update stock transfer status
            stockTransferStmt = conn.prepareStatement(consolidateStockTransferQuery);
            updateStockStmt = conn.prepareStatement(updateStockTransferQuery);
            for (StockTransfer stockTransfer : consolidation.getStockTransfers()) {
                stockTransferStmt.setInt(1, consolidatorId);
                stockTransferStmt.setString(2, stockTransfer.getStockNo());
                stockTransferStmt.setTimestamp(3, consolidation.getCreatedAt());
                stockTransferStmt.addBatch();

                // Update stock transfer status
                updateStockStmt.setString(1, stockTransfer.getStockNo());
                updateStockStmt.addBatch();
            }
            stockTransferStmt.executeBatch();
            updateStockStmt.executeBatch();

            // Step 3: Insert into consolidator_dispatch and update dispatch plan status
            dispatchStmt = conn.prepareStatement(consolidateDispatchQuery);
            updateDispatchStmt = conn.prepareStatement(updateDispatchPlanQuery);
            for (DispatchPlan dispatch : consolidation.getDispatchPlans()) {
                dispatchStmt.setInt(1, consolidatorId);
                dispatchStmt.setString(2, dispatch.getDispatchNo());
                dispatchStmt.setTimestamp(3, consolidation.getCreatedAt());
                dispatchStmt.addBatch();

                // Update dispatch status (assuming DispatchPlanStatus is an enum)
                updateDispatchStmt.setString(1, DispatchStatus.PICKING.toString());
                updateDispatchStmt.setString(2, dispatch.getDispatchNo());
                updateDispatchStmt.addBatch();
            }
            dispatchStmt.executeBatch();
            updateDispatchStmt.executeBatch();

            conn.commit(); // Commit transaction

            consolidation.setId(consolidatorId);
            return consolidation;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback if there's an error
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
            return null;
        } finally {
            // Close resources
            closeQuietly(rs);
            closeQuietly(insertStmt);
            closeQuietly(stockTransferStmt);
            closeQuietly(dispatchStmt);
            closeQuietly(updateStockStmt);
            closeQuietly(updateDispatchStmt);
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Utility method to close resources safely
    private void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public Consolidation updateConsolidation(Consolidation consolidation) {
        String updateQuery = "UPDATE consolidator SET status = ?, checked_by = ?, updated_at = ? WHERE id = ?";

        // Revert stock transfer status before deletion
        String revertStockTransferStatusQuery = "UPDATE stock_transfer SET status = 'REQUESTED' WHERE order_no IN (SELECT stock_transfer_no FROM consolidator_stock_transfers WHERE consolidator_id = ?)";
        String deleteStockTransfersQuery = "DELETE FROM consolidator_stock_transfers WHERE consolidator_id = ?";

        // Revert dispatch plan status before deletion
        String revertDispatchPlanStatusQuery = "UPDATE dispatch_plan SET status = 'Pending' WHERE dispatch_no IN (SELECT dispatch_no FROM consolidator_dispatches WHERE consolidator_id = ?)";
        String deleteDispatchQuery = "DELETE FROM consolidator_dispatches WHERE consolidator_id = ?";

        String insertStockTransfersQuery = "INSERT INTO consolidator_stock_transfers (consolidator_id, stock_transfer_no, created_at) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE created_at = VALUES(created_at)";
        String insertDispatchQuery = "INSERT INTO consolidator_dispatches (consolidator_id, dispatch_no, created_at) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE created_at = VALUES(created_at)";
        Connection conn = null;
        PreparedStatement updateStmt = null;
        PreparedStatement revertStockTransferStmt = null;
        PreparedStatement deleteStockTransfersStmt = null;
        PreparedStatement insertStockTransfersStmt = null;
        PreparedStatement revertDispatchStmt = null;
        PreparedStatement deleteDispatchStmt = null;
        PreparedStatement insertDispatchStmt = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            // Step 1: Update Consolidator table
            updateStmt = conn.prepareStatement(updateQuery);
            updateStmt.setString(1, consolidation.getStatus().toString());
            updateStmt.setInt(2, consolidation.getCheckedBy().getUser_id());
            updateStmt.setTimestamp(3, consolidation.getUpdatedAt());
            updateStmt.setInt(4, consolidation.getId());

            int affectedRows = updateStmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating consolidator failed, no rows affected.");
            }

            // Step 2: Revert Stock Transfer Status Before Deleting
            revertStockTransferStmt = conn.prepareStatement(revertStockTransferStatusQuery);
            revertStockTransferStmt.setInt(1, consolidation.getId());
            revertStockTransferStmt.executeUpdate();

            // Step 3: Delete old Stock Transfers and insert new ones
            deleteStockTransfersStmt = conn.prepareStatement(deleteStockTransfersQuery);
            deleteStockTransfersStmt.setInt(1, consolidation.getId());
            deleteStockTransfersStmt.executeUpdate();

            insertStockTransfersStmt = conn.prepareStatement(insertStockTransfersQuery);
            for (StockTransfer stockTransfer : consolidation.getStockTransfers()) {
                insertStockTransfersStmt.setInt(1, consolidation.getId());
                insertStockTransfersStmt.setString(2, stockTransfer.getStockNo());
                insertStockTransfersStmt.setTimestamp(3, consolidation.getUpdatedAt());
                insertStockTransfersStmt.addBatch();
            }
            insertStockTransfersStmt.executeBatch();

            // Step 4: Revert Dispatch Plan Status Before Deleting
            revertDispatchStmt = conn.prepareStatement(revertDispatchPlanStatusQuery);
            revertDispatchStmt.setInt(1, consolidation.getId());
            revertDispatchStmt.executeUpdate();

            // Step 5: Delete old Dispatch Plans and insert new ones
            deleteDispatchStmt = conn.prepareStatement(deleteDispatchQuery);
            deleteDispatchStmt.setInt(1, consolidation.getId());
            deleteDispatchStmt.executeUpdate();

            insertDispatchStmt = conn.prepareStatement(insertDispatchQuery);
            for (DispatchPlan dispatch : consolidation.getDispatchPlans()) {
                insertDispatchStmt.setInt(1, consolidation.getId());
                insertDispatchStmt.setString(2, dispatch.getDispatchNo());
                insertDispatchStmt.setTimestamp(3, consolidation.getUpdatedAt());
                insertDispatchStmt.addBatch();
            }
            insertDispatchStmt.executeBatch();

            conn.commit();
            return consolidation;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (updateStmt != null) updateStmt.close();
                if (revertStockTransferStmt != null) revertStockTransferStmt.close();
                if (deleteStockTransfersStmt != null) deleteStockTransfersStmt.close();
                if (insertStockTransfersStmt != null) insertStockTransfersStmt.close();
                if (revertDispatchStmt != null) revertDispatchStmt.close();
                if (deleteDispatchStmt != null) deleteDispatchStmt.close();
                if (insertDispatchStmt != null) insertDispatchStmt.close();
                if (conn != null) conn.setAutoCommit(true);
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public Consolidation deleteConsolidation(Consolidation consolidation) {
        return null;
    }

    public String generateConsolidationNoForDispatch() {
        String selectQuery = "SELECT consolidation_no_dispatch FROM pick_list_no FOR UPDATE";
        String updateQuery = "UPDATE pick_list_no SET consolidation_no_dispatch = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
             PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {

            connection.setAutoCommit(false);

            try (ResultSet resultSet = selectStmt.executeQuery()) {
                if (resultSet.next()) {
                    int no = resultSet.getInt("consolidation_no_dispatch");
                    int nextNo = no + 1;

                    updateStmt.setInt(1, nextNo);
                    updateStmt.executeUpdate();
                    connection.commit();

                    return String.format("COND-%05d", nextNo);
                }
            }
            connection.rollback();
        } catch (SQLException e) {
            if ("40001".equals(e.getSQLState())) { // Deadlock detected, retry
                return generateConsolidationNoForDispatch();
            }
            throw new RuntimeException("Failed to generate new dispatch number", e);
        }
        return null;
    }

    public String generateConsolidationNoForStockTransfer() {
        String selectQuery = "SELECT consolidation_no_stock_transfer FROM pick_list_no FOR UPDATE";
        String updateQuery = "UPDATE pick_list_no SET consolidation_no_stock_transfer = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
             PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {

            connection.setAutoCommit(false);

            try (ResultSet resultSet = selectStmt.executeQuery()) {
                if (resultSet.next()) {
                    int no = resultSet.getInt("consolidation_no_stock_transfer");
                    int nextNo = no + 1;

                    updateStmt.setInt(1, nextNo);
                    updateStmt.executeUpdate();
                    connection.commit();

                    return String.format("CONS-%05d", nextNo);
                }
            }
            connection.rollback();
        } catch (SQLException e) {
            if ("40001".equals(e.getSQLState())) { // Deadlock detected, retry
                return generateConsolidationNoForDispatch();
            }
            throw new RuntimeException("Failed to generate new dispatch number", e);
        }
        return null;
    }

    StockTransferDAO stockTransferDAO = new StockTransferDAO();

    public List<StockTransfer> getStockTransfersForConsolidation(Consolidation selectedConsolidation) {
        String query = "SELECT stock_transfer_no FROM consolidator_stock_transfers WHERE consolidator_id = ?";
        List<StockTransfer> stockTransfers = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, selectedConsolidation.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                StockTransfer stockTransfer = stockTransferDAO.getStockTransferDetails(rs.getString("stock_transfer_no"));
                if (stockTransfer != null) {
                    stockTransfers.add(stockTransfer);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stockTransfers; // Return the full list of stock transfers
    }


    DispatchPlanDAO dispatchPlanDAO = new DispatchPlanDAO();

    public List<DispatchPlan> getDispatchPlansForConsolidation(Consolidation selectedConsolidation) {
        String query = "SELECT dispatch_no FROM consolidator_dispatches WHERE consolidator_id = ?";
        List<DispatchPlan> dispatchPlans = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, selectedConsolidation.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                DispatchPlan dispatchPlan = dispatchPlanDAO.getDispatchPlanDetails(rs.getString("dispatch_no"));
                if (dispatchPlan != null) {
                    dispatchPlans.add(dispatchPlan);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dispatchPlans; // Return the full list of dispatch plans
    }

}
