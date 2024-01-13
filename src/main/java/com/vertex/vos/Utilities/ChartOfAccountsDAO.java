package com.vertex.vos.Utilities;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChartOfAccountsDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public List<String> getAllAccountTitlesForMemo() {
        List<String> accountTitles = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT account_title FROM chart_of_accounts WHERE memo_type = 1");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String accountTitle = resultSet.getString("account_title");
                accountTitles.add(accountTitle);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exceptions appropriately
        }
        return accountTitles;
    }
}
