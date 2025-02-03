package com.vertex.vos.DAO;

import com.vertex.vos.Objects.Inventory;
import com.vertex.vos.Objects.SalesReturn;
import com.vertex.vos.Objects.SalesReturnDetail;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDateTime;
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

    SalesInvoiceDAO salesInvoiceDAO = new SalesInvoiceDAO();

    public List<String> getSalesInvoiceNumbers() {
        return salesInvoiceDAO.getAllInvoiceNumbersUnlinkedToSalesReturns();
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

    SalesmanDAO salesmanDAO = new SalesmanDAO();
    CustomerDAO customerDAO = new CustomerDAO();
    ProductDAO productDAO = new ProductDAO();

    public SalesReturn getSalesReturnByReturnNumber(String returnNumber) throws SQLException {
        String salesReturnSql = "SELECT * FROM sales_return WHERE return_number = ?";
        String salesReturnDetailSql = "SELECT * FROM sales_return_details WHERE return_no = ?";

        SalesReturn salesReturn = null;
        ObservableList<SalesReturnDetail> salesReturnDetails = FXCollections.observableArrayList();

        try (Connection connection = dataSource.getConnection()) {
            // Fetch Sales Return
            try (PreparedStatement salesReturnStatement = connection.prepareStatement(salesReturnSql)) {
                salesReturnStatement.setString(1, returnNumber);
                ResultSet resultSet = salesReturnStatement.executeQuery();

                if (resultSet.next()) {
                    salesReturn = new SalesReturn();
                    salesReturn.setReturnNumber(resultSet.getString("return_number"));
                    salesReturn.setCustomer(customerDAO.getCustomerByCode(resultSet.getString("customer_code")));
                    salesReturn.setSalesman(salesmanDAO.getSalesmanDetails(resultSet.getInt("salesman_id"))); // Assuming Salesman has an ID constructor
                    salesReturn.setReturnDate(resultSet.getTimestamp("return_date"));
                    salesReturn.setTotalAmount(resultSet.getDouble("total_amount"));
                    salesReturn.setDiscountAmount(resultSet.getDouble("discount_amount"));
                    salesReturn.setGrossAmount(resultSet.getDouble("gross_amount"));
                    salesReturn.setRemarks(resultSet.getString("remarks"));
                    salesReturn.setCreatedBy(resultSet.getInt("created_by"));
                    salesReturn.setPriceType(resultSet.getString("price_type"));
                    salesReturn.setThirdParty(resultSet.getBoolean("isThirdParty"));
                    salesReturn.setStatus(resultSet.getString("status"));
                    salesReturn.setPosted(resultSet.getBoolean("isPosted"));
                    salesReturn.setReceived(resultSet.getBoolean("isReceived"));
                    salesReturn.setReceivedAt(resultSet.getTimestamp("received_at"));
                    salesReturn.setSales_invoice_id(resultSet.getInt("invoice_no"));
                } else {
                    return null; // No sales return found
                }
            }

            // Fetch Sales Return Details
            try (PreparedStatement salesReturnDetailStatement = connection.prepareStatement(salesReturnDetailSql)) {
                salesReturnDetailStatement.setString(1, returnNumber);
                ResultSet resultSet = salesReturnDetailStatement.executeQuery();

                while (resultSet.next()) {
                    SalesReturnDetail detail = new SalesReturnDetail();
                    detail.setProductId(resultSet.getInt("product_id"));
                    detail.setProduct(productDAO.getProductById(detail.getProductId()));
                    detail.setQuantity(resultSet.getInt("quantity"));
                    detail.setUnitPrice(resultSet.getDouble("unit_price"));
                    detail.setTotalAmount(resultSet.getDouble("total_amount"));
                    detail.setGrossAmount(resultSet.getDouble("gross_amount"));
                    detail.setDiscountAmount(resultSet.getDouble("discount_amount"));
                    detail.setReason(resultSet.getString("reason"));
                    detail.setSalesReturnTypeId(resultSet.getInt("sales_return_type_id"));
                    detail.setStatus(resultSet.getString("status"));

                    salesReturnDetails.add(detail);
                }
            }

            salesReturn.setSalesReturnDetails(salesReturnDetails);
        }

        return salesReturn;
    }


    public boolean createSalesReturn(SalesReturn salesReturn, ObservableList<SalesReturnDetail> productsForSalesReturn) throws SQLException {
        String salesReturnSql = "INSERT INTO sales_return (return_number, customer_code, salesman_id, return_date, total_amount, discount_amount, gross_amount, remarks, created_by, created_at, updated_at, price_type, isThirdParty, status, isPosted, isReceived, received_at, invoice_no) " + // Added invoice_no
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " + // Added invoice_no
                "ON DUPLICATE KEY UPDATE " +
                "customer_code = VALUES(customer_code), salesman_id = VALUES(salesman_id), return_date = VALUES(return_date), " +
                "total_amount = VALUES(total_amount), discount_amount = VALUES(discount_amount), gross_amount = VALUES(gross_amount), " +
                "remarks = VALUES(remarks), updated_at = VALUES(updated_at), status = VALUES(status), isPosted = VALUES(isPosted), " +
                "isReceived = VALUES(isReceived), received_at = VALUES(received_at), invoice_no = VALUES(invoice_no)"; // Added invoice_no

        String salesReturnDetailSql = "INSERT INTO sales_return_details (return_no, product_id, quantity, unit_price, total_amount, gross_amount, discount_amount, reason, sales_return_type_id, created_at, updated_at, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "quantity = VALUES(quantity), unit_price = VALUES(unit_price), total_amount = VALUES(total_amount), gross_amount = VALUES(gross_amount), " +
                "discount_amount = VALUES(discount_amount), reason = VALUES(reason), updated_at = VALUES(updated_at), status = VALUES(status), sales_return_type_id = VALUES(sales_return_type_id)";

        try (Connection connection = dataSource.getConnection()) {
            // Begin transaction
            connection.setAutoCommit(false);

            try (PreparedStatement salesReturnStatement = connection.prepareStatement(salesReturnSql, Statement.RETURN_GENERATED_KEYS)) {
                Timestamp now = Timestamp.valueOf(LocalDateTime.now());

                salesReturnStatement.setString(1, salesReturn.getReturnNumber());
                salesReturnStatement.setString(2, salesReturn.getCustomer().getCustomerCode());
                salesReturnStatement.setInt(3, salesReturn.getSalesman().getId());
                salesReturnStatement.setTimestamp(4, salesReturn.getReturnDate());

                // Compute total amounts
                double grossAmount = productsForSalesReturn.stream().mapToDouble(SalesReturnDetail::getTotalAmount).sum();
                double discountAmount = productsForSalesReturn.stream().mapToDouble(SalesReturnDetail::getDiscountAmount).sum();
                double totalAmount = grossAmount - discountAmount;

                salesReturnStatement.setDouble(5, totalAmount);
                salesReturnStatement.setDouble(6, discountAmount);
                salesReturnStatement.setDouble(7, grossAmount);
                salesReturnStatement.setString(8, salesReturn.getRemarks());
                salesReturnStatement.setInt(9, salesReturn.getCreatedBy());
                salesReturnStatement.setTimestamp(10, salesReturn.getCreatedAt());
                salesReturnStatement.setTimestamp(11, now);
                salesReturnStatement.setString(12, salesReturn.getPriceType());
                salesReturnStatement.setBoolean(13, salesReturn.isThirdParty());
                salesReturnStatement.setString(14, salesReturn.getStatus());
                salesReturnStatement.setBoolean(15, salesReturn.isPosted());
                salesReturnStatement.setBoolean(16, salesReturn.isReceived());
                salesReturnStatement.setTimestamp(17, salesReturn.getReceivedAt());
                salesReturnStatement.setInt(18, salesReturn.getSales_invoice_id()); // Set invoice_no

                // Execute the sales return header insertion/update
                salesReturnStatement.executeUpdate();

                // Insert/update sales return details
                try (PreparedStatement salesReturnDetailStatement = connection.prepareStatement(salesReturnDetailSql)) {
                    for (SalesReturnDetail detail : productsForSalesReturn) {
                        salesReturnDetailStatement.setString(1, salesReturn.getReturnNumber());
                        salesReturnDetailStatement.setInt(2, detail.getProductId());
                        salesReturnDetailStatement.setInt(3, detail.getQuantity());
                        salesReturnDetailStatement.setDouble(4, detail.getUnitPrice());
                        salesReturnDetailStatement.setDouble(5, detail.getTotalAmount());

                        double detailGrossAmount = detail.getTotalAmount() + detail.getDiscountAmount();
                        salesReturnDetailStatement.setDouble(6, detailGrossAmount);
                        salesReturnDetailStatement.setDouble(7, detail.getDiscountAmount());
                        salesReturnDetailStatement.setString(8, detail.getReason());
                        salesReturnDetailStatement.setInt(9, detail.getSalesReturnTypeId());
                        salesReturnDetailStatement.setTimestamp(10, now);
                        salesReturnDetailStatement.setTimestamp(11, now);
                        salesReturnDetailStatement.setString(12, detail.getStatus());

                        salesReturnDetailStatement.addBatch(); // Add to batch
                    }

                    // Execute batch insert/update for details
                    salesReturnDetailStatement.executeBatch();
                }

                // Commit the transaction if everything is successful
                connection.commit();

                if (salesReturn.isReceived()) {
                    List<Inventory> inventories = new ArrayList<>();

                    InventoryDAO inventoryDAO = new InventoryDAO();
                    for (SalesReturnDetail salesReturnDetail : productsForSalesReturn) {
                        Inventory inventory = getInventory(salesReturnDetail, salesReturn);
                        inventories.add(inventory);
                    }

                    inventoryDAO.updateInventoryBulk(inventories, connection);
                }

                return true;

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

    private static Inventory getInventory(SalesReturnDetail salesReturnDetail, SalesReturn salesReturn) {
        Inventory inventory = new Inventory();
        inventory.setProductId(salesReturnDetail.getProduct().getProductId());
        inventory.setQuantity(salesReturnDetail.getQuantity());
        if (salesReturnDetail.getSalesReturnTypeId() == 5) {
            inventory.setBranchId(salesReturn.getSalesman().getBadBranchCode());
        } else {
            inventory.setBranchId(salesReturn.getSalesman().getGoodBranchCode());
        }
        return inventory;
    }

    public SalesReturn getSalesReturnById(int returnId) throws SQLException {
        String sql = "SELECT * FROM sales_return WHERE return_id = ?";
        SalesReturn salesReturn = null;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, returnId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                salesReturn = new SalesReturn();
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
                salesReturn.setPosted(resultSet.getBoolean("isPosted"));
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
