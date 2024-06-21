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
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmployeeDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public ObservableList<String> getAllUserNames() {
        ObservableList<String> userNames = FXCollections.observableArrayList();
        String sql = "SELECT user_fname, user_mname, user_lname FROM user";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String firstName = resultSet.getString("user_fname");
                String middleName = resultSet.getString("user_mname");
                String lastName = resultSet.getString("user_lname");

                String fullName = buildFullName(firstName, middleName, lastName);
                userNames.add(fullName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return userNames;
    }

    private String buildFullName(String firstName, String middleName, String lastName) {
        StringBuilder fullNameBuilder = new StringBuilder(firstName);
        if (middleName != null && !middleName.trim().isEmpty()) {
            fullNameBuilder.append(" ").append(middleName);
        }
        fullNameBuilder.append(" ").append(lastName);
        return fullNameBuilder.toString();
    }

    public ObservableList<String> getAllEmployeeNamesWhereDepartment(int departmentId) {
        ObservableList<String> userNames = FXCollections.observableArrayList();
        String sql = "SELECT user_fname, user_mname, user_lname FROM user WHERE user_department = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, departmentId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String firstName = resultSet.getString("user_fname");
                    String middleName = resultSet.getString("user_mname");
                    String lastName = resultSet.getString("user_lname");

                    String fullName = buildFullName(firstName, middleName, lastName);
                    userNames.add(fullName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return userNames;
    }


    public User getUserByFullName(String fullName) {
        User user = null;
        String[] names = fullName.trim().split("\\s+");
        if (names.length < 2) {
            throw new IllegalArgumentException("Full name must include at least first and last name");
        }

        String firstName = names[0].trim();
        String lastName = names[names.length - 1].trim();
        StringBuilder middleNameBuilder = new StringBuilder();
        for (int i = 1; i < names.length - 1; i++) {
            middleNameBuilder.append(names[i].trim());
            if (i < names.length - 2) {
                middleNameBuilder.append(" ");
            }
        }
        String middleName = middleNameBuilder.toString().trim();

        String sql = middleName.isEmpty() ?
                "SELECT * FROM user WHERE user_fname = ? AND user_lname = ? AND (user_mname IS NULL OR user_mname = '')" :
                "SELECT * FROM user WHERE user_fname = ? AND user_lname = ? AND user_mname = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            if (!middleName.isEmpty()) {
                preparedStatement.setString(3, middleName);
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
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
                    int userDepartment = resultSet.getInt("user_department");
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
        } catch (SQLException e) {
            e.printStackTrace(); // Consider logging this to a file or a logging framework
        }

        return user;
    }

    public boolean initialEmployeeRegistration(User user) {
        boolean success = false;
        try (Connection connection = dataSource.getConnection()) {
            String sql = "INSERT INTO user (user_fname, user_mname, user_lname, user_province, user_city, user_brgy, user_contact, user_email, user_department, user_tin, user_sss, user_philhealth, user_bday, user_dateOfHire, user_position) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, user.getUser_fname());
                preparedStatement.setString(2, user.getUser_mname());
                preparedStatement.setString(3, user.getUser_lname());
                preparedStatement.setString(4, user.getUser_province());
                preparedStatement.setString(5, user.getUser_city());
                preparedStatement.setString(6, user.getUser_brgy());
                preparedStatement.setString(7, user.getUser_contact());
                preparedStatement.setString(8, user.getUser_email());
                preparedStatement.setInt(9, user.getUser_department());
                preparedStatement.setString(10, user.getUser_tin());
                preparedStatement.setString(11, user.getUser_sss());
                preparedStatement.setString(12, user.getUser_philhealth());
                preparedStatement.setDate(13, user.getUser_bday());
                preparedStatement.setDate(14, user.getUser_dateOfHire());
                preparedStatement.setString(15, user.getUser_position());

                int rowsInserted = preparedStatement.executeUpdate();
                if (rowsInserted > 0) {
                    success = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }
        return success;
    }

    public boolean changePassword(int id, String newPassword) {
        boolean success = false;
        try (Connection connection = dataSource.getConnection()) {
            String sql = "UPDATE user SET user_password = ? WHERE user_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, newPassword);
                preparedStatement.setInt(2, id);

                int rowsUpdated = preparedStatement.executeUpdate();
                if (rowsUpdated > 0) {
                    success = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }
        return success;
    }

    public int getUserIdByFullName(String fullName) {
        int userId = -1;

        try (Connection connection = dataSource.getConnection()) {
            // Split the full name into parts
            String[] names = fullName.trim().split("\\s+");

            if (names.length < 3) {
                // If there's not at least a first, middle, and last name, return -1 or handle it appropriately
                return userId;
            }

            // Last name is always the last part
            String lastName = names[names.length - 1];

            // Middle name is the second last part
            String middleName = names[names.length - 2];

            // First name is everything before the middle name and last name
            StringBuilder firstNameBuilder = new StringBuilder();
            for (int i = 0; i < names.length - 2; i++) {
                firstNameBuilder.append(names[i]);
                if (i < names.length - 3) {
                    firstNameBuilder.append(" ");
                }
            }
            String firstName = firstNameBuilder.toString();

            // SQL query to find the user by first, middle, and last name
            String sql = "SELECT user_id FROM user WHERE user_fname = ? AND user_lname = ? AND user_mname = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, firstName);
                preparedStatement.setString(2, lastName);
                preparedStatement.setString(3, middleName);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        userId = resultSet.getInt("user_id");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userId;
    }


    public String getFullNameById(int userId) {
        String fullName = null;

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT user_fname, user_mname, user_lname FROM user WHERE user_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, userId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String firstName = resultSet.getString("user_fname");
                        String middleName = resultSet.getString("user_mname");
                        String lastName = resultSet.getString("user_lname");

                        StringBuilder fullNameBuilder = new StringBuilder();
                        fullNameBuilder.append(firstName);
                        if (middleName != null && !middleName.isEmpty()) {
                            fullNameBuilder.append(" ").append(middleName);
                        }
                        fullNameBuilder.append(" ").append(lastName);

                        fullName = fullNameBuilder.toString();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return fullName;
    }

    public boolean updateUser(User updatedUser) {
        boolean success = false;
        String sql = "UPDATE user SET user_fname = ?, user_mname = ?, user_lname = ?, user_province = ?, user_city = ?, user_brgy = ?, " +
                "user_contact = ?, user_email = ?, user_department = ?, user_tin = ?, user_sss = ?, user_philhealth = ?, user_bday = ?, " +
                "user_dateOfHire = ?, user_position = ? WHERE user_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, updatedUser.getUser_fname());
            preparedStatement.setString(2, updatedUser.getUser_mname());
            preparedStatement.setString(3, updatedUser.getUser_lname());
            preparedStatement.setString(4, updatedUser.getUser_province());
            preparedStatement.setString(5, updatedUser.getUser_city());
            preparedStatement.setString(6, updatedUser.getUser_brgy());
            preparedStatement.setString(7, updatedUser.getUser_contact());
            preparedStatement.setString(8, updatedUser.getUser_email());
            preparedStatement.setInt(9, updatedUser.getUser_department());
            preparedStatement.setString(10, updatedUser.getUser_tin());
            preparedStatement.setString(11, updatedUser.getUser_sss());
            preparedStatement.setString(12, updatedUser.getUser_philhealth());
            preparedStatement.setDate(13, updatedUser.getUser_bday());
            preparedStatement.setDate(14, updatedUser.getUser_dateOfHire());
            preparedStatement.setString(15, updatedUser.getUser_position());
            preparedStatement.setInt(16, updatedUser.getUser_id());

            int rowsUpdated = preparedStatement.executeUpdate();
            if (rowsUpdated > 0) {
                success = true;
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Consider logging this error
        }

        return success;
    }
}
