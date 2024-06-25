package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.Branch;
import com.vertex.vos.Constructors.PurchaseOrder;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
                purchaseOrder.setTotalDiscountedAmount(resultSet.getBigDecimal("discounted_amount"));
                purchaseOrder.setTotalGrossAmount(resultSet.getBigDecimal("gross_amount"));
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

    public boolean entryGeneralReceive(PurchaseOrder purchaseOrder) throws SQLException {
        String query = "INSERT INTO purchase_order (purchase_order_no, supplier_name, receiving_type, payment_type, " +
                "price_type, date_encoded, date_approved, date_received, encoder_id, approver_id, receiver_id, " +
                "transaction_type, status, date, time, datetime, receipt_required) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "purchase_order_no = VALUES(purchase_order_no), " +
                "supplier_name = VALUES(supplier_name), " +
                "receiving_type = VALUES(receiving_type), " +
                "payment_type = VALUES(payment_type), " +
                "price_type = VALUES(price_type), " +
                "date_encoded = VALUES(date_encoded), " +
                "date_approved = VALUES(date_approved), " +
                "date_received = VALUES(date_received), " +
                "encoder_id = VALUES(encoder_id), " +
                "approver_id = VALUES(approver_id), " +
                "receiver_id = VALUES(receiver_id), " +
                "transaction_type = VALUES(transaction_type), " +
                "status = VALUES(status), " +
                "date = VALUES(date), " +
                "time = VALUES(time), " +
                "datetime = VALUES(datetime), " +
                "receipt_required = VALUES(receipt_required)";

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false); // Disable auto-commit to start transaction

            try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setInt(1, purchaseOrder.getPurchaseOrderNo());
                preparedStatement.setInt(2, purchaseOrder.getSupplierName());
                preparedStatement.setInt(3, purchaseOrder.getReceivingType());
                preparedStatement.setInt(4, purchaseOrder.getPaymentType());
                preparedStatement.setString(5, purchaseOrder.getPriceType());
                preparedStatement.setTimestamp(6, Timestamp.valueOf(purchaseOrder.getDateEncoded()));
                preparedStatement.setTimestamp(7, Timestamp.valueOf(purchaseOrder.getDateApproved()));
                preparedStatement.setTimestamp(8, Timestamp.valueOf(purchaseOrder.getDateReceived()));
                preparedStatement.setInt(9, purchaseOrder.getEncoderId());
                preparedStatement.setInt(10, purchaseOrder.getApproverId());
                preparedStatement.setInt(11, purchaseOrder.getReceiverId());
                preparedStatement.setInt(12, purchaseOrder.getTransactionType());
                preparedStatement.setInt(13, purchaseOrder.getStatus());

                // Adding current date and time
                LocalDate currentDate = LocalDate.now();
                LocalTime currentTime = LocalTime.now();
                LocalDateTime currentDateTime = LocalDateTime.now();

                preparedStatement.setDate(14, Date.valueOf(currentDate));
                preparedStatement.setTime(15, Time.valueOf(currentTime));
                preparedStatement.setTimestamp(16, Timestamp.valueOf(currentDateTime));
                preparedStatement.setBoolean(17, purchaseOrder.getReceiptRequired());

                int rowsAffected = preparedStatement.executeUpdate();

                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    purchaseOrder.setPurchaseOrderId(generatedKeys.getInt(1));
                }

                connection.commit(); // Commit transaction
                return rowsAffected > 0; // Return true if at least one row was affected
            } catch (SQLException e) {
                connection.rollback(); // Rollback transaction if an exception occurs
                throw e; // Re-throw the exception after rollback
            }
        }
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

    public boolean approvePurchaseOrder(int purchaseOrderNo, int approverId, boolean receiptRequired, double vatAmount, double withholdingTaxAmount, double totalAmount, double grossAmount, double discountedAmount, LocalDateTime dateApproved, LocalDate receivingDate) throws SQLException {
        String query = "UPDATE purchase_order SET approver_id = ?, receipt_required = ?, vat_amount = ?, withholding_tax_amount = ?, total_amount = ?, gross_amount = ?, discounted_amount = ?, date_approved = ?, status = ?, lead_time_receiving = ? WHERE purchase_order_no = ?";

        int approvedStatus = 3;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, approverId);
            preparedStatement.setBoolean(2, receiptRequired);
            if (receiptRequired) {
                preparedStatement.setDouble(3, vatAmount);
                preparedStatement.setDouble(4, withholdingTaxAmount);
            } else {
                preparedStatement.setDouble(3, 0);
                preparedStatement.setDouble(4, 0);
            }
            preparedStatement.setDouble(5, totalAmount);
            preparedStatement.setDouble(6, grossAmount);
            preparedStatement.setDouble(7, discountedAmount);
            preparedStatement.setTimestamp(8, Timestamp.valueOf(dateApproved));
            preparedStatement.setInt(9, approvedStatus);
            preparedStatement.setDate(10, Date.valueOf(receivingDate));
            preparedStatement.setInt(11, purchaseOrderNo);

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0; // Return true if rows were affected (update successful)
        }
    }


    public PurchaseOrder getPurchaseOrderByOrderNo(int orderNo) throws SQLException {
        PurchaseOrder purchaseOrder = null;
        String query = "SELECT * FROM purchase_order WHERE purchase_order_no = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, orderNo);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    purchaseOrder = mapResultSetToPurchaseOrder(resultSet);
                }
            }
        }

        return purchaseOrder;
    }

    private PurchaseOrder mapResultSetToPurchaseOrder(ResultSet resultSet) throws SQLException {
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
        purchaseOrder.setDateEncoded(resultSet.getTimestamp("date_encoded").toLocalDateTime());
        purchaseOrder.setDate(resultSet.getDate("date").toLocalDate());
        purchaseOrder.setDatetime(resultSet.getTimestamp("datetime").toLocalDateTime());
        purchaseOrder.setLeadTimeReceiving(getLocalDateOrNull(resultSet.getDate("lead_time_receiving")));
        purchaseOrder.setLeadTimePayment(getLocalDateOrNull(resultSet.getDate("lead_time_payment")));
        purchaseOrder.setTotalGrossAmount(resultSet.getBigDecimal("gross_amount"));
        purchaseOrder.setWithholdingTaxAmount(resultSet.getBigDecimal("withholding_tax_amount"));
        purchaseOrder.setVatAmount(resultSet.getBigDecimal("vat_amount"));
        purchaseOrder.setTotalDiscountedAmount(resultSet.getBigDecimal("discounted_amount"));
        purchaseOrder.setTotalAmount(resultSet.getBigDecimal("total_amount"));
        purchaseOrder.setEncoderId(resultSet.getInt("encoder_id"));
        purchaseOrder.setApproverId(resultSet.getInt("approver_id"));
        purchaseOrder.setReceiverId(resultSet.getInt("receiver_id"));
        purchaseOrder.setFinanceId(resultSet.getInt("finance_id"));
        purchaseOrder.setVoucherId(resultSet.getInt("voucher_id"));
        purchaseOrder.setTransactionType(resultSet.getInt("transaction_type"));
        purchaseOrder.setDateApproved(getLocalDateTimeOrNull(resultSet.getTimestamp("date_approved")));
        purchaseOrder.setDateReceived(getLocalDateTimeOrNull(resultSet.getTimestamp("date_received")));
        purchaseOrder.setDateFinanced(getLocalDateTimeOrNull(resultSet.getTimestamp("date_financed")));
        purchaseOrder.setDateVouchered(getLocalDateTimeOrNull(resultSet.getTimestamp("date_vouchered")));
        purchaseOrder.setStatus(resultSet.getInt("status"));

        // Set additional string fields
        setAdditionalStringFields(purchaseOrder);

        return purchaseOrder;
    }

    private LocalDate getLocalDateOrNull(Date date) {
        return date != null ? date.toLocalDate() : null;
    }

    private LocalDateTime getLocalDateTimeOrNull(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    private void setAdditionalStringFields(PurchaseOrder purchaseOrder) throws SQLException {
        TransactionTypeDAO transactionTypeDAO = new TransactionTypeDAO();
        purchaseOrder.setTransactionTypeString(transactionTypeDAO.getTransactionTypeById(purchaseOrder.getTransactionType()));

        SupplierDAO supplierDAO = new SupplierDAO();
        purchaseOrder.setSupplierNameString(supplierDAO.getSupplierNameById(purchaseOrder.getSupplierName()));

        StatusDAO statusDAO = new StatusDAO();
        purchaseOrder.setStatusString(statusDAO.getTransactionStatusById(purchaseOrder.getStatus()));
    }



    int debug;

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

    public ObservableList<String> getBranchNamesForPurchaseOrder(int purchaseOrderId) throws SQLException {
        BranchDAO branchDAO = new BranchDAO();
        ObservableList<String> branches = FXCollections.observableArrayList();
        String query = "SELECT DISTINCT branch_id FROM purchase_order_products WHERE purchase_order_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, purchaseOrderId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int branchId = resultSet.getInt("branch_id");
                String branchName = branchDAO.getBranchNameById(branchId);
                branches.add(branchName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return branches;
    }

    public ObservableList<String> getBranchNamesForPurchaseOrderGeneralReceive(int purchaseOrderId) throws SQLException {
        BranchDAO branchDAO = new BranchDAO();
        ObservableList<String> branches = FXCollections.observableArrayList();
        String query = "SELECT DISTINCT branch_id FROM purchase_order_receiving WHERE purchase_order_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, purchaseOrderId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int branchId = resultSet.getInt("branch_id");
                String branchName = branchDAO.getBranchNameById(branchId);
                branches.add(branchName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return branches;
    }

    public ObservableList<String> getAllPOForReceivingFromPO() {
        ObservableList<String> poNumbers = FXCollections.observableArrayList();

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT purchase_order_no FROM purchase_order WHERE payment_type != '0' AND status = '3'";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String purchase_order_no = resultSet.getString("purchase_order_no");
                        poNumbers.add(purchase_order_no);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }
        return poNumbers;
    }

    public ObservableList<String> getAllPOForGeneralReceive() {
        ObservableList<String> poNumbers = FXCollections.observableArrayList();

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT purchase_order_no FROM purchase_order WHERE payment_type = '0' AND status = '3'";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String purchase_order_no = resultSet.getString("purchase_order_no");
                        poNumbers.add(purchase_order_no);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }
        return poNumbers;
    }

    public int getPurchaseOrderIDByPurchaseNO(int poNo) {
        String sql = "SELECT purchase_order_id FROM purchase_order WHERE purchase_order_no = ?";
        int poId = -1;
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, poNo);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        poId = resultSet.getInt("purchase_order_id");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }
        return poId;
    }

    public boolean receivePurchaseOrder(PurchaseOrder purchaseOrder, boolean receiveStatus) {
        int purchaseOrderId = purchaseOrder.getPurchaseOrderId();
        try {
            if (receiveStatus) {
                updatePurchaseOrderStatus(purchaseOrderId, 9); // Discrepancies still exist
                return false;
            } else {
                updatePurchaseOrderStatus(purchaseOrderId, 6); // All items received successfully
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updatePurchaseOrderStatus(int purchaseOrderId, int status) throws SQLException {
        String query = "UPDATE purchase_order SET status = ? WHERE purchase_order_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, status);
            preparedStatement.setInt(2, purchaseOrderId);
            preparedStatement.executeUpdate();
        }
    }

}

