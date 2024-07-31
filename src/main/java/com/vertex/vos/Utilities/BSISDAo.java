package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.BSIS;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BSISDAo {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public ObservableList<BSIS> getBsisDetails() {
        ObservableList<BSIS> bsisObservableList = FXCollections.observableArrayList();
        String sqlQuery = "SELECT * FROM bsis_types";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String code = resultSet.getString("bsis_code");
                String name = resultSet.getString("bsis_name");

                BSIS bsis = new BSIS(id, code, name);
                bsisObservableList.add(bsis);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
        return bsisObservableList;
    }

    public String getBSISCodeById(int id) {
        String sqlQuery = "SELECT bsis_code FROM bsis_types WHERE id = ?";
        String code = null; // Default value if not found

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    code = resultSet.getString("bsis_code");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
        return code;
    }

    public ObservableList<String> getAllBSISCodes() {
        ObservableList<String> bsisCodes = FXCollections.observableArrayList();
        String sqlQuery = "SELECT bsis_code FROM bsis_types";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String code = resultSet.getString("bsis_code");
                bsisCodes.add(code);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
        return bsisCodes;
    }

    public int getBSISCodeByString(String value) {
        int id = 0; // Default value if not found
        String sqlQuery = "SELECT id FROM bsis_types WHERE bsis_code = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, value);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    id = resultSet.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
        return id;
    }
}
