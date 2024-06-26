package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.ChartOfAccounts;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChartOfAccountsDAO {
    private static final Logger logger = LoggerFactory.getLogger(ChartOfAccountsDAO.class);
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    // Create
    public boolean addAccount(ChartOfAccounts account) {
        String query = "INSERT INTO chart_of_accounts (gl_code, account_title, bsis_code, account_type, balance_type, description, memo_type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, account.getGlCode());
            statement.setString(2, account.getAccountTitle());
            statement.setInt(3, account.getBsisCodeId());
            statement.setInt(4, account.getAccountTypeId());
            statement.setInt(5, account.getBalanceTypeId());
            statement.setString(6, account.getDescription());
            statement.setBoolean(7, account.isMemoType());

            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            logger.error("Error adding account", e);
            return false;
        }
    }
    public ObservableList<String> getAllAccountNames() {
        ObservableList<String> accountNames = FXCollections.observableArrayList();
        String query = "SELECT account_title FROM chart_of_accounts";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String accountTitle = resultSet.getString("account_title");
                accountNames.add(accountTitle);
            }
        } catch (SQLException e) {
            logger.error("Error fetching all account names", e);
        }
        return accountNames;
    }
    // Read all accounts
    public List<ChartOfAccounts> getAllAccounts() {
        List<ChartOfAccounts> accounts = new ArrayList<>();
        String query = "SELECT * FROM chart_of_accounts";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                ChartOfAccounts account = mapResultSetToChartOfAccounts(resultSet);
                accounts.add(account);
            }
        } catch (SQLException e) {
            logger.error("Error fetching all accounts", e);
        }
        return accounts;
    }

    // Update
    public boolean updateAccount(ChartOfAccounts account) {
        String query = "UPDATE chart_of_accounts SET gl_code = ?, account_title = ?, bsis_code = ?, account_type = ?, balance_type = ?, description = ?, memo_type = ? WHERE coa_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, account.getGlCode());
            statement.setString(2, account.getAccountTitle());
            statement.setInt(3, account.getBsisCodeId());
            statement.setInt(4, account.getAccountTypeId());
            statement.setInt(5, account.getBalanceTypeId());
            statement.setString(6, account.getDescription());
            statement.setBoolean(7, account.isMemoType());
            statement.setInt(8, account.getCoaId());

            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            logger.error("Error updating account", e);
            return false;
        }
    }

    // Delete
    public boolean deleteAccount(int coaId) {
        String query = "DELETE FROM chart_of_accounts WHERE coa_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, coaId);
            int rowsDeleted = statement.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            logger.error("Error deleting account", e);
            return false;
        }
    }

    // Helper method to map ResultSet to ChartOfAccounts
    private ChartOfAccounts mapResultSetToChartOfAccounts(ResultSet resultSet) throws SQLException {
        ChartOfAccounts account = new ChartOfAccounts();
        account.setCoaId(resultSet.getInt("coa_id"));
        account.setGlCode(resultSet.getInt("gl_code"));
        account.setAccountTitle(resultSet.getString("account_title"));
        account.setBsisCodeId(resultSet.getInt("bsis_code"));
        account.setAccountTypeId(resultSet.getInt("account_type"));
        account.setBalanceTypeId(resultSet.getInt("balance_type"));
        account.setDescription(resultSet.getString("description"));
        account.setMemoType(resultSet.getBoolean("memo_type"));
        return account;
    }

    public ObservableList<String> getAllAccountTitlesForMemo() {
        ObservableList<String> accountNames = FXCollections.observableArrayList();
        String query = "SELECT account_title FROM chart_of_accounts WHERE memo_type = '1'";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String accountTitle = resultSet.getString("account_title");
                accountNames.add(accountTitle);
            }
        } catch (SQLException e) {
            logger.error("Error fetching all account names", e);
        }
        return accountNames;
    }
}
