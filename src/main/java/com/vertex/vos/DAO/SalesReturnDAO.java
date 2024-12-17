package com.vertex.vos.DAO;

import com.vertex.vos.Objects.SalesReturn;
import com.vertex.vos.Objects.SalesReturnDetail;
import com.vertex.vos.Utilities.CustomerDAO;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SalesReturnDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    // Static maps for return type mappings
    private static Map<Integer, String> typeIdToNameMap = new HashMap<>();
    private static Map<String, Integer> typeNameToIdMap = new HashMap<>();

    static {
        initializeMappings();
    }

    private static void initializeMappings() {
        String query = "SELECT type_id, type_name FROM sales_return_type";
        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int typeId = rs.getInt("type_id");
                String typeName = rs.getString("type_name");
                typeIdToNameMap.put(typeId, typeName);
                typeNameToIdMap.put(typeName, typeId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int generateSalesReturnNo() {
        String sqlSelect = "SELECT no FROM sales_return_no LIMIT 1 FOR UPDATE";
        String sqlUpdate = "UPDATE sales_return_no SET no = no + 1 LIMIT 1";
        int salesReturnNumber = 0;

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            connection.setAutoCommit(false);
            try {
                ResultSet resultSet = statement.executeQuery(sqlSelect);
                if (resultSet.next()) {
                    salesReturnNumber = resultSet.getInt("no");
                    statement.executeUpdate(sqlUpdate);
                } else {
                    throw new SQLException("No sales return number found");
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            if (e.getSQLState().equals("40001")) { // Deadlock retry
                return generateSalesReturnNo();
            }
            throw new RuntimeException("Failed to generate sales return number", e);
        }
        return salesReturnNumber;
    }

    public boolean createSalesReturn(SalesReturn salesReturn, ObservableList<SalesReturnDetail> productsForSalesReturn) throws SQLException {
        String salesReturnSql = "INSERT INTO sales_return (return_number, customer_code, return_date, total_amount, remarks, created_by, status, isThirdParty, price_type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String salesReturnDetailSql = "INSERT INTO sales_return_details (return_no, product_id, quantity, unit_price, total_price, reason, sales_return_type_id, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection()) {
            // Begin transaction
            connection.setAutoCommit(false);

            try (PreparedStatement salesReturnStatement = connection.prepareStatement(salesReturnSql, Statement.RETURN_GENERATED_KEYS)) {
                salesReturnStatement.setString(1, salesReturn.getReturnNumber());
                salesReturnStatement.setString(2, salesReturn.getCustomerCode());
                salesReturnStatement.setTimestamp(3, salesReturn.getReturnDate());
                salesReturn.setTotalAmount(productsForSalesReturn.stream().mapToDouble(SalesReturnDetail::getTotalAmount).sum());
                salesReturnStatement.setDouble(4, salesReturn.getTotalAmount());
                salesReturnStatement.setString(5, salesReturn.getRemarks());
                salesReturnStatement.setInt(6, salesReturn.getCreatedBy());
                salesReturnStatement.setString(7, salesReturn.getStatus());
                salesReturnStatement.setBoolean(8, salesReturn.isThirdParty());
                salesReturnStatement.setString(9, salesReturn.getPriceType());

                // Execute the sales return header insertion
                int affectedRows = salesReturnStatement.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("Failed to insert sales return, no rows affected.");
                }

                // Get the generated sales return ID
                try (ResultSet generatedKeys = salesReturnStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int salesReturnId = generatedKeys.getInt(1);

                        // Insert the details
                        try (PreparedStatement salesReturnDetailStatement = connection.prepareStatement(salesReturnDetailSql)) {
                            for (SalesReturnDetail detail : productsForSalesReturn) {
                                salesReturnDetailStatement.setString(1, salesReturn.getReturnNumber()); // Set return_id from SalesReturn
                                salesReturnDetailStatement.setInt(2, detail.getProductId());
                                salesReturnDetailStatement.setInt(3, detail.getQuantity());
                                salesReturnDetailStatement.setDouble(4, detail.getUnitPrice());
                                salesReturnDetailStatement.setDouble(5, detail.getTotalAmount());
                                salesReturnDetailStatement.setString(6, detail.getReason());
                                salesReturnDetailStatement.setInt(7, detail.getSalesReturnTypeId());
                                salesReturnDetailStatement.setString(8, detail.getStatus());
                                salesReturnDetailStatement.addBatch(); // Add to batch
                            }

                            // Execute batch insert for details
                            int[] detailRows = salesReturnDetailStatement.executeBatch();

                            // Check if any detail insertion failed
                            if (detailRows.length != productsForSalesReturn.size()) {
                                throw new SQLException("Failed to insert all sales return details.");
                            }
                        }

                        // Commit the transaction if everything is successful
                        connection.commit();
                        return true;

                    } else {
                        throw new SQLException("Failed to retrieve sales return ID.");
                    }
                }

            } catch (SQLException ex) {
                // Rollback transaction on failure
                connection.rollback();
                throw ex;
            } finally {
                // Restore auto-commit mode
                connection.setAutoCommit(true);
            }
        }
    }


    public SalesReturn getSalesReturnById(int returnId) throws SQLException {
        String sql = "SELECT * FROM sales_return WHERE return_id = ?";
        SalesReturn salesReturn = null;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, returnId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                salesReturn = new SalesReturn(
                        resultSet.getInt("return_id"),
                        resultSet.getString("return_number"),
                        resultSet.getString("customer_code"),
                        resultSet.getTimestamp("return_date"),
                        resultSet.getDouble("total_amount"),
                        resultSet.getString("remarks"),
                        resultSet.getInt("created_by"),
                        resultSet.getTimestamp("created_at"),
                        resultSet.getTimestamp("updated_at"),
                        resultSet.getString("status"),
                        resultSet.getBoolean("isThirdParty")
                );
            }
        }

        return salesReturn;
    }

    public List<SalesReturn> getAllSalesReturns() {
        String sql = "SELECT * FROM sales_return";
        List<SalesReturn> salesReturns = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                salesReturns.add(extractSalesReturnFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving sales returns: " + e.getMessage());
            return new ArrayList<>();
        }

        return salesReturns;
    }

    CustomerDAO customerDAO = new CustomerDAO();

    private SalesReturn extractSalesReturnFromResultSet(ResultSet resultSet) throws SQLException {
        SalesReturn salesReturn = new SalesReturn();
        salesReturn.setReturnId(resultSet.getInt("return_id"));
        salesReturn.setReturnNumber(resultSet.getString("return_number"));
        salesReturn.setCustomerCode(resultSet.getString("customer_code"));
        salesReturn.setCustomer(customerDAO.getCustomerByCode(salesReturn.getCustomerCode()));
        salesReturn.setReturnDate(resultSet.getTimestamp("return_date"));
        salesReturn.setTotalAmount(resultSet.getDouble("total_amount"));
        salesReturn.setRemarks(resultSet.getString("remarks"));
        salesReturn.setCreatedBy(resultSet.getInt("created_by"));
        salesReturn.setCreatedAt(resultSet.getTimestamp("created_at"));
        salesReturn.setUpdatedAt(resultSet.getTimestamp("updated_at"));
        salesReturn.setStatus(resultSet.getString("status"));
        salesReturn.setThirdParty(resultSet.getBoolean("isThirdParty"));
        salesReturn.setPriceType(resultSet.getString("price_type"));
        return salesReturn;
    }

    public void updateSalesReturn(SalesReturn salesReturn) throws SQLException {
        String sql = "UPDATE sales_return SET return_number = ?, customer_id = ?, return_date = ?, " +
                "total_amount = ?, remarks = ?, status = ? WHERE return_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, salesReturn.getReturnNumber());
            statement.setString(2, salesReturn.getCustomerCode());
            statement.setTimestamp(3, salesReturn.getReturnDate());
            statement.setDouble(4, salesReturn.getTotalAmount());
            statement.setString(5, salesReturn.getRemarks());
            statement.setString(6, salesReturn.getStatus());
            statement.setInt(7, salesReturn.getReturnId());

            statement.executeUpdate();
        }
    }

    public void deleteSalesReturn(int returnId) throws SQLException {
        String sql = "DELETE FROM sales_return WHERE return_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, returnId);
            statement.executeUpdate();
        }
    }

    // Method to get a map of type_id -> type_name
    public static Map<Integer, String> getTypeIdToNameMap() {
        return new HashMap<>(typeIdToNameMap);
    }

    // Method to get a map of type_name -> type_id
    public static Map<String, Integer> getTypeNameToIdMap() {
        return new HashMap<>(typeNameToIdMap);
    }

    public ObservableList<SalesReturn> getSalesReturnObservableList() {
        List<SalesReturn> salesReturns = getAllSalesReturns();
        return FXCollections.observableArrayList(salesReturns);
    }
}
