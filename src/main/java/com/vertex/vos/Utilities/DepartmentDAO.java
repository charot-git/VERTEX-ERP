package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.Department;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class DepartmentDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();


    public String getDepartmentNameById(int departmentId) {
        String departmentName = null;

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT department_name FROM department WHERE department_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, departmentId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        departmentName = resultSet.getString("department_name");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }

        return departmentName;
    }
    public ObservableList<String> getAllDepartmentNames() {
        ObservableList<String> departmentNames = FXCollections.observableArrayList();

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT department_name FROM department";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String departmentName = resultSet.getString("department_name");
                        departmentNames.add(departmentName);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }

        return departmentNames;
    }


    public ObservableList<Department> getAllDepartments() {
        ObservableList<Department> departments = FXCollections.observableArrayList();

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT * FROM department";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        int departmentId = resultSet.getInt("department_id");
                        String departmentName = resultSet.getString("department_name");
                        String parentDivision = resultSet.getString("parent_division");
                        String description = resultSet.getString("department_description");
                        String departmentHead = resultSet.getString("department_head");
                        int taxId = resultSet.getInt("tax_id");
                        Date dateAdded = resultSet.getDate("date_added");

                        Department department = new Department(departmentId, departmentName, parentDivision,
                                description, departmentHead, taxId, dateAdded);

                        departments.add(department);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }

        return departments;
    }

    public int getDepartmentIdByName(String departmentName) {
        int departmentId = -1; // Default value if department not found

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT department_id FROM department WHERE department_name = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, departmentName);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        departmentId = resultSet.getInt("department_id");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }

        return departmentId;
    }
}
