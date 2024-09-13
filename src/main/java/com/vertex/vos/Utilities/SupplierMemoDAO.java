package com.vertex.vos.Utilities;

import com.zaxxer.hikari.HikariDataSource;
import com.vertex.vos.Objects.CreditDebitMemo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierMemoDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public boolean addSupplierMemo(CreditDebitMemo memo) {
        String insertQuery = "INSERT INTO suppliers_memo " +
                "(memo_number, type, supplier_id, date, amount, reason, status, chart_of_account, encoder_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
        String sql = "UPDATE suppliers_memo SET memo_number = ?, type = ?, supplier_id = ?, date = ?, amount = ?, reason = ?, status = ?, chart_of_account = ?, encoder_id = ? WHERE id = ?";
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
            statement.setInt(10, memo.getId());
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

    //get memos for purchase_order


    ChartOfAccountsDAO chartOfAccountsDAO = new ChartOfAccountsDAO();
    SupplierDAO supplierDAO = new SupplierDAO();

    private CreditDebitMemo mapResultSetToCreditDebitMemo(ResultSet rs) throws SQLException {
        CreditDebitMemo memo = new CreditDebitMemo();
        memo.setId(rs.getInt("id"));
        memo.setMemoNumber(rs.getString("memo_number"));
        memo.setType(rs.getInt("type"));
        String memoType = memo.getType() == 1 ? "Credit Memo" : "Debit Memo";
        memo.setTypeName(memoType);
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
        memo.setEncoderId(rs.getInt("encoder_id"));

        return memo;
    }

    public ObservableList<CreditDebitMemo> getSupplierCreditMemos(int supplierId) {
        String sql = "SELECT * FROM suppliers_memo WHERE type = 1 AND supplier_id = ? AND status = 'Available'";
        List<CreditDebitMemo> memos = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, supplierId);

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

    public ObservableList<CreditDebitMemo> getSupplierDebitMemos(int supplierId) {
        String sql = "SELECT * FROM suppliers_memo WHERE type = 2 AND supplier_id = ? AND status = 'Available'";
        List<CreditDebitMemo> memos = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, supplierId);

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
        String sql = "UPDATE suppliers_memo SET status = ? WHERE memo_number = ?";
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

    public String getReasonByMemoNumber(String memoNumber) {
        String sql = "SELECT reason FROM suppliers_memo WHERE memo_number = ?";
        String reason = null;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            // Set the memoNumber parameter in the SQL query
            statement.setString(1, memoNumber);

            // Execute the query and process the ResultSet
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    reason = resultSet.getString("reason");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exceptions, possibly with DialogUtils.showErrorMessage("Error fetching reason by memo number");
        }

        return reason;
    }

}
