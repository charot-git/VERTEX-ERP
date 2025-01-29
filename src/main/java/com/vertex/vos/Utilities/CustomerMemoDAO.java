package com.vertex.vos.Utilities;

import com.zaxxer.hikari.HikariDataSource;
import com.vertex.vos.Objects.CreditDebitMemo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerMemoDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public boolean addCustomerMemo(CreditDebitMemo memo) {
        String insertQuery = "INSERT INTO customers_memo " +
                "(memo_number, type, customer_id, applied_date, amount, reason, status, chart_of_account, encoder_id, isPending) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?,?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(insertQuery)) {

            statement.setString(1, memo.getMemoNumber());
            statement.setInt(2, memo.getType());
            statement.setInt(3, memo.getTargetId());
            statement.setDate(4, memo.getDate());
            statement.setDouble(5, memo.getAmount());
            statement.setString(6, memo.getReason());
            statement.setString(7, memo.getStatus());
            statement.setInt(8, memo.getChartOfAccount());
            statement.setInt(9, memo.getEncoderId());
            statement.setBoolean(10, memo.isPending());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public CreditDebitMemo getCustomerMemoById(int id) {
        String sql = "SELECT * FROM customers_memo WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCreditDebitMemo(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<CreditDebitMemo> getAllCustomerMemos() {
        String sql = "SELECT * FROM customers_memo";
        List<CreditDebitMemo> memos = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                CreditDebitMemo memo = mapResultSetToCreditDebitMemo(rs);
                memos.add(memo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return memos;
    }

    public List<CreditDebitMemo> getAllCustomerCreditMemo() {
        String sql = "SELECT * FROM customers_memo WHERE type = 1";
        return fetchMemosByType(sql);
    }

    public List<CreditDebitMemo> getAllCustomerDebitMemo() {
        String sql = "SELECT * FROM customers_memo WHERE type = 2";
        return fetchMemosByType(sql);
    }

    private List<CreditDebitMemo> fetchMemosByType(String sql) {
        List<CreditDebitMemo> memos = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()) {
            while (rs.next()) {
                CreditDebitMemo memo = mapResultSetToCreditDebitMemo(rs);
                memos.add(memo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return memos;
    }

    public boolean updateCustomerMemo(CreditDebitMemo memo) {
        String sql = "UPDATE customers_memo SET memo_number = ?, type = ?, customer_id = ?, date = ?, amount = ?, reason = ?, status = ?, chart_of_account = ?, encoder_id = ? , isPending = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, memo.getMemoNumber());
            statement.setInt(2, memo.getType());
            statement.setInt(3, memo.getTargetId());
            statement.setDate(4, memo.getDate());
            statement.setDouble(5, memo.getAmount());
            statement.setString(6, memo.getReason());
            statement.setString(7, memo.getStatus());
            statement.setInt(8, memo.getChartOfAccount());
            statement.setInt(9, memo.getEncoderId());
            statement.setBoolean(10, memo.isPending());
            statement.setInt(11, memo.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCustomerMemo(int id) {
        String sql = "DELETE FROM customers_memo WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    ChartOfAccountsDAO chartOfAccountsDAO = new ChartOfAccountsDAO();
    CustomerDAO customerDAO = new CustomerDAO();

    private CreditDebitMemo mapResultSetToCreditDebitMemo(ResultSet rs) throws SQLException {
        CreditDebitMemo memo = new CreditDebitMemo();
        memo.setId(rs.getInt("id"));
        memo.setMemoNumber(rs.getString("memo_number"));
        memo.setType(rs.getInt("type"));
        memo.setTargetId(rs.getInt("customer_id"));
        memo.setDate(rs.getDate("date"));
        memo.setAmount(rs.getDouble("amount"));
        memo.setReason(rs.getString("reason"));
        memo.setStatus(rs.getString("status"));
        memo.setChartOfAccount(rs.getInt("chart_of_account"));
        memo.setCreatedAt(rs.getTimestamp("created_at"));
        memo.setUpdatedAt(rs.getTimestamp("updated_at"));
        memo.setChartOfAccountName(chartOfAccountsDAO.getChartOfAccountNameById(memo.getChartOfAccount()));
        memo.setTargetName(customerDAO.getCustomerStoreNameById(memo.getTargetId()));
        memo.setEncoderId(rs.getInt("encoder_id"));
        memo.setPending(rs.getBoolean("isPending"));

        return memo;
    }

    public ObservableList<CreditDebitMemo> getCustomerCreditMemos(int customerId) {
        String sql = "SELECT * FROM customers_memo WHERE type = 1 AND customer_id = ? AND status = 'Available'";
        List<CreditDebitMemo> memos = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, customerId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    CreditDebitMemo memo = mapResultSetToCreditDebitMemo(rs);
                    memos.add(memo);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return FXCollections.observableArrayList(memos);
    }

    public ObservableList<CreditDebitMemo> getCustomerDebitMemos(int customerId) {
        String sql = "SELECT * FROM customers_memo WHERE type = 2 AND customer_id = ? AND status = 'Available'";
        List<CreditDebitMemo> memos = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, customerId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    CreditDebitMemo memo = mapResultSetToCreditDebitMemo(rs);
                    memos.add(memo);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return FXCollections.observableArrayList(memos);
    }

    public boolean updateMemoStatus(int memoNumber, String newStatus) {
        String sql = "UPDATE customers_memo SET status = ? WHERE memo_number = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, newStatus);
            statement.setInt(2, memoNumber);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
