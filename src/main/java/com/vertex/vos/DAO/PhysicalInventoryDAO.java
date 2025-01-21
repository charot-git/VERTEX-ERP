package com.vertex.vos.DAO;

import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhysicalInventoryDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    PhysicalInventoryDetailsDAO physicalInventoryDetailsDAO = new PhysicalInventoryDetailsDAO();

    // Create a new PhysicalInventory record
    public boolean createPhysicalInventory(PhysicalInventory inventory, ObservableList<PhysicalInventoryDetails> details) {
        String insertInventorySQL = "INSERT INTO physical_inventory (ph_no, date_encoded, cutOff_date, price_type, stock_type, branch_id, remarks, isComitted, isCancelled, total_amount, supplier_id, category_id, encoder_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertDetailsSQL = "INSERT INTO physical_inventory_details (ph_id, product_id, unit_price, system_count, physical_count, variance, difference_cost, amount) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        // Start a transaction
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);  // Start transaction

            // Insert the physical inventory
            try (PreparedStatement stmt = connection.prepareStatement(insertInventorySQL, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, inventory.getPhNo());
                stmt.setTimestamp(2, inventory.getDateEncoded());
                stmt.setTimestamp(3, inventory.getCutOffDate());
                stmt.setString(4, inventory.getPriceType());
                stmt.setString(5, inventory.getStockType());
                stmt.setInt(6, inventory.getBranch().getId());
                stmt.setString(7, inventory.getRemarks());
                stmt.setBoolean(8, inventory.isCommitted());
                stmt.setBoolean(9, inventory.isCancelled());
                stmt.setDouble(10, inventory.getTotalAmount());
                stmt.setInt(11, inventory.getSupplier().getId());
                stmt.setInt(12, inventory.getCategory().getCategoryId());
                stmt.setInt(13, inventory.getEncoderId());

                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    connection.rollback();
                    return false;
                }

                // Get the generated inventory ID
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int inventoryId = generatedKeys.getInt(1);

                        // Insert the inventory details for each product
                        try (PreparedStatement detailsStmt = connection.prepareStatement(insertDetailsSQL)) {
                            for (PhysicalInventoryDetails detail : details) {
                                detailsStmt.setInt(1, inventoryId);  // Set the inventory ID for the detail
                                detailsStmt.setInt(2, detail.getProduct().getProductId());
                                detailsStmt.setDouble(3, detail.getUnitPrice());  // unit_price
                                detailsStmt.setInt(4, detail.getSystemCount());  // system_count
                                detailsStmt.setInt(5, detail.getPhysicalCount());  // physical_count
                                detailsStmt.setInt(6, detail.getVariance());  // variance
                                detailsStmt.setDouble(7, detail.getDifferenceCost());  // difference_cost
                                detailsStmt.setDouble(8, detail.getAmount());  // amount
                                detailsStmt.addBatch();  // Batch the inserts
                            }
                            detailsStmt.executeBatch();  // Execute all the batch inserts
                        }

                        // Commit transaction if both the inventory and details are inserted successfully
                        connection.commit();
                        return true;
                    } else {
                        connection.rollback();
                        return false;
                    }
                }

            } catch (SQLException e) {
                connection.rollback();  // Rollback transaction on failure
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Retrieve a PhysicalInventory record by ID
    public PhysicalInventory getPhysicalInventoryById(int id) {
        String sql = "SELECT * FROM physical_inventory WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPhysicalInventory(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Retrieve all PhysicalInventory records
    public List<PhysicalInventory> getAllPhysicalInventories() {
        List<PhysicalInventory> inventories = new ArrayList<>();
        String sql = "SELECT * FROM physical_inventory";
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                inventories.add(mapResultSetToPhysicalInventory(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return inventories;
    }

    // Update a PhysicalInventory record
    public boolean updatePhysicalInventory(PhysicalInventory inventory) {
        String sql = "UPDATE physical_inventory SET ph_no = ?, cutOff_date = ?, price_type = ?, stock_type = ?, branch_id = ?, remarks = ?, isComitted = ?, isCancelled = ?, total_amount = ?, supplier_id = ?, category_id = ?, encoder_id = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, inventory.getPhNo());
            stmt.setTimestamp(2, inventory.getCutOffDate());
            stmt.setString(3, inventory.getPriceType());
            stmt.setString(4, inventory.getStockType());
            stmt.setInt(5, inventory.getBranch().getId());
            stmt.setString(6, inventory.getRemarks());
            stmt.setBoolean(7, inventory.isCommitted());
            stmt.setBoolean(8, inventory.isCancelled());
            stmt.setDouble(9, inventory.getTotalAmount());
            stmt.setInt(10, inventory.getSupplier().getId());
            stmt.setInt(11, inventory.getCategory().getCategoryId());
            stmt.setInt(12, inventory.getEncoderId());
            stmt.setInt(13, inventory.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete a PhysicalInventory record by ID
    public boolean deletePhysicalInventory(int id) {
        String sql = "DELETE FROM physical_inventory WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    BranchDAO branchDAO = new BranchDAO();
    CategoriesDAO categoriesDAO = new CategoriesDAO();
    EmployeeDAO employeeDAO = new EmployeeDAO();
    SupplierDAO supplierDAO = new SupplierDAO();

    // Helper method to map ResultSet to PhysicalInventory
    private PhysicalInventory mapResultSetToPhysicalInventory(ResultSet rs) throws SQLException {
        Branch branch = new Branch();
        branch.setId(rs.getInt("branch_id"));
        branch.setBranchDescription(branchDAO.getBranchNameById(branch.getId()));
        branch.setBranchCode(branchDAO.getBranchCodeById(branch.getId()));
        Supplier supplier = new Supplier();
        supplier.setId(rs.getInt("supplier_id"));
        supplier.setSupplierName(supplierDAO.getSupplierNameById(supplier.getId()));

        Category category = new Category();
        category.setCategoryId(rs.getInt("category_id"));
        category.setCategoryName(categoriesDAO.getCategoryNameById(category.getCategoryId()));

        return new PhysicalInventory(
                rs.getInt("id"),
                rs.getString("ph_no"),
                rs.getTimestamp("date_encoded"),
                rs.getTimestamp("cutOff_date"),
                rs.getString("price_type"),
                rs.getString("stock_type"),
                branch,
                rs.getString("remarks"),
                rs.getBoolean("isComitted"),
                rs.getBoolean("isCancelled"),
                rs.getDouble("total_amount"),
                supplier,
                category,
                rs.getInt("encoder_id")
        );
    }

    public int getNextNo() {
        int nextNo = 0;
        String updateQuery = "UPDATE physical_inventory_no SET no = no + 1";
        String selectQuery = "SELECT no FROM physical_inventory_no ORDER BY no DESC LIMIT 1";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
             PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
             ResultSet resultSet = selectStatement.executeQuery()) {

            if (resultSet.next()) {
                nextNo = resultSet.getInt("no");
            }

            updateStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return nextNo;
    }


}
