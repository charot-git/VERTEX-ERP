package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.User;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WarehouseBrandLinkDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public boolean linkBrandToWarehouseman(int userId, int brandId) {
        String sql = "INSERT INTO warehouse_brand (user_id, brand_id) VALUES (?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, brandId);
            int rowsInserted = preparedStatement.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean unlinkBrandFromWarehouseman(int userId, int brandId) {
        String sql = "DELETE FROM warehouse_brand WHERE user_id = ? AND brand_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, brandId);
            int rowsDeleted = preparedStatement.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ObservableList<Integer> getLinkedBrands(int userId) {
        ObservableList<Integer> brandIds = FXCollections.observableArrayList();
        String sql = "SELECT brand_id FROM warehouse_brand WHERE user_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    int brandId = resultSet.getInt("brand_id");
                    brandIds.add(brandId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return brandIds;
    }

    public ObservableList<String> getBrandNames(User user) {
        ObservableList<String> brandNames = FXCollections.observableArrayList();
        String sql = """
                SELECT b.brand_name
                FROM warehouse_brand wb
                JOIN brand b ON wb.brand_id = b.brand_id
                WHERE wb.user_id = ?
            """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, user.getUser_id());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String brandName = resultSet.getString("brand_name");
                    brandNames.add(brandName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return brandNames;
    }
}
