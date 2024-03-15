package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.Nature;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NatureDAO {

    public ObservableList<Nature> getNatureDetails() {
        ObservableList<Nature> natureList = FXCollections.observableArrayList();
        String sqlQuery = "SELECT nature_id, nature_name FROM nature";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int natureId = resultSet.getInt("nature_id");
                String natureName = resultSet.getString("nature_name");

                Nature nature = new Nature(natureId, natureName);
                natureList.add(nature);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return natureList;
    }

    public boolean createNature(String natureName) {
        String insertQuery = "INSERT INTO nature (nature_name) VALUES (?)";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

            preparedStatement.setString(1, natureName);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Nature created successfully: " + natureName);
                return true;
            } else {
                System.out.println("Failed to create nature: " + natureName);
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
            // Optionally, show an error message dialog to the user
            DialogUtils.showErrorMessage("Database Error", "An error occurred while creating the nature: " + natureName);
            return false;
        }
    }


    public int getNatureIdByName(String natureName) {
        String sqlQuery = "SELECT nature_id FROM nature WHERE nature_name = ?";
        int natureId = -1; // Default value indicating not found

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, natureName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    natureId = resultSet.getInt("nature_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle SQL exceptions here
        }

        return natureId;
    }

    public String getNatureNameById(int natureId) {
        String sqlQuery = "SELECT nature_name FROM nature WHERE nature_id = ?";
        String natureName = null; // Default value indicating not found

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setInt(1, natureId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    natureName = resultSet.getString("nature_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle SQL exceptions here
        }

        return natureName;
    }
    public ObservableList<String> getNatureNames() {
        ObservableList<String> natureNames = FXCollections.observableArrayList();
        String sqlQuery = "SELECT nature_name FROM nature";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String natureName = resultSet.getString("nature_name");
                natureNames.add(natureName);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return natureNames;
    }

}
