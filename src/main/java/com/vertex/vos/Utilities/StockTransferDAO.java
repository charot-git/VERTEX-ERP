package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.Inventory;
import com.vertex.vos.Objects.Product;
import com.vertex.vos.Objects.ProductsInTransact;
import com.vertex.vos.Objects.StockTransfer;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StockTransferDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    InventoryDAO inventoryDAO = new InventoryDAO();

    public boolean insertStockTransfers(List<StockTransfer> stockTransfers) {
        String sql = "INSERT INTO stock_transfer (order_no, source_branch, target_branch, product_id, " +
                "ordered_quantity, amount, date_requested, lead_date, status, date_received, encoder_id, receiver_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "ordered_quantity = VALUES(ordered_quantity), " +
                "amount = VALUES(amount), " +
                "date_requested = VALUES(date_requested), " +
                "lead_date = VALUES(lead_date), " +
                "status = VALUES(status), " +
                "date_received = VALUES(date_received), " +
                "encoder_id = VALUES(encoder_id), " +
                "receiver_id = VALUES(receiver_id);";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            connection.setAutoCommit(false); // Start transaction

            List<StockTransfer> receivedTransfers = new ArrayList<>();

            for (StockTransfer stockTransfer : stockTransfers) {
                statement.setString(1, stockTransfer.getOrderNo());
                statement.setInt(2, stockTransfer.getSourceBranch());
                statement.setInt(3, stockTransfer.getTargetBranch());
                statement.setInt(4, stockTransfer.getProductId());
                statement.setInt(5, stockTransfer.getOrderedQuantity());
                statement.setDouble(6, stockTransfer.getAmount());
                statement.setDate(7, stockTransfer.getDateRequested());
                statement.setDate(8, stockTransfer.getLeadDate());
                statement.setString(9, stockTransfer.getStatus());
                statement.setTimestamp(10, stockTransfer.getDateReceived());
                statement.setInt(11, stockTransfer.getEncoderId());
                statement.setInt(12, stockTransfer.getReceiverId());

                if ("RECEIVED".equals(stockTransfer.getStatus())) {
                    receivedTransfers.add(stockTransfer); // Collect for inventory update
                }

                statement.addBatch();
            }

            int[] results = statement.executeBatch(); // Execute batch
            boolean stockUpdateSuccess = true;

            // Update inventory only for received transfers
            if (!receivedTransfers.isEmpty()) {
                stockUpdateSuccess = updateInventory(receivedTransfers, connection);
            }

            if (Arrays.stream(results).sum() > 0 && stockUpdateSuccess) {
                connection.commit(); // Commit transaction if everything is successful
                return true;
            } else {
                connection.rollback(); // Rollback on failure
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean updateInventory(List<StockTransfer> stockTransfers, Connection connection) {
        List<Inventory> inventoryUpdates = new ArrayList<>();

        for (StockTransfer transfer : stockTransfers) {
            Inventory inventoryAdd = new Inventory();
            inventoryAdd.setProductId(transfer.getProductId());
            inventoryAdd.setBranchId(transfer.getTargetBranch()); // Add to Target Branch
            inventoryAdd.setQuantity(transfer.getOrderedQuantity());

            Inventory inventorySubtract = new Inventory();
            inventorySubtract.setProductId(transfer.getProductId());
            inventorySubtract.setBranchId(transfer.getSourceBranch()); // Subtract from Source Branch
            inventorySubtract.setQuantity(-transfer.getOrderedQuantity()); // Negative to subtract

            inventoryUpdates.add(inventoryAdd);
            inventoryUpdates.add(inventorySubtract);
        }

        if (inventoryUpdates.isEmpty()) {
            return false; // Nothing to update
        }

        try {
            return inventoryDAO.updateInventoryBulk(inventoryUpdates, connection);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Method to retrieve all stock transfer records from the database
    public List<StockTransfer> getAllStockTransfers() throws SQLException {
        List<StockTransfer> stockTransfers = new ArrayList<>();
        String sql = "SELECT * FROM stock_transfer";

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                StockTransfer stockTransfer = new StockTransfer();
                stockTransfer.setOrderNo(resultSet.getString("order_no"));
                stockTransfer.setSourceBranch(resultSet.getInt("source_branch"));
                stockTransfer.setTargetBranch(resultSet.getInt("target_branch"));
                stockTransfer.setProductId(resultSet.getInt("product_id"));
                stockTransfer.setOrderedQuantity(resultSet.getInt("ordered_quantity"));
                stockTransfer.setAmount(resultSet.getDouble("amount"));
                stockTransfer.setDateRequested(resultSet.getDate("date_requested"));
                stockTransfer.setLeadDate(resultSet.getDate("lead_date"));
                stockTransfer.setStatus(resultSet.getString("status"));
                stockTransfer.setDateReceived(resultSet.getTimestamp("date_received"));
                stockTransfer.setReceiverId(resultSet.getInt("receiver_id"));
                stockTransfer.setEncoderId(resultSet.getInt("encoder_id"));

                stockTransfers.add(stockTransfer);
            }
        }
        return stockTransfers;
    }

    // Method to generate stock transfer number
    public int generateStockTransferNumber() throws SQLException {
        String sqlSelect = "SELECT no FROM stock_transfer_no LIMIT 1 FOR UPDATE";
        String sqlUpdate = "UPDATE stock_transfer_no SET no = no + 1 LIMIT 1";
        int stockTransferNumber = 0;

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            connection.setAutoCommit(false);
            ResultSet resultSet = statement.executeQuery(sqlSelect);
            if (resultSet.next()) {
                int currentNumber = resultSet.getInt("no");
                stockTransferNumber = currentNumber;
                statement.executeUpdate(sqlUpdate);
                connection.commit();
            }
        } catch (SQLException e) {
            if (e.getSQLState().equals("40001")) { // Deadlock retry
                return generateStockTransferNumber();
            }
            throw e;
        }
        return stockTransferNumber;
    }


    public boolean deleteStockTransfers(List<ProductsInTransact> removedProducts, String orderNo, int branchId) {
        String sql = "DELETE FROM stock_transfer WHERE product_id = ? AND order_no = ? AND target_branch = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            connection.setAutoCommit(false); // Start transaction

            for (ProductsInTransact product : removedProducts) {
                pstmt.setInt(1, product.getProductId()); // Extract product ID
                pstmt.setString(2, orderNo);
                pstmt.setInt(3, branchId);
                pstmt.addBatch(); // Add to batch
            }

            int[] results = pstmt.executeBatch(); // Execute batch delete
            connection.commit(); // Commit transaction

            return Arrays.stream(results).sum() > 0; // Return true if any row was deleted

        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Return false if an error occurs
        }
    }


    ProductDAO productDAO = new ProductDAO();

    public List<ProductsInTransact> getProductsAndQuantityByOrderNo(String orderNo) throws SQLException {
        String sql = "SELECT product_id, ordered_quantity, amount FROM stock_transfer WHERE order_no = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, orderNo);

            try (ResultSet rs = pstmt.executeQuery()) {
                List<ProductsInTransact> productsList = new ArrayList<>();

                while (rs.next()) {
                    int productId = rs.getInt("product_id");
                    int orderedQuantity = rs.getInt("ordered_quantity");

                    ProductsInTransact product = new ProductsInTransact();
                    product.setProductId(productId);
                    product.setOrderedQuantity(orderedQuantity);
                    Product productDetails = productDAO.getProductDetails(productId);
                    product.setDescription(productDetails.getDescription());
                    product.setUnit(productDetails.getUnitOfMeasurementString());
                    product.setPaymentAmount(rs.getDouble("amount"));

                    productsList.add(product);
                }

                return productsList;
            }
        }
    }

    public List<StockTransfer> getAllDistinctStockTransfersAndSetToTable() throws SQLException {
        List<StockTransfer> stockTransfers = new ArrayList<>();
        String sql = "SELECT DISTINCT order_no, source_branch, target_branch, lead_date, date_requested, date_received ,status FROM stock_transfer";

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                StockTransfer stockTransfer = new StockTransfer();
                stockTransfer.setOrderNo(resultSet.getString("order_no"));
                stockTransfer.setSourceBranch(resultSet.getInt("source_branch"));
                stockTransfer.setTargetBranch(resultSet.getInt("target_branch"));
                stockTransfer.setLeadDate(resultSet.getDate("lead_date"));
                stockTransfer.setStatus(resultSet.getString("status"));
                stockTransfer.setDateRequested(resultSet.getDate("date_requested"));
                stockTransfer.setDateReceived(resultSet.getTimestamp("date_received"));

                stockTransfers.add(stockTransfer);
            }
        }
        return stockTransfers;
    }

    public StockTransfer getStockTransferDetails(String orderNo) throws SQLException {
        StockTransfer stockTransfer = null;
        String sql = "SELECT DISTINCT order_no, source_branch, target_branch, lead_date, date_requested , date_received,status FROM stock_transfer WHERE order_no = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, orderNo);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    stockTransfer = new StockTransfer();
                    stockTransfer.setOrderNo(resultSet.getString("order_no"));
                    stockTransfer.setSourceBranch(resultSet.getInt("source_branch"));
                    stockTransfer.setTargetBranch(resultSet.getInt("target_branch"));
                    stockTransfer.setLeadDate(resultSet.getDate("lead_date"));
                    stockTransfer.setStatus(resultSet.getString("status"));
                    stockTransfer.setDateRequested(resultSet.getDate("date_requested"));
                    stockTransfer.setDateReceived(resultSet.getTimestamp("date_received"));
                }
            }
        }
        return stockTransfer;
    }

    public int getAvailableQuantityForProduct(int productId, int branchId) throws SQLException {
        int availableQuantity = 0;
        String sql = "SELECT quantity FROM inventory WHERE product_id = ? AND branch_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, productId);
            statement.setInt(2, branchId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    availableQuantity = resultSet.getInt("quantity");
                }
            }
        }
        return availableQuantity;
    }



}
