package com.vertex.vos.Utilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class StoreTypeDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public ObservableList<String> getAllStoreTypes() throws SQLException {
        ObservableList<String> storeTypes = FXCollections.observableArrayList();
        String query = "SELECT store_type FROM store_type";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String storeType = resultSet.getString("store_type");
                storeTypes.add(storeType);
            }
        }

        return storeTypes;
    }

    public int getStoreTypeIdByName(String storeTypeName) throws SQLException {
        String query = "SELECT id FROM store_type WHERE store_type = ?";
        int storeTypeId = -1; // Default value if not found

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, storeTypeName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    storeTypeId = resultSet.getInt("id");
                }
            }
        }

        return storeTypeId;
    }
    // If you need to add more methods for CRUD operations, you can do so here
}
