package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.Division;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DivisionDAO {

    public ObservableList<Division> getAllDivisions() {
        ObservableList<Division> divisions = FXCollections.observableArrayList();

        String sqlQuery = "SELECT * FROM division";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                Division division = new Division();
                division.setId(resultSet.getInt("division_id"));
                division.setDivisionName(resultSet.getString("division_name"));
                division.setDivisionDescription(resultSet.getString("division_description"));
                division.setDivisionHead(resultSet.getString("division_head"));
                division.setDivisionCode(resultSet.getString("division_code"));
                division.setDateAdded(resultSet.getDate("date_added"));

                divisions.add(division);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return divisions;
    }

    public ObservableList<String> getAllDivisionNames() {
        ObservableList<String> divisionNames = FXCollections.observableArrayList();

        String sqlQuery = "SELECT division_name FROM division";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String divisionName = resultSet.getString("division_name");
                divisionNames.add(divisionName);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return divisionNames;
    }
    public int getDivisionIdByName(String divisionName) {
        String sqlQuery = "SELECT division_id FROM division WHERE division_name = ?";
        int divisionId = -1; // Default value if division is not found

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, divisionName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    divisionId = resultSet.getInt("division_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return divisionId;
    }

    public String getDivisionNameById(int divisionId) {
        String sqlQuery = "SELECT division_name FROM division WHERE division_id = ?";
        String divisionName = null;

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setInt(1, divisionId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    divisionName = resultSet.getString("division_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return divisionName;
    }

}
