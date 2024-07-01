package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.AssetsAndEquipment;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AssetsAndEquipmentDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public List<AssetsAndEquipment> getAllAssetsAndEquipment() {
        List<AssetsAndEquipment> assetsList = new ArrayList<>();
        String query = "SELECT * FROM assets_and_equipment";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                AssetsAndEquipment asset = new AssetsAndEquipment();
                asset.setId(resultSet.getInt("id"));
                asset.setItemImage(resultSet.getString("item_image"));
                asset.setItemName(resultSet.getString("item_name"));
                asset.setQuantity(resultSet.getInt("quantity"));
                asset.setDepartment(resultSet.getInt("department"));
                asset.setEmployee(resultSet.getInt("employee"));
                asset.setCostPerItem(resultSet.getDouble("cost_per_item"));
                asset.setTotal(resultSet.getDouble("total"));
                asset.setCondition(resultSet.getInt("condition"));
                asset.setLifeSpan(resultSet.getInt("life_span"));
                asset.setEncoder(resultSet.getInt("encoder"));
                asset.setDateAcquired(resultSet.getTimestamp("date_acquired").toLocalDateTime());

                assetsList.add(asset);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your application's needs
        }
        return assetsList;
    }
}
