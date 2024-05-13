package com.vertex.vos.Utilities;

import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReceivingTypeDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public void addReceivingType(String description) {
        String sql = "INSERT INTO receiving_type (description) VALUES (?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, description);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<String> getAllReceivingTypes() {
        ObservableList<String> receivingTypes = FXCollections.observableArrayList();
        String sql = "SELECT description FROM receiving_type";
        try (Connection conn = dataSource.getConnection();
             Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                String description = resultSet.getString("description");
                receivingTypes.add(description);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return receivingTypes;
    }

    // Other CRUD operations can be implemented similarly
}
