package com.vertex.vos.Utilities;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DiscountDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public boolean updateProductDiscount(int id, int type_id) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE product_per_supplier SET discount_type = ? WHERE id = ?")) {

            statement.setInt(1, type_id);
            statement.setInt(2, id);
            int rowsAffected = statement.executeUpdate();

            return rowsAffected > 0;
        }
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


}
