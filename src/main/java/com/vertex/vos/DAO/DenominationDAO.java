package com.vertex.vos.DAO;

import com.vertex.vos.Objects.Denomination;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DenominationDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public List<Denomination> getAllDenominations() {
        List<Denomination> denominations = new ArrayList<>();
        String sql = "SELECT * FROM denomination ORDER BY amount DESC";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Denomination denomination = new Denomination();
                denomination.setId(resultSet.getInt("id"));
                denomination.setAmount(resultSet.getDouble("amount"));
                // Assuming Denomination has a constructor or setters for these fields
                denominations.add(denomination);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return denominations;
    }
}
