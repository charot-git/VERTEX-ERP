package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.Operation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class OperationDAO {

    private Map<Integer, Operation> operationCache = new HashMap<>();
    private Map<String, Integer> operationNameToIdCache = new HashMap<>();
    private Map<Integer, String> operationIdToNameCache = new HashMap<>();

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
                operationCache.put(operation.getId(), operation);
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

    public Operation getOperationById(int operationId) {
        if (operationCache.containsKey(operationId)) {
            return operationCache.get(operationId);
        }

        String sqlQuery = "SELECT * FROM operation WHERE id = ?";
        Operation operation = null;

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setInt(1, operationId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    operation = new Operation();
                    operation.setId(resultSet.getInt("id"));
                    operation.setOperationCode(resultSet.getString("operation_code"));
                    operation.setOperationName(resultSet.getString("operation_name"));
                    operation.setDateModified(resultSet.getTimestamp("date_modified"));
                    operation.setEncoderId(resultSet.getInt("encoder_id"));
                    operation.setCompanyId(resultSet.getInt("company_id"));
                    operation.setType(resultSet.getInt("type"));
                    operation.setDefinition(resultSet.getString("definition"));

                    operationCache.put(operationId, operation);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return operation;
    }

    public int getOperationIdByName(String operationName) {
        if (operationNameToIdCache.containsKey(operationName)) {
            return operationNameToIdCache.get(operationName);
        }

        String sqlQuery = "SELECT id FROM operation WHERE operation_name = ?";
        int operationId = -1; // Set a default value indicating not found

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, operationName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    operationId = resultSet.getInt("id");
                    operationNameToIdCache.put(operationName, operationId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return operationId;
    }

    public String getOperationNameById(int operationId) {
        if (operationIdToNameCache.containsKey(operationId)) {
            return operationIdToNameCache.get(operationId);
        }

        String sqlQuery = "SELECT operation_name FROM operation WHERE id = ?";
        String operationName = null;

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setInt(1, operationId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    operationName = resultSet.getString("operation_name");
                    operationIdToNameCache.put(operationId, operationName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return operationName;
    }
}