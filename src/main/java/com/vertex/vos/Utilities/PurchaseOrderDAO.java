package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.Branch;
import com.vertex.vos.Constructors.PurchaseOrder;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public List<PurchaseOrder> getAllPurchaseOrders() throws SQLException {
        List<PurchaseOrder> purchaseOrders = new ArrayList<>();
        String query = "SELECT * FROM purchase_order";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                PurchaseOrder purchaseOrder = new PurchaseOrder();
                purchaseOrder.setPurchaseOrderId(resultSet.getInt("purchase_order_id"));
                purchaseOrder.setPurchaseOrderNo(resultSet.getInt("purchase_order_no"));
                purchaseOrder.setReference(resultSet.getString("reference"));
                purchaseOrder.setRemark(resultSet.getString("remark"));
                purchaseOrder.setBarcode(resultSet.getString("barcode"));
                purchaseOrder.setSupplierName(resultSet.getInt("supplier_name"));
                purchaseOrder.setReceivingType(resultSet.getInt("receiving_type"));
                purchaseOrder.setPaymentType(resultSet.getInt("payment_type"));
                purchaseOrder.setPriceType(resultSet.getString("price_type"));
                purchaseOrder.setReceiptRequired(resultSet.getBoolean("receipt_required"));
                purchaseOrder.setVatAmount(resultSet.getBigDecimal("vat_amount"));
                purchaseOrder.setWithholdingTaxAmount(resultSet.getBigDecimal("withholding_tax_amount"));
                purchaseOrder.setDateEncoded(resultSet.getTimestamp("date_encoded").toLocalDateTime());
                purchaseOrder.setDate(resultSet.getDate("date").toLocalDate());
                purchaseOrder.setDatetime(resultSet.getTimestamp("datetime").toLocalDateTime());
                purchaseOrder.setTotalAmount(resultSet.getBigDecimal("total_amount"));
                purchaseOrder.setEncoderId(resultSet.getInt("encoder_id"));
                purchaseOrder.setApproverId(resultSet.getInt("approver_id"));
                purchaseOrder.setReceiverId(resultSet.getInt("receiver_id"));
                purchaseOrder.setFinanceId(resultSet.getInt("finance_id"));
                purchaseOrder.setVoucherId(resultSet.getInt("voucher_id"));
                purchaseOrder.setTransactionType(resultSet.getInt("transaction_type"));
                Timestamp dateApprovedTimestamp = resultSet.getTimestamp("date_approved");
                if (dateApprovedTimestamp != null) {
                    purchaseOrder.setDateApproved(dateApprovedTimestamp.toLocalDateTime());
                }

                Timestamp dateReceivedTimestamp = resultSet.getTimestamp("date_received");
                if (dateReceivedTimestamp != null) {
                    purchaseOrder.setDateReceived(dateReceivedTimestamp.toLocalDateTime());
                }

                Timestamp dateFinancedTimestamp = resultSet.getTimestamp("date_financed");
                if (dateFinancedTimestamp != null) {
                    purchaseOrder.setDateFinanced(dateFinancedTimestamp.toLocalDateTime());
                }

                Timestamp dateVoucheredTimestamp = resultSet.getTimestamp("date_vouchered");
                if (dateVoucheredTimestamp != null) {
                    purchaseOrder.setDateVouchered(dateVoucheredTimestamp.toLocalDateTime());
                }

                purchaseOrder.setStatus(resultSet.getInt("status"));

                TransactionTypeDAO transactionTypeDAO = new TransactionTypeDAO();
                purchaseOrder.setTransactionTypeString(transactionTypeDAO.getTransactionTypeById(resultSet.getInt("transaction_type")));

                SupplierDAO supplierDAO = new SupplierDAO();
                purchaseOrder.setSupplierNameString(supplierDAO.getSupplierNameById(resultSet.getInt("supplier_name")));

                StatusDAO statusDAO = new StatusDAO();
                purchaseOrder.setStatusString(statusDAO.getTransactionStatusById(resultSet.getInt("status")));
                purchaseOrders.add(purchaseOrder);
            }
        }

        return purchaseOrders;
    }
    public boolean entryPurchaseOrder(PurchaseOrder purchaseOrder) throws SQLException {
        String query = "INSERT INTO purchase_order (purchase_order_no, supplier_name, receiving_type, payment_type, " +
                "price_type, date_encoded, date, time, datetime, encoder_id, transaction_type, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, purchaseOrder.getPurchaseOrderNo());
            preparedStatement.setInt(2, purchaseOrder.getSupplierName());
            preparedStatement.setInt(3, purchaseOrder.getReceivingType());
            preparedStatement.setInt(4, purchaseOrder.getPaymentType());
            preparedStatement.setString(5, purchaseOrder.getPriceType());
            preparedStatement.setTimestamp(6, Timestamp.valueOf(purchaseOrder.getDateEncoded()));
            preparedStatement.setDate(7, Date.valueOf(purchaseOrder.getDate()));
            preparedStatement.setTime(8, Time.valueOf(purchaseOrder.getTime()));
            preparedStatement.setTimestamp(9, Timestamp.valueOf(purchaseOrder.getDatetime()));
            preparedStatement.setInt(10, purchaseOrder.getEncoderId());
            preparedStatement.setInt(11, purchaseOrder.getTransactionType());
            preparedStatement.setInt(12, purchaseOrder.getStatus());

            int rowsAffected = preparedStatement.executeUpdate();

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                purchaseOrder.setPurchaseOrderId(generatedKeys.getInt(1));
            }
            return rowsAffected > 0; // Return true if at least one row was affected
        }
    }

    public PurchaseOrder getPurchaseOrderByOrderNo(int orderNo) throws SQLException {
        PurchaseOrder purchaseOrder = null;
        String query = "SELECT * FROM purchase_order WHERE purchase_order_no = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, orderNo);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                purchaseOrder = new PurchaseOrder();
                purchaseOrder.setPurchaseOrderId(resultSet.getInt("purchase_order_id"));
                purchaseOrder.setPurchaseOrderNo(resultSet.getInt("purchase_order_no"));
                purchaseOrder.setReference(resultSet.getString("reference"));
                purchaseOrder.setRemark(resultSet.getString("remark"));
                purchaseOrder.setBarcode(resultSet.getString("barcode"));
                purchaseOrder.setSupplierName(resultSet.getInt("supplier_name"));
                purchaseOrder.setReceivingType(resultSet.getInt("receiving_type"));
                purchaseOrder.setPaymentType(resultSet.getInt("payment_type"));
                purchaseOrder.setPriceType(resultSet.getString("price_type"));
                purchaseOrder.setReceiptRequired(resultSet.getBoolean("receipt_required"));
                purchaseOrder.setVatAmount(resultSet.getBigDecimal("vat_amount"));
                purchaseOrder.setWithholdingTaxAmount(resultSet.getBigDecimal("withholding_tax_amount"));
                purchaseOrder.setDateEncoded(resultSet.getTimestamp("date_encoded").toLocalDateTime());
                purchaseOrder.setDate(resultSet.getDate("date").toLocalDate());
                purchaseOrder.setDatetime(resultSet.getTimestamp("datetime").toLocalDateTime());
                purchaseOrder.setTotalAmount(resultSet.getBigDecimal("total_amount"));
                purchaseOrder.setEncoderId(resultSet.getInt("encoder_id"));
                purchaseOrder.setApproverId(resultSet.getInt("approver_id"));
                purchaseOrder.setReceiverId(resultSet.getInt("receiver_id"));
                purchaseOrder.setFinanceId(resultSet.getInt("finance_id"));
                purchaseOrder.setVoucherId(resultSet.getInt("voucher_id"));
                purchaseOrder.setTransactionType(resultSet.getInt("transaction_type"));
                purchaseOrder.setStatus(resultSet.getInt("status"));

                TransactionTypeDAO transactionTypeDAO = new TransactionTypeDAO();
                purchaseOrder.setTransactionTypeString(transactionTypeDAO.getTransactionTypeById(resultSet.getInt("transaction_type")));

                SupplierDAO supplierDAO = new SupplierDAO();
                purchaseOrder.setSupplierNameString(supplierDAO.getSupplierNameById(resultSet.getInt("supplier_name")));

                StatusDAO statusDAO = new StatusDAO();
                purchaseOrder.setStatusString(statusDAO.getTransactionStatusById(resultSet.getInt("status")));
            }
        }

        return purchaseOrder;
    }

    public List<Branch> getBranchesForPurchaseOrder(int purchaseOrderId) throws SQLException {
        BranchDAO branchDAO = new BranchDAO();
        List<Branch> branches = new ArrayList<>();
        String query = "SELECT DISTINCT branch_id FROM purchase_order_products WHERE purchase_order_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, purchaseOrderId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int branchId = resultSet.getInt("branch_id");
                Branch branch = branchDAO.getBranchById(branchId);
                if (branch != null) {
                    branches.add(branch);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return branches;
    }

}

