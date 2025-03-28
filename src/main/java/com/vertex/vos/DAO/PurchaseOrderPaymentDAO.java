package com.vertex.vos.DAO;

import com.vertex.vos.Objects.PurchaseOrder;
import com.vertex.vos.Objects.PurchaseOrderPayment;
import com.vertex.vos.Utilities.ChartOfAccountsDAO;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.vertex.vos.Utilities.PurchaseOrderDAO;
import com.vertex.vos.Utilities.SupplierDAO;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PurchaseOrderPaymentDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    PurchaseOrderDAO purchaseOrderDAO = new PurchaseOrderDAO();
    ChartOfAccountsDAO chartOfAccountsDAO = new ChartOfAccountsDAO();
    SupplierDAO supplierDAO = new SupplierDAO();


    public ObservableList<PurchaseOrderPayment> getSupplierPayments(int purchaseOrderId) {
        String sql = "SELECT purchase_order_id, supplier_id, paid_amount, chart_of_account, created_at FROM purchase_order_payment WHERE purchase_order_id = ?";
        ObservableList<PurchaseOrderPayment> purchaseOrderPayments = FXCollections.observableArrayList();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // Set the purchaseOrderId parameter in the SQL query
            statement.setInt(1, purchaseOrderId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    PurchaseOrderPayment payment = new PurchaseOrderPayment();

                    // Assuming PurchaseOrder is another object, you'd have to fetch and set it accordingly
                    PurchaseOrder purchaseOrder = purchaseOrderDAO.getPurchaseOrderByOrderNo(purchaseOrderId);

                    payment.setPurchaseOrder(purchaseOrder);

                    payment.setSupplierId(resultSet.getInt("supplier_id"));
                    payment.setSupplierName(supplierDAO.getSupplierNameById(resultSet.getInt("supplier_id")));
                    payment.setPaidAmount(resultSet.getDouble("paid_amount"));
                    payment.setChartOfAccountId(resultSet.getInt("chart_of_account"));
                    payment.setChartOfAccountName(chartOfAccountsDAO.getChartOfAccountNameById(resultSet.getInt("chart_of_account")));
                    payment.setCreatedAt(resultSet.getTimestamp("created_at"));
                    purchaseOrderPayments.add(payment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exceptions, possibly with DialogUtils.showErrorMessage("Error fetching supplier payments");
        }

        return purchaseOrderPayments;
    }

    public boolean insertPayment(int purchaseOrderId, int supplierId, BigDecimal paidAmount, int chartOfAccount) {
        String sql = "INSERT INTO purchase_order_payment (purchase_order_id, supplier_id, paid_amount, chart_of_account) VALUES (?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, purchaseOrderId);
            statement.setInt(2, supplierId);
            statement.setBigDecimal(3, paidAmount);
            statement.setInt(4, chartOfAccount);

            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace(); // Handle or log the exception as needed
            return false; // Return false indicating failure
        }
    }

    public void updatePayment(int paymentId, int supplierId, double paidAmount, int chartOfAccount) throws SQLException {
        String sql = "UPDATE purchase_order_payment SET supplier_id = ?, paid_amount = ?, chart_of_account = ? WHERE purchase_order_totals_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, supplierId);
            statement.setBigDecimal(2, BigDecimal.valueOf(paidAmount));
            statement.setInt(3, chartOfAccount);
            statement.setInt(4, paymentId);
            statement.executeUpdate();
        }
    }

    public void deletePayment(int paymentId) throws SQLException {
        String sql = "DELETE FROM purchase_order_payment WHERE purchase_order_totals_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, paymentId);
            statement.executeUpdate();
        }
    }

    //get getChartOfAccount

    public int getChartOfAccount(int purchaseOrderNo) {
        String sql = "SELECT chart_of_account FROM purchase_order_payment WHERE purchase_order_id = ?";
        int chartOfAccount = 0;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, purchaseOrderNo);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    chartOfAccount = resultSet.getInt("chart_of_account");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chartOfAccount;
    }


    public BigDecimal getTotalPaidAmountForPurchaseOrder(int purchaseOrderId) {
        String sql = "SELECT SUM(paid_amount) AS total_paid_amount FROM purchase_order_payment WHERE purchase_order_id = ?";
        BigDecimal totalPaidAmount = BigDecimal.ZERO;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, purchaseOrderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    totalPaidAmount = resultSet.getBigDecimal("total_paid_amount");
                    System.out.println(totalPaidAmount);
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return totalPaidAmount;
    }

    public void holdPayment(int purchaseOrderId, int supplierId, double paidAmount, int chartOfAccount) throws SQLException {
        String sql = "INSERT INTO purchase_order_payment (purchase_order_id, supplier_id, paid_amount, chart_of_account) VALUES (?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, purchaseOrderId);
            statement.setInt(2, supplierId);
            statement.setBigDecimal(3, BigDecimal.valueOf(paidAmount));
            statement.setInt(4, chartOfAccount);
            statement.executeUpdate();
        }
    }
}
