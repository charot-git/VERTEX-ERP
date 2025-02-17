package com.vertex.vos.Utilities;

import com.vertex.vos.DAO.AccountTypeDAO;
import com.vertex.vos.Objects.ChartOfAccounts;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ChartOfAccountsDAO {
    private static final Logger logger = LoggerFactory.getLogger(ChartOfAccountsDAO.class);
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public boolean addAccount(ChartOfAccounts account) {
        String query = "INSERT INTO chart_of_accounts (gl_code, account_title, bsis_code, account_type, balance_type, description, memo_type, added_by, date_added) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, account.getGlCode());
            statement.setString(2, account.getAccountTitle());
            statement.setInt(3, account.getBsisCodeId());
            statement.setInt(4, account.getAccountTypeId());
            statement.setInt(5, account.getBalanceType().getId());
            statement.setString(6, account.getDescription());
            statement.setBoolean(7, account.isMemoType());
            statement.setInt(8, account.getAddedBy());
            statement.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            statement.executeUpdate();
            return true;
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

    //getChartOfAccountById
    public ChartOfAccounts getChartOfAccountById(int chartOfAccount) {
        String query = "SELECT * FROM chart_of_accounts WHERE coa_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, chartOfAccount);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToChartOfAccounts(resultSet);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting chart of account by id", e);
        }
        return null;
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
        String query = "UPDATE chart_of_accounts SET gl_code = ?, account_title = ?, bsis_code = ?, account_type = ?, balance_type = ?, description = ?, memo_type = ?, added_by = ?, date_added = ? WHERE coa_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, account.getGlCode());
            statement.setString(2, account.getAccountTitle());
            statement.setInt(3, account.getBsisCodeId());
            statement.setInt(4, account.getAccountTypeId());
            statement.setInt(5, account.getBalanceType().getId());
            statement.setString(6, account.getDescription());
            statement.setBoolean(7, account.isMemoType());
            statement.setInt(8, account.getAddedBy());
            statement.setTimestamp(9, new Timestamp(account.getDateAdded().getTime()));
            statement.setInt(10, account.getCoaId());
            return statement.executeUpdate() > 0;
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

    BSISDAo bsisDao = new BSISDAo();
    AccountTypeDAO accountTypeDao = new AccountTypeDAO();
    BalanceTypeDAO balanceTypeDAO = new BalanceTypeDAO();

    // Helper method to map ResultSet to ChartOfAccounts
    private ChartOfAccounts mapResultSetToChartOfAccounts(ResultSet resultSet) throws SQLException {
        ChartOfAccounts account = new ChartOfAccounts();
        account.setCoaId(resultSet.getInt("coa_id"));
        account.setGlCode(resultSet.getString("gl_code"));
        account.setAccountTitle(resultSet.getString("account_title"));
        int bsisCode = resultSet.getInt("bsis_code");
        int accountTypeId = resultSet.getInt("account_type");
        int balanceTypeId = resultSet.getInt("balance_type");
        account.setBsisCodeId(bsisCode);
        account.setBsisCodeString(bsisDao.getBSISCodeById(bsisCode));
        account.setAccountTypeId(accountTypeId);
        account.setAccountTypeString(accountTypeDao.getAccountTypeNameById(accountTypeId));
        account.setBalanceType(balanceTypeDAO.getBalanceTypeById(balanceTypeId));
        account.setDescription(resultSet.getString("description"));
        account.setMemoType(resultSet.getBoolean("memo_type"));
        account.setAddedBy(resultSet.getInt("added_by"));
        account.setDateAdded(resultSet.getTimestamp("date_added"));
        account.setPayment(resultSet.getBoolean("isPayment"));
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

    //getAllChartOfAccounts
    public ObservableList<ChartOfAccounts> getAllChartOfAccounts() {
        ObservableList<ChartOfAccounts> chartOfAccounts = FXCollections.observableArrayList();
        String query = "SELECT * FROM chart_of_accounts";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                ChartOfAccounts account = mapResultSetToChartOfAccounts(resultSet);
                chartOfAccounts.add(account);
            }
        } catch (SQLException e) {
            logger.error("Error fetching all chart of accounts", e);
        }
        return chartOfAccounts;
    }

    public ObservableList<String> getAllCreditAccountTitles() {
        ObservableList<String> accountNames = FXCollections.observableArrayList();
        String query = "SELECT account_title FROM chart_of_accounts WHERE balance_type = '1'";

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

    public ObservableList<String> getAllDebitAccountTitles() {
        ObservableList<String> accountNames = FXCollections.observableArrayList();
        String query = "SELECT account_title FROM chart_of_accounts WHERE balance_type = '2'";

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

    public int getChartOfAccountIdByName(String selectedItem) {
        String query = "SELECT coa_id FROM chart_of_accounts WHERE account_title = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, selectedItem);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("coa_id");
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting chart of account id by name", e);
        }
        return 0;
    }

    public String getChartOfAccountNameById(int chartOfAccount) {
        String query = "SELECT account_title FROM chart_of_accounts WHERE coa_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, chartOfAccount);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("account_title");
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting chart of account name by id", e);
        }
        return "";
    }
}
