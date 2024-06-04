package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.Operation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OperationDAO {

    public ObservableList<Operation> getAllOperations() {
        ObservableList<Operation> operations = FXCollections.observableArrayList();

        String sqlQuery = "SELECT * FROM operation";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                Operation operation = new Operation();
                operation.setId(resultSet.getInt("id"));
                operation.setOperationCode(resultSet.getString("operation_code"));
                operation.setOperationName(resultSet.getString("operation_name"));
                operation.setDateModified(resultSet.getTimestamp("date_modified"));
                operation.setEncoderId(resultSet.getInt("encoder_id"));
                operation.setCompanyId(resultSet.getInt("company_id"));
                operation.setType(resultSet.getInt("type"));
                operation.setDefinition(resultSet.getString("definition"));

                operations.add(operation);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return operations;
    }

    public ObservableList<String> getAllOperationNames() {
        ObservableList<String> operationNames = FXCollections.observableArrayList();

        String sqlQuery = "SELECT operation_name FROM operation";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String operationName = resultSet.getString("operation_name");
                operationNames.add(operationName);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return operationNames;
    }

    public int getOperationIdByName(String operationName) {
        String sqlQuery = "SELECT id FROM operation WHERE operation_name = ?";
        int operationId = -1; // Set a default value indicating not found

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, operationName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    operationId = resultSet.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return operationId;
    }


    public String getOperationNameById(int operationId) {
        String sqlQuery = "SELECT operation_name FROM operation WHERE id = ?";
        String operationName = null;

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setInt(1, operationId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    operationName = resultSet.getString("operation_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return operationName;
    }

}
