package com.vertex.vos.Utilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariDataSource;

public class CreditTypeDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public int getCreditTypeIdByName(String creditTypeName) throws SQLException {
        int creditTypeId = -1; // Default value if credit type is not found
        String query = "SELECT id FROM credit_type WHERE credit_name = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, creditTypeName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    creditTypeId = resultSet.getInt("id");
                }
            }
        }

        return creditTypeId;
    }

    // If you need to add more methods for CRUD operations, you can do so here
}