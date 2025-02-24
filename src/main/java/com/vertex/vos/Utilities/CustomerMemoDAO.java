package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.BalanceType;
import com.vertex.vos.Objects.CustomerMemo;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class CustomerMemoDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    ChartOfAccountsDAO chartOfAccountsDAO = new ChartOfAccountsDAO();
    SupplierDAO supplierDAO = new SupplierDAO();
    CustomerDAO customerDAO = new CustomerDAO();
    SalesmanDAO salesmanDAO = new SalesmanDAO();
    BalanceTypeDAO balanceTypeDAO = new BalanceTypeDAO();

    // Create a new customer memo
    public boolean createCustomerMemo(CustomerMemo memo) {
        String sql = "INSERT INTO customers_memo (memo_number, supplier_id, type, customer_id, salesman_id, amount, applied_amount, reason, status, chart_of_account, encoder_id, isPending) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, memo.getMemoNumber());
            stmt.setInt(2, memo.getSupplier().getId());
            stmt.setInt(3, memo.getBalanceType().getId());
            stmt.setInt(4, memo.getCustomer().getCustomerId());
            stmt.setInt(5, memo.getSalesman().getId());
            stmt.setDouble(6, memo.getAmount());
            stmt.setDouble(7, memo.getAppliedAmount());
            stmt.setString(8, memo.getReason());
            stmt.setString(9, memo.getStatus().toDisplayString());  // Convert enum to DB format
            stmt.setInt(10, memo.getChartOfAccount().getCoaId());
            stmt.setInt(11, memo.getEncoderId());
            stmt.setBoolean(12, memo.getIsPending());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Read (Retrieve) a memo by ID
    public CustomerMemo getCustomerMemoById(int id) {
        String sql = "SELECT * FROM customers_memo WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToCustomerMemo(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Update an existing customer memo
    public boolean updateCustomerMemo(CustomerMemo memo) {
        String sql = "UPDATE customers_memo SET memo_number=?, supplier_id=?, type=?, customer_id=?, salesman_id=?, amount=?, applied_amount=?, reason=?, status=?, chart_of_account=?, encoder_id=?, isPending=? WHERE id=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, memo.getMemoNumber());
            stmt.setInt(2, memo.getSupplier().getId());
            stmt.setInt(3, memo.getBalanceType().getId());
            stmt.setInt(4, memo.getCustomer().getCustomerId());
            stmt.setInt(5, memo.getSalesman().getId());
            stmt.setDouble(6, memo.getAmount());
            stmt.setDouble(7, memo.getAppliedAmount());
            stmt.setString(8, memo.getReason());
            stmt.setString(9, memo.getStatus().toDisplayString());  // Convert enum to DB format
            stmt.setInt(10, memo.getChartOfAccount().getCoaId());
            stmt.setInt(11, memo.getEncoderId());
            stmt.setBoolean(12, memo.getIsPending());
            stmt.setInt(13, memo.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete a memo by ID
    public boolean deleteCustomerMemo(int id) {
        String sql = "DELETE FROM customers_memo WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Helper method to map ResultSet to CustomerMemo object
    private CustomerMemo mapResultSetToCustomerMemo(ResultSet rs) throws SQLException {
        CustomerMemo memo = new CustomerMemo();

        memo.setId(rs.getInt("id"));
        memo.setMemoNumber(rs.getString("memo_number"));
        memo.setChartOfAccount(chartOfAccountsDAO.getChartOfAccountById(rs.getInt("chart_of_account")));
        memo.setSupplier(supplierDAO.getSupplierById(rs.getInt("supplier_id")));
        memo.setBalanceType(balanceTypeDAO.getBalanceTypeById(rs.getInt("type")));
        memo.setCustomer(customerDAO.getCustomer(rs.getInt("customer_id")));
        memo.setSalesman(salesmanDAO.getSalesmanDetails(rs.getInt("salesman_id")));
        memo.setAmount(rs.getDouble("amount"));
        memo.setAppliedAmount(rs.getDouble("applied_amount"));
        memo.setReason(rs.getString("reason"));
        memo.setStatus(CustomerMemo.MemoStatus.fromDbValue(rs.getString("status")));  // Convert DB string to enum
        memo.setEncoderId(rs.getInt("encoder_id"));
        memo.setIsPending(rs.getBoolean("isPending"));

        return memo;
    }


    public ObservableList<CustomerMemo> getAllCustomersWhereBalanceType(BalanceType balanceType) {
        String sql = "SELECT * FROM customers_memo WHERE type = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, balanceType.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                ObservableList<CustomerMemo> customerMemos = FXCollections.observableArrayList();

                while (rs.next()) {
                    customerMemos.add(mapResultSetToCustomerMemo(rs));
                }

                return customerMemos;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }
}
