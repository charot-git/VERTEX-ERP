package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.ProductsInTransact;
import com.vertex.vos.Constructors.StockTransfer;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockTransferDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public boolean insertStockTransfer(StockTransfer stockTransfer) {
        String sql = "INSERT INTO stock_transfer (order_no, source_branch, target_branch, product_id, " +
                "ordered_quantity, amount, date_requested, lead_date, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, stockTransfer.getOrderNo());
            statement.setInt(2, stockTransfer.getSourceBranch());
            statement.setInt(3, stockTransfer.getTargetBranch());
            statement.setInt(4, stockTransfer.getProductId());
            statement.setInt(5, stockTransfer.getOrderedQuantity());
            statement.setDouble(6, stockTransfer.getAmount());
            statement.setDate(7, stockTransfer.getDateRequested());
            statement.setDate(8, stockTransfer.getLeadDate());
            statement.setString(9, stockTransfer.getStatus());

            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0; // Return true if rows were inserted
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Return false if an exception occurs
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

    // Method to retrieve distinct stock transfer records by order_no from the database
    public List<StockTransfer> getAllDistinctStockTransfersAndSetToTable() throws SQLException {
        List<StockTransfer> stockTransfers = new ArrayList<>();
        String sql = "SELECT DISTINCT order_no, source_branch, target_branch, lead_date, status FROM stock_transfer";

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

                stockTransfers.add(stockTransfer);
            }
        }
        return stockTransfers;
    }

    public StockTransfer getStockTransferDetails(String orderNo) throws SQLException {
        StockTransfer stockTransfer = null;
        String sql = "SELECT DISTINCT order_no, source_branch, target_branch, lead_date, status FROM stock_transfer WHERE order_no = ?";

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
                }
            }
        }
        return stockTransfer;
    }

}
