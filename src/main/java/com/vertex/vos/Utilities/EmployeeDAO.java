package com.vertex.vos.Utilities;

import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public ObservableList<String> getAllUserNames() {
        ObservableList<String> userNames = FXCollections.observableArrayList();

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT user_fname, user_mname, user_lname FROM user";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String firstName = resultSet.getString("user_fname");
                        String middleName = resultSet.getString("user_mname");
                        String lastName = resultSet.getString("user_lname");
                        String fullName = firstName + " " + (middleName != null ? middleName + " " : "") + lastName;
                        userNames.add(fullName);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }

        return userNames;
    }

}
