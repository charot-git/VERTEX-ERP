package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.DiscountType;
import com.vertex.vos.Objects.LineDiscount;
import com.zaxxer.hikari.HikariDataSource;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DiscountDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public boolean updateProductDiscount(int id, int supplierId, int typeId) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE product_per_supplier SET discount_type = ? WHERE product_id = ? AND supplier_id = ?")) {

            statement.setInt(1, typeId);
            statement.setInt(2, id);
            statement.setInt(3, supplierId);

            int rowsAffected = statement.executeUpdate();

            return rowsAffected > 0;
        }
    }

    public List<BigDecimal> getLineDiscountsByDiscountTypeId(int discountTypeId) {
        List<BigDecimal> lineDiscounts = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT percentage FROM line_discount ld JOIN line_per_discount_type lpd ON ld.id = lpd.line_id WHERE lpd.type_id = ?")) {

            statement.setInt(1, discountTypeId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                BigDecimal discountValue = resultSet.getBigDecimal("percentage");
                lineDiscounts.add(discountValue);
            }
        } catch (SQLException e) {
            // Handle the exception here
            e.printStackTrace();
            // You can choose to throw the exception or return an empty list or any other appropriate action
            return new ArrayList<>();
        }
        return lineDiscounts;
    }


    public int getProductDiscountForProductTypeId(int productId, int supplierId) {
        int discountTypeId = -1; // Default value indicating no discount type found

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT discount_type FROM product_per_supplier WHERE product_id = ? AND supplier_id = ?")) {

            statement.setInt(1, productId);
            statement.setInt(2, supplierId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    discountTypeId = resultSet.getInt("discount_type");
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error retrieving discount type for product and supplier", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error opening database connection", e);
        }

        return discountTypeId;
    }


    public DiscountType getDiscountTypeById(int typeId) throws SQLException {
        DiscountType discountType = null;
        String query = "SELECT * FROM discount_type WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, typeId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                // Instantiate the DiscountType object
                discountType = new DiscountType();
                discountType.setId(resultSet.getInt("id"));
                discountType.setTypeName(resultSet.getString("discount_type"));
            }
        }

        return discountType; // Will return null if no record is found
    }


    public String getDiscountTypeNameById(int typeId) throws SQLException {
        String discountTypeName = null;
        String query = "SELECT discount_type FROM discount_type WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, typeId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                discountTypeName = resultSet.getString("discount_type");
            }
        }

        return discountTypeName;
    }


    public boolean lineDiscountCreate(String lineDiscount, double percentage) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO line_discount (line_discount, percentage) VALUES (?, ?)")) {

            statement.setString(1, lineDiscount);
            statement.setDouble(2, percentage);
            int rowsAffected = statement.executeUpdate();

            return rowsAffected > 0;
        }
    }

    public boolean discountTypeCreate(String discountType) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO discount_type (discount_type) VALUES (?)")) {

            statement.setString(1, discountType);
            int rowsAffected = statement.executeUpdate();

            return rowsAffected > 0;
        }
    }

    public boolean linkLineDiscountWithType(int lineId, int typeId) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO line_per_discount_type (line_id, type_id) VALUES (?, ?)")) {

            statement.setInt(1, lineId);
            statement.setInt(2, typeId);
            int rowsAffected = statement.executeUpdate();

            return rowsAffected > 0;
        }
    }

    public List<LineDiscount> getAllLineDiscounts() throws SQLException {
        List<LineDiscount> lineDiscounts = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM line_discount")) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int lineId = resultSet.getInt("id");
                String lineDiscountName = resultSet.getString("line_discount");
                BigDecimal discountValue = resultSet.getBigDecimal("percentage");

                // Create LineDiscount objects and add them to the list
                LineDiscount lineDiscount = new LineDiscount(lineId, lineDiscountName, discountValue);
                lineDiscounts.add(lineDiscount);
            }
        }
        return lineDiscounts;
    }


    public List<LineDiscount> getAllLineDiscountsByType(int typeId) {
        List<LineDiscount> lineDiscounts = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT ld.id, ld.line_discount, ld.percentage " +
                             "FROM line_discount ld " +
                             "JOIN line_per_discount_type lpd ON ld.id = lpd.line_id " +
                             "WHERE lpd.type_id = ?")) {

            statement.setInt(1, typeId);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int lineId = resultSet.getInt("id");
                String lineDiscountName = resultSet.getString("line_discount");
                BigDecimal discountValue = resultSet.getBigDecimal("percentage");

                // Create LineDiscount objects and add them to the list
                LineDiscount lineDiscount = new LineDiscount(lineId, lineDiscountName, discountValue);
                lineDiscounts.add(lineDiscount);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return lineDiscounts;
    }

    public List<DiscountType> getAllDiscountTypes() {
        List<DiscountType> discountTypes = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM discount_type")) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String typeName = resultSet.getString("discount_type");
                int id = resultSet.getInt("id");
                DiscountType discountType = new DiscountType();
                discountType.setTypeName(typeName);
                discountType.setId(id);
                discountTypes.add(discountType);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all discount types", e);
        }
        return discountTypes;
    }

    public List<String> getAllDiscountTypeNames() throws SQLException {
        List<String> typeNames = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM discount_type")) {

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String typeName = resultSet.getString("discount_type");
                typeNames.add(typeName);
            }
        }
        return typeNames;
    }


    public int getLineDiscountIdByName(String lineDiscountName) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id FROM line_discount WHERE line_discount = ?")) {
            statement.setString(1, lineDiscountName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
        }
        return -1; // Return -1 if no ID found for the given name
    }

    public int getDiscountTypeIdByName(String discountTypeName) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id FROM discount_type WHERE discount_type = ?")) {
            statement.setString(1, discountTypeName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
        }
        return -1;
    }

    public boolean isLineDiscountLinkedWithType(int lineDiscountId, int typeId) throws SQLException {
        String query = "SELECT COUNT(*) FROM line_per_discount_type WHERE line_id = ? AND type_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, lineDiscountId);
            statement.setInt(2, typeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0; // If count > 0, it means the link exists
                }
            }
        }
        return false; // Default return false if something goes wrong or no link found
    }
}
