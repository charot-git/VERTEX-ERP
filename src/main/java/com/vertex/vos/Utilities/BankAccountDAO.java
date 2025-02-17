package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.BankAccount;
import com.vertex.vos.Objects.BankName;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BankAccountDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();



    // Method to insert a new bank account record
    public boolean addBankAccount(BankAccount bankAccount) {
        String query = "INSERT INTO bank_accounts " +
                "(bank_name, account_number, bank_description, branch, ifsc_code, opening_balance, province, city, baranggay, email, mobile_no, contact_person, is_active, created_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, bankAccount.getBankName());
            statement.setString(2, bankAccount.getAccountNumber());
            statement.setString(3, bankAccount.getBankDescription());
            statement.setString(4, bankAccount.getBranch());
            statement.setString(5, bankAccount.getIfscCode());
            statement.setBigDecimal(6, bankAccount.getOpeningBalance());
            statement.setString(7, bankAccount.getProvince());
            statement.setString(8, bankAccount.getCity());
            statement.setString(9, bankAccount.getBaranggay());
            statement.setString(10, bankAccount.getEmail());
            statement.setString(11, bankAccount.getMobileNo());
            statement.setString(12, bankAccount.getContactPerson());
            statement.setBoolean(13, bankAccount.isActive());
            statement.setInt(14, bankAccount.getCreatedBy());

            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                bankAccount.setBankId(generatedKeys.getInt(1));
            }

            return true; // Return true if the insertion was successful
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your application's needs
            return false; // Return false if an exception occurred
        }
    }

    // Method to retrieve all bank accounts
    public List<BankAccount> getAllBankAccounts() {
        List<BankAccount> bankAccounts = new ArrayList<>();
        String query = "SELECT * FROM bank_accounts";

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                BankAccount bankAccount = extractBankAccountFromResultSet(resultSet);
                bankAccounts.add(bankAccount);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your application's needs
        }

        return bankAccounts;
    }

    // Method to update a bank account record
    public void updateBankAccount(BankAccount bankAccount) {
        String query = "UPDATE bank_accounts SET " +
                "bank_name = ?, account_number = ?, bank_description = ?, branch = ?, ifsc_code = ?, " +
                "opening_balance = ?, province = ?, city = ?, baranggay = ?, email = ?, " +
                "mobile_no = ?, contact_person = ?, is_active = ? " +
                "WHERE bank_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, bankAccount.getBankName());
            statement.setString(2, bankAccount.getAccountNumber());
            statement.setString(3, bankAccount.getBankDescription());
            statement.setString(4, bankAccount.getBranch());
            statement.setString(5, bankAccount.getIfscCode());
            statement.setBigDecimal(6, bankAccount.getOpeningBalance());
            statement.setString(7, bankAccount.getProvince());
            statement.setString(8, bankAccount.getCity());
            statement.setString(9, bankAccount.getBaranggay());
            statement.setString(10, bankAccount.getEmail());
            statement.setString(11, bankAccount.getMobileNo());
            statement.setString(12, bankAccount.getContactPerson());
            statement.setBoolean(13, bankAccount.isActive());
            statement.setInt(14, bankAccount.getBankId());

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your application's needs
        }
    }

    // Method to delete a bank account record
    public void deleteBankAccount(int bankId) {
        String query = "DELETE FROM bank_accounts WHERE bank_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, bankId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your application's needs
        }
    }

    // Helper method to extract BankAccount object from ResultSet
    private BankAccount extractBankAccountFromResultSet(ResultSet resultSet) throws SQLException {
        return new BankAccount(
                resultSet.getInt("bank_id"),
                resultSet.getString("bank_name"),
                resultSet.getString("account_number"),
                resultSet.getString("bank_description"),
                resultSet.getString("branch"),
                resultSet.getString("ifsc_code"),
                resultSet.getBigDecimal("opening_balance"),
                resultSet.getString("province"),
                resultSet.getString("city"),
                resultSet.getString("baranggay"),
                resultSet.getString("email"),
                resultSet.getString("mobile_no"),
                resultSet.getString("contact_person"),
                resultSet.getBoolean("is_active"),
                resultSet.getTimestamp("created_at"),
                resultSet.getInt("created_by")
        );
    }

    public BankAccount getBankAccountById(int bankId) {
        String query = "SELECT * FROM bank_accounts WHERE bank_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, bankId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return extractBankAccountFromResultSet(resultSet);
            } else {
                return null; // Return null if no bank account is found
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your application's needs
            return null; // Return null if an exception occurred
        }
    }

    //getBankById
    public BankName getBankNameById(int bankId) {
        String query = "SELECT * FROM bank_names WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, bankId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String bankName = resultSet.getString("bank_name");
                return new BankName(bankId, bankName); // Assuming BankName has a constructor that accepts a string
            } else {
                return null; // Return null if no bank name is found
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your application's needs
            return null; // Return null if an exception occurred
        }
    }

    public List<BankName> getBankNames() {
        List<BankName> bankNames = new ArrayList<>();
        String query = "SELECT * FROM bank_names";

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            // Iterate over the result set and add each bank name to the list
            while (resultSet.next()) {
                String bankName = resultSet.getString("bank_name");
                int bankId = resultSet.getInt("id");
                bankNames.add(new BankName(bankId, bankName)); // Assuming BankName has a constructor that accepts a string
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Handle exceptions properly in real-world applications
        }

        return bankNames;
    }
}
