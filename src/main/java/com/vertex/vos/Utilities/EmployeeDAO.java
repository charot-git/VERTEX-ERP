package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.User;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;

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

    public User getUserByFullName(String fullName) {
        User user = null;
        String[] names = fullName.split("\\s+");
        String firstName = names[0];
        String lastName = names[names.length - 1];
        StringBuilder middleNameBuilder = new StringBuilder();
        for (int i = 1; i < names.length - 1; i++) {
            middleNameBuilder.append(names[i]);
            if (i < names.length - 2) {
                middleNameBuilder.append(" ");
            }
        }
        String middleName = middleNameBuilder.toString();

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT * FROM user WHERE user_fname = ? AND user_lname = ? AND user_mname = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, firstName);
                preparedStatement.setString(2, lastName);
                preparedStatement.setString(3, middleName);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        // Populate the user object with data from the result set
                        int userId = resultSet.getInt("user_id");
                        String userEmail = resultSet.getString("user_email");
                        String userPassword = resultSet.getString("user_password");
                        String userContact = resultSet.getString("user_contact");
                        String userProvince = resultSet.getString("user_province");
                        String userCity = resultSet.getString("user_city");
                        String userBrgy = resultSet.getString("user_brgy");
                        String userSss = resultSet.getString("user_sss");
                        String userPhilhealth = resultSet.getString("user_philhealth");
                        String userTin = resultSet.getString("user_tin");
                        String userPosition = resultSet.getString("user_position");
                        String userDepartment = resultSet.getString("user_department");
                        String userTags = resultSet.getString("user_tags");
                        Date userDateOfHire = resultSet.getDate("user_dateOfHire");
                        Date userBday = resultSet.getDate("user_bday");
                        int roleId = resultSet.getInt("role_id");
                        String userImage = resultSet.getString("user_image");

                        user = new User(userId, userEmail, userPassword, firstName, middleName, lastName, userContact,
                                userProvince, userCity, userBrgy, userSss, userPhilhealth, userTin, userPosition,
                                userDepartment, userDateOfHire, userTags, userBday, roleId, userImage);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }

        return user;
    }
}
