package com.vertex.vos.Utilities;

import com.zaxxer.hikari.HikariDataSource;
import com.vertex.vos.Objects.CreditDebitMemo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierMemoDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public boolean addSupplierMemo(CreditDebitMemo memo) {
        String insertQuery = "INSERT INTO suppliers_memo " +
                "(memo_number, type, supplier_id, date, amount, reason, status, chart_of_account) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

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

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public CreditDebitMemo getSupplierMemoById(int id) {
        String sql = "SELECT * FROM suppliers_memo WHERE id = ?";
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

    public List<CreditDebitMemo> getAllSupplierMemos() {
        String sql = "SELECT * FROM suppliers_memo";
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

    public List<CreditDebitMemo> getAllSupplierCreditMemo() {
        String sql = "SELECT * FROM suppliers_memo WHERE type = 1";
        return fetchMemosByType(sql);
    }

    public List<CreditDebitMemo> getAllSupplierDebitMemo() {
        String sql = "SELECT * FROM suppliers_memo WHERE type = 2";
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

    public boolean updateSupplierMemo(CreditDebitMemo memo) {
        String sql = "UPDATE suppliers_memo SET memo_number = ?, type = ?, supplier_id = ?, date = ?, amount = ?, reason = ?, status = ?, chart_of_account = ? WHERE id = ?";
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
            statement.setInt(9, memo.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteSupplierMemo(int id) {
        String sql = "DELETE FROM suppliers_memo WHERE id = ?";
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
    SupplierDAO supplierDAO = new SupplierDAO();

    private CreditDebitMemo mapResultSetToCreditDebitMemo(ResultSet rs) throws SQLException {
        CreditDebitMemo memo = new CreditDebitMemo();
        memo.setId(rs.getInt("id"));
        memo.setMemoNumber(rs.getString("memo_number"));
        memo.setType(rs.getInt("type"));
        memo.setTargetId(rs.getInt("supplier_id"));
        memo.setDate(rs.getDate("date"));
        memo.setAmount(rs.getDouble("amount"));
        memo.setReason(rs.getString("reason"));
        memo.setStatus(rs.getString("status"));
        memo.setChartOfAccount(rs.getInt("chart_of_account"));
        memo.setCreatedAt(rs.getTimestamp("created_at"));
        memo.setUpdatedAt(rs.getTimestamp("updated_at"));
        memo.setChartOfAccountName(chartOfAccountsDAO.getChartOfAccountNameById(memo.getChartOfAccount()));
        memo.setTargetName(supplierDAO.getSupplierNameById(memo.getTargetId()));

        return memo;
    }

    public CreditDebitMemo getSupplierCreditMemos(int supplierId) {
        String sql = "SELECT * FROM suppliers_memo WHERE type = 1 AND supplier_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, supplierId);

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

    public CreditDebitMemo getSupplierDebitMemos(int supplierId) {
        String sql = "SELECT * FROM suppliers_memo WHERE type = 1 AND supplier_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, supplierId);

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
}
