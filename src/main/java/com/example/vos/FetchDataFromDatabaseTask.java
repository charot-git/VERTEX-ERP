package com.example.vos;

import com.zaxxer.hikari.HikariDataSource;
import javafx.concurrent.Task;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FetchDataFromDatabaseTask extends Task<List<User>> {

    private final HikariDataSource dataSource;

    public FetchDataFromDatabaseTask(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected List<User> call() throws Exception {
        List<User> userList = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT * FROM user";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next() && !isCancelled()) {
                        User user = new User();
                        user.setUser_id(resultSet.getInt("user_id"));
                        user.setUser_fname(resultSet.getString("user_fname"));
                        user.setUser_mname(resultSet.getString("user_mname"));
                        user.setUser_lname(resultSet.getString("user_lname"));
                        user.setUser_bday(resultSet.getDate("user_bday"));
                        user.setUser_brgy(resultSet.getString("user_brgy"));
                        user.setUser_city(resultSet.getString("user_city"));
                        user.setUser_province(resultSet.getString("user_province"));
                        user.setUser_contact(resultSet.getString("user_contact"));
                        user.setUser_department(resultSet.getString("user_department"));
                        user.setUser_email(resultSet.getString("user_email"));
                        user.setUser_tin(resultSet.getString("user_tin"));
                        user.setUser_philhealth(resultSet.getString("user_philhealth"));
                        user.setUser_sss(resultSet.getString("user_sss"));
                        user.setUser_tags(resultSet.getString("user_tags"));
                        user.setUser_dateOfHire(resultSet.getDate("user_dateOfHire"));
                        user.setUser_position(resultSet.getString("user_position"));
                        userList.add(user);
                    }
                }
            }

        } catch (SQLException e) {
            updateMessage("Error occurred while fetching data: " + e.getMessage());
            throw e;
        }

        return userList;
    }
}
