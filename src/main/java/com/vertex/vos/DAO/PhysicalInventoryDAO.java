package com.vertex.vos.DAO;

import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhysicalInventoryDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    // Create a new PhysicalInventory record
    public boolean createPhysicalInventory(PhysicalInventory inventory) {
        String sql = "INSERT INTO physical_inventory (ph_no, date_encoded, cutOff_date, price_type, stock_type, branch_id, remarks, isComitted, isCancelled, total_amount, supplier_id, category_id, encoder_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

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

            return stmt.executeUpdate() > 0;

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
